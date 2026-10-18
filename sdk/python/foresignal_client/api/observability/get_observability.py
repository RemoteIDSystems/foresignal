import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_observability_include_item import GetObservabilityIncludeItem
from ...models.observability_coverage import ObservabilityCoverage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    family: str,
    region: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
    include: list[GetObservabilityIncludeItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["family"] = family

    params["region"] = region

    params["bbox"] = bbox

    json_at: str | Unset = UNSET
    if not isinstance(at, Unset):
        json_at = at.isoformat()
    params["at"] = json_at

    params["band"] = band

    json_include: list[str] | Unset = UNSET
    if not isinstance(include, Unset):
        json_include = []
        for include_item_data in include:
            include_item = include_item_data.value
            json_include.append(include_item)

    params["include"] = json_include

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/observability",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ObservabilityCoverage | Problem | None:
    if response.status_code == 200:
        response_200 = ObservabilityCoverage.from_dict(response.json())

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
) -> Response[ObservabilityCoverage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    family: str,
    region: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
    include: list[GetObservabilityIncludeItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ObservabilityCoverage | Problem]:
    """Detection probability per region or cell

     Combined p_detect with interval and methods per region (`region`) or H3 res-4 cell (`bbox`), using
    estimates valid at `at` (never today's for past times). `absenceOk` is false (reason
    LOW_DETECTABILITY) below absenceMinDetect. Family `social-attention` (134, method `platform-volume`)
    is the expected post volume per platform from the trailing 8-week baseline; it is ok only when a
    complete-stream platform was healthy and the regional baseline is ≥ `social.minRegionBaseline` (else
    LOW_SOCIAL_COVERAGE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (str):
        region (str | Unset):
        bbox (str | Unset):
        at (datetime.datetime | Unset):
        band (str | Unset):
        include (list[GetObservabilityIncludeItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ObservabilityCoverage | Problem]
    """

    kwargs = _get_kwargs(
        family=family,
        region=region,
        bbox=bbox,
        at=at,
        band=band,
        include=include,
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
    family: str,
    region: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
    include: list[GetObservabilityIncludeItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ObservabilityCoverage | Problem | None:
    """Detection probability per region or cell

     Combined p_detect with interval and methods per region (`region`) or H3 res-4 cell (`bbox`), using
    estimates valid at `at` (never today's for past times). `absenceOk` is false (reason
    LOW_DETECTABILITY) below absenceMinDetect. Family `social-attention` (134, method `platform-volume`)
    is the expected post volume per platform from the trailing 8-week baseline; it is ok only when a
    complete-stream platform was healthy and the regional baseline is ≥ `social.minRegionBaseline` (else
    LOW_SOCIAL_COVERAGE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (str):
        region (str | Unset):
        bbox (str | Unset):
        at (datetime.datetime | Unset):
        band (str | Unset):
        include (list[GetObservabilityIncludeItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ObservabilityCoverage | Problem
    """

    return sync_detailed(
        client=client,
        family=family,
        region=region,
        bbox=bbox,
        at=at,
        band=band,
        include=include,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    family: str,
    region: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
    include: list[GetObservabilityIncludeItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ObservabilityCoverage | Problem]:
    """Detection probability per region or cell

     Combined p_detect with interval and methods per region (`region`) or H3 res-4 cell (`bbox`), using
    estimates valid at `at` (never today's for past times). `absenceOk` is false (reason
    LOW_DETECTABILITY) below absenceMinDetect. Family `social-attention` (134, method `platform-volume`)
    is the expected post volume per platform from the trailing 8-week baseline; it is ok only when a
    complete-stream platform was healthy and the regional baseline is ≥ `social.minRegionBaseline` (else
    LOW_SOCIAL_COVERAGE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (str):
        region (str | Unset):
        bbox (str | Unset):
        at (datetime.datetime | Unset):
        band (str | Unset):
        include (list[GetObservabilityIncludeItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ObservabilityCoverage | Problem]
    """

    kwargs = _get_kwargs(
        family=family,
        region=region,
        bbox=bbox,
        at=at,
        band=band,
        include=include,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    family: str,
    region: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
    include: list[GetObservabilityIncludeItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ObservabilityCoverage | Problem | None:
    """Detection probability per region or cell

     Combined p_detect with interval and methods per region (`region`) or H3 res-4 cell (`bbox`), using
    estimates valid at `at` (never today's for past times). `absenceOk` is false (reason
    LOW_DETECTABILITY) below absenceMinDetect. Family `social-attention` (134, method `platform-volume`)
    is the expected post volume per platform from the trailing 8-week baseline; it is ok only when a
    complete-stream platform was healthy and the regional baseline is ≥ `social.minRegionBaseline` (else
    LOW_SOCIAL_COVERAGE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (str):
        region (str | Unset):
        bbox (str | Unset):
        at (datetime.datetime | Unset):
        band (str | Unset):
        include (list[GetObservabilityIncludeItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ObservabilityCoverage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            family=family,
            region=region,
            bbox=bbox,
            at=at,
            band=band,
            include=include,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
