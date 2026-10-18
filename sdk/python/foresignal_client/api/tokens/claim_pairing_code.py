from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.api_token_created import ApiTokenCreated
from ...models.pairing_claim import PairingClaim
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: PairingClaim,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/pairing/claim",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ApiTokenCreated | Problem | None:
    if response.status_code == 201:
        response_201 = ApiTokenCreated.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ApiTokenCreated | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: PairingClaim,
) -> Response[ApiTokenCreated | Problem]:
    """Claim a pairing code for a token

     Exchanges a valid code plus device metadata for a new token (`createdVia=pairing`), returned once.
    Single use with constant-time comparison; limited to 5 attempts per minute per client IP and 50 per
    hour globally (then 429). Every failure is audited.

    **Scope:** open (no token; the credential is in the request).

    Args:
        body (PairingClaim): Claim a pairing code from a device (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ApiTokenCreated | Problem]
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
    body: PairingClaim,
) -> ApiTokenCreated | Problem | None:
    """Claim a pairing code for a token

     Exchanges a valid code plus device metadata for a new token (`createdVia=pairing`), returned once.
    Single use with constant-time comparison; limited to 5 attempts per minute per client IP and 50 per
    hour globally (then 429). Every failure is audited.

    **Scope:** open (no token; the credential is in the request).

    Args:
        body (PairingClaim): Claim a pairing code from a device (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ApiTokenCreated | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: PairingClaim,
) -> Response[ApiTokenCreated | Problem]:
    """Claim a pairing code for a token

     Exchanges a valid code plus device metadata for a new token (`createdVia=pairing`), returned once.
    Single use with constant-time comparison; limited to 5 attempts per minute per client IP and 50 per
    hour globally (then 429). Every failure is audited.

    **Scope:** open (no token; the credential is in the request).

    Args:
        body (PairingClaim): Claim a pairing code from a device (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ApiTokenCreated | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: PairingClaim,
) -> ApiTokenCreated | Problem | None:
    """Claim a pairing code for a token

     Exchanges a valid code plus device metadata for a new token (`createdVia=pairing`), returned once.
    Single use with constant-time comparison; limited to 5 attempts per minute per client IP and 50 per
    hour globally (then 429). Every failure is audited.

    **Scope:** open (no token; the credential is in the request).

    Args:
        body (PairingClaim): Claim a pairing code from a device (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ApiTokenCreated | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
