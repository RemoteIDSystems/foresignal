from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.exchange_receipt import ExchangeReceipt
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    received_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/exchange/received/{received_id}/acknowledge".format(
            received_id=quote(str(received_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExchangeReceipt | Problem | None:
    if response.status_code == 200:
        response_200 = ExchangeReceipt.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

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
    received_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExchangeReceipt | Problem]:
    """Acknowledge a received package

     Records the caller's acknowledgement and sends a receipt to the sender when the peer channel exists.

    **Scope:** `read`.

    Args:
        received_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExchangeReceipt | Problem]
    """

    kwargs = _get_kwargs(
        received_id=received_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    received_id: str,
    *,
    client: AuthenticatedClient,
) -> ExchangeReceipt | Problem | None:
    """Acknowledge a received package

     Records the caller's acknowledgement and sends a receipt to the sender when the peer channel exists.

    **Scope:** `read`.

    Args:
        received_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExchangeReceipt | Problem
    """

    return sync_detailed(
        received_id=received_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    received_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExchangeReceipt | Problem]:
    """Acknowledge a received package

     Records the caller's acknowledgement and sends a receipt to the sender when the peer channel exists.

    **Scope:** `read`.

    Args:
        received_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExchangeReceipt | Problem]
    """

    kwargs = _get_kwargs(
        received_id=received_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    received_id: str,
    *,
    client: AuthenticatedClient,
) -> ExchangeReceipt | Problem | None:
    """Acknowledge a received package

     Records the caller's acknowledgement and sends a receipt to the sender when the peer channel exists.

    **Scope:** `read`.

    Args:
        received_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExchangeReceipt | Problem
    """

    return (
        await asyncio_detailed(
            received_id=received_id,
            client=client,
        )
    ).parsed
