# Benchmarking and model comparison

The project now includes a small comparison helper intended for user-facing model bakeoffs rather than microbenchmarking.

## Goals

- compare forecasting quality with a shared rolling-origin protocol
- keep examples dependency-light
- produce metrics users can reason about directly

## Recommended benchmark setup

- choose a fixed minimum train window
- use a short horizon (1 to seasonal period)
- compare at least one smoothing model, one Box-Jenkins model, and one state-space model
- report MAE, RMSE, sMAPE, MASE, and Ljung-Box p-values where available

See `examples/BenchmarkComparisonExample.java` for a concrete benchmark entry point.


## Publishing results

Use `tslib.evaluation.BenchmarkMarkdown` to convert `BenchmarkSummary` rows into a markdown table that can be pasted into release notes, the README, or `docs/BENCHMARK_RESULTS.md`.
