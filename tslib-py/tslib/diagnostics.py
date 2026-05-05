from typing import List

from ._http import _Session
from .models import LjungBoxResult


class DiagnosticsAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def ljung_box(
        self,
        residuals: List[float],
        *,
        lags: int = 10,
        degrees_of_freedom_adjustment: int = 0,
    ) -> LjungBoxResult:
        body = {
            "residuals": residuals,
            "lags": lags,
            "degreesOfFreedomAdjustment": degrees_of_freedom_adjustment,
        }
        return LjungBoxResult._from(self._s.post("/api/diagnostics/ljung-box", body))
