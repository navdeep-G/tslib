from dataclasses import dataclass
from typing import Dict, List, Optional


@dataclass
class PredictionInterval:
    step: int
    point_forecast: float
    lower: float
    upper: float
    confidence_level: float

    @classmethod
    def _from(cls, d: dict) -> "PredictionInterval":
        return cls(
            step=d["step"],
            point_forecast=d["pointForecast"],
            lower=d["lower"],
            upper=d["upper"],
            confidence_level=d["confidenceLevel"],
        )


def _intervals(d: dict, key: str = "intervals") -> List[PredictionInterval]:
    return [PredictionInterval._from(i) for i in (d.get(key) or [])]


@dataclass
class AnalyzeResult:
    average: float
    variance: float
    standard_deviation: float
    min: float
    max: float
    min_index: int
    max_index: int
    autocorrelation: float
    autocovariance: float
    acf: List[float]
    pacf: List[float]
    adf_statistic: float
    stationary: bool
    log_transformed: List[float]
    first_difference: List[float]
    rolling_average: List[float]

    @classmethod
    def _from(cls, d: dict) -> "AnalyzeResult":
        return cls(
            average=d["average"],
            variance=d["variance"],
            standard_deviation=d["standardDeviation"],
            min=d["min"],
            max=d["max"],
            min_index=d["minIndex"],
            max_index=d["maxIndex"],
            autocorrelation=d["autocorrelation"],
            autocovariance=d["autocovariance"],
            acf=list(d.get("acf") or []),
            pacf=list(d.get("pacf") or []),
            adf_statistic=d["adfStatistic"],
            stationary=d["stationary"],
            log_transformed=list(d.get("logTransformed") or []),
            first_difference=list(d.get("firstDifference") or []),
            rolling_average=list(d.get("rollingAverage") or []),
        )


@dataclass
class ArimaResult:
    forecasts: List[float]
    intervals: List[PredictionInterval]
    ar_coefficients: List[float]
    ma_coefficients: List[float]
    intercept: float
    innovation_variance: float
    p: int
    d: int
    q: int

    @classmethod
    def _from(cls, d: dict) -> "ArimaResult":
        return cls(
            forecasts=list(d.get("forecasts") or []),
            intervals=_intervals(d),
            ar_coefficients=list(d.get("arCoefficients") or []),
            ma_coefficients=list(d.get("maCoefficients") or []),
            intercept=d.get("intercept", 0.0),
            innovation_variance=d.get("innovationVariance", 0.0),
            p=d.get("p", 0),
            d=d.get("d", 0),
            q=d.get("q", 0),
        )


@dataclass
class SarimaResult:
    forecasts: List[float]
    intervals: List[PredictionInterval]
    ar_coefficients: List[float]
    ma_coefficients: List[float]
    seasonal_ar_coefficients: List[float]
    seasonal_ma_coefficients: List[float]
    intercept: float
    innovation_variance: float
    p: int
    d: int
    q: int
    seasonal_p: int
    seasonal_d: int
    seasonal_q: int
    seasonal_period: int

    @classmethod
    def _from(cls, d: dict) -> "SarimaResult":
        return cls(
            forecasts=list(d.get("forecasts") or []),
            intervals=_intervals(d),
            ar_coefficients=list(d.get("arCoefficients") or []),
            ma_coefficients=list(d.get("maCoefficients") or []),
            seasonal_ar_coefficients=list(d.get("seasonalArCoefficients") or []),
            seasonal_ma_coefficients=list(d.get("seasonalMaCoefficients") or []),
            intercept=d.get("intercept", 0.0),
            innovation_variance=d.get("innovationVariance", 0.0),
            p=d.get("p", 0),
            d=d.get("d", 0),
            q=d.get("q", 0),
            seasonal_p=d.get("seasonalP", 0),
            seasonal_d=d.get("seasonalD", 0),
            seasonal_q=d.get("seasonalQ", 0),
            seasonal_period=d.get("seasonalPeriod", 12),
        )


@dataclass
class ArimaxResult:
    forecasts: List[float]
    ar_coefficients: List[float]
    ma_coefficients: List[float]
    exogenous_coefficients: List[float]
    innovation_variance: float

    @classmethod
    def _from(cls, d: dict) -> "ArimaxResult":
        return cls(
            forecasts=list(d.get("forecasts") or []),
            ar_coefficients=list(d.get("arCoefficients") or []),
            ma_coefficients=list(d.get("maCoefficients") or []),
            exogenous_coefficients=list(d.get("exogenousCoefficients") or []),
            innovation_variance=d.get("innovationVariance", 0.0),
        )


