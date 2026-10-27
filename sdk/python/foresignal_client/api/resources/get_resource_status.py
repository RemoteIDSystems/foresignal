from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.resource_status import ResourceStatus
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/system/resources",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ResourceStatus | None:
    if response.status_code == 200:
        response_200 = ResourceStatus.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ResourceStatus]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ResourceStatus]:
    """Get host resource status

     Live host disk free space, memory and database size with an overall ok/warn/critical status (177),
    rated against `GET /v1/settings/thresholds`. Readings are cached for up to 30 s. A `warn` or
    `critical` gauge also appears in `/v1/system.warnings` as `DISK_LOW`, `DISK_CRITICAL`, `MEMORY_LOW`
    or `MEMORY_CRITICAL`. Requires `storage.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ResourceStatus]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | ResourceStatus | None:
    """Get host resource status

     Live host disk free space, memory and database size with an overall ok/warn/critical status (177),
    rated against `GET /v1/settings/thresholds`. Readings are cached for up to 30 s. A `warn` or
    `critical` gauge also appears in `/v1/system.warnings` as `DISK_LOW`, `DISK_CRITICAL`, `MEMORY_LOW`
    or `MEMORY_CRITICAL`. Requires `storage.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ResourceStatus
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ResourceStatus]:
    """Get host resource status

     Live host disk free space, memory and database size with an overall ok/warn/critical status (177),
    rated against `GET /v1/settings/thresholds`. Readings are cached for up to 30 s. A `warn` or
    `critical` gauge also appears in `/v1/system.warnings` as `DISK_LOW`, `DISK_CRITICAL`, `MEMORY_LOW`
    or `MEMORY_CRITICAL`. Requires `storage.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ResourceStatus]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | ResourceStatus | None:
    """Get host resource status

     Live host disk free space, memory and database size with an overall ok/warn/critical status (177),
    rated against `GET /v1/settings/thresholds`. Readings are cached for up to 30 s. A `warn` or
    `critical` gauge also appears in `/v1/system.warnings` as `DISK_LOW`, `DISK_CRITICAL`, `MEMORY_LOW`
    or `MEMORY_CRITICAL`. Requires `storage.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ResourceStatus
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
