from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    pairing_code_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/pairing-codes/{pairing_code_id}/qr.svg".format(
            pairing_code_id=quote(str(pairing_code_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    pairing_code_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[Problem]:
    """QR code of a pairing payload

     SVG QR code (ZXing) encoding the code's `qrPayload`, for scanning with the app. Available while the
    code is unclaimed and unexpired, and until a restart (the code is stored hashed; its payload is held
    in memory only).

    **Scope:** `admin`.

    Args:
        pairing_code_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        pairing_code_id=pairing_code_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    pairing_code_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Problem | None:
    """QR code of a pairing payload

     SVG QR code (ZXing) encoding the code's `qrPayload`, for scanning with the app. Available while the
    code is unclaimed and unexpired, and until a restart (the code is stored hashed; its payload is held
    in memory only).

    **Scope:** `admin`.

    Args:
        pairing_code_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        pairing_code_id=pairing_code_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    pairing_code_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[Problem]:
    """QR code of a pairing payload

     SVG QR code (ZXing) encoding the code's `qrPayload`, for scanning with the app. Available while the
    code is unclaimed and unexpired, and until a restart (the code is stored hashed; its payload is held
    in memory only).

    **Scope:** `admin`.

    Args:
        pairing_code_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        pairing_code_id=pairing_code_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    pairing_code_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Problem | None:
    """QR code of a pairing payload

     SVG QR code (ZXing) encoding the code's `qrPayload`, for scanning with the app. Available while the
    code is unclaimed and unexpired, and until a restart (the code is stored hashed; its payload is held
    in memory only).

    **Scope:** `admin`.

    Args:
        pairing_code_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            pairing_code_id=pairing_code_id,
            client=client,
        )
    ).parsed
