import filters.Filter
import org.bytedeco.opencv.global.opencv_core
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.opencv_core.Mat
import java.io.File
import java.util.Random
import kotlin.math.abs

const val imageSizeBound = 2400
const val filterSizeBound = 9

// create a grayscale Mat filled with random values between 0 and 255
fun createRandomImage(rows: Int, cols: Int): Mat {
    val image = Mat(rows, cols, opencv_core.CV_8UC1)
    val rng = java.util.Random()
    for (y in 0 until rows) {
        for (x in 0 until cols) {
            val value = rng.nextInt(256).toByte()
            image.ptr(y, x).put(value)
        }
    }
    return image
}

fun createRandomFilter(size: Int): Filter {
    require(size % 2 == 1) { "Kernel size must be odd" }
    val kernel = Array(size) { DoubleArray(size) }
    val min = 0.0001
    val max = 10.0

    for (i in 0 until size) {
        for (j in 0 until size) {
            val value = min + Random().nextDouble() * (max - min)
            kernel[i][j] = value
        }
    }

    val sum = kernel.sumOf { row -> row.sum() }
    // normalize if sum is not zero
    if (sum != 0.0) {
        for (i in 0 until size) {
            for (j in 0 until size) {
                kernel[i][j] /= sum
            }
        }
    }
    val factor = Random().nextDouble()
    return Filter(kernel = kernel, factor = factor)
}

fun setRandomFilterSize(): Int {
    val size = Random().nextInt(3, filterSizeBound)
    // to get the odd size
    return size + (size + 1) % 2
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

fun padKernelWithZeros(filter: Filter, newSize: Int): Filter {
    require(newSize % 2 == 1) { "New kernel size must be odd" }
    val kernel = filter.kernel
    val oldSize = kernel.size
    require(kernel[0].size == oldSize) { "Kernel must be square" }
    require(newSize >= oldSize) { "New size must be >= old size" }

    val pad = (newSize - oldSize) / 2
    val newKernel = Array(newSize) { y ->
        DoubleArray(newSize) { x ->
            val ky = y - pad
            val kx = x - pad
            if (ky in kernel.indices && kx in kernel[0].indices) kernel[ky][kx] else 0.0
        }
    }
    return Filter(kernel = newKernel, factor = filter.factor, bias = filter.bias)
}

fun composeKernels(filter1: Filter, filter2: Filter): Filter {
    val kernel1 = filter1.kernel
    val kernel2 = filter2.kernel
    val size1 = kernel1.size
    val size2 = kernel2.size
    val newSize = size1 + size2 - 1
    val result = Array(newSize) { DoubleArray(newSize) { 0.0 } }

    for (i in 0 until newSize) {
        for (j in 0 until newSize) {
            var sum = 0.0
            for (ki in 0 until size1) {
                for (kj in 0 until size1) {
                    val i2 = i - ki
                    val j2 = j - kj
                    if (i2 in 0 until size2 && j2 in 0 until size2) {
                        sum += kernel1[ki][kj] * kernel2[i2][j2]
                    }
                }
            }
            result[i][j] = sum
        }
    }
    return Filter(kernel = result, factor = filter1.factor * filter2.factor, bias = filter1.bias * filter2.factor + filter2.bias)
}

fun loadRandomImageFromResources(): Mat {
    val resourceURL = {}::class.java.getResource("/images")
        ?: throw IllegalArgumentException("Resources root not found")
    val folder = File(resourceURL.toURI())
    val imageFiles = folder.listFiles { file ->
        file.isFile && file.name.endsWith(".bmp")
    } ?: throw IllegalStateException("No image files found in resources")
    val randomFile = imageFiles.random()
    return opencv_imgcodecs.imread(randomFile.absolutePath)
}

// fun Mat.toArray2D(): Array<IntArray> {
//    val result = Array(rows()) { IntArray(cols()) }
//    for (y in 0 until rows()) {
//        for (x in 0 until cols()) {
//            result[y][x] = this.ptr(y, x).get(0).toInt() and 0xFF
//        }
//    }
//    return result
// }
