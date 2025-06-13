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
