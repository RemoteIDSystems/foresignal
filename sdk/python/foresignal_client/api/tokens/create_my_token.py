from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.api_token_created import ApiTokenCreated
from ...models.api_token_input import ApiTokenInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ApiTokenInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/me/tokens",
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
    body: ApiTokenInput,
) -> Response[ApiTokenCreated | Problem]:
    """Mint an API token

     Mints an API token owned by the calling user, with the existing area/path/tile-key restrictions; its
    scope cannot exceed the caller's own permissions (D2/188). The secret is shown once. Any signed-in
    user; needs a session (an API token is 401), so a token never mints another (R2-025). The token is
    `createdVia: self` and owned by the caller's principal; `principalId` is refused (400). Its scope is
    at most `admin` for a user holding every permission, `agent` for one whose sessions are `agent` or
    `admin`, else `read`; `privateAccess` needs an admin-derived session; anything wider is 403
    TOKEN_EXCEEDS_USER. On every request the token is capped again by its owner's current roles,
    narrowed by the owner's data restriction, refused (401) while the owner may not sign in, and audited
    as the owner (R2-025).

    **Scope:** `read`.

    Args:
        body (ApiTokenInput):

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
    body: ApiTokenInput,
) -> ApiTokenCreated | Problem | None:
    """Mint an API token

     Mints an API token owned by the calling user, with the existing area/path/tile-key restrictions; its
    scope cannot exceed the caller's own permissions (D2/188). The secret is shown once. Any signed-in
    user; needs a session (an API token is 401), so a token never mints another (R2-025). The token is
    `createdVia: self` and owned by the caller's principal; `principalId` is refused (400). Its scope is
    at most `admin` for a user holding every permission, `agent` for one whose sessions are `agent` or
    `admin`, else `read`; `privateAccess` needs an admin-derived session; anything wider is 403
    TOKEN_EXCEEDS_USER. On every request the token is capped again by its owner's current roles,
    narrowed by the owner's data restriction, refused (401) while the owner may not sign in, and audited
    as the owner (R2-025).

    **Scope:** `read`.

    Args:
        body (ApiTokenInput):

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
    body: ApiTokenInput,
) -> Response[ApiTokenCreated | Problem]:
    """Mint an API token

     Mints an API token owned by the calling user, with the existing area/path/tile-key restrictions; its
    scope cannot exceed the caller's own permissions (D2/188). The secret is shown once. Any signed-in
    user; needs a session (an API token is 401), so a token never mints another (R2-025). The token is
    `createdVia: self` and owned by the caller's principal; `principalId` is refused (400). Its scope is
    at most `admin` for a user holding every permission, `agent` for one whose sessions are `agent` or
    `admin`, else `read`; `privateAccess` needs an admin-derived session; anything wider is 403
    TOKEN_EXCEEDS_USER. On every request the token is capped again by its owner's current roles,
    narrowed by the owner's data restriction, refused (401) while the owner may not sign in, and audited
    as the owner (R2-025).

    **Scope:** `read`.

    Args:
        body (ApiTokenInput):

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
    body: ApiTokenInput,
) -> ApiTokenCreated | Problem | None:
    """Mint an API token

     Mints an API token owned by the calling user, with the existing area/path/tile-key restrictions; its
    scope cannot exceed the caller's own permissions (D2/188). The secret is shown once. Any signed-in
    user; needs a session (an API token is 401), so a token never mints another (R2-025). The token is
    `createdVia: self` and owned by the caller's principal; `principalId` is refused (400). Its scope is
    at most `admin` for a user holding every permission, `agent` for one whose sessions are `agent` or
    `admin`, else `read`; `privateAccess` needs an admin-derived session; anything wider is 403
    TOKEN_EXCEEDS_USER. On every request the token is capped again by its owner's current roles,
    narrowed by the owner's data restriction, refused (401) while the owner may not sign in, and audited
    as the owner (R2-025).

    **Scope:** `read`.

    Args:
        body (ApiTokenInput):

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
