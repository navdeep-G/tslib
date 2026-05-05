from typing import List

from ._http import _Session
from .models import EtsResult, KalmanFilterResult, LocalLevelFilterResult


class StatespaceAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def local_level_forecast(
        self,
        data: List[float],
        *,
        steps: int = 1,
        confidence_level: float = 0.95,
    ) -> EtsResult:
        body = {"data": data, "steps": steps, "confidenceLevel": confidence_level}
        return EtsResult._from(self._s.post("/api/statespace/local-level/forecast", body))

    def local_level_filter(
        self,
        data: List[float],
        *,
        steps: int = 1,
        confidence_level: float = 0.95,
    ) -> LocalLevelFilterResult:
        body = {"data": data, "steps": steps, "confidenceLevel": confidence_level}
        return LocalLevelFilterResult._from(self._s.post("/api/statespace/local-level/filter", body))

    def kalman_filter(
        self,
        data: List[float],
        *,
        process_variance: float = 1.0,
        observation_variance: float = 1.0,
        steps: int = 0,
    ) -> KalmanFilterResult:
        body = {
            "data": data,
            "processVariance": process_variance,
            "observationVariance": observation_variance,
            "steps": steps,
        }
        return KalmanFilterResult._from(self._s.post("/api/statespace/kalman/filter", body))
