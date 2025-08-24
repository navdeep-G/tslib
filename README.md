# Time Series Library in Java

## Overview

`tslib` is a modular Java library for time series analysis and forecasting. It includes implementations of key smoothing algorithms, moving averages, data transformation tools, and stationarity testing — all essential for modeling and analyzing sequential data in domains like finance, economics, and engineering.

---

## 🚀 Features

- 📈 Core time series models: **Exponential Smoothing**, **Moving Averages**
- 🔁 Support for trend and seasonality: Single, Double, and Triple Exponential Smoothing
- 🧪 Stationarity testing with Augmented Dickey-Fuller (ADF)
- 🔄 Built-in utilities for transformation (log, sqrt, Box-Cox)
- 📊 Statistical summary utilities: autocovariance, ACF, PACF, mean, std, and more
- 🧹 Clean architecture with extensible interfaces

---

## 🧠 Core Components

### 📊 Statistical Utilities

- `tslib.stats.Stats`: Utility methods for calculating mean, variance, autocovariance, ACF, PACF, and more.

---

### 🔄 Data Transformation

- `tslib.transform.Transform`: Preprocessing methods for log, square root, cube root, and Box-Cox transformations.

---

### 📈 Moving Average Models

- `tslib.movingaverage.SimpleMovingAverage`: Fixed-window **SMA** for smoothing time series.
- `tslib.movingaverage.CumulativeMovingAverage`: Real-time **CMA** update of the running mean.
- `tslib.movingaverage.ExponentialMovingAverage`: **EMA** implementation with decay factor.

Each model implements the shared `MovingAverage` interface for consistency.

---

### 🔁 Exponential Smoothing Models

- `tslib.model.SingleExpSmoothing`: Single Exponential Smoothing (level only).
- `tslib.model.DoubleExpSmoothing`: Double Exponential Smoothing (Holt's method – level + trend).
- `tslib.model.TripleExpSmoothing`: Triple Exponential Smoothing (Holt-Winters – level, trend, seasonality).

All models implement the `ExponentialSmoothing` interface.

---

### 🧪 Stationarity Testing

- `tslib.tests.AugmentedDickeyFuller`: Java implementation of the ADF test, used to check for unit roots in time series data.

---

## 📂 Example Usage

```java
List<Double> data = Util.ReadFile("data/hotel.txt");

ExponentialSmoothing model = new TripleExpSmoothing(0.5, 0.3, 0.2, 12, false);
List<Double> forecast = model.forecast(data, 5);

MovingAverage sma = new SimpleMovingAverage(3);
List<Double> smoothed = sma.compute(data);

double lambda = BoxCox.lambdaSearch(data);
List<Double> transformed = BoxCox.transform(data, lambda);
