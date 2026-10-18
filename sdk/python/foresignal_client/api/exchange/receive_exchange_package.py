from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.exchange_receipt import ExchangeReceipt
from ...models.problem import Problem
from ...types import File, Response


def _get_kwargs(
    *,
    body: File,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/exchange/inbox",
    }

    _kwargs["content"] = body.payload
    headers["Content-Type"] = "application/vnd.foresignal.package+zip"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExchangeReceipt | Problem | None:
    if response.status_code == 202:
        response_202 = ExchangeReceipt.from_dict(response.json())

        return response_202

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if response.status_code == 422:
        response_422 = Problem.from_dict(response.json())

        return response_422

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExchangeReceipt | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: File,
) -> Response[ExchangeReceipt | Problem]:
    """Receive a package from a peer

     Peer delivery. Verifies the detached JWS against the sender's JWKS (fetched from the peer or
    pinned), content hashes and that recipient principals exist; enforces TLP locally (RED visible only
    to named recipients) and imports read-only as `remote:` objects that never enter local evidence or
    the ledger.

    **Scope:** `peer` (federation peer credential, 113).

    Args:
        body (File):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExchangeReceipt | Problem]
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
    body: File,
) -> ExchangeReceipt | Problem | None:
    """Receive a package from a peer

     Peer delivery. Verifies the detached JWS against the sender's JWKS (fetched from the peer or
    pinned), content hashes and that recipient principals exist; enforces TLP locally (RED visible only
    to named recipients) and imports read-only as `remote:` objects that never enter local evidence or
    the ledger.

    **Scope:** `peer` (federation peer credential, 113).

    Args:
        body (File):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExchangeReceipt | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: File,
) -> Response[ExchangeReceipt | Problem]:
    """Receive a package from a peer

     Peer delivery. Verifies the detached JWS against the sender's JWKS (fetched from the peer or
    pinned), content hashes and that recipient principals exist; enforces TLP locally (RED visible only
    to named recipients) and imports read-only as `remote:` objects that never enter local evidence or
    the ledger.

    **Scope:** `peer` (federation peer credential, 113).

    Args:
        body (File):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExchangeReceipt | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: File,
) -> ExchangeReceipt | Problem | None:
    """Receive a package from a peer

     Peer delivery. Verifies the detached JWS against the sender's JWKS (fetched from the peer or
    pinned), content hashes and that recipient principals exist; enforces TLP locally (RED visible only
    to named recipients) and imports read-only as `remote:` objects that never enter local evidence or
    the ledger.

    **Scope:** `peer` (federation peer credential, 113).

    Args:
        body (File):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExchangeReceipt | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
