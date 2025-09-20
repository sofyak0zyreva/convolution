package convolution

import filters.filterPool
import java.io.File

fun promptForImageOrDir(): List<String> {
    while (true) {
        print("Enter path to image or directory: ")
        val input = readlnOrNull()?.trim()

        if (input.isNullOrBlank()) {
            println("❌ Path cannot be empty.")
            continue
        }

        val file = File(input)

        if (!file.exists()) {
            println("❌ Path does not exist: $input")
            continue
        }

        // Single image file
        if (file.isFile) {
            try {
                loadImage(file.absolutePath) // check if image is valid
                return listOf(file.absolutePath)
            } catch (e: Exception) {
                println("❌ Cannot load image. Reason: ${e.message}")
                continue
            }
        }

        // Directory
        if (file.isDirectory) {
            val files = file.listFiles { f -> f.isFile }
                ?.map { it.absolutePath }
                ?.sorted()
                ?: emptyList()

            if (files.isEmpty()) {
                println("❌ Directory is empty. No images to process.")
                continue
            }

            return files
        }

        println("❌ Not a valid file or directory: $input")
    }
}

fun promptForFilterName(): String {
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

fun promptForMode(): ConvolutionMode {
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
                print("Enter batch size [default 1 -- per row]: ")
                val batch = readlnOrNull()?.toIntOrNull() ?: 1
                return ConvolutionMode.ParallelRows(batch)
            }
            3 -> {
                print("Enter batch size [default 1 -- per column]: ")
                val batch = readlnOrNull()?.toIntOrNull() ?: 1
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
//    val imagePaths = listOf(
//        "src/main/resources/images/bird.bmp",
//        "src/main/resources/images/scenery.bmp",
//        "src/main/resources/images/cat.bmp",
//        "src/main/resources/images/cat.bmp",
//        "src/main/resources/images/cat.bmp",
//        "src/main/resources/images/cat.bmp",
//        "src/main/resources/images/cat.bmp",
//        "src/main/resources/images/cat.bmp",
//        "src/main/resources/images/cat.bmp",
//        "src/main/resources/images/cat.bmp"
//    )
    try {
        val imagePaths = promptForImageOrDir()
        val imageNum = imagePaths.size
        val selectedMode = promptForMode()
        val filterName = promptForFilterName()
        val filter = filterPool[filterName] ?: throw IllegalArgumentException("Filter should not be null.")
        println("Applying filter: $filterName...")

        if (imageNum == 1) {
            val imagePath = imagePaths[0]
            val inputImage = loadImage(imagePath)
            val grayImage = inputImage.toGrayscale()
            val filename = File(imagePath).nameWithoutExtension
            val resultImage = grayImage.convolveWithMode(filter, selectedMode)
            File("output").apply { mkdirs() }
            val modeName = selectedMode.toString()
            val outputPath = "output${File.separator}${filename}_${filterName}_$modeName.bmp"
            saveImage(resultImage, outputPath)
            println("✅ Done! Output saved to: $outputPath")
        } else {
            runAsyncPipeline(imagePaths, filter, filterName, selectedMode)
            println("✅ Pipeline finished")
        }
    } catch (e: Exception) {
        System.err.println("❌ Error: ${e.message}")
    }
}
