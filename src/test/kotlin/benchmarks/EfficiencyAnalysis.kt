package benchmarks

import assertMatEquals
import convolution.*
import createRandomFilter
import createRandomImage
import filters.Filter
import filters.filterPool
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.math.sqrt
import kotlin.time.DurationUnit
import kotlin.time.measureTime

data class BenchmarkResult(
    val mode: ConvolutionMode,
    val times: List<Double>,
) {
    private val n: Int get() = times.size
    private val average: Double get() = times.average()
    private val stdDev: Double get() = sqrt(times.sumOf { (it - average).let { d -> d * d } } / (n - 1))
    private val stdErrorOfTheMean: Double get() = stdDev / sqrt(n.toDouble())

    override fun toString(): String {
        return buildString {
            appendLine("Mode: $mode")
            appendLine("  Average Time:          %.4f s".format(average))
            appendLine("  Std Error Of The Mean: %.4f s".format(stdErrorOfTheMean))
        }
    }
}

inline fun estimateTime(block: () -> Unit) : Double {
    return measureTime { block() }.toDouble(DurationUnit.SECONDS)
}

fun benchmarkSizes(
    image: Mat,
    filter: Filter,
    modeFactory: (Int) -> ConvolutionMode,
    sizesToTry: List<Int>,
    runs: Int = 10
): List<BenchmarkResult> {
    return sizesToTry.map { size ->
        val mode = modeFactory(size)
        val times = List(runs) {
            when (mode) {
                ConvolutionMode.Sequential -> estimateTime { image.seqConvolve(filter) }
                ConvolutionMode.ParallelPixels -> estimateTime { image.parallelConvolvePixels(filter) }
                is ConvolutionMode.ParallelRows -> estimateTime { image.parallelConvolveRows(filter, mode.batchSize) }
                is ConvolutionMode.ParallelCols -> estimateTime { image.parallelConvolveCols(filter, mode.batchSize) }
                is ConvolutionMode.ParallelTiles -> estimateTime { image.parallelConvolveTiles(filter, mode.tileWidth, mode.tileHeight) }}
        }
        BenchmarkResult(mode, times)
    }
}

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
    val testImage = createRandomImage(512, 512)
//    val testFilter = createRandomFilter(5)
//
//    val testSizes = listOf(
//        1,
//        4,
//        8,
//        16,
//        32,
//        64,
//        128,
//        256,
//        512
//    )
//
//    val benchmarkOptimalSize: ((Int) -> ConvolutionMode) -> List<BenchmarkResult> = { modeFactory ->
//        benchmarkSizes(
//            image = testImage,
//            filter = testFilter,
//            sizesToTry = testSizes,
//            modeFactory = modeFactory
//        )
//    }
//    val rowResult = benchmarkOptimalSize { ConvolutionMode.ParallelRows(it) }
//    val colResult = benchmarkOptimalSize { ConvolutionMode.ParallelCols(it) }
//    val tileResult = benchmarkOptimalSize { size -> ConvolutionMode.ParallelTiles(size, size) }
//    rowResult.forEach(::println)
//    colResult.forEach(::println)
//    tileResult.forEach(::println)

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