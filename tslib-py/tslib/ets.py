from typing import List

from ._http import _Session
from .models import EtsResult


class EtsAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def single(
        self,
        data: List[float],
        *,
        alpha: float = 0.3,
        steps: int = 1,
        confidence_level: float = 0.95,
    ) -> EtsResult:
        body = {
            "data": data,
            "alpha": alpha,
            "steps": steps,
            "confidenceLevel": confidence_level,
        }
        return EtsResult._from(self._s.post("/api/ets/single/forecast", body))

    def double(
        self,
        data: List[float],
        *,
        alpha: float = 0.3,
        gamma: float = 0.1,
        initialization_method: int = 0,
        steps: int = 1,
        confidence_level: float = 0.95,
    ) -> EtsResult:
        body = {
            "data": data,
            "alpha": alpha,
            "gamma": gamma,
            "initializationMethod": initialization_method,
            "steps": steps,
            "confidenceLevel": confidence_level,
        }
        return EtsResult._from(self._s.post("/api/ets/double/forecast", body))

    def triple(
        self,
        data: List[float],
        *,
        alpha: float = 0.3,
        beta: float = 0.1,
        gamma: float = 0.1,
        period: int = 12,
        steps: int = 1,
        confidence_level: float = 0.95,
    ) -> EtsResult:
        body = {
            "data": data,
            "alpha": alpha,
            "beta": beta,
            "gamma": gamma,
            "period": period,
            "steps": steps,
            "confidenceLevel": confidence_level,
        }
        return EtsResult._from(self._s.post("/api/ets/triple/forecast", body))
