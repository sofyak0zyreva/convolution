import convolution.seqConvolve
import filters.createBasicFilter
import filters.filterPool
import org.bytedeco.opencv.global.opencv_core.countNonZero
import org.bytedeco.opencv.opencv_core.Mat
import org.junit.jupiter.api.Assertions.assertTrue
import java.util.Random
import kotlin.test.Test

abstract class BaseSequentialConvolutionTests {
    protected lateinit var image: Mat
    abstract fun setupImage()

    @org.junit.jupiter.api.BeforeEach
    fun setup() {
        setupImage()
    }

    @Test
    fun `identity filter should return the same image`() {
        val identityFilter = filterPool["identity"] ?: throw IllegalArgumentException("Filter must exist in the pool")
        val result = image.seqConvolve(identityFilter)
        assertMatEquals(image, result)
    }

    @Test
    fun `zero kernel should return a black image`() {
        val size = setRandomFilterSize()
        val zeroKernel = Array(size) { DoubleArray(size) { 0.0 } }
        val result = image.seqConvolve(createBasicFilter(zeroKernel))
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
        val result = image.seqConvolve(createBasicFilter(shiftLeft)).seqConvolve(createBasicFilter(shiftRight))
        assertMatEquals(image, result)
    }

    @Test
    fun `zero-padding a kernel should not change the result`() {
        val filter = createRandomFilter(5)
        val paddedFilter = padKernelWithZeros(filter, filterSizeBound)
        val resultOriginal = image.seqConvolve(filter)
        val resultPadded = image.seqConvolve(paddedFilter)
        assertMatEquals(resultOriginal, resultPadded)
    }

    @Test
    fun `composition of two filters is the same as sequential application`() {
        val filter1 = createRandomFilter(setRandomFilterSize())
        val filter2 = createRandomFilter(setRandomFilterSize())
        val composedKernel = composeKernels(filter1, filter2)
        val seqResult = image.seqConvolve(filter1).seqConvolve(filter2)
        val composedResult = image.seqConvolve(composedKernel)
        assertMatEquals(seqResult, composedResult, 1)
    }
}

class SequentialConvolutionTestsWithRandomImages : BaseSequentialConvolutionTests() {
    override fun setupImage() {
        val imageHeight = Random().nextInt(imageSizeBound)
        val imageWidth = Random().nextInt(imageSizeBound)
        image = createRandomImage(imageHeight, imageWidth)
    }
}

class SequentialConvolutionTestsWithTestImages : BaseSequentialConvolutionTests() {
    override fun setupImage() {
        image = loadRandomImageFromResources()
    }
}
