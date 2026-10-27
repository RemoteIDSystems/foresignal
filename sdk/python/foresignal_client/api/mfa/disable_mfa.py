from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.mfa_disable_request import MfaDisableRequest
from ...models.mfa_status import MfaStatus
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: MfaDisableRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/me/mfa/disable",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MfaStatus | Problem | None:
    if response.status_code == 200:
        response_200 = MfaStatus.from_dict(response.json())

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
) -> Response[MfaStatus | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: MfaDisableRequest,
) -> Response[MfaStatus | Problem]:
    """Disable MFA

     Disables MFA after re-authenticating with a code or the account password. A role that requires MFA
    (Admin, Operator) refuses this (409 MFA_REQUIRED; an administrator reset is the way out); 409
    MFA_NOT_ENABLED with nothing to disable. A wrong code is 400 MFA_CODE_INVALID, a wrong password 400
    VALIDATION_FAILED; at most 10 code checks a minute per user (429). Any signed-in user (a session; an
    API token is 401), whatever their roles, so an Operator can enroll (R2-024).

    **Scope:** `read`.

    Args:
        body (MfaDisableRequest): Disable MFA, re-authenticating with a code or the account
            password

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MfaStatus | Problem]
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
    body: MfaDisableRequest,
) -> MfaStatus | Problem | None:
    """Disable MFA

     Disables MFA after re-authenticating with a code or the account password. A role that requires MFA
    (Admin, Operator) refuses this (409 MFA_REQUIRED; an administrator reset is the way out); 409
    MFA_NOT_ENABLED with nothing to disable. A wrong code is 400 MFA_CODE_INVALID, a wrong password 400
    VALIDATION_FAILED; at most 10 code checks a minute per user (429). Any signed-in user (a session; an
    API token is 401), whatever their roles, so an Operator can enroll (R2-024).

    **Scope:** `read`.

    Args:
        body (MfaDisableRequest): Disable MFA, re-authenticating with a code or the account
            password

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MfaStatus | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: MfaDisableRequest,
) -> Response[MfaStatus | Problem]:
    """Disable MFA

     Disables MFA after re-authenticating with a code or the account password. A role that requires MFA
    (Admin, Operator) refuses this (409 MFA_REQUIRED; an administrator reset is the way out); 409
    MFA_NOT_ENABLED with nothing to disable. A wrong code is 400 MFA_CODE_INVALID, a wrong password 400
    VALIDATION_FAILED; at most 10 code checks a minute per user (429). Any signed-in user (a session; an
    API token is 401), whatever their roles, so an Operator can enroll (R2-024).

    **Scope:** `read`.

    Args:
        body (MfaDisableRequest): Disable MFA, re-authenticating with a code or the account
            password

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MfaStatus | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: MfaDisableRequest,
) -> MfaStatus | Problem | None:
    """Disable MFA

     Disables MFA after re-authenticating with a code or the account password. A role that requires MFA
    (Admin, Operator) refuses this (409 MFA_REQUIRED; an administrator reset is the way out); 409
    MFA_NOT_ENABLED with nothing to disable. A wrong code is 400 MFA_CODE_INVALID, a wrong password 400
    VALIDATION_FAILED; at most 10 code checks a minute per user (429). Any signed-in user (a session; an
    API token is 401), whatever their roles, so an Operator can enroll (R2-024).

    **Scope:** `read`.

    Args:
        body (MfaDisableRequest): Disable MFA, re-authenticating with a code or the account
            password

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MfaStatus | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
