from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.outbound_settings import OutboundSettings
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: OutboundSettings,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/outbound",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> OutboundSettings | Problem | None:
    if response.status_code == 200:
        response_200 = OutboundSettings.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[OutboundSettings | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: OutboundSettings,
) -> Response[OutboundSettings | Problem]:
    """Set outbound URL policy overrides

     Deny entries win over allow entries, and allow entries never unblock the always-blocked set (link-
    local, cloud metadata and the other not-forwardable special-purpose addresses). Host entries are
    exact (case-insensitive, trailing dot ignored), `*.suffix` for subdomains only, or an IP literal;
    CIDR entries are returned as their network address; at most 500 entries per list. A bad entry is 400
    VALIDATION_FAILED with a field such as `allowCidrs[0]`.

    **Scope:** `admin`.

    Args:
        body (OutboundSettings): Admin overrides of the outbound URL policy (035); deny wins

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OutboundSettings | Problem]
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
    body: OutboundSettings,
) -> OutboundSettings | Problem | None:
    """Set outbound URL policy overrides

     Deny entries win over allow entries, and allow entries never unblock the always-blocked set (link-
    local, cloud metadata and the other not-forwardable special-purpose addresses). Host entries are
    exact (case-insensitive, trailing dot ignored), `*.suffix` for subdomains only, or an IP literal;
    CIDR entries are returned as their network address; at most 500 entries per list. A bad entry is 400
    VALIDATION_FAILED with a field such as `allowCidrs[0]`.

    **Scope:** `admin`.

    Args:
        body (OutboundSettings): Admin overrides of the outbound URL policy (035); deny wins

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OutboundSettings | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: OutboundSettings,
) -> Response[OutboundSettings | Problem]:
    """Set outbound URL policy overrides

     Deny entries win over allow entries, and allow entries never unblock the always-blocked set (link-
    local, cloud metadata and the other not-forwardable special-purpose addresses). Host entries are
    exact (case-insensitive, trailing dot ignored), `*.suffix` for subdomains only, or an IP literal;
    CIDR entries are returned as their network address; at most 500 entries per list. A bad entry is 400
    VALIDATION_FAILED with a field such as `allowCidrs[0]`.

    **Scope:** `admin`.

    Args:
        body (OutboundSettings): Admin overrides of the outbound URL policy (035); deny wins

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OutboundSettings | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: OutboundSettings,
) -> OutboundSettings | Problem | None:
    """Set outbound URL policy overrides

     Deny entries win over allow entries, and allow entries never unblock the always-blocked set (link-
    local, cloud metadata and the other not-forwardable special-purpose addresses). Host entries are
    exact (case-insensitive, trailing dot ignored), `*.suffix` for subdomains only, or an IP literal;
    CIDR entries are returned as their network address; at most 500 entries per list. A bad entry is 400
    VALIDATION_FAILED with a field such as `allowCidrs[0]`.

    **Scope:** `admin`.

    Args:
        body (OutboundSettings): Admin overrides of the outbound URL policy (035); deny wins

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OutboundSettings | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