@dataclass
class OrderSearchResult:
    model_type: str
    p: int
    d: int
    q: int
    seasonal_p: int
    seasonal_d: int
    seasonal_q: int
    seasonal_period: int
    criterion: str
    score: float

    @classmethod
    def _from(cls, d: dict) -> "OrderSearchResult":
        return cls(
            model_type=d.get("modelType", ""),
            p=d.get("p", 0),
            d=d.get("d", 0),
            q=d.get("q", 0),
            seasonal_p=d.get("seasonalP", 0),
            seasonal_d=d.get("seasonalD", 0),
            seasonal_q=d.get("seasonalQ", 0),
            seasonal_period=d.get("seasonalPeriod", 0),
            criterion=d.get("criterion", ""),
            score=d.get("score", 0.0),
        )


@dataclass
class VarResult:
    forecasts: List[List[float]]
    lag_order: int
    num_series: int
    aic: float

    @classmethod
    def _from(cls, d: dict) -> "VarResult":
        return cls(
            forecasts=d.get("forecasts") or [],
            lag_order=d.get("lagOrder", 0),
            num_series=d.get("numSeries", 0),
            aic=d.get("aic", 0.0),
        )


@dataclass
class StlResult:
    trend: List[float]
    seasonal: List[float]
    remainder: List[float]
    reconstructed: List[float]

    @classmethod
    def _from(cls, d: dict) -> "StlResult":
        return cls(
            trend=list(d.get("trend") or []),
            seasonal=list(d.get("seasonal") or []),
            remainder=list(d.get("remainder") or []),
            reconstructed=list(d.get("reconstructed") or []),
        )


@dataclass
class LjungBoxResult:
    statistic: float
    p_value: float
    lags: int
    rejects_at_five_percent: bool

    @classmethod
    def _from(cls, d: dict) -> "LjungBoxResult":
        return cls(
            statistic=d["statistic"],
            p_value=d["pvalue"],
            lags=d["lags"],
            rejects_at_five_percent=d["rejectsAtFivePercent"],
        )


@dataclass
class EtsResult:
    forecasts: List[float]
    intervals: List[PredictionInterval]

    @classmethod
    def _from(cls, d: dict) -> "EtsResult":
        return cls(
            forecasts=list(d.get("forecasts") or []),
            intervals=_intervals(d),
        )


@dataclass
class MetricsResult:
    mae: float
    rmse: float
    mape: float
    smape: float
    mase: float
    mean_error: float

    @classmethod
    def _from(cls, d: dict) -> "MetricsResult":
        return cls(
            mae=d.get("mae", 0.0),
            rmse=d.get("rmse", 0.0),
            mape=d.get("mape", 0.0),
            smape=d.get("smape", 0.0),
            mase=d.get("mase", 0.0),
            mean_error=d.get("meanError", 0.0),
        )


@dataclass
class BacktestResult:
    actual: List[float]
    forecast: List[float]
    origins: List[int]
    mae: float
    rmse: float
    mape: float
    smape: float
    mase: float

    @classmethod
    def _from(cls, d: dict) -> "BacktestResult":
        return cls(
            actual=list(d.get("actual") or []),
            forecast=list(d.get("forecast") or []),
            origins=list(d.get("origins") or []),
            mae=d.get("mae", 0.0),
            rmse=d.get("rmse", 0.0),
            mape=d.get("mape", 0.0),
            smape=d.get("smape", 0.0),
            mase=d.get("mase", 0.0),
        )


@dataclass
class TrainTestSplitResult:
    train: List[float]
    test: List[float]
    train_size: int
    test_size: int

    @classmethod
    def _from(cls, d: dict) -> "TrainTestSplitResult":
        return cls(
            train=list(d.get("train") or []),
            test=list(d.get("test") or []),
            train_size=d.get("trainSize", 0),
            test_size=d.get("testSize", 0),
        )


@dataclass
class BenchmarkSummary:
    model_name: str
    mae: float
    rmse: float
    mape: float
    smape: float
    mase: float

    @classmethod
    def _from(cls, d: dict) -> "BenchmarkSummary":
        return cls(
            model_name=d.get("modelName", ""),
            mae=d.get("mae", 0.0),
            rmse=d.get("rmse", 0.0),
            mape=d.get("mape", 0.0),
            smape=d.get("smape", 0.0),
            mase=d.get("mase", 0.0),
        )


@dataclass
class OutlierResult:
    outlier_indices: List[int]

    @classmethod
    def _from(cls, d: dict) -> "OutlierResult":
        return cls(outlier_indices=list(d.get("outlierIndices") or []))


@dataclass
class BoxCoxResult:
    result: List[float]
    lambda_: Optional[float]

    @classmethod
    def _from(cls, d: dict) -> "BoxCoxResult":
        return cls(
            result=list(d.get("result") or []),
            lambda_=d.get("lambda"),
        )


@dataclass
class AdfResult:
    statistic: float
    p_value: float
    lag: int
    stationary: bool
    needs_diff: bool

    @classmethod
    def _from(cls, d: dict) -> "AdfResult":
        return cls(
            statistic=d["statistic"],
            p_value=d["pvalue"],
            lag=d["lag"],
            stationary=d["stationary"],
            needs_diff=d["needsDiff"],
        )


