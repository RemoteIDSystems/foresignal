from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.protected_resource_metadata import ProtectedResourceMetadata
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/.well-known/oauth-protected-resource",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ProtectedResourceMetadata | None:
    if response.status_code == 200:
        response_200 = ProtectedResourceMetadata.from_dict(response.json())

        return response_200

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ProtectedResourceMetadata]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ProtectedResourceMetadata]:
    """Protected resource metadata

     RFC 9728 metadata for the MCP resource (same document as the `/mcp` path variant). Clients find it
    through `WWW-Authenticate: Bearer resource_metadata=…` on 401 from `/mcp`.

    While `mcp.auth.mode` is `oauth` or `both`, `/mcp` always needs a credential (open reads do not
    apply there): a request without one, or with an invalid token, gets `401` with `WWW-Authenticate:
    Bearer resource_metadata="{base}/.well-known/oauth-protected-resource/mcp", scope="foresignal:read"`
    (plus `error="invalid_token"` when a token was presented); a valid token whose scopes map to
    nothing, or an identity-provider token calling a tool above its mapped scope, gets `403` with `WWW-
    Authenticate: Bearer error="insufficient_scope", scope="…", resource_metadata="…"`. The `scope`
    values are the identity-provider scopes of `mcp.auth.scopeMap` that reach the needed Foresignal
    scope. REST paths never accept identity-provider tokens.

    **Scope:** `open` (no token).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProtectedResourceMetadata]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | ProtectedResourceMetadata | None:
    """Protected resource metadata

     RFC 9728 metadata for the MCP resource (same document as the `/mcp` path variant). Clients find it
    through `WWW-Authenticate: Bearer resource_metadata=…` on 401 from `/mcp`.

    While `mcp.auth.mode` is `oauth` or `both`, `/mcp` always needs a credential (open reads do not
    apply there): a request without one, or with an invalid token, gets `401` with `WWW-Authenticate:
    Bearer resource_metadata="{base}/.well-known/oauth-protected-resource/mcp", scope="foresignal:read"`
    (plus `error="invalid_token"` when a token was presented); a valid token whose scopes map to
    nothing, or an identity-provider token calling a tool above its mapped scope, gets `403` with `WWW-
    Authenticate: Bearer error="insufficient_scope", scope="…", resource_metadata="…"`. The `scope`
    values are the identity-provider scopes of `mcp.auth.scopeMap` that reach the needed Foresignal
    scope. REST paths never accept identity-provider tokens.

    **Scope:** `open` (no token).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProtectedResourceMetadata
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ProtectedResourceMetadata]:
    """Protected resource metadata

     RFC 9728 metadata for the MCP resource (same document as the `/mcp` path variant). Clients find it
    through `WWW-Authenticate: Bearer resource_metadata=…` on 401 from `/mcp`.

    While `mcp.auth.mode` is `oauth` or `both`, `/mcp` always needs a credential (open reads do not
    apply there): a request without one, or with an invalid token, gets `401` with `WWW-Authenticate:
    Bearer resource_metadata="{base}/.well-known/oauth-protected-resource/mcp", scope="foresignal:read"`
    (plus `error="invalid_token"` when a token was presented); a valid token whose scopes map to
    nothing, or an identity-provider token calling a tool above its mapped scope, gets `403` with `WWW-
    Authenticate: Bearer error="insufficient_scope", scope="…", resource_metadata="…"`. The `scope`
    values are the identity-provider scopes of `mcp.auth.scopeMap` that reach the needed Foresignal
    scope. REST paths never accept identity-provider tokens.

    **Scope:** `open` (no token).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProtectedResourceMetadata]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | ProtectedResourceMetadata | None:
    """Protected resource metadata

     RFC 9728 metadata for the MCP resource (same document as the `/mcp` path variant). Clients find it
    through `WWW-Authenticate: Bearer resource_metadata=…` on 401 from `/mcp`.

    While `mcp.auth.mode` is `oauth` or `both`, `/mcp` always needs a credential (open reads do not
    apply there): a request without one, or with an invalid token, gets `401` with `WWW-Authenticate:
    Bearer resource_metadata="{base}/.well-known/oauth-protected-resource/mcp", scope="foresignal:read"`
    (plus `error="invalid_token"` when a token was presented); a valid token whose scopes map to
    nothing, or an identity-provider token calling a tool above its mapped scope, gets `403` with `WWW-
    Authenticate: Bearer error="insufficient_scope", scope="…", resource_metadata="…"`. The `scope`
    values are the identity-provider scopes of `mcp.auth.scopeMap` that reach the needed Foresignal
    scope. REST paths never accept identity-provider tokens.

    **Scope:** `open` (no token).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProtectedResourceMetadata
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
