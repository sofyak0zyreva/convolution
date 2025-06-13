package convolution

import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat

internal fun loadImage(path: String): Mat {
    val mat = opencv_imgcodecs.imread(path)
    if (mat.empty()) throw IllegalArgumentException("Failed to load image: $path")
    return mat
}

internal fun saveImage(image: Mat, path: String) {
    if (!opencv_imgcodecs.imwrite(path, image)) throw IllegalStateException("Failed to save image: $path")
}

internal fun Mat.toGrayscale(): Mat {
    val grayMat = Mat()
    opencv_imgproc.cvtColor(this, grayMat, opencv_imgproc.COLOR_BGR2GRAY) // Note: OpenCV uses BGR order by default!
    return grayMat
}
