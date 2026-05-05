from typing import List, Optional

from ._http import _Session
from .models import BoxCoxResult


class TransformsAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def _simple(self, path: str, data: List[float]) -> List[float]:
        return list(self._s.post(path, {"data": data})["forecasts"])

    def log(self, data: List[float]) -> List[float]:
        return self._simple("/api/transform/log", data)

    def sqrt(self, data: List[float]) -> List[float]:
        return self._simple("/api/transform/sqrt", data)

    def cbrt(self, data: List[float]) -> List[float]:
        return self._simple("/api/transform/cbrt", data)

    def root(self, data: List[float], r: float) -> List[float]:
        return list(self._s.post("/api/transform/root", {"data": data, "r": r})["forecasts"])

    def boxcox(
        self,
        data: List[float],
        *,
        lambda_: Optional[float] = None,
        lower_bound: Optional[float] = None,
        upper_bound: Optional[float] = None,
    ) -> BoxCoxResult:
        body: dict = {"data": data}
        if lambda_ is not None:
            body["lambda"] = lambda_
        if lower_bound is not None:
            body["lowerBound"] = lower_bound
        if upper_bound is not None:
            body["upperBound"] = upper_bound
        return BoxCoxResult._from(self._s.post("/api/transform/boxcox", body))

    def inverse_boxcox(self, data: List[float], lambda_: float) -> List[float]:
        return list(
            self._s.post("/api/transform/boxcox/inverse", {"data": data, "lambda": lambda_})["forecasts"]
        )

    def difference(self, data: List[float], *, order: int = 1) -> List[float]:
        return list(
            self._s.post("/api/transform/difference", {"data": data, "order": order})["forecasts"]
        )

    def seasonal_difference(
        self, data: List[float], lag: int, *, order: int = 1
    ) -> List[float]:
        return list(
            self._s.post(
                "/api/transform/seasonal-difference",
                {"data": data, "lag": lag, "order": order},
            )["forecasts"]
        )

    def inverse_difference(
        self, data: List[float], history: List[float], *, order: int = 1
    ) -> List[float]:
        return list(
            self._s.post(
                "/api/transform/difference/inverse",
                {"data": data, "history": history, "order": order},
            )["forecasts"]
        )
