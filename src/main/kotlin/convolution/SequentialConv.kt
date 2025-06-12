package convolution

import filters.Filter
import filters.checkFilterSize
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.opencv_core.Mat

fun Mat.seqConvolve(filter: Filter): Mat {
    checkFilterSize(filter)

    val output = Mat(rows(), cols(), CV_8UC1) // 8-bit grayscale output
    for (x in 0 until cols()) {
        for (y in 0 until rows()) {
            val pixelValue = convolvePixel(x, y, filter, this)
            output.ptr(y, x).put(pixelValue)
        }
    }
    return output
}