@dataclass
class KpssResult:
    statistic: float
    lags: int
    regression_type: str
    stationary_at_five_percent: bool
    stationary_at_one_percent: bool
    critical_value_five_percent: float
    critical_value_one_percent: float

    @classmethod
    def _from(cls, d: dict) -> "KpssResult":
        return cls(
            statistic=d["statistic"],
            lags=d["lags"],
            regression_type=d["regressionType"],
            stationary_at_five_percent=d["stationaryAtFivePercent"],
            stationary_at_one_percent=d["stationaryAtOnePercent"],
            critical_value_five_percent=d["criticalValueFivePercent"],
            critical_value_one_percent=d["criticalValueOnePercent"],
        )


@dataclass
class AutoArimaResult:
    forecasts: List[float]
    intervals: List[PredictionInterval]
    best_order: OrderSearchResult
    seasonal: bool

    @classmethod
    def _from(cls, d: dict) -> "AutoArimaResult":
        return cls(
            forecasts=list(d.get("forecasts") or []),
            intervals=_intervals(d),
            best_order=OrderSearchResult._from(d.get("bestOrder") or {}),
            seasonal=d.get("seasonal", False),
        )


@dataclass
class AutoEtsResult:
    forecasts: List[float]
    intervals: List[PredictionInterval]
    best_type: str
    best_parameters: List[float]
    best_score: float

    @classmethod
    def _from(cls, d: dict) -> "AutoEtsResult":
        return cls(
            forecasts=list(d.get("forecasts") or []),
            intervals=_intervals(d),
            best_type=d.get("bestType", ""),
            best_parameters=list(d.get("bestParameters") or []),
            best_score=d.get("bestScore", 0.0),
        )


@dataclass
class LocalLevelFilterResult:
    filtered_states: List[float]
    smoothed_signal: List[float]
    process_variance: float
    observation_variance: float
    log_likelihood: float
    forecasts: List[float]
    forecast_variances: List[float]
    intervals: List[PredictionInterval]

    @classmethod
    def _from(cls, d: dict) -> "LocalLevelFilterResult":
        return cls(
            filtered_states=list(d.get("filteredStates") or []),
            smoothed_signal=list(d.get("smoothedSignal") or []),
            process_variance=d.get("processVariance", 0.0),
            observation_variance=d.get("observationVariance", 0.0),
            log_likelihood=d.get("logLikelihood", 0.0),
            forecasts=list(d.get("forecasts") or []),
            forecast_variances=list(d.get("forecastVariances") or []),
            intervals=_intervals(d),
        )


@dataclass
class KalmanFilterResult:
    predicted_states: List[float]
    filtered_states: List[float]
    filtered_covariances: List[float]
    innovations: List[float]
    log_likelihood: float
    forecasts: List[float]
    forecast_variances: List[float]

    @classmethod
    def _from(cls, d: dict) -> "KalmanFilterResult":
        return cls(
            predicted_states=list(d.get("predictedStates") or []),
            filtered_states=list(d.get("filteredStates") or []),
            filtered_covariances=list(d.get("filteredCovariances") or []),
            innovations=list(d.get("innovations") or []),
            log_likelihood=d.get("logLikelihood", 0.0),
            forecasts=list(d.get("forecasts") or []),
            forecast_variances=list(d.get("forecastVariances") or []),
        )


@dataclass
class ModelSpec:
    """Describes a model for backtest / benchmark requests."""

    type: str
    p: Optional[int] = None
    d: Optional[int] = None
    q: Optional[int] = None
    seasonal_p: Optional[int] = None
    seasonal_d: Optional[int] = None
    seasonal_q: Optional[int] = None
    seasonal_period: Optional[int] = None
    alpha: Optional[float] = None
    beta: Optional[float] = None
    gamma: Optional[float] = None
    initialization_method: Optional[int] = None
    period: Optional[int] = None
    max_p: Optional[int] = None
    max_d: Optional[int] = None
    max_q: Optional[int] = None
    criterion: Optional[str] = None

    def _to_dict(self) -> dict:
        result: Dict[str, object] = {"type": self.type}
        _map = {
            "p": "p", "d": "d", "q": "q",
            "seasonal_p": "seasonalP", "seasonal_d": "seasonalD", "seasonal_q": "seasonalQ",
            "seasonal_period": "seasonalPeriod",
            "alpha": "alpha", "beta": "beta", "gamma": "gamma",
            "initialization_method": "initializationMethod",
            "period": "period",
            "max_p": "maxP", "max_d": "maxD", "max_q": "maxQ",
            "criterion": "criterion",
        }
        for attr, key in _map.items():
            val = getattr(self, attr)
            if val is not None:
                result[key] = val
        return result


@dataclass
class NamedModel:
    name: str
    spec: ModelSpec

    def _to_dict(self) -> dict:
        return {"name": self.name, "spec": self.spec._to_dict()}
