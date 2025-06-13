package benchmarks

import convolution.*
import createRandomFilter
import createRandomImage
import filters.Filter
import org.bytedeco.opencv.opencv_core.Mat

// function to measure execution time with different batch or tile sizes
// to then choose the most optimal one to compare across all convolution implementations
private fun benchmarkSizes(
    image: Mat,
    filter: Filter,
    modeFactory: (Int) -> ConvolutionMode,
    sizesToTry: List<Int>,
    runs: Int = 10
): List<BenchmarkResult> {
    return sizesToTry.map { size ->
        val mode = modeFactory(size)
        val times = List(runs) { measureSingleModeTime(image, mode, filter) }
        BenchmarkResult(mode, times)
    }
}

fun main() {
    val testImage = createRandomImage(512, 512)
    val testFilter = createRandomFilter(5)

    val testSizes = listOf(
        1,
        4,
        8,
        16,
        32,
        64,
        128,
        256,
        512
    )

    val benchmarkOptimalSize: ((Int) -> ConvolutionMode) -> List<BenchmarkResult> = { modeFactory ->
        benchmarkSizes(
            image = testImage,
            filter = testFilter,
            sizesToTry = testSizes,
            modeFactory = modeFactory
        )
    }
    val rowResult = benchmarkOptimalSize { ConvolutionMode.ParallelRows(it) }
    val colResult = benchmarkOptimalSize { ConvolutionMode.ParallelCols(it) }
    val tileResult = benchmarkOptimalSize { size -> ConvolutionMode.ParallelTiles(size, size) }
    rowResult.forEach(::println)
    colResult.forEach(::println)
    tileResult.forEach(::println)
}
