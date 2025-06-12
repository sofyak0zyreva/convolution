import convolution.*
import java.util.*
import kotlin.math.min
import kotlin.test.Test

class ParallelConvolutionTests{
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
            val parResult = when (mode) {
                is ConvolutionMode.ParallelPixels -> image.parallelConvolvePixels(filter)
                is ConvolutionMode.ParallelRows -> image.parallelConvolveRows(filter, mode.batchSize)
                is ConvolutionMode.ParallelCols -> image.parallelConvolveCols(filter, mode.batchSize)
                is ConvolutionMode.ParallelTiles -> image.parallelConvolveTiles(filter, mode.tileWidth, mode.tileHeight)
                else -> image.seqConvolve(filter)
            }
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
            val parResult = when (mode) {
                is ConvolutionMode.ParallelPixels -> image.parallelConvolvePixels(filter)
                is ConvolutionMode.ParallelRows -> image.parallelConvolveRows(filter, mode.batchSize)
                is ConvolutionMode.ParallelCols -> image.parallelConvolveCols(filter, mode.batchSize)
                is ConvolutionMode.ParallelTiles -> image.parallelConvolveTiles(filter, mode.tileWidth, mode.tileHeight)
                else -> image.seqConvolve(filter)
            }
            val seqResult = image.seqConvolve(filter)
            assertMatEquals(seqResult, parResult)
        }
    }
}