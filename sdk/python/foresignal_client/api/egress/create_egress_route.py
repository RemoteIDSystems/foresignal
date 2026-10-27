from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.egress_route import EgressRoute
from ...models.egress_route_input import EgressRouteInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: EgressRouteInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/egress",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EgressRoute | Problem | None:
    if response.status_code == 201:
        response_201 = EgressRoute.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

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
) -> Response[EgressRoute | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: EgressRouteInput,
) -> Response[EgressRoute | Problem]:
    """Create an egress route

     Adds a proxy, WireGuard profile or bound interface to the egress pool; credentials are stored
    encrypted, never returned. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** `url` is `http://host:port` or `socks5://host:port` (no path, no credentials in it) for
    those types, and its host must pass the outbound URL policy's `LAN_ALLOWED` profile (400
    URL_NOT_ALLOWED otherwise); `interface` takes `boundInterface` (an interface name or an address
    literal); names are unique ignoring case; `wireguard` takes `secrets.wireguardConfig` and is
    recorded but always unavailable, because the server cannot bring a tunnel up. Secrets are sealed
    with the instance key and never returned; `secretsSet` names those stored.

    Args:
        body (EgressRouteInput): Create or replace an egress route; credentials and WireGuard
            config are write-only and stored encrypted

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EgressRoute | Problem]
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
    body: EgressRouteInput,
) -> EgressRoute | Problem | None:
    """Create an egress route

     Adds a proxy, WireGuard profile or bound interface to the egress pool; credentials are stored
    encrypted, never returned. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** `url` is `http://host:port` or `socks5://host:port` (no path, no credentials in it) for
    those types, and its host must pass the outbound URL policy's `LAN_ALLOWED` profile (400
    URL_NOT_ALLOWED otherwise); `interface` takes `boundInterface` (an interface name or an address
    literal); names are unique ignoring case; `wireguard` takes `secrets.wireguardConfig` and is
    recorded but always unavailable, because the server cannot bring a tunnel up. Secrets are sealed
    with the instance key and never returned; `secretsSet` names those stored.

    Args:
        body (EgressRouteInput): Create or replace an egress route; credentials and WireGuard
            config are write-only and stored encrypted

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EgressRoute | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: EgressRouteInput,
) -> Response[EgressRoute | Problem]:
    """Create an egress route

     Adds a proxy, WireGuard profile or bound interface to the egress pool; credentials are stored
    encrypted, never returned. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** `url` is `http://host:port` or `socks5://host:port` (no path, no credentials in it) for
    those types, and its host must pass the outbound URL policy's `LAN_ALLOWED` profile (400
    URL_NOT_ALLOWED otherwise); `interface` takes `boundInterface` (an interface name or an address
    literal); names are unique ignoring case; `wireguard` takes `secrets.wireguardConfig` and is
    recorded but always unavailable, because the server cannot bring a tunnel up. Secrets are sealed
    with the instance key and never returned; `secretsSet` names those stored.

    Args:
        body (EgressRouteInput): Create or replace an egress route; credentials and WireGuard
            config are write-only and stored encrypted

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EgressRoute | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: EgressRouteInput,
) -> EgressRoute | Problem | None:
    """Create an egress route

     Adds a proxy, WireGuard profile or bound interface to the egress pool; credentials are stored
    encrypted, never returned. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** `url` is `http://host:port` or `socks5://host:port` (no path, no credentials in it) for
    those types, and its host must pass the outbound URL policy's `LAN_ALLOWED` profile (400
    URL_NOT_ALLOWED otherwise); `interface` takes `boundInterface` (an interface name or an address
    literal); names are unique ignoring case; `wireguard` takes `secrets.wireguardConfig` and is
    recorded but always unavailable, because the server cannot bring a tunnel up. Secrets are sealed
    with the instance key and never returned; `secretsSet` names those stored.

    Args:
        body (EgressRouteInput): Create or replace an egress route; credentials and WireGuard
            config are write-only and stored encrypted

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EgressRoute | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
