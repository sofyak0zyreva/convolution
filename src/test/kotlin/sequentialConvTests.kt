import org.bytedeco.opencv.opencv_core.Mat
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import kotlin.math.abs
import kotlin.test.Test
import org.junit.jupiter.api.Assertions.assertTrue
import org.bytedeco.opencv.global.opencv_core.countNonZero
import org.bytedeco.opencv.global.opencv_imgcodecs.imread
import org.junit.jupiter.api.BeforeEach
import java.io.File


// create a grayscale Mat filled with random values between 0 and 255
fun createRandomImage(rows: Int, cols: Int): Mat {
    val image = Mat(rows, cols, CV_8UC1)
    val rng = java.util.Random()
    for (y in 0 until rows) {
        for (x in 0 until cols) {
            val value = rng.nextInt(256).toByte()
            image.ptr(y, x).put(value)
        }
    }
    return image
}

fun createRandomKernel(size: Int): Array<DoubleArray> {
    require(size % 2 == 1) {"Kernel size must be odd"}
    val kernel = Array(size){ DoubleArray(size) }
    val rng = java.util.Random()
    val min = -10.0
    val max = 10.0
    for (y in 0 until size) {
        for (x in 0 until size) {
            kernel[y][x] = rng.nextDouble() * (max - min) + min
         }
    }
    return kernel
}

fun assertMatEquals(expected: Mat, actual: Mat, tolerance: Int = 0) {
    require(expected.rows() == actual.rows() && expected.cols() == actual.cols())
    for (y in 0 until expected.rows()) {
        for (x in 0 until expected.cols()) {
            // fixing the difference between signed and unsigned bytes in Kotlin and OpenCV
            val e = expected.ptr(y, x).get(0).toInt() and 0xFF
            val a = actual.ptr(y, x).get(0).toInt() and 0xFF
            assert(abs(e - a) <= tolerance) { "Mismatch at ($x, $y): $e != $a" }
        }
    }
}

fun padKernelWithZeros(kernel: Array<DoubleArray>, newSize: Int): Array<DoubleArray> {
    require(newSize % 2 == 1) { "New kernel size must be odd" }
    val oldSize = kernel.size
    require(kernel[0].size == oldSize) { "Kernel must be square" }
    require(newSize >= oldSize) { "New size must be >= old size" }

    val pad = (newSize - oldSize) / 2
    return Array(newSize) { y ->
        DoubleArray(newSize) { x ->
            val ky = y - pad
            val kx = x - pad
            if (ky in kernel.indices && kx in kernel[0].indices) kernel[ky][kx] else 0.0
        }
    }
}

fun flipKernel(kernel: Array<DoubleArray>): Array<DoubleArray> {
    return Array(kernel.size) { i ->
        DoubleArray(kernel[0].size) { j ->
            kernel[kernel.size - 1 - i][kernel[0].size - 1 - j]
        }
    }
}

fun composeKernels(
    kernel1: Array<DoubleArray>,
    kernel2: Array<DoubleArray>
): Array<DoubleArray> {
    val size1 = kernel1.size
    println(size1)
    val size2 = kernel2.size
    println(size2)
    val newSize = size1 + size2 - 1

    val result = Array(newSize) { DoubleArray(newSize) { 0.0 } }
    println("composed kernel")
    for (i in 0 until newSize) {
        for (j in 0 until newSize) {
            for (ki in 0 until size1) {
                for (kj in 0 until size1) {
                    val i2 = i - ki
                    val j2 = j - kj
                    if (i2 in 0 until size2 && j2 in 0 until size2) {
                        result[i][j] += kernel1[ki][kj] * kernel2[i2][j2]
                    }
                }
            }
            println(result[i][j])
        }
    }
    return result
}

fun Mat.toArray2D(): Array<IntArray> {
    val result = Array(rows()) { IntArray(cols()) }
    for (y in 0 until rows()) {
        for (x in 0 until cols()) {
            result[y][x] = this.ptr(y, x).get(0).toInt() and 0xFF
        }
    }
    return result
}

class SequentialConvolutionTestsWithRandomImages{
    private lateinit var image: Mat

    @BeforeEach
    fun setup() {
        val imageSize = java.util.Random().nextInt(imageSizeBound)
        image = createRandomImage(imageSize, imageSize)
    }

    @Test
    fun `identity filter should return the same image`() {
        val identityKernel = kernelPool["identity"] ?: throw IllegalArgumentException("Kernel must exist in the pool")
        val result = image.seqConvolve(identityKernel)
        assertMatEquals(image, result)
    }

