"""Integration tests — require a live tslib-api server.

Run with:  TSLIB_BASE_URL=http://localhost:8080 pytest -m integration -v
"""
import math

import pytest

pytestmark = pytest.mark.integration


def _ok(v: float) -> bool:
    return isinstance(v, (int, float)) and math.isfinite(float(v))


def _all_ok(seq) -> bool:
    return len(seq) > 0 and all(_ok(v) for v in seq)


# ---------------------------------------------------------------------------
# Analyze
# ---------------------------------------------------------------------------

def test_analyze(client, airline):
    r = client.analyze.analyze(airline[:48], k=1, n=10, window_size=5)
    assert _ok(r.average) and r.average > 0
    assert _ok(r.variance) and r.variance >= 0
    assert _ok(r.adf_statistic)
    assert isinstance(r.stationary, bool)
    assert _all_ok(r.acf)
    assert _all_ok(r.rolling_average)
    assert len(r.log_transformed) == 48
    assert len(r.first_difference) == 47


# ---------------------------------------------------------------------------
# ARIMA
# ---------------------------------------------------------------------------

def test_arima_forecast(client, airline):
    r = client.arima.forecast(airline[:120], 1, 1, 1, steps=12)
    assert len(r.forecasts) == 12
    assert _all_ok(r.forecasts)
    assert _ok(r.innovation_variance)
    assert r.p == 1 and r.d == 1 and r.q == 1


def test_arima_forecast_with_intervals(client, airline):
    r = client.arima.forecast(airline[:120], 1, 1, 1, steps=6, include_intervals=True)
    assert len(r.forecasts) == 6
    assert len(r.intervals) == 6
    for iv in r.intervals:
        assert iv.lower <= iv.point_forecast <= iv.upper
        assert _ok(iv.lower) and _ok(iv.upper)


def test_sarima_forecast(client, airline):
    r = client.arima.sarima_forecast(
        airline[:120], 1, 1, 1, 1, 1, 1,
        seasonal_period=12, steps=12, include_intervals=True,
    )
    assert len(r.forecasts) == 12
    assert len(r.intervals) == 12
    assert r.seasonal_period == 12
    assert all(iv.lower <= iv.upper for iv in r.intervals)


def test_arimax_forecast(client, airline):
    n = 60
    exog = [[float(i % 12)] for i in range(n)]
    future_exog = [[float(i % 12)] for i in range(6)]
    r = client.arima.arimax_forecast(airline[:n], exog, future_exog, 1, 1, 1)
    assert len(r.forecasts) == 6
    assert _all_ok(r.forecasts)
    assert len(r.exogenous_coefficients) >= 1


def test_order_search(client, airline):
    r = client.arima.order_search(airline[:60], max_p=2, max_d=2, max_q=2)
    assert r.p >= 0 and r.d >= 0 and r.q >= 0
    assert r.criterion in ("AIC", "BIC", "AICc")
    assert _ok(r.score)


def test_var_forecast(client, airline):
    s1 = [float(v) for v in airline[:60]]
    s2 = [v * 1.1 + 5 for v in s1]
    r = client.arima.var_forecast([s1, s2], steps=6)
    assert len(r.forecasts) == 2
    assert all(len(fc) == 6 for fc in r.forecasts)
    assert r.lag_order >= 1
    assert _ok(r.aic)


# ---------------------------------------------------------------------------
# ETS
# ---------------------------------------------------------------------------

def test_ets_single(client, airline):
    r = client.ets.single(airline[:60], steps=12)
    assert len(r.forecasts) == 12
    assert _all_ok(r.forecasts)
    assert len(r.intervals) == 12


def test_ets_double(client, airline):
    r = client.ets.double(airline[:60], steps=6)
    assert len(r.forecasts) == 6
    assert _all_ok(r.forecasts)


def test_ets_triple(client, airline):
    r = client.ets.triple(airline[:120], period=12, steps=12)
    assert len(r.forecasts) == 12
    assert _all_ok(r.forecasts)
    assert len(r.intervals) == 12
    for iv in r.intervals:
        assert iv.lower <= iv.upper


# ---------------------------------------------------------------------------
# State space
# ---------------------------------------------------------------------------

def test_local_level_forecast(client, airline):
    r = client.statespace.local_level_forecast(airline[:60], steps=12)
    assert len(r.forecasts) == 12
    assert _all_ok(r.forecasts)
    assert len(r.intervals) == 12


