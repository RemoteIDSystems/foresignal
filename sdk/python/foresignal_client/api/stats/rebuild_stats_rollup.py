from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.stats_rollup_rebuild import StatsRollupRebuild
from ...models.stats_rollup_rebuild_request import StatsRollupRebuildRequest
from ...types import Response


def _get_kwargs(
    *,
    body: StatsRollupRebuildRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/stats/rollup/rebuild",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | StatsRollupRebuild | None:
    if response.status_code == 202:
        response_202 = StatsRollupRebuild.from_dict(response.json())

        return response_202

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | StatsRollupRebuild]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: StatsRollupRebuildRequest,
) -> Response[Problem | StatsRollupRebuild]:
    """Rebuild activity rollups

     Recomputes closed hourly and daily buckets in the range that still have raw data.

    **Scope:** `admin`.

    Args:
        body (StatsRollupRebuildRequest): Range of closed buckets to recompute from raw rows (038)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsRollupRebuild]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: StatsRollupRebuildRequest,
) -> Problem | StatsRollupRebuild | None:
    """Rebuild activity rollups

     Recomputes closed hourly and daily buckets in the range that still have raw data.

    **Scope:** `admin`.

    Args:
        body (StatsRollupRebuildRequest): Range of closed buckets to recompute from raw rows (038)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsRollupRebuild
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: StatsRollupRebuildRequest,
) -> Response[Problem | StatsRollupRebuild]:
    """Rebuild activity rollups

     Recomputes closed hourly and daily buckets in the range that still have raw data.

    **Scope:** `admin`.

    Args:
        body (StatsRollupRebuildRequest): Range of closed buckets to recompute from raw rows (038)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsRollupRebuild]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: StatsRollupRebuildRequest,
) -> Problem | StatsRollupRebuild | None:
    """Rebuild activity rollups

     Recomputes closed hourly and daily buckets in the range that still have raw data.

    **Scope:** `admin`.

    Args:
        body (StatsRollupRebuildRequest): Range of closed buckets to recompute from raw rows (038)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsRollupRebuild
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
