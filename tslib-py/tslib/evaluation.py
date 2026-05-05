from typing import List, Optional

from ._http import _Session
from .models import BacktestResult, BenchmarkSummary, MetricsResult, ModelSpec, NamedModel, TrainTestSplitResult


class EvaluationAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def metrics(
        self,
        actual: List[float],
        forecast: List[float],
        *,
        training_series: Optional[List[float]] = None,
        seasonal_period: int = 1,
    ) -> MetricsResult:
        body: dict = {
            "actual": actual,
            "forecast": forecast,
            "seasonalPeriod": seasonal_period,
        }
        if training_series is not None:
            body["trainingSeries"] = training_series
        return MetricsResult._from(self._s.post("/api/evaluate/metrics", body))

    def backtest(
        self,
        data: List[float],
        model_spec: ModelSpec,
        *,
        min_train_size: int = 20,
        horizon: int = 1,
        step_size: int = 1,
        seasonal_period: int = 1,
    ) -> BacktestResult:
        body = {
            "data": data,
            "modelSpec": model_spec._to_dict(),
            "minTrainSize": min_train_size,
            "horizon": horizon,
            "stepSize": step_size,
            "seasonalPeriod": seasonal_period,
        }
        return BacktestResult._from(self._s.post("/api/evaluate/backtest", body))

    def train_test_split(
        self,
        data: List[float],
        *,
        train_size: Optional[int] = None,
        train_ratio: Optional[float] = None,
    ) -> TrainTestSplitResult:
        body: dict = {"data": data}
        if train_size is not None:
            body["trainSize"] = train_size
        if train_ratio is not None:
            body["trainRatio"] = train_ratio
        return TrainTestSplitResult._from(self._s.post("/api/evaluate/train-test-split", body))

    def benchmark(
        self,
        data: List[float],
        models: List[NamedModel],
        *,
        min_train_size: int = 20,
        horizon: int = 1,
        step_size: int = 1,
        seasonal_period: int = 1,
    ) -> List[BenchmarkSummary]:
        body = {
            "data": data,
            "models": [m._to_dict() for m in models],
            "minTrainSize": min_train_size,
            "horizon": horizon,
            "stepSize": step_size,
            "seasonalPeriod": seasonal_period,
        }
        return [BenchmarkSummary._from(r) for r in self._s.post("/api/evaluate/benchmark", body)]
