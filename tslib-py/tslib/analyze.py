from typing import List

from ._http import _Session
from .models import AnalyzeResult


class AnalyzeAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def analyze(
        self,
        data: List[float],
        *,
        k: int = 1,
        n: int = 10,
        window_size: int = 5,
    ) -> AnalyzeResult:
        body = {"data": data, "k": k, "n": n, "windowSize": window_size}
        return AnalyzeResult._from(self._s.post("/api/analyze", body))
