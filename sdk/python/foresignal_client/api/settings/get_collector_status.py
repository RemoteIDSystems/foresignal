from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.collector_status import CollectorStatus
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/settings/collector/status",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CollectorStatus | Problem | None:
    if response.status_code == 200:
        response_200 = CollectorStatus.from_dict(response.json())

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
) -> Response[CollectorStatus | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[CollectorStatus | Problem]:
    """Get collector status

     The pull governor's live state (prompt 160), so a client can show it working rather than only its
    setting: `cap` is the `maxConcurrentPulls` in force (`getCollectorSettings`), `inUse` how many
    polling fetches hold a permit and are pulling from upstream right now, and `waiting` how many are
    queued for one. `inUse` may briefly exceed `cap` just after `updateCollectorSettings` lowers it,
    until running pulls finish. Streams are not counted. Read-only; a snapshot at the time of the
    request.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CollectorStatus | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> CollectorStatus | Problem | None:
    """Get collector status

     The pull governor's live state (prompt 160), so a client can show it working rather than only its
    setting: `cap` is the `maxConcurrentPulls` in force (`getCollectorSettings`), `inUse` how many
    polling fetches hold a permit and are pulling from upstream right now, and `waiting` how many are
    queued for one. `inUse` may briefly exceed `cap` just after `updateCollectorSettings` lowers it,
    until running pulls finish. Streams are not counted. Read-only; a snapshot at the time of the
    request.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CollectorStatus | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[CollectorStatus | Problem]:
    """Get collector status

     The pull governor's live state (prompt 160), so a client can show it working rather than only its
    setting: `cap` is the `maxConcurrentPulls` in force (`getCollectorSettings`), `inUse` how many
    polling fetches hold a permit and are pulling from upstream right now, and `waiting` how many are
    queued for one. `inUse` may briefly exceed `cap` just after `updateCollectorSettings` lowers it,
    until running pulls finish. Streams are not counted. Read-only; a snapshot at the time of the
    request.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CollectorStatus | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> CollectorStatus | Problem | None:
    """Get collector status

     The pull governor's live state (prompt 160), so a client can show it working rather than only its
    setting: `cap` is the `maxConcurrentPulls` in force (`getCollectorSettings`), `inUse` how many
    polling fetches hold a permit and are pulling from upstream right now, and `waiting` how many are
    queued for one. `inUse` may briefly exceed `cap` just after `updateCollectorSettings` lowers it,
    until running pulls finish. Streams are not counted. Read-only; a snapshot at the time of the
    request.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CollectorStatus | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
