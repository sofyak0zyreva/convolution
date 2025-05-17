import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat

fun loadImage(path: String): Mat {
    val mat = opencv_imgcodecs.imread(path)
    if (mat.empty()) throw IllegalArgumentException("Failed to load image: $path")
    return mat
}

fun saveImage(image: Mat, path: String) {
    if (!opencv_imgcodecs.imwrite(path, image)) throw IllegalStateException("Failed to save image: $path")
}

fun Mat.toGrayscale(): Mat {
    val grayMat = Mat()
    opencv_imgproc.cvtColor(this, grayMat, opencv_imgproc.COLOR_BGR2GRAY) // Note: OpenCV uses BGR order by default!
    return grayMat
}

fun main() {
    // Initialize OpenCV and dependencies
    Loader.load(org.bytedeco.opencv.global.opencv_imgcodecs::class.java)

    try {
        val inputImage = loadImage("1920-1080-sample.bmp")
        val grayImage = inputImage.toGrayscale()

        val kernel5x5 = arrayOf(
            floatArrayOf(1f / 25, 1f / 25, 1f / 25, 1f / 25, 1f / 25),
            floatArrayOf(1f / 25, 1f / 25, 1f / 25, 1f / 25, 1f / 25),
            floatArrayOf(1f / 25, 1f / 25, 1f / 25, 1f / 25, 1f / 25),
            floatArrayOf(1f / 25, 1f / 25, 1f / 25, 1f / 25, 1f / 25),
            floatArrayOf(1f / 25, 1f / 25, 1f / 25, 1f / 25, 1f / 25)
        )

        val blurImage = grayImage.seqConvolve(kernel5x5)
        saveImage(blurImage, "output_blur.bmp")

        println("✅ Blur convolution done!")
    } catch (e: Exception) {
        System.err.println("Error: ${e.message}")
    }
}
