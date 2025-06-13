package benchmarks

import convolution.ConvolutionMode
import kotlin.math.sqrt

internal data class BenchmarkResult(
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