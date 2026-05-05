"""
Quickstart — tslib-py

Requires a running tslib-api server (default: http://localhost:8080).
Start one with:  docker run -p 8080:8080 navdeep-g/tslib-api:latest
"""
import tslib
from tslib import ModelSpec, NamedModel, TslibClient

client = TslibClient("http://localhost:8080")

# ── Sample data ──────────────────────────────────────────────────────────────
data = [
    112, 118, 132, 129, 121, 135, 148, 148, 136, 119, 104, 118,
    115, 126, 141, 135, 125, 149, 170, 170, 158, 133, 114, 140,
    145, 150, 178, 163, 172, 178, 199, 199, 184, 162, 146, 166,
    171, 180, 193, 181, 183, 218, 230, 242, 209, 191, 172, 194,
]

# ── 1. Auto-ARIMA forecast ────────────────────────────────────────────────────
result = client.selection.auto_arima(data, steps=12)
print("Auto-ARIMA forecasts:", result.forecasts)
print("Best order:", result.best_order.p, result.best_order.d, result.best_order.q)

# ── 2. Holt-Winters (triple ETS) with intervals ──────────────────────────────
hw = client.ets.triple(data, period=12, steps=12, alpha=0.3, beta=0.1, gamma=0.1)
print("\nHolt-Winters forecasts:", hw.forecasts)
if hw.intervals:
    print("95% interval for step 1:", hw.intervals[0].lower, "–", hw.intervals[0].upper)

# ── 3. STL decomposition ──────────────────────────────────────────────────────
stl = client.decomposition.stl(data, period=12)
print("\nSTL trend (last 5):", stl.trend[-5:])
print("STL seasonal (last 5):", stl.seasonal[-5:])

# ── 4. Stationarity check ────────────────────────────────────────────────────
adf = client.stationarity.adf(data)
print(f"\nADF statistic: {adf.statistic:.4f}, p-value: {adf.p_value:.4f}, stationary: {adf.stationary}")

# ── 5. Quick transform ───────────────────────────────────────────────────────
log_data = client.transforms.log(data)
print("\nLog-transformed (first 5):", log_data[:5])

# ── 6. Metrics ───────────────────────────────────────────────────────────────
train = data[:36]
test = data[36:]
arima_fc = client.arima.forecast(train, 1, 1, 1, steps=len(test))
metrics = client.evaluation.metrics(test, arima_fc.forecasts)
print(f"\nARIMA(1,1,1) out-of-sample — MAE: {metrics.mae:.2f}, RMSE: {metrics.rmse:.2f}, MAPE: {metrics.mape:.4f}")

# ── 7. Benchmark comparison ──────────────────────────────────────────────────
models = [
    NamedModel("ARIMA(1,1,1)", ModelSpec(type="ARIMA", p=1, d=1, q=1)),
    NamedModel("AutoETS",      ModelSpec(type="AUTO_ETS")),
    NamedModel("LocalLevel",   ModelSpec(type="LOCAL_LEVEL")),
]
summaries = client.evaluation.benchmark(data, models, min_train_size=24, horizon=1)
print("\nBenchmark:")
for s in summaries:
    print(f"  {s.model_name:<20} MAE={s.mae:.2f}  RMSE={s.rmse:.2f}")
