package benchmarks

import convolution.ConvolutionMode
import convolution.loadImage
import convolution.parallelConvolveCols
import convolution.parallelConvolvePixels
import convolution.parallelConvolveRows
import convolution.parallelConvolveTiles
import convolution.promptForFilterName
import convolution.promptForImagePath
import convolution.promptForMode
import convolution.seqConvolve
import convolution.toGrayscale
import createRandomImage
import filters.Filter
import filters.filterPool
import imageSizeBound
import org.bytedeco.opencv.opencv_core.Mat
import java.util.Random
import kotlin.time.DurationUnit
import kotlin.time.measureTime

internal inline fun estimateTime(block: () -> Unit): Double {
    return measureTime { block() }.toDouble(DurationUnit.SECONDS)
}

internal fun measureSingleModeTime(image: Mat, mode: ConvolutionMode, filter: Filter): Double {
    return when (mode) {
        ConvolutionMode.Sequential -> estimateTime { image.seqConvolve(filter) }
        ConvolutionMode.ParallelPixels -> estimateTime { image.parallelConvolvePixels(filter) }
        is ConvolutionMode.ParallelRows -> estimateTime { image.parallelConvolveRows(filter, mode.batchSize) }
        is ConvolutionMode.ParallelCols -> estimateTime { image.parallelConvolveCols(filter, mode.batchSize) }
        is ConvolutionMode.ParallelTiles -> estimateTime { image.parallelConvolveTiles(filter, mode.tileWidth, mode.tileHeight) }
    }
}

private fun benchmarkSingleMode(
    image: Mat,
    filter: Filter,
    mode: ConvolutionMode,
    runs: Int = 10
): BenchmarkResult {
    val times = List(runs) { measureSingleModeTime(image, mode, filter) }
    return BenchmarkResult(mode, times)
}

private fun loadOrGenerateImage(): Mat {
    println("\nChoose to load or generate an image:")
    val options = listOf("load image", "create random image")

    options.forEachIndexed { index, name ->
        println("  [$index] $name")
    }
    while (true) {
        print("Select mode index: ")
        when (readlnOrNull()?.toIntOrNull()) {
            0 -> return loadImage(promptForImagePath()).toGrayscale()
            1 -> {
                print("Enter image size (it's square) [skip to choose random]: ")
                val size = readlnOrNull()?.toIntOrNull() ?: Random().nextInt(imageSizeBound)
                return createRandomImage(size, size)
            }
            else -> println("❌ Invalid index. Please enter a number from the list.")
        }
    }
}

// simply measuring execution time
fun main() {
    val inputImage = loadOrGenerateImage()
    println("Image size is ${inputImage.cols()}x${inputImage.rows()}")
    val selectedMode = promptForMode()

    val filterName = promptForFilterName()
    val filter = filterPool[filterName] ?: throw IllegalArgumentException("Filter should not be null.")
    println("Applying filter: $filterName...")

    val result = benchmarkSingleMode(inputImage, filter, selectedMode)
    println("Performance information:")
    println(result)
}
