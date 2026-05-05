from typing import List, Optional

from ._http import _Session
from .models import ArimaResult, ArimaxResult, OrderSearchResult, SarimaResult, VarResult


class ArimaAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def forecast(
        self,
        data: List[float],
        p: int,
        d: int,
        q: int,
        *,
        steps: int = 1,
        confidence_level: float = 0.95,
        include_intervals: bool = False,
        max_iterations: int = 200,
        tolerance: float = 1e-8,
    ) -> ArimaResult:
        path = "/api/arima/forecast-intervals" if include_intervals else "/api/arima/forecast"
        body = {
            "data": data,
            "p": p,
            "d": d,
            "q": q,
            "steps": steps,
            "confidenceLevel": confidence_level,
            "maxIterations": max_iterations,
            "tolerance": tolerance,
        }
        return ArimaResult._from(self._s.post(path, body))

    def sarima_forecast(
        self,
        data: List[float],
        p: int,
        d: int,
        q: int,
        seasonal_p: int,
        seasonal_d: int,
        seasonal_q: int,
        *,
        seasonal_period: int = 12,
        steps: int = 1,
        confidence_level: float = 0.95,
        include_intervals: bool = False,
        max_iterations: int = 200,
        tolerance: float = 1e-8,
    ) -> SarimaResult:
        path = "/api/sarima/forecast-intervals" if include_intervals else "/api/sarima/forecast"
        body = {
            "data": data,
            "p": p,
            "d": d,
            "q": q,
            "seasonalP": seasonal_p,
            "seasonalD": seasonal_d,
            "seasonalQ": seasonal_q,
            "seasonalPeriod": seasonal_period,
            "steps": steps,
            "confidenceLevel": confidence_level,
            "maxIterations": max_iterations,
            "tolerance": tolerance,
        }
        return SarimaResult._from(self._s.post(path, body))

    def arimax_forecast(
        self,
        data: List[float],
        exogenous: List[List[float]],
        future_exogenous: List[List[float]],
        p: int,
        d: int,
        q: int,
        *,
        max_iterations: int = 200,
        tolerance: float = 1e-8,
    ) -> ArimaxResult:
        body = {
            "data": data,
            "exogenous": exogenous,
            "futureExogenous": future_exogenous,
            "p": p,
            "d": d,
            "q": q,
            "maxIterations": max_iterations,
            "tolerance": tolerance,
        }
        return ArimaxResult._from(self._s.post("/api/arimax/forecast", body))

    def order_search(
        self,
        data: List[float],
        *,
        max_p: int = 3,
        max_d: int = 2,
        max_q: int = 3,
        criterion: str = "AIC",
        max_seasonal_p: Optional[int] = None,
        max_seasonal_d: Optional[int] = None,
        max_seasonal_q: Optional[int] = None,
        seasonal_period: Optional[int] = None,
    ) -> OrderSearchResult:
        body: dict = {
            "data": data,
            "maxP": max_p,
            "maxD": max_d,
            "maxQ": max_q,
            "criterion": criterion,
        }
        if max_seasonal_p is not None:
            body["maxSeasonalP"] = max_seasonal_p
        if max_seasonal_d is not None:
            body["maxSeasonalD"] = max_seasonal_d
        if max_seasonal_q is not None:
            body["maxSeasonalQ"] = max_seasonal_q
        if seasonal_period is not None:
            body["seasonalPeriod"] = seasonal_period
        return OrderSearchResult._from(self._s.post("/api/arima/order-search", body))

    def var_forecast(
        self,
        series: List[List[float]],
        *,
        steps: int = 1,
        lag_order: Optional[int] = None,
        max_lag: int = 5,
    ) -> VarResult:
        body: dict = {"series": series, "steps": steps, "maxLag": max_lag}
        if lag_order is not None:
            body["lagOrder"] = lag_order
        return VarResult._from(self._s.post("/api/var/forecast", body))
