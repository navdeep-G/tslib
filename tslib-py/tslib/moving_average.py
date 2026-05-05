from typing import List, Optional

from ._http import _Session


class MovingAverageAPI:
    def __init__(self, session: _Session) -> None:
        self._s = session

    def compute(
        self,
        data: List[float],
        *,
        period: int = 5,
        type: str = "SIMPLE",
        alpha: Optional[float] = None,
    ) -> List[float]:
        body: dict = {"data": data, "period": period, "type": type}
        if alpha is not None:
            body["alpha"] = alpha
        return list(self._s.post("/api/moving-average", body)["forecasts"])
