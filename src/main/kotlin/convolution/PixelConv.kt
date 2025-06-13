package convolution

import filters.Filter
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.math.roundToInt

fun convolvePixel(x: Int, y: Int, filter: Filter, input: Mat): Byte {
    val kernel = filter.kernel
    val kRadius = kernel.size / 2
    var sum = 0.0
    for (ky in -kRadius..kRadius) {
        for (kx in -kRadius..kRadius) {
            // wrap-around coordinates
            val wrappedY = (y + ky + input.rows()) % input.rows()
            val wrappedX = (x + kx + input.cols()) % input.cols()
            // get pixel value from the input matrix
            val pixelValue = (input.ptr(wrappedY, wrappedX).get(0).toInt() and 0xFF).toDouble()
            // multiply by kernel
            sum += pixelValue * kernel[ky + kRadius][kx + kRadius]
        }
    }
    // clamp to valid 8-bit range and store in output matrix
    val clampedValue = (sum * filter.factor + filter.bias).roundToInt().coerceIn(0, 255)
    return clampedValue.toByte()
}
