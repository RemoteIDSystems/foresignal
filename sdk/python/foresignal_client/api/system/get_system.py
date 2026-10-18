from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.system_info import SystemInfo
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/system",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SystemInfo | None:
    if response.status_code == 200:
        response_200 = SystemInfo.from_dict(response.json())

        return response_200

    if 400 <= response.status_code <= 499:
        response_4xx = Problem.from_dict(response.json())

        return response_4xx

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SystemInfo]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SystemInfo]:
    """System information

     Anonymous callers get only version, apiVersion, startedAt, uptimeSeconds, status, features and
    limits; callers presenting a valid token of at least the `read` scope get the full document (023;
    every valid token has at least `read`) (database, records, sources, scheduler, ingestWriter,
    streams, notifications, history per tier, catchUp and warnings, which include
    PUBLIC_BASE_URL_INSECURE and PUBLIC_BASE_URL_MISMATCH from 023).

    **Scope:** `open` (no token; a valid token returns the full document).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SystemInfo]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | SystemInfo | None:
    """System information

     Anonymous callers get only version, apiVersion, startedAt, uptimeSeconds, status, features and
    limits; callers presenting a valid token of at least the `read` scope get the full document (023;
    every valid token has at least `read`) (database, records, sources, scheduler, ingestWriter,
    streams, notifications, history per tier, catchUp and warnings, which include
    PUBLIC_BASE_URL_INSECURE and PUBLIC_BASE_URL_MISMATCH from 023).

    **Scope:** `open` (no token; a valid token returns the full document).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SystemInfo
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SystemInfo]:
    """System information

     Anonymous callers get only version, apiVersion, startedAt, uptimeSeconds, status, features and
    limits; callers presenting a valid token of at least the `read` scope get the full document (023;
    every valid token has at least `read`) (database, records, sources, scheduler, ingestWriter,
    streams, notifications, history per tier, catchUp and warnings, which include
    PUBLIC_BASE_URL_INSECURE and PUBLIC_BASE_URL_MISMATCH from 023).

    **Scope:** `open` (no token; a valid token returns the full document).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SystemInfo]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | SystemInfo | None:
    """System information

     Anonymous callers get only version, apiVersion, startedAt, uptimeSeconds, status, features and
    limits; callers presenting a valid token of at least the `read` scope get the full document (023;
    every valid token has at least `read`) (database, records, sources, scheduler, ingestWriter,
    streams, notifications, history per tier, catchUp and warnings, which include
    PUBLIC_BASE_URL_INSECURE and PUBLIC_BASE_URL_MISMATCH from 023).

    **Scope:** `open` (no token; a valid token returns the full document).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SystemInfo
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
