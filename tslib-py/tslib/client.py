from typing import Optional

from ._http import _Session
from .analyze import AnalyzeAPI
from .arima import ArimaAPI
from .data_quality import DataQualityAPI
from .decomposition import DecompositionAPI
from .diagnostics import DiagnosticsAPI
from .ets import EtsAPI
from .evaluation import EvaluationAPI
from .moving_average import MovingAverageAPI
from .selection import SelectionAPI
from .stationarity import StationarityAPI
from .statespace import StatespaceAPI
from .transforms import TransformsAPI


class TslibClient:
    """Client for the tslib time-series REST API.

    Args:
        base_url: Server URL, e.g. ``"http://localhost:8080"``.
        api_key: Value sent as ``X-API-Key`` header when API key auth is enabled.
        timeout: Request timeout in seconds.
    """

    def __init__(
        self,
        base_url: str = "http://localhost:8080",
        *,
        api_key: Optional[str] = None,
        timeout: float = 30.0,
    ) -> None:
        session = _Session(base_url, api_key=api_key, timeout=timeout)
        self.analyze = AnalyzeAPI(session)
        self.arima = ArimaAPI(session)
        self.ets = EtsAPI(session)
        self.statespace = StatespaceAPI(session)
        self.transforms = TransformsAPI(session)
        self.decomposition = DecompositionAPI(session)
        self.evaluation = EvaluationAPI(session)
        self.data_quality = DataQualityAPI(session)
        self.stationarity = StationarityAPI(session)
        self.moving_average = MovingAverageAPI(session)
        self.selection = SelectionAPI(session)
        self.diagnostics = DiagnosticsAPI(session)
