package filters

val kernelPool = mapOf(
    "blur_3x3" to Array(3) { DoubleArray(3) { 1.0/9 } },
    "blur_5x5" to Array(5) { DoubleArray(5) { 1.0/25 } },
    "identity" to arrayOf(
        doubleArrayOf(0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 1.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0)
        ),
    "sharpen" to arrayOf(
        doubleArrayOf(0.0, -1.0, 0.0),
        doubleArrayOf(-1.0, 5.0, -1.0),
        doubleArrayOf(0.0, -1.0, 0.0)
    ),
    "edge_detect" to arrayOf(
        doubleArrayOf(-1.0, -1.0, -1.0),
        doubleArrayOf(-1.0, 8.0, -1.0),
        doubleArrayOf(-1.0, -1.0, -1.0)
    ),
    "motion_blur" to arrayOf(
        doubleArrayOf(1.0 / 9, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 1.0 / 9, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 1.0 / 9, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 1.0 / 9, 0.0, 0.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0 / 9, 0.0, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 1.0 / 9, 0.0, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 / 9, 0.0, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 / 9, 0.0),
        doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 / 9),
    )
)

fun createBasicFilter(kernel: Array<DoubleArray>): Filter {
    return Filter(kernel = kernel)
}
fun checkFilterSize(filter: Filter) {
    val kernel = filter.kernel
    require(kernel.size % 2 == 1 && kernel[0].size % 2 == 1) { "Kernel must have odd dimensions" }
}
