from typing import List, Optional

from ._http import _Session
from .models import StlResult


class DecompositionAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def stl(
        self,
        data: List[float],
        period: int,
        *,
        trend_window: Optional[int] = None,
        seasonal_window: Optional[int] = None,
        iterations: Optional[int] = None,
        outer_iterations: Optional[int] = None,
    ) -> StlResult:
        body: dict = {"data": data, "period": period}
        if trend_window is not None:
            body["trendWindow"] = trend_window
        if seasonal_window is not None:
            body["seasonalWindow"] = seasonal_window
        if iterations is not None:
            body["iterations"] = iterations
        if outer_iterations is not None:
            body["outerIterations"] = outer_iterations
        return StlResult._from(self._s.post("/api/decompose/stl", body))
