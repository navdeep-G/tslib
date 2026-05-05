"""Unit tests — all HTTP calls are intercepted via responses library."""
import pytest
import responses as rsps_lib

import tslib
from tslib import TslibClient
from tslib.exceptions import TslibAPIError

BASE = "http://localhost:8080"


@pytest.fixture
def client():
    return TslibClient(BASE)


# ---------------------------------------------------------------------------
# Helpers
# ---------------------------------------------------------------------------

def _mock_post(url, payload):
    rsps_lib.add(rsps_lib.POST, url, json=payload, status=200)


# ---------------------------------------------------------------------------
# Instantiation
# ---------------------------------------------------------------------------

def test_client_instantiation():
    c = TslibClient("http://example.com", api_key="secret", timeout=10.0)
    assert c.arima is not None
    assert c.ets is not None
    assert c.transforms is not None


# ---------------------------------------------------------------------------
# ARIMA
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_arima_forecast(client):
    _mock_post(
        f"{BASE}/api/arima/forecast",
        {
            "forecasts": [101.0, 102.0],
            "intervals": [],
            "arCoefficients": [0.5],
            "maCoefficients": [],
            "intercept": 1.0,
            "innovationVariance": 0.1,
            "p": 1,
            "d": 1,
            "q": 0,
        },
    )
    result = client.arima.forecast([1.0, 2.0, 3.0], 1, 1, 0, steps=2)
    assert result.forecasts == [101.0, 102.0]
    assert result.p == 1
    assert result.intervals == []


@rsps_lib.activate
def test_arima_forecast_with_intervals(client):
    interval = {"step": 1, "pointForecast": 101.0, "lower": 95.0, "upper": 107.0, "confidenceLevel": 0.95}
    _mock_post(
        f"{BASE}/api/arima/forecast-intervals",
        {"forecasts": [101.0], "intervals": [interval], "arCoefficients": [], "maCoefficients": [],
         "intercept": 0.0, "innovationVariance": 1.0, "p": 1, "d": 0, "q": 0},
    )
    result = client.arima.forecast([1.0, 2.0], 1, 0, 0, include_intervals=True)
    assert len(result.intervals) == 1
    assert result.intervals[0].lower == 95.0
    assert result.intervals[0].upper == 107.0


@rsps_lib.activate
def test_var_forecast(client):
    _mock_post(
        f"{BASE}/api/var/forecast",
        {"forecasts": [[1.1, 2.1], [1.2, 2.2]], "lagOrder": 2, "numSeries": 2, "aic": -10.5},
    )
    result = client.arima.var_forecast([[1.0, 2.0, 3.0], [4.0, 5.0, 6.0]], steps=2)
    assert len(result.forecasts) == 2
    assert result.lag_order == 2
    assert result.aic == pytest.approx(-10.5)


# ---------------------------------------------------------------------------
# ETS
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_ets_single(client):
    _mock_post(
        f"{BASE}/api/ets/single/forecast",
        {"forecasts": [5.0], "intervals": []},
    )
    result = client.ets.single([1.0, 2.0, 3.0, 4.0])
    assert result.forecasts == [5.0]


@rsps_lib.activate
def test_ets_triple(client):
    _mock_post(
        f"{BASE}/api/ets/triple/forecast",
        {"forecasts": [10.0, 11.0], "intervals": []},
    )
    result = client.ets.triple(list(range(1, 25)), period=12, steps=2)
    assert result.forecasts == [10.0, 11.0]


# ---------------------------------------------------------------------------
# State space
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_local_level_forecast(client):
    _mock_post(
        f"{BASE}/api/statespace/local-level/forecast",
        {"forecasts": [3.5], "intervals": []},
    )
    result = client.statespace.local_level_forecast([1.0, 2.0, 3.0])
    assert result.forecasts == [3.5]