    @Test
    fun `zero kernel should return a black image`() {
        val zeroKernel = Array(3) { DoubleArray(3) { 0.0 } }
        val result = image.seqConvolve(zeroKernel)
        assertTrue(countNonZero(result) == 0)
    }

    @Test
    fun `shift left then right should be identity`() {
        val shiftLeft = arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0),
            doubleArrayOf(1.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0)
        )
        val shiftRight = arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 1.0),
            doubleArrayOf(0.0, 0.0, 0.0)
        )
        val result = image.seqConvolve(shiftLeft).seqConvolve(shiftRight)
        assertMatEquals(image, result)
    }

    @Test
    fun `zero-padding a kernel should not change the result`() {
        val kernel = createRandomKernel(5)
        val paddedKernel = padKernelWithZeros(kernel, 9)
        val resultOriginal = image.seqConvolve(kernel)
        val resultPadded = image.seqConvolve(paddedKernel)
        assertMatEquals(resultOriginal, resultPadded)
    }

//    @Test
//    fun `composition of two filters is the same as sequential application`() {
//        val kernel1 = kernelPool["sharpen"] ?: throw IllegalArgumentException("Kernel must exist in the pool")
//        val kernel2 = kernelPool[ "sharpen"] ?: throw IllegalArgumentException("Kernel must exist in the pool")
//        val flippedKernel2 = flipKernel(kernel2)
////        val composedKernel = composeKernels(kernel1, flippedKernel2)
//        val composedKernel = composeKernels(kernel2, kernel1)
//        println(composedKernel.joinToString("\n") { it.joinToString(" ") })
//        println(composedKernel.size)
//        val test_image = image
//        val seqResult = test_image.seqConvolve(kernel1).seqConvolve(kernel2)
//        val array = seqResult.toArray2D()
//        println(array.joinToString("\n") { it.joinToString(" ") })
//        val composedResult = test_image.seqConvolve(composedKernel)
//        val array2 = composedResult.toArray2D()
//        println()
//        println(array2.joinToString("\n") { it.joinToString(" ") })
//        assertMatEquals(seqResult, composedResult, 1)
//    }

}

fun loadRandomImageFromResources(): Mat {
    val resourceURL = {}::class.java.getResource("/images")
        ?: throw IllegalArgumentException("Resources root not found")
    val folder = File(resourceURL.toURI())
    val imageFiles = folder.listFiles { file ->
        file.isFile && file.name.endsWith(".bmp")
    } ?: throw IllegalStateException("No image files found in resources")
    imageFiles.forEach { println(it.name) }
    val randomFile = imageFiles.random()
    return imread(randomFile.absolutePath)
}

class SequentialConvolutionTestsWithTestImages{
    private lateinit var image: Mat

    @BeforeEach
    fun setup() {
//        val resource = {}::class.java.getResource("/cat.bmp")
//        image = imread(resource.path)
        image = loadRandomImageFromResources()
    }

    @Test
    fun `identity filter should return the same image`() {
        val identityKernel = kernelPool["identity"] ?: throw IllegalArgumentException("Kernel must exist in the pool")
        val result = image.seqConvolve(identityKernel)
        assertMatEquals(image, result)
    }

    @Test
    fun `zero kernel should return a black image`() {
        val zeroKernel = Array(3) { DoubleArray(3) { 0.0 } }
        val result = image.seqConvolve(zeroKernel)
        assertTrue(countNonZero(result) == 0)
    }

    @Test
    fun `shift left then right should be identity`() {
        val shiftLeft = arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0),
            doubleArrayOf(1.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 0.0)
        )
        val shiftRight = arrayOf(
            doubleArrayOf(0.0, 0.0, 0.0),
            doubleArrayOf(0.0, 0.0, 1.0),
            doubleArrayOf(0.0, 0.0, 0.0)
        )
        val result = image.seqConvolve(shiftLeft).seqConvolve(shiftRight)
        assertMatEquals(image, result)
    }

    @Test
    fun `zero-padding a kernel should not change the result`() {
        val kernel = createRandomKernel(5)
        val paddedKernel = padKernelWithZeros(kernel, 9)
        val resultOriginal = image.seqConvolve(kernel)
        val resultPadded = image.seqConvolve(paddedKernel)
        assertMatEquals(resultOriginal, resultPadded)
    }

}
