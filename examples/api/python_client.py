"""
tslib REST API — Python client examples
Install: pip install requests
Start the server: cd tslib-api && ../gradlew bootRun
Swagger UI: http://localhost:8080/swagger-ui
"""

import requests
import json

BASE = "http://localhost:8080/api"

# AirPassengers monthly data (24 observations for brevity)
DATA = [112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118,
        115, 126, 141, 135, 125, 149, 170, 170, 158, 133, 114, 140]


def post(path, payload):
    resp = requests.post(f"{BASE}{path}", json=payload)
    resp.raise_for_status()
    return resp.json()


def section(title):
    print(f"\n{'='*60}")
    print(f"  {title}")
    print('='*60)


# ── ARIMA ────────────────────────────────────────────────────────
section("ARIMA Forecast")
result = post("/arima/forecast", {"data": DATA, "p": 1, "d": 1, "q": 1, "steps": 6})
print("Forecasts:", result["forecasts"])
print("AR coefficients:", result["arCoefficients"])

section("ARIMA with Prediction Intervals (95%)")
result = post("/arima/forecast-intervals", {
    "data": DATA, "p": 1, "d": 1, "q": 1, "steps": 6, "confidenceLevel": 0.95
})
for interval in result["intervals"]:
    print(f"  Step {interval['step']}: {interval['pointForecast']:.2f} "
          f"[{interval['lower']:.2f}, {interval['upper']:.2f}]")

# ── SARIMA ───────────────────────────────────────────────────────
section("SARIMA(1,1,1)(1,1,1)12")
result = post("/sarima/forecast", {
    "data": DATA, "p": 1, "d": 1, "q": 1,
    "seasonalP": 1, "seasonalD": 1, "seasonalQ": 1, "seasonalPeriod": 12,
    "steps": 12
})
print("12-step forecast:", result["forecasts"])

# ── AutoARIMA ────────────────────────────────────────────────────
section("AutoARIMA")
result = post("/auto/arima/forecast", {
    "data": DATA, "maxP": 3, "maxD": 2, "maxQ": 3,
    "steps": 6, "criterion": "AIC"
})
order = result["bestOrder"]
print(f"Best order: ARIMA({order['p']},{order['d']},{order['q']})  score={order['score']:.4f}")
print("Forecasts:", result["forecasts"])

# ── AutoETS ──────────────────────────────────────────────────────
section("AutoETS (with seasonal period)")
result = post("/auto/ets/forecast", {
    "data": DATA, "steps": 6, "seasonalPeriod": 12
})
print(f"Best ETS type: {result['bestType']}, score={result['bestScore']:.4f}")
print("Forecasts:", result["forecasts"])

# ── ETS variants ─────────────────────────────────────────────────
section("Holt-Winters (Triple ETS)")
result = post("/ets/triple/forecast", {
    "data": DATA, "alpha": 0.3, "beta": 0.1, "gamma": 0.1,
    "period": 12, "steps": 12
})
print("Forecasts:", result["forecasts"])

# ── VAR ──────────────────────────────────────────────────────────
section("VAR (two correlated series)")
result = post("/var/forecast", {
    "series": [DATA, DATA],
    "steps": 4
})
print(f"Lag order: {result['lagOrder']}, AIC: {result['aic']:.4f}")
print("Forecasts per series:", result["forecasts"])

# ── State Space ──────────────────────────────────────────────────
section("Local Level Model")
result = post("/statespace/local-level/forecast", {"data": DATA, "steps": 6})
print("Forecasts:", result["forecasts"])

# ── Evaluation ───────────────────────────────────────────────────
section("Forecast Metrics")
result = post("/evaluate/metrics", {
    "actual":   [100, 110, 120, 115, 125],
    "forecast": [102, 108, 122, 113, 127],
    "trainingSeries": [90, 95, 100, 105],
    "seasonalPeriod": 1
})
print(f"MAE={result['mae']:.4f}  RMSE={result['rmse']:.4f}  "
      f"MAPE={result['mape']:.4f}  SMAPE={result['smape']:.4f}")

section("Rolling-Origin Backtest")
result = post("/evaluate/backtest", {
    "data": DATA,
    "modelSpec": {"type": "ARIMA", "p": 1, "d": 1, "q": 1},
    "minTrainSize": 12, "horizon": 3
})
print(f"Backtest MAE={result['mae']:.4f}  RMSE={result['rmse']:.4f}")

section("Train/Test Split")
result = post("/evaluate/train-test-split", {"data": DATA, "trainRatio": 0.8})
print(f"Train: {result['trainSize']} obs, Test: {result['testSize']} obs")

# ── Stationarity & Diagnostics ───────────────────────────────────
section("ADF Test")
result = post("/tests/adf", {"data": DATA})
print(f"ADF stat={result['statistic']:.4f}, p={result['pValue']:.4f}, "
      f"stationary={result['stationary']}")

section("KPSS Test")
result = post("/tests/kpss", {"data": DATA, "regressionType": "LEVEL"})
print(f"KPSS stat={result['statistic']:.4f}, stationary@5%={result['stationaryAtFivePercent']}")

# ── Decomposition ────────────────────────────────────────────────
section("STL Decomposition")
result = post("/decompose/stl", {"data": DATA, "period": 12})
print("Trend (first 6):", result["trend"][:6])
print("Seasonal (first 12):", result["seasonal"][:12])

# ── Transforms ───────────────────────────────────────────────────
section("Box-Cox Transform (auto lambda)")
result = post("/transform/boxcox", {"data": DATA})
print(f"Optimal lambda: {result['lambda']:.4f}")

section("First Differencing")
result = post("/transform/difference", {"data": DATA, "order": 1})
print("Differenced (first 6):", result["forecasts"][:6])

# ── Data Quality ─────────────────────────────────────────────────
section("Outlier Detection (Z-score)")
result = post("/dataquality/outliers", {"data": DATA, "method": "Z_SCORE", "threshold": 2.0})
print("Outlier indices:", result["outlierIndices"])

# ── Analysis ─────────────────────────────────────────────────────
section("Comprehensive Analysis")
result = post("/analyze", {"data": DATA, "k": 1, "n": 6, "windowSize": 3})
print(f"Mean={result['average']:.2f}, Std={result['standardDeviation']:.2f}")
print(f"ADF stat={result['adfStatistic']:.4f}, Stationary={result['stationary']}")
print("ACF (6 lags):", [f"{v:.3f}" for v in result["acf"]])

# ── Moving Averages ───────────────────────────────────────────────
section("Moving Averages")
for ma_type in ["SIMPLE", "EMA", "WEIGHTED", "CUMULATIVE"]:
    result = post("/moving-average", {"data": DATA, "period": 3, "type": ma_type})
    print(f"  {ma_type} (first 6): {result['forecasts'][:6]}")