@rsps_lib.activate
def test_kalman_filter(client):
    _mock_post(
        f"{BASE}/api/statespace/kalman/filter",
        {
            "predictedStates": [1.0, 2.0],
            "filteredStates": [1.1, 2.1],
            "filteredCovariances": [0.5, 0.4],
            "innovations": [0.1, 0.1],
            "logLikelihood": -5.0,
            "forecasts": [],
            "forecastVariances": [],
        },
    )
    result = client.statespace.kalman_filter([1.0, 2.0])
    assert result.filtered_states == [1.1, 2.1]
    assert result.log_likelihood == pytest.approx(-5.0)


# ---------------------------------------------------------------------------
# Transforms
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_transform_log(client):
    _mock_post(f"{BASE}/api/transform/log", {"forecasts": [0.0, 0.693, 1.099]})
    result = client.transforms.log([1.0, 2.0, 3.0])
    assert result[0] == pytest.approx(0.0)


@rsps_lib.activate
def test_transform_boxcox(client):
    _mock_post(f"{BASE}/api/transform/boxcox", {"result": [0.1, 0.2], "lambda": 0.5})
    result = client.transforms.boxcox([1.0, 2.0])
    assert result.lambda_ == pytest.approx(0.5)
    assert result.result == [0.1, 0.2]


@rsps_lib.activate
def test_transform_difference(client):
    _mock_post(f"{BASE}/api/transform/difference", {"forecasts": [1.0, 1.0]})
    result = client.transforms.difference([1.0, 2.0, 3.0])
    assert result == [1.0, 1.0]


# ---------------------------------------------------------------------------
# Decomposition
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_stl(client):
    _mock_post(
        f"{BASE}/api/decompose/stl",
        {"trend": [1.0], "seasonal": [0.1], "remainder": [-0.1], "reconstructed": [1.0]},
    )
    result = client.decomposition.stl([1.0, 2.0, 3.0], period=12)
    assert result.trend == [1.0]
    assert result.seasonal == [0.1]


# ---------------------------------------------------------------------------
# Evaluation
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_metrics(client):
    _mock_post(
        f"{BASE}/api/evaluate/metrics",
        {"mae": 1.0, "rmse": 1.5, "mape": 0.1, "smape": 0.09, "mase": 0.8, "meanError": 0.5},
    )
    result = client.evaluation.metrics([10.0, 20.0], [11.0, 21.0])
    assert result.mae == pytest.approx(1.0)
    assert result.rmse == pytest.approx(1.5)


@rsps_lib.activate
def test_train_test_split(client):
    _mock_post(
        f"{BASE}/api/evaluate/train-test-split",
        {"train": [1.0, 2.0, 3.0], "test": [4.0, 5.0], "trainSize": 3, "testSize": 2},
    )
    result = client.evaluation.train_test_split([1.0, 2.0, 3.0, 4.0, 5.0], train_ratio=0.6)
    assert result.train_size == 3
    assert result.test_size == 2


# ---------------------------------------------------------------------------
# Data quality
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_impute(client):
    _mock_post(f"{BASE}/api/dataquality/impute", {"forecasts": [1.0, 1.5, 2.0]})
    result = client.data_quality.impute([1.0, None, 2.0])  # type: ignore[list-item]
    assert result == [1.0, 1.5, 2.0]


@rsps_lib.activate
def test_outliers(client):
    _mock_post(f"{BASE}/api/dataquality/outliers", {"outlierIndices": [4]})
    result = client.data_quality.outliers([1.0, 1.1, 0.9, 1.0, 100.0])
    assert result.outlier_indices == [4]


# ---------------------------------------------------------------------------
# Stationarity tests
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_adf(client):
    _mock_post(
        f"{BASE}/api/tests/adf",
        {"statistic": -3.5, "pValue": 0.01, "lag": 1, "stationary": True, "needsDiff": False},
    )
    result = client.stationarity.adf([1.0, 2.0, 3.0])
    assert result.stationary is True
    assert result.needs_diff is False


