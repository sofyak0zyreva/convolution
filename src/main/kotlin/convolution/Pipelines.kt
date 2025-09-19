package convolution

import java.io.File
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.runBlocking
import filters.Filter

fun runAsyncPipeline(
    paths: List<String>,
    filter: Filter,
    filterName: String,
    mode: ConvolutionMode,
    concurrency: Int = 8
) = runBlocking {
    File("output").mkdirs()
    readImages(paths)
        .buffer(5)
        .convolveImages(filter, mode, concurrency)
        .writeImages(filterName, mode)
        .catch { e -> println("Pipeline failed: ${e.message}") }
        .collect()
}

fun runSeqPipeline(
    paths: List<String>,
    filter: Filter,
    filterName: String,
    mode: ConvolutionMode
) {
    File("output").mkdirs()

    for (path in paths) {
        val file = File(path)
        val name = file.nameWithoutExtension
        println("Reading: ${file.name}")
        val img = loadImage(path).toGrayscale()
        println("Read: $name")

        println("Convolving: ${file.name}")
        val result = img.convolveWithMode(filter, mode)
        println("Convolved: ${file.name}")
        val outputPath = "output/${name}_${filterName}_$mode.bmp"
        saveImage(result, outputPath)
        println("Saved: $outputPath")
        result.release()
    }
}
