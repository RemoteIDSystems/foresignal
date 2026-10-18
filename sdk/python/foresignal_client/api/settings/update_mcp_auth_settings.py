from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.mcp_auth_settings import McpAuthSettings
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: McpAuthSettings,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/mcp-auth",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> McpAuthSettings | Problem | None:
    if response.status_code == 200:
        response_200 = McpAuthSettings.from_dict(response.json())

        return response_200

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
) -> Response[McpAuthSettings | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: McpAuthSettings,
) -> Response[McpAuthSettings | Problem]:
    """Update MCP authorization settings

     Replaces `mcp.auth`. `admin` can never appear in `scopeMap` (400). With `mode` `oauth` or `both`,
    `issuer` is required, and `audience` too unless `server.publicBaseUrl` is set. `issuer` and
    `jwksUri` must be absolute http(s) URLs without query or fragment that pass the 035 outbound URL
    policy (`LAN_ALLOWED`; plain `http` only towards loopback, private or tailnet addresses). Applies
    only to `/mcp`; REST keeps Foresignal tokens.

    **Scope:** `admin`.

    Args:
        body (McpAuthSettings): Settings `mcp.auth`: remote MCP authorization delegated to the
            operator's identity provider (no built-in authorization server, no device grant)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[McpAuthSettings | Problem]
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
    body: McpAuthSettings,
) -> McpAuthSettings | Problem | None:
    """Update MCP authorization settings

     Replaces `mcp.auth`. `admin` can never appear in `scopeMap` (400). With `mode` `oauth` or `both`,
    `issuer` is required, and `audience` too unless `server.publicBaseUrl` is set. `issuer` and
    `jwksUri` must be absolute http(s) URLs without query or fragment that pass the 035 outbound URL
    policy (`LAN_ALLOWED`; plain `http` only towards loopback, private or tailnet addresses). Applies
    only to `/mcp`; REST keeps Foresignal tokens.

    **Scope:** `admin`.

    Args:
        body (McpAuthSettings): Settings `mcp.auth`: remote MCP authorization delegated to the
            operator's identity provider (no built-in authorization server, no device grant)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        McpAuthSettings | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: McpAuthSettings,
) -> Response[McpAuthSettings | Problem]:
    """Update MCP authorization settings

     Replaces `mcp.auth`. `admin` can never appear in `scopeMap` (400). With `mode` `oauth` or `both`,
    `issuer` is required, and `audience` too unless `server.publicBaseUrl` is set. `issuer` and
    `jwksUri` must be absolute http(s) URLs without query or fragment that pass the 035 outbound URL
    policy (`LAN_ALLOWED`; plain `http` only towards loopback, private or tailnet addresses). Applies
    only to `/mcp`; REST keeps Foresignal tokens.

    **Scope:** `admin`.

    Args:
        body (McpAuthSettings): Settings `mcp.auth`: remote MCP authorization delegated to the
            operator's identity provider (no built-in authorization server, no device grant)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[McpAuthSettings | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: McpAuthSettings,
) -> McpAuthSettings | Problem | None:
    """Update MCP authorization settings

     Replaces `mcp.auth`. `admin` can never appear in `scopeMap` (400). With `mode` `oauth` or `both`,
    `issuer` is required, and `audience` too unless `server.publicBaseUrl` is set. `issuer` and
    `jwksUri` must be absolute http(s) URLs without query or fragment that pass the 035 outbound URL
    policy (`LAN_ALLOWED`; plain `http` only towards loopback, private or tailnet addresses). Applies
    only to `/mcp`; REST keeps Foresignal tokens.

    **Scope:** `admin`.

    Args:
        body (McpAuthSettings): Settings `mcp.auth`: remote MCP authorization delegated to the
            operator's identity provider (no built-in authorization server, no device grant)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        McpAuthSettings | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
