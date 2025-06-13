package filters

val filterPool = mapOf(
    "blur_3x3" to Filter(Array(3) { DoubleArray(3) { 1.0 / 9 } }),
    "blur_5x5" to Filter(Array(5) { DoubleArray(5) { 1.0 / 25 } }),
    "identity" to Filter(
        arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0)
        )
    ),
    "sharpen" to Filter(
        arrayOf(
            doubleArrayOf(0.0, -1.0, 0.0),
            doubleArrayOf(-1.0, 5.0, -1.0),
            doubleArrayOf(0.0, -1.0, 0.0)
        )
    ),
    "edge_detect" to Filter(
        arrayOf(
            doubleArrayOf(-1.0, -1.0, -1.0),
            doubleArrayOf(-1.0, 8.0, -1.0),
            doubleArrayOf(-1.0, -1.0, -1.0)
        )
    ),
    "motion_blur" to Filter(
        arrayOf(
            doubleArrayOf(1.0 / 9, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 1.0 / 9, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 1.0 / 9, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 1.0 / 9, 0.0, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 1.0 / 9, 0.0, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 1.0 / 9, 0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 / 9, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 / 9, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0 / 9)
        )
    ),
    "gaussian_blur_3x3" to Filter(
        arrayOf(
            doubleArrayOf(1.0, 2.0, 1.0),
            doubleArrayOf(2.0, 4.0, 2.0),
            doubleArrayOf(1.0, 2.0, 1.0)
        ),
        factor = 1.0 / 16
    ),
    "gaussian_blur_5x5" to Filter(
        arrayOf(
            doubleArrayOf(1.0, 4.0, 6.0, 4.0, 1.0),
            doubleArrayOf(4.0, 16.0, 24.0, 16.0, 4.0),
            doubleArrayOf(6.0, 24.0, 36.0, 24.0, 6.0),
            doubleArrayOf(4.0, 16.0, 24.0, 16.0, 4.0),
            doubleArrayOf(1.0, 4.0, 6.0, 4.0, 1.0)
        ),
        factor = 1.0 / 256
    ),
    "emboss" to Filter(
        arrayOf(
            doubleArrayOf(-1.0, -1.0, -1.0, -1.0, 0.0),
            doubleArrayOf(-1.0, -1.0, -1.0, 0.0, 1.0),
            doubleArrayOf(-1.0, -1.0, 0.0, 1.0, 1.0),
            doubleArrayOf(-1.0, 0.0, 1.0, 1.0, 1.0),
            doubleArrayOf(0.0, 1.0, 1.0, 1.0, 1.0)
        ),
        bias = 128.0
    )
)

fun createBasicFilter(kernel: Array<DoubleArray>): Filter {
    return Filter(kernel = kernel)
}
fun checkFilterSize(filter: Filter) {
    val kernel = filter.kernel
    require(kernel.size % 2 == 1 && kernel[0].size % 2 == 1) { "Kernel must have odd dimensions" }
}
