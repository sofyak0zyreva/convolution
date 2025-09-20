# Performance Analysis

## 1. Tuning Parallel Parameters

Before benchmarking on real filters, different configurations of parallel processing were tested to find optimal batch and tile sizes. This was done using a random image and filter, with each configuration measured over 10 runs.

### Parallel Rows

Performance improved quickly from `par_rows_1` to `par_rows_4`, and remained very stable through `par_rows_16`. Beyond that, execution time increased steadily, with `par_rows_512` being significantly slower. This suggests that small to moderate batch sizes (4–16 rows) are best, while too many threads introduce overhead or contention.

### Parallel Columns

The trend was similar to the row-based implementation. The best results appeared around `par_cols_8` and `par_cols_16`. Larger configurations again showed diminishing returns and eventually degraded performance. Very small batches were also less stable, likely due to overhead.

### Tiled Parallelism

Tiled execution was most efficient with `8x8` tiles, closely followed by `4x4` and `16x16`. Extremely small tiles (`1x1`) were less consistent, and very large ones (like `256x256` or `512x512`) led to much longer runtimes. This confirms that medium-sized tiles offer the best balance between concurrency and overhead.

### Conclusion

For all strategies, moderate configurations consistently performed best. Too many threads or too large tiles reduced performance due to synchronization and workload imbalance. This impacted the choices in the full benchmark phase.
## 2. Comparison of Implementations

With optimal parallel parameters established, different implementations were compared: sequential, pixel-wise parallel, row-based, column-based, and tiled. Filters used were from a predefined pool, images were from the test set in the `resources` directory.

### Performance

All parallel versions showed clear performance improvements over the sequential baseline. Among them, **row-based** and **tile-based** implementations consistently outperformed others across a range of filters, especially on smaller kernels like `blur_3x3` and `identity`, where they achieved speedups of up to **5 times**. For heavier filters like `motion_blur`, all parallel versions offered massive gains, cutting down execution time by **over 75%**.

**Pixel-wise** parallelism, while better than sequential, was never the fastest — it generally was behind the more structured row, column, and tile strategies. This is likely due to excessive thread overhead. **Column-based** implementation performed reasonably well, but typically fell short of row- and tile-based counterparts in both speed and stability.

Performance varied slightly by filter complexity. The `gauss_blur_3×3` and `gauss_blur_5×5` and simple blur filters benefited more from tile- and row-based execution. The `emboss` and sharpen `filters`, with more pronounced pixel influence, still ran efficiently with all parallel variants, though results were more sensitive to thread scheduling.

### Correctness

All implementations produced visually and numerically consistent outputs, compared against the sequential reference. Minor floating-point differences were within acceptable limits and had no seen impact on the image results.

The standard error of the mean (SEM) values were generally low across all modes, indicating stable and repeatable timing measurements. Larger SEMs appeared occasionally in pixel-wise parallelism, reflecting higher variability likely caused by thread management overhead and less efficient memory access patterns. In contrast, row- and tile-based approaches presented lower SEMs.

### Conclusion

Structured parallel approaches — particularly **row-based** and **tile-based** — offered the best performance and reliability across all filters and image types. Pixel-wise parallelism, despite being conceptually simple, proved inefficient in practice.

# Performance Analysis of Pipelines

## 1. Tuning Concurrency: Async Pipeline

The asynchronous pipeline allows multiple worker coroutines to perform image convolutions in parallel. Different values of concurrency (the number of simultaneous convolution workers) were tested, using the same filter (`gaussian_blur_3x3`) across the same [set of images](https://github.com/sofyak0zyreva/convolution/tree/main/src/main/resources/images).

### Observations

Increasing the number of workers generally reduces processing time for modes that split work by rows, columns, or tiles. For a typical CPU with 8 cores, **8 concurrent workers** is a reasonable maximum for testing, balancing CPU utilization and memory usage.

## 2. Comparing Pipelines

Fully sequential pipeline (reading, convolving, writing images one by one) was compared to the asynchronous pipeline (streaming images through reading → convolution → writing, with buffered and parallelized convolution stages).

### Async vs. Seq Pipeline Performance

**Async pipeline excels** when convolution can be parallelized across multiple images or within a single image (rows, columns, tiles). **Sequential pipeline** is simpler but suffers from total runtime inflation because the reading, convolution, and writing stages are not overlapped. For highly parallelizable convolution modes, async pipelines with reasonable concurrency (e.g., 8 workers) are **twice or even three times faster** compared to sequential execution. **Parallel pixels mode** can occasionally be slower in async pipelines because it aggressively splits work at the pixel level, increasing memory pressure and overhead from many small tasks.

### Conclusion

Asynchronous pipeline with controlled concurrency is optimal for large-scale image processing, while sequential pipelines remain useful for simple or memory-constrained scenarios.
