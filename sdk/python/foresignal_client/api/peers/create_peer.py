from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.peer import Peer
from ...models.peer_create import PeerCreate
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: PeerCreate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/peers",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Peer | Problem | None:
    if response.status_code == 201:
        response_201 = Peer.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Peer | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: PeerCreate,
) -> Response[Peer | Problem]:
    """Register a peer

     Registers a peer. Enabled PULL peers register dynamic sources `peer-{id}` (and `peer-{id}-trk` for
    TRACK kinds), pin the remote key on the first verified `/info` (TOFU) and replicate the selection.
    Base URL passes the outbound URL policy (URL_NOT_ALLOWED).

    **Scope:** `admin`.

    Args:
        body (PeerCreate): Register a peer instance (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Peer | Problem]
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
    body: PeerCreate,
) -> Peer | Problem | None:
    """Register a peer

     Registers a peer. Enabled PULL peers register dynamic sources `peer-{id}` (and `peer-{id}-trk` for
    TRACK kinds), pin the remote key on the first verified `/info` (TOFU) and replicate the selection.
    Base URL passes the outbound URL policy (URL_NOT_ALLOWED).

    **Scope:** `admin`.

    Args:
        body (PeerCreate): Register a peer instance (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Peer | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: PeerCreate,
) -> Response[Peer | Problem]:
    """Register a peer

     Registers a peer. Enabled PULL peers register dynamic sources `peer-{id}` (and `peer-{id}-trk` for
    TRACK kinds), pin the remote key on the first verified `/info` (TOFU) and replicate the selection.
    Base URL passes the outbound URL policy (URL_NOT_ALLOWED).

    **Scope:** `admin`.

    Args:
        body (PeerCreate): Register a peer instance (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Peer | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: PeerCreate,
) -> Peer | Problem | None:
    """Register a peer

     Registers a peer. Enabled PULL peers register dynamic sources `peer-{id}` (and `peer-{id}-trk` for
    TRACK kinds), pin the remote key on the first verified `/info` (TOFU) and replicate the selection.
    Base URL passes the outbound URL policy (URL_NOT_ALLOWED).

    **Scope:** `admin`.

    Args:
        body (PeerCreate): Register a peer instance (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Peer | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
