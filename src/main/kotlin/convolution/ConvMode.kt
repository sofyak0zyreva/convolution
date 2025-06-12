package convolution

import filters.Filter
import org.bytedeco.opencv.opencv_core.Mat

// options carry different parameters so not enum
sealed class ConvolutionMode {
    object Sequential : ConvolutionMode()
    object ParallelPixels : ConvolutionMode()
    class ParallelRows(val batchSize: Int) : ConvolutionMode()
    class ParallelCols(val batchSize: Int) : ConvolutionMode()
    class ParallelTiles(val tileWidth: Int, val tileHeight: Int) : ConvolutionMode()

    override fun toString(): String = when (this) {
        Sequential -> "seq"
        ParallelPixels -> "par_pixels"
        is ParallelRows -> "par_rows_${batchSize}"
        is ParallelCols -> "par_cols_${batchSize}"
        is ParallelTiles -> "par_tiles_${tileWidth}x${tileHeight}"
    }
}

fun Mat.convolveWithMode(filter: Filter, mode: ConvolutionMode): Mat = when (mode) {
    is ConvolutionMode.Sequential -> this.seqConvolve(filter)
    is ConvolutionMode.ParallelPixels -> this.parallelConvolvePixels(filter)
    is ConvolutionMode.ParallelRows -> this.parallelConvolveRows(filter, mode.batchSize)
    is ConvolutionMode.ParallelCols -> this.parallelConvolveCols(filter, mode.batchSize)
    is ConvolutionMode.ParallelTiles -> this.parallelConvolveTiles(filter, mode.tileWidth, mode.tileHeight)
}