def test_local_level_filter(client, airline):
    r = client.statespace.local_level_filter(airline[:60], steps=6)
    assert len(r.filtered_states) == 60
    assert _ok(r.process_variance) and r.process_variance >= 0
    assert _ok(r.observation_variance) and r.observation_variance >= 0
    assert _ok(r.log_likelihood)
    assert len(r.forecasts) == 6


def test_kalman_filter(client, airline):
    r = client.statespace.kalman_filter(airline[:60], steps=6)
    assert len(r.filtered_states) == 60
    assert len(r.predicted_states) == 60
    assert len(r.innovations) == 60
    assert _ok(r.log_likelihood)
    assert len(r.forecasts) == 6
    assert len(r.forecast_variances) == 6


# ---------------------------------------------------------------------------
# Auto-selection
# ---------------------------------------------------------------------------

def test_auto_arima(client, airline):
    r = client.selection.auto_arima(airline[:96], steps=12)
    assert len(r.forecasts) == 12
    assert _all_ok(r.forecasts)
    assert r.best_order.p >= 0


def test_auto_arima_seasonal(client, airline):
    r = client.selection.auto_arima(
        airline[:96], steps=12,
        max_seasonal_p=1, max_seasonal_d=1, max_seasonal_q=1,
        seasonal_period=12,
    )
    assert len(r.forecasts) == 12
    assert r.seasonal is True


def test_auto_ets(client, airline):
    r = client.selection.auto_ets(airline[:96], steps=12, seasonal_period=12)
    assert len(r.forecasts) == 12
    assert _all_ok(r.forecasts)
    assert isinstance(r.best_type, str) and len(r.best_type) > 0
    assert _ok(r.best_score)


# ---------------------------------------------------------------------------
# Transforms
# ---------------------------------------------------------------------------

def test_transform_log(client, airline):
    result = client.transforms.log(airline[:12])
    assert len(result) == 12
    assert all(v > 0 for v in result)


def test_transform_sqrt(client, airline):
    result = client.transforms.sqrt(airline[:12])
    assert len(result) == 12
    assert all(v > 0 for v in result)


def test_transform_boxcox_auto(client, airline):
    r = client.transforms.boxcox(airline[:48])
    assert len(r.result) == 48
    assert r.lambda_ is not None and _ok(r.lambda_)


def test_transform_boxcox_fixed(client, airline):
    r = client.transforms.boxcox(airline[:48], lambda_=0.5)
    assert len(r.result) == 48
    assert r.lambda_ == pytest.approx(0.5)
    restored = client.transforms.inverse_boxcox(r.result, 0.5)
    assert len(restored) == 48
    assert all(abs(a - b) < 1.0 for a, b in zip(restored, airline[:48]))


def test_transform_difference(client, airline):
    result = client.transforms.difference(airline[:24], order=1)
    assert len(result) == 23

    restored = client.transforms.inverse_difference(result, airline[:24], order=1)
    assert len(restored) == 23
    for a, b in zip(restored, airline[1:24]):
        assert abs(a - b) < 1e-6


def test_transform_seasonal_difference(client, airline):
    result = client.transforms.seasonal_difference(airline[:24], lag=12, order=1)
    assert len(result) == 12


# ---------------------------------------------------------------------------
# Decomposition
# ---------------------------------------------------------------------------

def test_stl(client, airline):
    r = client.decomposition.stl(airline, period=12)
    assert len(r.trend) == 144
    assert len(r.seasonal) == 144
    assert len(r.remainder) == 144
    assert len(r.reconstructed) == 144
    for i in range(144):
        assert abs(r.reconstructed[i] - airline[i]) < 1e-3


def test_stl_with_robustness(client, airline):
    r = client.decomposition.stl(airline, period=12, outer_iterations=3)
    assert len(r.trend) == 144
    assert _all_ok(r.remainder)


# ---------------------------------------------------------------------------
# Evaluation
# ---------------------------------------------------------------------------

def test_train_test_split(client, airline):
    r = client.evaluation.train_test_split(airline, train_ratio=0.8)
    assert r.train_size == 115
    assert r.test_size == 29
    assert len(r.train) == 115
    assert len(r.test) == 29


