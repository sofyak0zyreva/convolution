package convolution

import filters.Filter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapMerge
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.newFixedThreadPoolContext
import kotlinx.coroutines.withContext
import org.bytedeco.opencv.opencv_core.Mat
import java.io.File

// --- Reader ---
fun readImages(paths: List<String>): Flow<Pair<String, Mat>> = flow {
    for (path in paths) {
        val name = File(path).name
        println("Reading: $name")
        val mat = loadImage(path).toGrayscale()
        emit(name to mat)
        println("Read: $name")
    }
}.flowOn(Dispatchers.IO)

// --- Worker (convolution) ---
fun Flow<Pair<String, Mat>>.convolveImages(
    filter: Filter,
    mode: ConvolutionMode,
    concurrency: Int
): Flow<Pair<String, Mat>> {
    val convDispatcher = newFixedThreadPoolContext(
        Runtime.getRuntime().availableProcessors(),
        "convPool"
    )

    return this.flatMapMerge(concurrency) { (name, mat) ->
        flow {
            println("Convolving: $name")
            val result = withContext(convDispatcher) {
                mat.convolveWithMode(filter, mode)
            }
            emit(name to result)
            println("Convolved: $name")
        }
    }
}

// --- Writer ---
fun Flow<Pair<String, Mat>>.writeImages(filterName: String, mode: ConvolutionMode): Flow<Unit> =
    this.map { (name, mat) ->
        val filename = File(name).nameWithoutExtension
        val modeName = mode.toString()
        val outputPath = "output/${filename}_${filterName}_$modeName.bmp"
        saveImage(mat, outputPath)
        println("✅ Saved: $outputPath")
        mat.release()
    }.flowOn(Dispatchers.IO)
