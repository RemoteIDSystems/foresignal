from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.mfa_recovery_codes import MfaRecoveryCodes
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/me/mfa/recovery-codes",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MfaRecoveryCodes | Problem | None:
    if response.status_code == 200:
        response_200 = MfaRecoveryCodes.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[MfaRecoveryCodes | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[MfaRecoveryCodes | Problem]:
    """Generate MFA recovery codes

     Generates a fresh set of single-use recovery codes, invalidating any previous set; shown once. Each
    is accepted once wherever a TOTP code is (login, verifyMfa, disableMfa). Requires MFA to be enabled
    (409 MFA_NOT_ENABLED). Any signed-in user (a session; an API token is 401), whatever their roles, so
    an Operator can enroll (R2-024).

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MfaRecoveryCodes | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> MfaRecoveryCodes | Problem | None:
    """Generate MFA recovery codes

     Generates a fresh set of single-use recovery codes, invalidating any previous set; shown once. Each
    is accepted once wherever a TOTP code is (login, verifyMfa, disableMfa). Requires MFA to be enabled
    (409 MFA_NOT_ENABLED). Any signed-in user (a session; an API token is 401), whatever their roles, so
    an Operator can enroll (R2-024).

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MfaRecoveryCodes | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[MfaRecoveryCodes | Problem]:
    """Generate MFA recovery codes

     Generates a fresh set of single-use recovery codes, invalidating any previous set; shown once. Each
    is accepted once wherever a TOTP code is (login, verifyMfa, disableMfa). Requires MFA to be enabled
    (409 MFA_NOT_ENABLED). Any signed-in user (a session; an API token is 401), whatever their roles, so
    an Operator can enroll (R2-024).

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MfaRecoveryCodes | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> MfaRecoveryCodes | Problem | None:
    """Generate MFA recovery codes

     Generates a fresh set of single-use recovery codes, invalidating any previous set; shown once. Each
    is accepted once wherever a TOTP code is (login, verifyMfa, disableMfa). Requires MFA to be enabled
    (409 MFA_NOT_ENABLED). Any signed-in user (a session; an API token is 401), whatever their roles, so
    an Operator can enroll (R2-024).

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MfaRecoveryCodes | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