def test_metrics(client, airline):
    train, test = airline[:132], airline[132:]
    fc = client.arima.forecast(train, 1, 1, 1, steps=len(test))
    r = client.evaluation.metrics(
        test, fc.forecasts,
        training_series=train, seasonal_period=12,
    )
    assert _ok(r.mae) and r.mae >= 0
    assert _ok(r.rmse) and r.rmse >= r.mae
    assert _ok(r.mape) and r.mape >= 0
    assert _ok(r.mase) and r.mase >= 0


def test_backtest(client, airline):
    from tslib import ModelSpec
    spec = ModelSpec(type="ARIMA", p=1, d=1, q=1)
    r = client.evaluation.backtest(airline, spec, min_train_size=120, horizon=1, step_size=4)
    assert len(r.actual) > 0
    assert len(r.forecast) == len(r.actual)
    assert _ok(r.mae) and r.mae >= 0


def test_benchmark(client, airline):
    from tslib import ModelSpec, NamedModel
    models = [
        NamedModel("ARIMA(1,1,1)", ModelSpec(type="ARIMA", p=1, d=1, q=1)),
        NamedModel("AutoETS",      ModelSpec(type="AUTO_ETS")),
    ]
    summaries = client.evaluation.benchmark(
        airline, models, min_train_size=120, horizon=1, step_size=6,
    )
    assert len(summaries) == 2
    names = {s.model_name for s in summaries}
    assert "ARIMA(1,1,1)" in names and "AutoETS" in names
    for s in summaries:
        assert _ok(s.mae) and s.mae >= 0


# ---------------------------------------------------------------------------
# Data quality
# ---------------------------------------------------------------------------

def test_impute(client, airline):
    dirty = list(airline[:24])
    dirty[5] = None  # type: ignore[call-overload]
    dirty[12] = None  # type: ignore[call-overload]
    result = client.data_quality.impute(dirty)
    assert len(result) == 24
    assert all(v is not None and _ok(v) for v in result)


def test_outliers(client, airline):
    data = list(airline[:24]) + [9999.0]
    r = client.data_quality.outliers(data)
    assert 24 in r.outlier_indices


def test_winsorize(client, airline):
    result = client.data_quality.winsorize(airline, lower_probability=0.05, upper_probability=0.95)
    assert len(result) == 144
    assert min(result) >= min(airline) * 0.5
    assert max(result) <= max(airline) * 1.5


# ---------------------------------------------------------------------------
# Stationarity tests
# ---------------------------------------------------------------------------

def test_adf_nonstationary(client, airline):
    r = client.stationarity.adf(airline)
    assert isinstance(r.stationary, bool)
    assert isinstance(r.needs_diff, bool)
    assert _ok(r.statistic)
    assert 0 <= r.p_value <= 1


def test_adf_differenced_is_stationary(client, airline):
    diff = client.transforms.difference(list(airline), order=1)
    r = client.stationarity.adf(diff)
    assert r.stationary is True


def test_kpss(client, airline):
    r = client.stationarity.kpss(airline)
    assert isinstance(r.stationary_at_five_percent, bool)
    assert _ok(r.statistic) and r.statistic >= 0
    assert _ok(r.critical_value_five_percent)


# ---------------------------------------------------------------------------
# Moving average
# ---------------------------------------------------------------------------

def test_moving_average_simple(client, airline):
    result = client.moving_average.compute(airline, period=12, type="SIMPLE")
    assert len(result) > 0
    assert _all_ok(result)


def test_moving_average_ema(client, airline):
    result = client.moving_average.compute(airline, type="EMA", alpha=0.3)
    assert len(result) == len(airline)
    assert _all_ok(result)


# ---------------------------------------------------------------------------
# Diagnostics
# ---------------------------------------------------------------------------

def test_ljung_box_white_noise(client):
    import random
    random.seed(42)
    residuals = [random.gauss(0, 1) for _ in range(100)]
    r = client.diagnostics.ljung_box(residuals, lags=10)
    assert _ok(r.statistic) and r.statistic >= 0
    assert 0 <= r.p_value <= 1
    assert r.lags == 10
    assert r.rejects_at_five_percent is False


def test_ljung_box_autocorrelated(client, airline):
    r = client.diagnostics.ljung_box(list(airline), lags=10)
    assert r.rejects_at_five_percent is True
