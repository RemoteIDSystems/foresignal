from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.password_reset_request import PasswordResetRequest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: PasswordResetRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/auth/password/reset-request",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 202:
        response_202 = cast(Any, None)
        return response_202

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
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: PasswordResetRequest,
) -> Response[Any | Problem]:
    """Request a password-reset email

     Sends a single-use, expiring reset link to the address when it belongs to an active account. Always
    returns 202 whether or not the address exists, so accounts cannot be enumerated; requires SMTP to be
    configured to actually deliver (170).

    **Scope:** `open`.

    Args:
        body (PasswordResetRequest): Ask for a password-reset link by email; the response is
            always 202 so an address cannot be probed (165)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
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
    body: PasswordResetRequest,
) -> Any | Problem | None:
    """Request a password-reset email

     Sends a single-use, expiring reset link to the address when it belongs to an active account. Always
    returns 202 whether or not the address exists, so accounts cannot be enumerated; requires SMTP to be
    configured to actually deliver (170).

    **Scope:** `open`.

    Args:
        body (PasswordResetRequest): Ask for a password-reset link by email; the response is
            always 202 so an address cannot be probed (165)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: PasswordResetRequest,
) -> Response[Any | Problem]:
    """Request a password-reset email

     Sends a single-use, expiring reset link to the address when it belongs to an active account. Always
    returns 202 whether or not the address exists, so accounts cannot be enumerated; requires SMTP to be
    configured to actually deliver (170).

    **Scope:** `open`.

    Args:
        body (PasswordResetRequest): Ask for a password-reset link by email; the response is
            always 202 so an address cannot be probed (165)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: PasswordResetRequest,
) -> Any | Problem | None:
    """Request a password-reset email

     Sends a single-use, expiring reset link to the address when it belongs to an active account. Always
    returns 202 whether or not the address exists, so accounts cannot be enumerated; requires SMTP to be
    configured to actually deliver (170).

    **Scope:** `open`.

    Args:
        body (PasswordResetRequest): Ask for a password-reset link by email; the response is
            always 202 so an address cannot be probed (165)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
