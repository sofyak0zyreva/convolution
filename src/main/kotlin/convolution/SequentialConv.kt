package convolution

import filters.Filter
import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.math.roundToInt

fun Mat.seqConvolve(filter: Filter): Mat {
    val kernel = filter.kernel
    require(kernel.size % 2 == 1 && kernel[0].size % 2 == 1) { "Kernel must have odd dimensions" }

    val output = Mat(rows(), cols(), CV_8UC1) // 8-bit grayscale output
    val kRadius = kernel.size / 2
    for (x in 0 until cols()) {
        for (y in 0 until rows()) {
            var sum = 0.0
            for (ky in -kRadius..kRadius) {
                for (kx in -kRadius..kRadius) {
                    // wrap-around coordinates
                    val wrappedY = (y + ky + rows()) % rows()
                    val wrappedX = (x + kx + cols()) % cols()
                    // get pixel value from the input matrix
                    val pixelValue = (this.ptr(wrappedY, wrappedX).get(0).toInt() and 0xFF).toDouble()
                    // multiply by kernel
                    sum += pixelValue * kernel[ky + kRadius][kx + kRadius]
                }
            }
            // clamp to valid 8-bit range and store in output matrix
            val clampedValue = (sum * filter.factor + filter.bias).roundToInt().coerceIn(0, 255)
            output.ptr(y, x).put(clampedValue.toByte())

        }
    }
    return output
}
