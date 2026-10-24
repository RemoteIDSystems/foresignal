from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecast_calibration import ForecastCalibration
from ...models.forecast_numeric_calibration import ForecastNumericCalibration
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["forecaster"] = forecaster

    params["category"] = category

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/forecasts/calibration",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecastCalibration | ForecastNumericCalibration | Problem | None:
    if response.status_code == 200:

        def _parse_response_200(
            data: object,
        ) -> ForecastCalibration | ForecastNumericCalibration:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = ForecastCalibration.from_dict(data)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            response_200_type_1 = ForecastNumericCalibration.from_dict(data)

            return response_200_type_1

        response_200 = _parse_response_200(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ForecastCalibration | ForecastNumericCalibration | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
) -> Response[ForecastCalibration | ForecastNumericCalibration | Problem]:
    """Get calibration curve and bins

     For a binary forecaster: CORP reliability curve points (PAV) with the miscalibration, discrimination
    and uncertainty decomposition, plus a 10-bin table with counts (the fallback when n < 50). For a
    numeric-only forecaster or question (152): a quantile-reliability diagram (empirical coverage of
    each stated quantile vs its nominal level), a PIT histogram and a calibration-error summary (`type:
    numeric`).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        forecaster (str | Unset):
        category (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastCalibration | ForecastNumericCalibration | Problem]
    """

    kwargs = _get_kwargs(
        forecaster=forecaster,
        category=category,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
) -> ForecastCalibration | ForecastNumericCalibration | Problem | None:
    """Get calibration curve and bins

     For a binary forecaster: CORP reliability curve points (PAV) with the miscalibration, discrimination
    and uncertainty decomposition, plus a 10-bin table with counts (the fallback when n < 50). For a
    numeric-only forecaster or question (152): a quantile-reliability diagram (empirical coverage of
    each stated quantile vs its nominal level), a PIT histogram and a calibration-error summary (`type:
    numeric`).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        forecaster (str | Unset):
        category (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastCalibration | ForecastNumericCalibration | Problem
    """

    return sync_detailed(
        client=client,
        forecaster=forecaster,
        category=category,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
) -> Response[ForecastCalibration | ForecastNumericCalibration | Problem]:
    """Get calibration curve and bins

     For a binary forecaster: CORP reliability curve points (PAV) with the miscalibration, discrimination
    and uncertainty decomposition, plus a 10-bin table with counts (the fallback when n < 50). For a
    numeric-only forecaster or question (152): a quantile-reliability diagram (empirical coverage of
    each stated quantile vs its nominal level), a PIT histogram and a calibration-error summary (`type:
    numeric`).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        forecaster (str | Unset):
        category (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastCalibration | ForecastNumericCalibration | Problem]
    """

    kwargs = _get_kwargs(
        forecaster=forecaster,
        category=category,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    forecaster: str | Unset = UNSET,
    category: str | Unset = UNSET,
) -> ForecastCalibration | ForecastNumericCalibration | Problem | None:
    """Get calibration curve and bins

     For a binary forecaster: CORP reliability curve points (PAV) with the miscalibration, discrimination
    and uncertainty decomposition, plus a 10-bin table with counts (the fallback when n < 50). For a
    numeric-only forecaster or question (152): a quantile-reliability diagram (empirical coverage of
    each stated quantile vs its nominal level), a PIT histogram and a calibration-error summary (`type:
    numeric`).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        forecaster (str | Unset):
        category (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastCalibration | ForecastNumericCalibration | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            forecaster=forecaster,
            category=category,
        )
    ).parsed
