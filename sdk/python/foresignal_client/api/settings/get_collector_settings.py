from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.collector_settings import CollectorSettings
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/settings/collector",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CollectorSettings | Problem | None:
    if response.status_code == 200:
        response_200 = CollectorSettings.from_dict(response.json())

        return response_200

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
) -> Response[CollectorSettings | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[CollectorSettings | Problem]:
    """Get collector settings

     The collector's runtime settings (prompt 150). `maxConcurrentPulls` is the size of the global pull
    governor: how many polling fetches may pull from upstream at once across every source (default 2, 1
    to 16). Every scheduled and manual fetch, and every polling probe, holds a permit while it pulls;
    streams are not counted. Per-source intervals, host pacing and budgets apply beneath it unchanged.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CollectorSettings | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> CollectorSettings | Problem | None:
    """Get collector settings

     The collector's runtime settings (prompt 150). `maxConcurrentPulls` is the size of the global pull
    governor: how many polling fetches may pull from upstream at once across every source (default 2, 1
    to 16). Every scheduled and manual fetch, and every polling probe, holds a permit while it pulls;
    streams are not counted. Per-source intervals, host pacing and budgets apply beneath it unchanged.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CollectorSettings | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[CollectorSettings | Problem]:
    """Get collector settings

     The collector's runtime settings (prompt 150). `maxConcurrentPulls` is the size of the global pull
    governor: how many polling fetches may pull from upstream at once across every source (default 2, 1
    to 16). Every scheduled and manual fetch, and every polling probe, holds a permit while it pulls;
    streams are not counted. Per-source intervals, host pacing and budgets apply beneath it unchanged.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CollectorSettings | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> CollectorSettings | Problem | None:
    """Get collector settings

     The collector's runtime settings (prompt 150). `maxConcurrentPulls` is the size of the global pull
    governor: how many polling fetches may pull from upstream at once across every source (default 2, 1
    to 16). Every scheduled and manual fetch, and every polling probe, holds a permit while it pulls;
    streams are not counted. Per-source intervals, host pacing and budgets apply beneath it unchanged.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CollectorSettings | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
