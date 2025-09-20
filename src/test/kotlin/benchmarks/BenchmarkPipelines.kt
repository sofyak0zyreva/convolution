package benchmarks

import convolution.ConvolutionMode
import convolution.runAsyncPipeline
import convolution.runSeqPipeline
import filters.Filter
import filters.filterPool
import java.io.File

private fun getAllBmpPathsFromResources(): List<String> {
    val resourceURL = {}::class.java.getResource("/images")
        ?: throw IllegalArgumentException("Resources folder '/images' not found")
    val folder = File(resourceURL.toURI())
    val imageFiles = folder.listFiles { file ->
        file.isFile && file.name.endsWith(".bmp", ignoreCase = true)
    } ?: emptyArray()
    if (imageFiles.isEmpty()) {
        println("No BMP images found in resources")
    }
    return imageFiles.map { it.absolutePath }
}

private fun benchmarkPipeline(
    filter: Filter,
    filterName: String,
    runs: Int = 3,
    concurrency: Int? = null,
    pipelineFunc: (List<String>, Filter, String, ConvolutionMode, Int?) -> Unit
): List<BenchmarkResult> {
    val imagePaths = getAllBmpPathsFromResources()

    val modes = listOf(
        ConvolutionMode.Sequential,
        ConvolutionMode.ParallelPixels,
        ConvolutionMode.ParallelRows(batchSize = 4),
        ConvolutionMode.ParallelCols(batchSize = 8),
        ConvolutionMode.ParallelTiles(tileWidth = 8, tileHeight = 8)
    )

    return modes.map { mode ->
        val times = mutableListOf<Double>()
        repeat(runs) {
            val time = estimateTime {
                pipelineFunc(imagePaths, filter, filterName, mode, concurrency)
            }
            times += time
        }
        BenchmarkResult(mode = mode, times = times)
    }
}

fun main() {
    val allResults = mutableMapOf<String, List<BenchmarkResult>>()
    val filterName = "gaussian_blur_3x3"
    val filter = filterPool[filterName] ?: throw IllegalArgumentException("Filter cannot be null")
    val numWorkers = 8

    val asyncResults = benchmarkPipeline(filter, filterName, numWorkers) { paths, f, name, mode, concurrency ->
        runAsyncPipeline(paths, f, name, mode, concurrency ?: 1)
    }
    allResults["async, concurrency = $numWorkers"] = asyncResults

    val seqResults = benchmarkPipeline(filter, filterName) { paths, f, name, mode, _ ->
        runSeqPipeline(paths, f, name, mode)
    }
    allResults["seq"] = seqResults

    allResults.forEach { (pipeline, results) ->
        println("=== Pipeline type: $pipeline ===")
        results.forEach(::println)
    }
}