@rsps_lib.activate
def test_kpss(client):
    _mock_post(
        f"{BASE}/api/tests/kpss",
        {
            "statistic": 0.1,
            "lags": 3,
            "regressionType": "LEVEL",
            "stationaryAtFivePercent": True,
            "stationaryAtOnePercent": True,
            "criticalValueFivePercent": 0.463,
            "criticalValueOnePercent": 0.739,
        },
    )
    result = client.stationarity.kpss([1.0, 2.0, 3.0])
    assert result.stationary_at_five_percent is True


# ---------------------------------------------------------------------------
# Moving average
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_moving_average(client):
    _mock_post(f"{BASE}/api/moving-average", {"forecasts": [2.0, 3.0, 4.0]})
    result = client.moving_average.compute([1.0, 2.0, 3.0, 4.0, 5.0], period=3)
    assert result == [2.0, 3.0, 4.0]


# ---------------------------------------------------------------------------
# Auto-selection
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_auto_arima(client):
    best_order = {"modelType": "ARIMA", "p": 1, "d": 1, "q": 0, "seasonalP": 0,
                  "seasonalD": 0, "seasonalQ": 0, "seasonalPeriod": 0, "criterion": "AIC", "score": -50.0}
    _mock_post(
        f"{BASE}/api/auto/arima/forecast",
        {"forecasts": [5.0], "intervals": [], "bestOrder": best_order, "seasonal": False},
    )
    result = client.selection.auto_arima([1.0, 2.0, 3.0, 4.0])
    assert result.seasonal is False
    assert result.best_order.p == 1


@rsps_lib.activate
def test_auto_ets(client):
    _mock_post(
        f"{BASE}/api/auto/ets/forecast",
        {"forecasts": [5.0], "intervals": [], "bestType": "TRIPLE", "bestParameters": [0.3, 0.1, 0.1], "bestScore": -30.0},
    )
    result = client.selection.auto_ets([1.0, 2.0, 3.0, 4.0])
    assert result.best_type == "TRIPLE"


# ---------------------------------------------------------------------------
# Diagnostics
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_ljung_box(client):
    _mock_post(
        f"{BASE}/api/diagnostics/ljung-box",
        {"statistic": 5.2, "pValue": 0.87, "lags": 10, "rejectsAtFivePercent": False},
    )
    result = client.diagnostics.ljung_box([0.1, -0.2, 0.05])
    assert result.rejects_at_five_percent is False
    assert result.p_value == pytest.approx(0.87)


# ---------------------------------------------------------------------------
# Analyze
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_analyze(client):
    _mock_post(
        f"{BASE}/api/analyze",
        {
            "average": 2.0, "variance": 1.0, "standardDeviation": 1.0,
            "min": 1.0, "max": 3.0, "minIndex": 0, "maxIndex": 2,
            "autocorrelation": 0.5, "autocovariance": 0.5,
            "acf": [1.0, 0.5], "pacf": [1.0, 0.3],
            "adfStatistic": -2.5, "stationary": True,
            "logTransformed": [0.0, 0.693, 1.099],
            "firstDifference": [1.0, 1.0],
            "rollingAverage": [1.5, 2.0, 2.5],
        },
    )
    result = client.analyze.analyze([1.0, 2.0, 3.0])
    assert result.average == pytest.approx(2.0)
    assert result.stationary is True
    assert len(result.acf) == 2


# ---------------------------------------------------------------------------
# Error handling
# ---------------------------------------------------------------------------

@rsps_lib.activate
def test_api_error_raises(client):
    rsps_lib.add(
        rsps_lib.POST,
        f"{BASE}/api/arima/forecast",
        json={"message": "Bad request"},
        status=400,
    )
    with pytest.raises(TslibAPIError) as exc_info:
        client.arima.forecast([1.0], 1, 0, 0)
    assert exc_info.value.status_code == 400
    assert "Bad request" in str(exc_info.value)


@rsps_lib.activate
def test_api_error_500(client):
    rsps_lib.add(rsps_lib.POST, f"{BASE}/api/arima/forecast", body="Internal Server Error", status=500)
    with pytest.raises(TslibAPIError) as exc_info:
        client.arima.forecast([1.0], 1, 0, 0)
    assert exc_info.value.status_code == 500
