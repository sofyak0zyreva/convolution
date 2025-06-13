import convolution.ConvolutionMode
import convolution.convolveWithMode
import convolution.seqConvolve
import java.util.Random
import kotlin.math.min
import kotlin.test.Test

class ParallelConvTests {
    @Test
    fun `parallel convolution with random batch and tile sizes is the same as sequential`() {
        val imageHeight = Random().nextInt(imageSizeBound)
        val imageWidth = Random().nextInt(imageSizeBound)
        val image = createRandomImage(imageHeight, imageWidth)
        val imageBound = min(image.rows(), image.cols())
        val size = Random().nextInt(1, imageBound)
        val modes = listOf(
            ConvolutionMode.ParallelPixels,
            ConvolutionMode.ParallelRows(batchSize = size),
            ConvolutionMode.ParallelCols(batchSize = size),
            ConvolutionMode.ParallelTiles(tileWidth = size, tileHeight = size)
        )
        val filter = createRandomFilter(setRandomFilterSize())
        for (mode in modes) {
            val parResult = image.convolveWithMode(filter, mode)
            val seqResult = image.seqConvolve(filter)
            assertMatEquals(seqResult, parResult)
        }
    }

    @Test
    fun `parallel convolution with fixed batch and tile sizes is the same as sequential`() {
        val image = createRandomImage(128, 128)
        val modes = listOf(
            ConvolutionMode.ParallelPixels,
            ConvolutionMode.ParallelRows(batchSize = 1),
            ConvolutionMode.ParallelCols(batchSize = 1),
            ConvolutionMode.ParallelTiles(tileWidth = 1, tileHeight = 1),
            ConvolutionMode.ParallelRows(batchSize = 32),
            ConvolutionMode.ParallelCols(batchSize = 32),
            ConvolutionMode.ParallelTiles(tileWidth = 32, tileHeight = 32),
            ConvolutionMode.ParallelRows(batchSize = 64),
            ConvolutionMode.ParallelCols(batchSize = 64),
            ConvolutionMode.ParallelTiles(tileWidth = 64, tileHeight = 64),
            ConvolutionMode.ParallelTiles(tileWidth = 32, tileHeight = 64),
            ConvolutionMode.ParallelTiles(tileWidth = 64, tileHeight = 16)
        )
        val filter = createRandomFilter(setRandomFilterSize())
        for (mode in modes) {
            val parResult = image.convolveWithMode(filter, mode)
            val seqResult = image.seqConvolve(filter)
            assertMatEquals(seqResult, parResult)
        }
    }
}
