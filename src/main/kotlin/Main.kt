import org.bytedeco.javacpp.Loader
import org.bytedeco.opencv.global.opencv_imgcodecs
import org.bytedeco.opencv.global.opencv_imgproc
import org.bytedeco.opencv.opencv_core.Mat

fun loadImage(path: String): Mat {
    val mat = opencv_imgcodecs.imread(path)
    if (mat.empty()) throw IllegalArgumentException("Failed to load image: $path")
    return mat
}

fun saveImage(image: Mat, path: String) {
    if (!opencv_imgcodecs.imwrite(path, image)) throw IllegalStateException("Failed to save image: $path")
}

fun Mat.toGrayscale(): Mat {
    val grayMat = Mat()
    opencv_imgproc.cvtColor(this, grayMat, opencv_imgproc.COLOR_BGR2GRAY) // Note: OpenCV uses BGR order by default!
    return grayMat
}

fun promptForImagePath(): String {
    while (true) {
        print("Enter path to image (e.g., sample.bmp): ")
        val path = readlnOrNull()?.trim()
        if (!path.isNullOrBlank()) {
            try {
                val image = loadImage(path) // check if image is valid
                return path
            } catch (e: Exception) {
                println("❌ Cannot load image. Reason: ${e.message}")
            }
        } else {
            println("❌ Path cannot be empty.")
        }
    }
}

fun promptForKernelName(): String {
    val kernelNames = kernelPool.keys.toList()

    while (true) {
        println("\nAvailable kernels:")
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

        val kernelName = promptForKernelName()
        val selectedKernel = kernelPool[kernelName] ?: throw IllegalArgumentException("Kernel should not be null.")

        println("Applying kernel: $kernelName...")

        val resultImage = grayImage.seqConvolve(selectedKernel)
        val outputPath = "output_${kernelName}.bmp"
        saveImage(resultImage, outputPath)

        println("✅ Done! Output saved to: $outputPath")

    } catch (e: Exception) {
        System.err.println("❌ Error: ${e.message}")
    }
}
