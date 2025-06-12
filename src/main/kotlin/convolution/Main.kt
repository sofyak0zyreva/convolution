package convolution

import filters.filterPool
import org.bytedeco.javacpp.Loader
import java.io.File

private fun promptForImagePath(): String {
    while (true) {
        print("Enter path to image (e.g., sample.bmp): ")
        val path = readlnOrNull()?.trim()
        if (!path.isNullOrBlank()) {
            try {
                loadImage(path) // check if image is valid
                return path
            } catch (e: Exception) {
                println("❌ Cannot load image. Reason: ${e.message}")
            }
        } else {
            println("❌ Path cannot be empty.")
        }
    }
}

private fun promptForFilterName(): String {
    val filterNames = filterPool.keys.toList()

    while (true) {
        println("\nAvailable filters:")
        filterNames.forEachIndexed { index, name ->
            println("  [$index] $name")
        }

        print("Select filter index: ")
        val index = readlnOrNull()?.toIntOrNull()
        if (index != null && index in filterNames.indices) {
            return filterNames[index]
        } else {
            println("❌ Invalid index. Please enter a number from the list.")
        }
    }
}

private fun promptForMode(): ConvolutionMode {
    val options = listOf(
        "sequential",
        "parallel pixels",
        "parallel rows (with batch size)",
        "parallel columns (with batch size)",
        "parallel tiles (with tile size)"
    )

    println("\nAvailable convolution modes:")
    options.forEachIndexed { index, name ->
        println("  [$index] $name")
    }

    while (true) {
        print("Select mode index: ")
        when (readlnOrNull()?.toIntOrNull()) {
            0 -> return ConvolutionMode.Sequential
            1 -> return ConvolutionMode.ParallelPixels
            2 -> {
                print("Enter batch size [default 32]: ")
                val batch = readlnOrNull()?.toIntOrNull() ?: 32
                return ConvolutionMode.ParallelRows(batch)
            }
            3 -> {
                print("Enter batch size [default 32]: ")
                val batch = readlnOrNull()?.toIntOrNull() ?: 32
                return ConvolutionMode.ParallelCols(batch)
            }
            4 -> {
                print("Enter tile width [default 32]: ")
                val w = readlnOrNull()?.toIntOrNull() ?: 32
                print("Enter tile height [default 32]: ")
                val h = readlnOrNull()?.toIntOrNull() ?: 32
                return ConvolutionMode.ParallelTiles(w, h)
            }
            else -> println("❌ Invalid index. Please enter a number from the list.")
        }
    }
}

fun main() {
    Loader.load(org.bytedeco.opencv.global.opencv_imgcodecs::class.java)

    try {
        val imagePath = promptForImagePath()
        val inputImage = loadImage(imagePath)
        val grayImage = inputImage.toGrayscale()
        val filename = File(imagePath).nameWithoutExtension

        val selectedMode = promptForMode()
        val modeName = selectedMode.toString()

        val filterName = promptForFilterName()
        val filter = filterPool[filterName] ?: throw IllegalArgumentException("Filter should not be null.")
        println("Applying filter: $filterName...")

        val resultImage = grayImage.convolveWithMode(filter, selectedMode)
        val outputPath = "output_${filename}_${filterName}_${modeName}.bmp"
        saveImage(resultImage, outputPath)
        println("✅ Done! Output saved to: $outputPath")

    } catch (e: Exception) {
        System.err.println("❌ Error: ${e.message}")
    }
}
