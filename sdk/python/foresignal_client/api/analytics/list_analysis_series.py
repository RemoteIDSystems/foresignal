from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analysis_series_page import AnalysisSeriesPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_family: list[str] | Unset = UNSET
    if not isinstance(family, Unset):
        json_family = family

    params["family"] = json_family

    json_region: list[str] | Unset = UNSET
    if not isinstance(region, Unset):
        json_region = region

    params["region"] = json_region

    params["enabled"] = enabled

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analytics/series",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalysisSeriesPage | Problem | None:
    if response.status_code == 200:
        response_200 = AnalysisSeriesPage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AnalysisSeriesPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AnalysisSeriesPage | Problem]:
    """List analysis series

     Lists the analysis-series registry that change-point, event-study and forecast detectors run on.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (list[str] | Unset):
        region (list[str] | Unset):
        enabled (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalysisSeriesPage | Problem]
    """

    kwargs = _get_kwargs(
        family=family,
        region=region,
        enabled=enabled,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AnalysisSeriesPage | Problem | None:
    """List analysis series

     Lists the analysis-series registry that change-point, event-study and forecast detectors run on.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (list[str] | Unset):
        region (list[str] | Unset):
        enabled (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalysisSeriesPage | Problem
    """

    return sync_detailed(
        client=client,
        family=family,
        region=region,
        enabled=enabled,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AnalysisSeriesPage | Problem]:
    """List analysis series

     Lists the analysis-series registry that change-point, event-study and forecast detectors run on.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (list[str] | Unset):
        region (list[str] | Unset):
        enabled (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalysisSeriesPage | Problem]
    """

    kwargs = _get_kwargs(
        family=family,
        region=region,
        enabled=enabled,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AnalysisSeriesPage | Problem | None:
    """List analysis series

     Lists the analysis-series registry that change-point, event-study and forecast detectors run on.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (list[str] | Unset):
        region (list[str] | Unset):
        enabled (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalysisSeriesPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            family=family,
            region=region,
            enabled=enabled,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
