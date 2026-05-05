from typing import List

from ._http import _Session
from .models import OutlierResult


class DataQualityAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def impute(
        self,
        data: List[float],
        *,
        strategy: str = "LINEAR_INTERPOLATION",
    ) -> List[float]:
        return list(
            self._s.post("/api/dataquality/impute", {"data": data, "strategy": strategy})["forecasts"]
        )

    def outliers(
        self,
        data: List[float],
        *,
        method: str = "Z_SCORE",
        threshold: float = 3.0,
    ) -> OutlierResult:
        body = {"data": data, "method": method, "threshold": threshold}
        return OutlierResult._from(self._s.post("/api/dataquality/outliers", body))

    def winsorize(
        self,
        data: List[float],
        *,
        lower_probability: float = 0.05,
        upper_probability: float = 0.95,
    ) -> List[float]:
        body = {
            "data": data,
            "lowerProbability": lower_probability,
            "upperProbability": upper_probability,
        }
        return list(self._s.post("/api/dataquality/winsorize", body)["forecasts"])
