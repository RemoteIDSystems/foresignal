from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.rate_limit_settings import RateLimitSettings
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/settings/ratelimits",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RateLimitSettings | None:
    if response.status_code == 200:
        response_200 = RateLimitSettings.from_dict(response.json())

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
) -> Response[Problem | RateLimitSettings]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | RateLimitSettings]:
    """Get rate-limit settings

     Over a limit → 429 RATE_LIMITED with Retry-After. Admin tokens are exempt from `read` and `tiles`
    only. Repeated failed credentials back off per IP (doubling from 1 s to 300 s) without locking out a
    valid credential.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RateLimitSettings]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | RateLimitSettings | None:
    """Get rate-limit settings

     Over a limit → 429 RATE_LIMITED with Retry-After. Admin tokens are exempt from `read` and `tiles`
    only. Repeated failed credentials back off per IP (doubling from 1 s to 300 s) without locking out a
    valid credential.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RateLimitSettings
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | RateLimitSettings]:
    """Get rate-limit settings

     Over a limit → 429 RATE_LIMITED with Retry-After. Admin tokens are exempt from `read` and `tiles`
    only. Repeated failed credentials back off per IP (doubling from 1 s to 300 s) without locking out a
    valid credential.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RateLimitSettings]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | RateLimitSettings | None:
    """Get rate-limit settings

     Over a limit → 429 RATE_LIMITED with Retry-After. Admin tokens are exempt from `read` and `tiles`
    only. Repeated failed credentials back off per IP (doubling from 1 s to 300 s) without locking out a
    valid credential.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RateLimitSettings
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
