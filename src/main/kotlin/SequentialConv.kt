import org.bytedeco.opencv.global.opencv_core.CV_8UC1
import org.bytedeco.opencv.opencv_core.Mat
import kotlin.math.roundToInt

fun Mat.seqConvolve(kernel: Array<FloatArray>): Mat {
    require(kernel.size % 2 == 1 && kernel[0].size % 2 == 1) { "Kernel must have odd dimensions" }

    val output = Mat(rows(), cols(), CV_8UC1) // 8-bit grayscale output
    val kRadius = kernel.size / 2

    for (y in 0 until rows()) {
        for (x in 0 until cols()) {
            var sum = 0f

            for (ky in -kRadius..kRadius) {
                for (kx in -kRadius..kRadius) {
                    // Wrap-around coordinates
                    val wrappedY = (y + ky + rows()) % rows()
                    val wrappedX = (x + kx + cols()) % cols()

                    // Get pixel value from the input matrix
                    val pixelValue = (this.ptr(wrappedY, wrappedX).get(0).toInt() and 0xFF).toFloat()

                    // Multiply by kernel
                    sum += pixelValue * kernel[ky + kRadius][kx + kRadius]
                }
            }

            // Clamp to valid 8-bit range and store in output matrix
            val clampedValue = sum.roundToInt().coerceIn(0, 255).toByte()
            output.ptr(y, x).put(0L, clampedValue)
        }
    }
    return output
}
