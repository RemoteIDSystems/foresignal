from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.api_token import ApiToken
from ...models.problem import Problem
from ...models.update_token_body import UpdateTokenBody
from ...types import Response


def _get_kwargs(
    token_id: UUID,
    *,
    body: UpdateTokenBody,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/tokens/{token_id}".format(
            token_id=quote(str(token_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ApiToken | Problem | None:
    if response.status_code == 200:
        response_200 = ApiToken.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ApiToken | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    token_id: UUID,
    *,
    client: AuthenticatedClient,
    body: UpdateTokenBody,
) -> Response[ApiToken | Problem]:
    """Update an API token's licence policy

     Replaces the token's default licence policy (109): the floor every request made with the token
    applies, which a request may add to but never remove. `licencePolicy: null` or `[]` clears it. Other
    members are refused (400 VALIDATION_FAILED); the token value, scope and restrictions never change
    after creation. Path keys and revoked tokens are 404.

    **Scope:** `admin`.

    Args:
        token_id (UUID):
        body (UpdateTokenBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ApiToken | Problem]
    """

    kwargs = _get_kwargs(
        token_id=token_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    token_id: UUID,
    *,
    client: AuthenticatedClient,
    body: UpdateTokenBody,
) -> ApiToken | Problem | None:
    """Update an API token's licence policy

     Replaces the token's default licence policy (109): the floor every request made with the token
    applies, which a request may add to but never remove. `licencePolicy: null` or `[]` clears it. Other
    members are refused (400 VALIDATION_FAILED); the token value, scope and restrictions never change
    after creation. Path keys and revoked tokens are 404.

    **Scope:** `admin`.

    Args:
        token_id (UUID):
        body (UpdateTokenBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ApiToken | Problem
    """

    return sync_detailed(
        token_id=token_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    token_id: UUID,
    *,
    client: AuthenticatedClient,
    body: UpdateTokenBody,
) -> Response[ApiToken | Problem]:
    """Update an API token's licence policy

     Replaces the token's default licence policy (109): the floor every request made with the token
    applies, which a request may add to but never remove. `licencePolicy: null` or `[]` clears it. Other
    members are refused (400 VALIDATION_FAILED); the token value, scope and restrictions never change
    after creation. Path keys and revoked tokens are 404.

    **Scope:** `admin`.

    Args:
        token_id (UUID):
        body (UpdateTokenBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ApiToken | Problem]
    """

    kwargs = _get_kwargs(
        token_id=token_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    token_id: UUID,
    *,
    client: AuthenticatedClient,
    body: UpdateTokenBody,
) -> ApiToken | Problem | None:
    """Update an API token's licence policy

     Replaces the token's default licence policy (109): the floor every request made with the token
    applies, which a request may add to but never remove. `licencePolicy: null` or `[]` clears it. Other
    members are refused (400 VALIDATION_FAILED); the token value, scope and restrictions never change
    after creation. Path keys and revoked tokens are 404.

    **Scope:** `admin`.

    Args:
        token_id (UUID):
        body (UpdateTokenBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ApiToken | Problem
    """

    return (
        await asyncio_detailed(
            token_id=token_id,
            client=client,
            body=body,
        )
    ).parsed
