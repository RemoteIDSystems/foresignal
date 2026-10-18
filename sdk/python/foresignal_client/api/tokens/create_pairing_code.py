from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.pairing_code_created import PairingCodeCreated
from ...models.pairing_code_input import PairingCodeInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: PairingCodeInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/pairing-codes",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PairingCodeCreated | Problem | None:
    if response.status_code == 201:
        response_201 = PairingCodeCreated.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[PairingCodeCreated | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: PairingCodeInput,
) -> Response[PairingCodeCreated | Problem]:
    """Create a pairing code

     Creates a single-use code (8 Crockford base32 characters, `XXXX-XXXX`, stored hashed) that a phone
    or desktop app claims for a `read` or `agent` token without ever seeing an admin token. Returns the
    code and `qrPayload` once. `admin` scope is refused (400).

    **Scope:** `admin`.

    Args:
        body (PairingCodeInput): Create a pairing code a device can claim for a token (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PairingCodeCreated | Problem]
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
    body: PairingCodeInput,
) -> PairingCodeCreated | Problem | None:
    """Create a pairing code

     Creates a single-use code (8 Crockford base32 characters, `XXXX-XXXX`, stored hashed) that a phone
    or desktop app claims for a `read` or `agent` token without ever seeing an admin token. Returns the
    code and `qrPayload` once. `admin` scope is refused (400).

    **Scope:** `admin`.

    Args:
        body (PairingCodeInput): Create a pairing code a device can claim for a token (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PairingCodeCreated | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: PairingCodeInput,
) -> Response[PairingCodeCreated | Problem]:
    """Create a pairing code

     Creates a single-use code (8 Crockford base32 characters, `XXXX-XXXX`, stored hashed) that a phone
    or desktop app claims for a `read` or `agent` token without ever seeing an admin token. Returns the
    code and `qrPayload` once. `admin` scope is refused (400).

    **Scope:** `admin`.

    Args:
        body (PairingCodeInput): Create a pairing code a device can claim for a token (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PairingCodeCreated | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: PairingCodeInput,
) -> PairingCodeCreated | Problem | None:
    """Create a pairing code

     Creates a single-use code (8 Crockford base32 characters, `XXXX-XXXX`, stored hashed) that a phone
    or desktop app claims for a `read` or `agent` token without ever seeing an admin token. Returns the
    code and `qrPayload` once. `admin` scope is refused (400).

    **Scope:** `admin`.

    Args:
        body (PairingCodeInput): Create a pairing code a device can claim for a token (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PairingCodeCreated | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
