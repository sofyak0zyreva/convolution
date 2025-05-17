val kernelPool = mapOf(
    "blur_3x3" to arrayOf(
        floatArrayOf(1f / 9, 1f / 9, 1f / 9),
        floatArrayOf(1f / 9, 1f / 9, 1f / 9),
        floatArrayOf(1f / 9, 1f / 9, 1f / 9)
    ),
    "blur_5x5" to Array(5) { FloatArray(5) { 1f / 25 } },
    "sharpen" to arrayOf(
        floatArrayOf(0f, -1f, 0f),
        floatArrayOf(-1f, 5f, -1f),
        floatArrayOf(0f, -1f, 0f)
    ),
    "edge_detect" to arrayOf(
        floatArrayOf(-1f, -1f, -1f),
        floatArrayOf(-1f, 8f, -1f),
        floatArrayOf(-1f, -1f, -1f)
    )
)