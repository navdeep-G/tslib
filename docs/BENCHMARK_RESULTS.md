# Benchmark results

This file now contains measured benchmark summaries generated from the current repository snapshot on March 28, 2026. These are rolling-origin forecast accuracy comparisons, not low-level runtime microbenchmarks.

## How these numbers were generated

1. Compile the benchmark examples.
2. Run `examples/GenerateBenchmarkReportExample.java` for the small example-series benchmark.
3. Run `examples/HotelBenchmarkComparisonExample.java` for the bundled hotel dataset benchmark.
4. Paste the resulting markdown table into this document together with the dataset notes and protocol.

## Benchmark 1: example series from `GenerateBenchmarkReportExample`

### Dataset
- name: synthetic trend + mild seasonality example embedded in the benchmark example
- length: 12
- frequency / seasonal period: 3
- preprocessing: none

### Backtest protocol
- minimum train window: 6
- horizon: 1
- step size: 1 (rolling origin)

### Results

| Rank | Model | MAE | RMSE | MAPE | sMAPE | MASE |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| 1 | TripleExpSmoothing | 1.0004 | 1.0395 | 4.4856 | 4.4996 | 0.5240 |
| 2 | ARIMA(0,1,0) | 1.6728 | 1.8286 | 7.4286 | 7.4039 | 0.8762 |
| 3 | LocalLevel | 2.1547 | 2.5554 | 9.4503 | 10.1308 | 1.1287 |

### Notes
- On this short series, multiplicative Holt-Winters with period 3 performed best.
- The random-walk ARIMA baseline beat the local-level model on RMSE.
- Because the dataset is tiny, treat this as a smoke-test comparison rather than a robust model-selection result.

## Benchmark 2: bundled `data/hotel.txt` seasonal dataset

### Dataset
- name: hotel occupancy / bookings style monthly seasonal series from `data/hotel.txt`
- length: 180
- frequency / seasonal period: 12
- preprocessing: none

### Backtest protocol
- minimum train window: 60
- horizon: 1
- step size: 1 (rolling origin)

### Results

| Rank | Model | MAE | RMSE | MAPE | sMAPE | MASE |
| --- | --- | ---: | ---: | ---: | ---: | ---: |
| 1 | TripleExpSmoothing | 17.0595 | 21.9351 | 2.1743 | 2.1739 | 0.2443 |
| 2 | SARIMA(0,1,0)x(1,1,0,12) | 19.1474 | 23.8508 | 2.4302 | 2.4276 | 0.2742 |
| 3 | ARIMA(0,1,0) | 76.7365 | 101.2015 | 9.7881 | 9.5704 | 1.0991 |
| 4 | LocalLevel | 95.9678 | 115.2085 | 11.9125 | 11.8982 | 1.3745 |

### Notes
- On the bundled seasonal dataset, the seasonal exponential-smoothing model clearly outperformed the non-seasonal baselines.
- The simple seasonal SARIMA baseline was competitive but still behind TripleExpSmoothing under this protocol.
- ARIMA(0,1,0) and the local-level model behave like reasonable non-seasonal baselines here, but both miss the strong yearly seasonal structure.
- These results support the model-selection guidance: prefer explicit seasonal models when the series shows stable annual seasonality.
