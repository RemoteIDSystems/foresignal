from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.peer_ingest_key import PeerIngestKey
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    peer_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/peers/{peer_id}/ingest-key".format(
            peer_id=quote(str(peer_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PeerIngestKey | Problem | None:
    if response.status_code == 200:
        response_200 = PeerIngestKey.from_dict(response.json())

        return response_200

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
) -> Response[PeerIngestKey | Problem]:
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
) -> Response[PeerIngestKey | Problem]:
    """Issue or rotate a peer ingest key

     Issues a new `fsp_` peer ingest key (shown once) and revokes the previous one; the edge uses it for
    `/v1/federation/ingest/{peerId}` and `/v1/federation/bundle`.

    **Scope:** `admin`.

    Args:
        peer_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PeerIngestKey | Problem]
    """

    kwargs = _get_kwargs(
        peer_id=peer_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    peer_id: str,
    *,
    client: AuthenticatedClient,
) -> PeerIngestKey | Problem | None:
    """Issue or rotate a peer ingest key

     Issues a new `fsp_` peer ingest key (shown once) and revokes the previous one; the edge uses it for
    `/v1/federation/ingest/{peerId}` and `/v1/federation/bundle`.

    **Scope:** `admin`.

    Args:
        peer_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PeerIngestKey | Problem
    """

    return sync_detailed(
        peer_id=peer_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    peer_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[PeerIngestKey | Problem]:
    """Issue or rotate a peer ingest key

     Issues a new `fsp_` peer ingest key (shown once) and revokes the previous one; the edge uses it for
    `/v1/federation/ingest/{peerId}` and `/v1/federation/bundle`.

    **Scope:** `admin`.

    Args:
        peer_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PeerIngestKey | Problem]
    """

    kwargs = _get_kwargs(
        peer_id=peer_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    peer_id: str,
    *,
    client: AuthenticatedClient,
) -> PeerIngestKey | Problem | None:
    """Issue or rotate a peer ingest key

     Issues a new `fsp_` peer ingest key (shown once) and revokes the previous one; the edge uses it for
    `/v1/federation/ingest/{peerId}` and `/v1/federation/bundle`.

    **Scope:** `admin`.

    Args:
        peer_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PeerIngestKey | Problem
    """

    return (
        await asyncio_detailed(
            peer_id=peer_id,
            client=client,
        )
    ).parsed
