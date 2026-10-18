from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.peer_test_result import PeerTestResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    peer_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/peers/{peer_id}/test".format(
            peer_id=quote(str(peer_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PeerTestResult | Problem | None:
    if response.status_code == 200:
        response_200 = PeerTestResult.from_dict(response.json())

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
) -> Response[PeerTestResult | Problem]:
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
) -> Response[PeerTestResult | Problem]:
    """Test a peer connection

     Fetches the peer's `/v1/federation/info`, verifies its JWS against the remote JWKS and the pinned
    kid, and reports.

    **Scope:** `admin`.

    Args:
        peer_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PeerTestResult | Problem]
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
) -> PeerTestResult | Problem | None:
    """Test a peer connection

     Fetches the peer's `/v1/federation/info`, verifies its JWS against the remote JWKS and the pinned
    kid, and reports.

    **Scope:** `admin`.

    Args:
        peer_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PeerTestResult | Problem
    """

    return sync_detailed(
        peer_id=peer_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    peer_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[PeerTestResult | Problem]:
    """Test a peer connection

     Fetches the peer's `/v1/federation/info`, verifies its JWS against the remote JWKS and the pinned
    kid, and reports.

    **Scope:** `admin`.

    Args:
        peer_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PeerTestResult | Problem]
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
) -> PeerTestResult | Problem | None:
    """Test a peer connection

     Fetches the peer's `/v1/federation/info`, verifies its JWS against the remote JWKS and the pinned
    kid, and reports.

    **Scope:** `admin`.

    Args:
        peer_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PeerTestResult | Problem
    """

    return (
        await asyncio_detailed(
            peer_id=peer_id,
            client=client,
        )
    ).parsed
