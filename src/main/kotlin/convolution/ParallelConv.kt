package convolution

import filters.Filter
import filters.checkFilterSize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.math.min

// runBlocking { ... } creates a coroutine scope that blocks the current thread until all launched coroutines inside it finish
fun Mat.parallelConvolvePixels(filter: Filter): Mat = runBlocking {
    checkFilterSize(filter)
    val output = Mat(rows(), cols(), CV_8UC1)

    for (y in 0 until rows()) {
        for (x in 0 until cols()) {
            // launch{ ... } launches a new coroutine for this batch
            // Dispatchers.Default:
            // the coroutine will run on a background thread pool, these threads are shared across the app
            launch(Dispatchers.Default) {
                val pixelValue = convolvePixel(x, y, filter, this@parallelConvolvePixels)
                output.ptr(y, x).put(pixelValue)
            }
        }
    }
    return@runBlocking output
}

fun Mat.parallelConvolveRows(filter: Filter, batchSize: Int): Mat = runBlocking {
    checkFilterSize(filter)
    val output = Mat(rows(), cols(), CV_8UC1)

    val numBatches = (rows() + batchSize - 1) / batchSize
    for (i in 0 until numBatches) {
        val startRow = i * batchSize
        val endRow = min(startRow + batchSize, rows())
        launch(Dispatchers.Default) {
            for (y in startRow until endRow) {
                for (x in 0 until cols()) {
                    val pixelValue = convolvePixel(x, y, filter, this@parallelConvolveRows)
                    output.ptr(y, x).put(pixelValue)
                }
            }
        }
    }
    return@runBlocking output
}

fun Mat.parallelConvolveCols(filter: Filter, batchSize: Int): Mat = runBlocking {
    checkFilterSize(filter)
    val output = Mat(rows(), cols(), CV_8UC1)

    val numBatches = (cols() + batchSize - 1) / batchSize
    for (i in 0 until numBatches) {
        val startColumn = i * batchSize
        val endColumn = min(startColumn + batchSize, cols())
        launch(Dispatchers.Default) {
            for (x in startColumn until endColumn) {
                for (y in 0 until rows()) {
                    val pixelValue = convolvePixel(x, y, filter, this@parallelConvolveCols)
                    output.ptr(y, x).put(pixelValue)
                }
            }
        }
    }
    return@runBlocking output
}

fun Mat.parallelConvolveTiles(filter: Filter, tileWidth: Int, tileHeight: Int): Mat = runBlocking {
    checkFilterSize(filter)
    val output = Mat(rows(), cols(), CV_8UC1)

    for (tileY in 0 until rows() step tileHeight) {
        for (tileX in 0 until cols() step tileWidth) {
            launch(Dispatchers.Default) {
                val endY = min(tileY + tileHeight, rows())
                val endX = min(tileX + tileWidth, cols())
                for (y in tileY until endY) {
                    for (x in tileX until endX) {
                        val result = convolvePixel(x, y, filter, this@parallelConvolveTiles)
                        output.ptr(y, x).put(result)
                    }
                }
            }
        }
    }
    return@runBlocking output
}
