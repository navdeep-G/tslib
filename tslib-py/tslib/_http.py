from typing import Optional

import requests

from .exceptions import TslibAPIError


class _Session:
    def __init__(
        self,
        base_url: str,
        api_key: Optional[str] = None,
        timeout: float = 30.0,
    ) -> None:
        self._base_url = base_url.rstrip("/")
        self._timeout = timeout
        self._headers = {"Content-Type": "application/json", "Accept": "application/json"}
        if api_key:
            self._headers["X-API-Key"] = api_key

    def post(self, path: str, body: dict) -> dict:
        url = f"{self._base_url}{path}"
        resp = requests.post(url, json=body, headers=self._headers, timeout=self._timeout)
        if not resp.ok:
            try:
                detail = resp.json().get("message") or resp.text
            except Exception:
                detail = resp.text
            raise TslibAPIError(resp.status_code, detail)
        return resp.json()
