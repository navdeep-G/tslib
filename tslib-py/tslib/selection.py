from typing import List, Optional

from ._http import _Session
from .models import AutoArimaResult, AutoEtsResult


class SelectionAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def auto_arima(
        self,
        data: List[float],
        *,
        max_p: int = 3,
        max_d: int = 2,
        max_q: int = 3,
        steps: int = 1,
        confidence_level: float = 0.95,
        criterion: str = "AIC",
        max_seasonal_p: Optional[int] = None,
        max_seasonal_d: Optional[int] = None,
        max_seasonal_q: Optional[int] = None,
        seasonal_period: Optional[int] = None,
    ) -> AutoArimaResult:
        body: dict = {
            "data": data,
            "maxP": max_p,
            "maxD": max_d,
            "maxQ": max_q,
            "steps": steps,
            "confidenceLevel": confidence_level,
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
        return AutoArimaResult._from(self._s.post("/api/auto/arima/forecast", body))

    def auto_ets(
        self,
        data: List[float],
        *,
        steps: int = 1,
        confidence_level: float = 0.95,
        seasonal_period: Optional[int] = None,
    ) -> AutoEtsResult:
        body: dict = {
            "data": data,
            "steps": steps,
            "confidenceLevel": confidence_level,
        }
        if seasonal_period is not None:
            body["seasonalPeriod"] = seasonal_period
        return AutoEtsResult._from(self._s.post("/api/auto/ets/forecast", body))
