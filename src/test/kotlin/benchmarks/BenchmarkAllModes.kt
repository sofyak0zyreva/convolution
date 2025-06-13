package benchmarks

import assertMatEquals
import convolution.*
import filters.Filter
import filters.filterPool
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Mat

// function that compares execution times of all convolution implementations
fun benchmarkAllModes(
    image: Mat,
    filter: Filter,
    runs: Int = 10,
    verifyCorrectness: Boolean = true
): List<BenchmarkResult> {
    val modes = listOf(
        ConvolutionMode.Sequential,
        ConvolutionMode.ParallelPixels,
        ConvolutionMode.ParallelRows(batchSize = 4),
        ConvolutionMode.ParallelCols(batchSize = 8),
        ConvolutionMode.ParallelTiles(tileWidth = 4, tileHeight = 4)
    )
    val reference = image.seqConvolve(filter)
    return modes.map { mode ->
        val times = mutableListOf<Double>()
        var result = image
        repeat(runs) {
            val time = when (mode) {
                ConvolutionMode.Sequential -> estimateTime { result = image.seqConvolve(filter) }
                ConvolutionMode.ParallelPixels -> estimateTime { result = image.parallelConvolvePixels(filter) }
                is ConvolutionMode.ParallelRows -> estimateTime { result = image.parallelConvolveRows(filter, mode.batchSize) }
                is ConvolutionMode.ParallelCols -> estimateTime { result = image.parallelConvolveCols(filter, mode.batchSize) }
                is ConvolutionMode.ParallelTiles -> estimateTime { result = image.parallelConvolveTiles(filter, mode.tileWidth, mode.tileHeight) }}
            times += time
            if (verifyCorrectness) {
                assertMatEquals(reference, result)
            }
        }
        BenchmarkResult(
            mode = mode,
            times = times,
        )
    }
}

fun main() {
    val resource = object {}.javaClass.getResource("/images/cat.bmp")
    requireNotNull(resource) { "Image not found!" }
    val testImage = opencv_imgcodecs.imread(resource.path)
    val allResults = mutableMapOf<String, List<BenchmarkResult>>()
    for ((filterName, filter) in filterPool) {
        val results = benchmarkAllModes(
            image = testImage,
            filter = filter
        )
        allResults[filterName] = results
    }

    allResults.forEach { (filterName, results) ->
        println("=== Filter: $filterName ===")
        results.forEach(::println)
    }
}
