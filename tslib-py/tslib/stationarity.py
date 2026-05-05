from typing import List, Optional

from ._http import _Session
from .models import AdfResult, KpssResult


class StationarityAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def adf(self, data: List[float], *, lag: Optional[int] = None) -> AdfResult:
        body: dict = {"data": data}
        if lag is not None:
            body["lag"] = lag
        return AdfResult._from(self._s.post("/api/tests/adf", body))

    def kpss(
        self,
        data: List[float],
        *,
        regression_type: str = "LEVEL",
        lags: Optional[int] = None,
    ) -> KpssResult:
        body: dict = {"data": data, "regressionType": regression_type}
        if lags is not None:
            body["lags"] = lags
        return KpssResult._from(self._s.post("/api/tests/kpss", body))
