from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.peer import Peer
from ...models.peer_update import PeerUpdate
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    peer_id: str,
    *,
    body: PeerUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/peers/{peer_id}".format(
            peer_id=quote(str(peer_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Peer | Problem | None:
    if response.status_code == 200:
        response_200 = Peer.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

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
    peer_id: str,
    *,
    client: AuthenticatedClient,
    body: PeerUpdate,
) -> Response[Peer | Problem]:
    """Replace a peer's settings

     Replaces settings; omitted token keeps the stored one. Only admin can change `pinnedKid`.

    **Scope:** `admin`.

    Args:
        peer_id (str):
        body (PeerUpdate): Replace a peer's settings; omit token to keep it (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Peer | Problem]
    """

    kwargs = _get_kwargs(
        peer_id=peer_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    peer_id: str,
    *,
    client: AuthenticatedClient,
    body: PeerUpdate,
) -> Peer | Problem | None:
    """Replace a peer's settings

     Replaces settings; omitted token keeps the stored one. Only admin can change `pinnedKid`.

    **Scope:** `admin`.

    Args:
        peer_id (str):
        body (PeerUpdate): Replace a peer's settings; omit token to keep it (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Peer | Problem
    """

    return sync_detailed(
        peer_id=peer_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    peer_id: str,
    *,
    client: AuthenticatedClient,
    body: PeerUpdate,
) -> Response[Peer | Problem]:
    """Replace a peer's settings

     Replaces settings; omitted token keeps the stored one. Only admin can change `pinnedKid`.

    **Scope:** `admin`.

    Args:
        peer_id (str):
        body (PeerUpdate): Replace a peer's settings; omit token to keep it (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Peer | Problem]
    """

    kwargs = _get_kwargs(
        peer_id=peer_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    peer_id: str,
    *,
    client: AuthenticatedClient,
    body: PeerUpdate,
) -> Peer | Problem | None:
    """Replace a peer's settings

     Replaces settings; omitted token keeps the stored one. Only admin can change `pinnedKid`.

    **Scope:** `admin`.

    Args:
        peer_id (str):
        body (PeerUpdate): Replace a peer's settings; omit token to keep it (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Peer | Problem
    """

    return (
        await asyncio_detailed(
            peer_id=peer_id,
            client=client,
            body=body,
        )
    ).parsed
