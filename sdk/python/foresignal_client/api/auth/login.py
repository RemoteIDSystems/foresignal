from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.login_request import LoginRequest
from ...models.problem import Problem
from ...models.session_token import SessionToken
from ...types import Response


def _get_kwargs(
    *,
    body: LoginRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/auth/login",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SessionToken | None:
    if response.status_code == 200:
        response_200 = SessionToken.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SessionToken]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: LoginRequest,
) -> Response[Problem | SessionToken]:
    """Log in with email and password

     Verifies the email + Argon2id password (and the MFA code when enrolled) and issues a session bearer
    (166). The session carries the user's effective permissions (the union of the user's roles). A
    suspended or disabled account is refused (403). Repeated failures from one address are rate-limited
    (429). When MFA is enrolled but no `mfaCode` is given, the response reports `mfaRequired` and issues
    no usable session (`token` empty, `expiresAt` null); a wrong `mfaCode` (TOTP or recovery code) is
    the same 401 INVALID_CREDENTIALS (R2-024). A session of a user holding the Admin or Operator role
    who has not enrolled MFA may read, enroll (enrollMfa, verifyMfa), set preferences, sign out and
    revoke its own sessions; every other write is 403 MFA_ENROLLMENT_REQUIRED until verifyMfa activates
    MFA (R2-024).

    **Scope:** `open`.

    Args:
        body (LoginRequest): Email + password credentials, with an optional MFA code (D1)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SessionToken]
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
    body: LoginRequest,
) -> Problem | SessionToken | None:
    """Log in with email and password

     Verifies the email + Argon2id password (and the MFA code when enrolled) and issues a session bearer
    (166). The session carries the user's effective permissions (the union of the user's roles). A
    suspended or disabled account is refused (403). Repeated failures from one address are rate-limited
    (429). When MFA is enrolled but no `mfaCode` is given, the response reports `mfaRequired` and issues
    no usable session (`token` empty, `expiresAt` null); a wrong `mfaCode` (TOTP or recovery code) is
    the same 401 INVALID_CREDENTIALS (R2-024). A session of a user holding the Admin or Operator role
    who has not enrolled MFA may read, enroll (enrollMfa, verifyMfa), set preferences, sign out and
    revoke its own sessions; every other write is 403 MFA_ENROLLMENT_REQUIRED until verifyMfa activates
    MFA (R2-024).

    **Scope:** `open`.

    Args:
        body (LoginRequest): Email + password credentials, with an optional MFA code (D1)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SessionToken
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: LoginRequest,
) -> Response[Problem | SessionToken]:
    """Log in with email and password

     Verifies the email + Argon2id password (and the MFA code when enrolled) and issues a session bearer
    (166). The session carries the user's effective permissions (the union of the user's roles). A
    suspended or disabled account is refused (403). Repeated failures from one address are rate-limited
    (429). When MFA is enrolled but no `mfaCode` is given, the response reports `mfaRequired` and issues
    no usable session (`token` empty, `expiresAt` null); a wrong `mfaCode` (TOTP or recovery code) is
    the same 401 INVALID_CREDENTIALS (R2-024). A session of a user holding the Admin or Operator role
    who has not enrolled MFA may read, enroll (enrollMfa, verifyMfa), set preferences, sign out and
    revoke its own sessions; every other write is 403 MFA_ENROLLMENT_REQUIRED until verifyMfa activates
    MFA (R2-024).

    **Scope:** `open`.

    Args:
        body (LoginRequest): Email + password credentials, with an optional MFA code (D1)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SessionToken]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: LoginRequest,
) -> Problem | SessionToken | None:
    """Log in with email and password

     Verifies the email + Argon2id password (and the MFA code when enrolled) and issues a session bearer
    (166). The session carries the user's effective permissions (the union of the user's roles). A
    suspended or disabled account is refused (403). Repeated failures from one address are rate-limited
    (429). When MFA is enrolled but no `mfaCode` is given, the response reports `mfaRequired` and issues
    no usable session (`token` empty, `expiresAt` null); a wrong `mfaCode` (TOTP or recovery code) is
    the same 401 INVALID_CREDENTIALS (R2-024). A session of a user holding the Admin or Operator role
    who has not enrolled MFA may read, enroll (enrollMfa, verifyMfa), set preferences, sign out and
    revoke its own sessions; every other write is 403 MFA_ENROLLMENT_REQUIRED until verifyMfa activates
    MFA (R2-024).

    **Scope:** `open`.

    Args:
        body (LoginRequest): Email + password credentials, with an optional MFA code (D1)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SessionToken
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
