package convolution

import filters.createBasicFilter
import filters.kernelPool
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
    val kernelNames = kernelPool.keys.toList()

    while (true) {
        println("\nAvailable filters:")
        kernelNames.forEachIndexed { index, name ->
            println("  [$index] $name")
        }

        print("Select kernel index: ")
        val index = readlnOrNull()?.toIntOrNull()
        if (index != null && index in kernelNames.indices) {
            return kernelNames[index]
        } else {
            println("❌ Invalid index. Please enter a number from the list.")
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

        val filterName = promptForFilterName()
        val selectedKernel = kernelPool[filterName] ?: throw IllegalArgumentException("filters.Filter should not be null.")
        println("Applying filter: $filterName...")

        val resultImage = grayImage.seqConvolve(createBasicFilter(selectedKernel))
        val outputPath = "output_${filename}_${filterName}.bmp"
        saveImage(resultImage, outputPath)
        println("✅ Done! Output saved to: $outputPath")

    } catch (e: Exception) {
        System.err.println("❌ Error: ${e.message}")
    }
}
