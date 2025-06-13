# Image Convolution in Kotlin

This project implements and benchmarks sequential and several parallel approaches to applying 2D filters to grayscale images.

## ✨ Features

- Sequential and parallel convolution 
- Interactive CLI tool for applying filters to images
- Performance benchmarking and analysis
- Predefined filters
- Support for user-supplied or built-in test images

## Built-in Filters

All filters are defined as 2D matrices (kernels), with optional normalization (`factor`) and offset (`bias`).

* `blur_3x3`, `blur_5x5`
* `gaussian_blur_3x3`, `gaussian_blur_5x5`
* `sharpen`
* `edge_detect`
* `motion_blur`
* `identity`
* `emboss`

## Modes of Convolution

| Mode     | Description                      |
| -------- | -------------------------------- |
| `seq`    | Standard single-threaded version |
| `pixels` | Parallelized per-pixel           |
| `rows`   | Rows processed in parallel   |
| `cols`   | Columns processed in parallel          |
| `tiles`  | Blocks (tiles) of the image      |

## 🧪 Requirements

- JDK 17+
- Kotlin
- Gradle
- OpenCV via [JavaCPP](https://github.com/bytedeco/javacpp)
  
## Getting started
Clone the repo:
```bash
git clone git@github.com:sofyak0zyreva/convolution.git
```
Run the following command to install the dependencies:

```bash
./gradlew build
```

## ▶️ Usage

To apply a filter to an image via CLI:

```bash
./gradlew run --quiet --console=plain
```

You'll be prompted to:

1. Enter an image path (or use defaults from _resources/images/_). You can enter path from the repository root or absolute path
2. Select a mode (with optional batch/tile sizes -- they are responsible for how many pixels will be allocated per coroutine)
3. Choose a filter 

The result will be saved as a new `.bmp` file in the project directory's `output` folder.


## 🧵 Performance Benchmarking
You can simply run `main()` in the specified file.

To measure the performance of a specific mode (`BenchmarkSizes.kt`):

```kotlin
val result = benchmarkSingleMode(inputImage, filter, ConvolutionMode.ParallelRows(8))
println(result)
```
Here, you can also use randomly generated images of a chosen size.

To compare all modes (`BenchmarkAllModes.kt`):

```kotlin
val results = benchmarkAllModes(image, filter)
results.forEach(::println)
```

To benchmark scalability (e.g., for rows/cols/tiles) (`EfficiencyAnalysis.kt`):

```kotlin
val sizes = listOf(1, 4, 8, 16, 32, 64, 128)
benchmarkSizes(image, filter, { ConvolutionMode.ParallelRows(8) }, sizes) 
```



## ✅ Testing & Correctness

* Sequential implementation serves as the reference with key points preserved:
  - Compositionality: applying filters sequentially should equal applying their composition  
  (e.g., `apply(filter1, apply(filter2, img)) == apply(filter1 ⊕ filter2, img)`)
  - Identity: some filters compose to identity (e.g., _shift-left _then_ shift-right_)
  - Zero-padding: expanding filters with zeros shouldn't change results
  - Known-output filters: test with trivial filters (_zero filter, identity filter_)
* All modes are tested against the sequential implementation for numerical accuracy
* Standard Error of the Mean (SEM) is reported in benchmarks. See [Performance Analysis](./src/test/kotlin/benchmarks/EfficiencyAnalysis.md),
[Plots](./src/test/kotlin/benchmarks/plots), and [Results](./src/test/kotlin/benchmarks/results) for more

Run:

```bash
./gradlew test
```

## 📂 Directory Structure

```
src/
├── main/
│   ├── kotlin/           ← Core logic, filters, modes, and CLI
│   └── resources/
│       └── images/       ← Sample input images
└── test/
    └── kotlin/
        ├── benchmark/    ← Performance analysis, plots, results
        └── ...           ← Tests

```

## License
This project uses JavaCPP Presets for OpenCV and OpenCV, both licensed under Apache License 2.0.
See [`LICENSE`](LICENSE) for details.
