from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.api_token import ApiToken
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/me/tokens",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | list[ApiToken] | None:
    if response.status_code == 200:
        response_200 = []
        _response_200 = response.json()
        for response_200_item_data in _response_200:
            response_200_item = ApiToken.from_dict(response_200_item_data)

            response_200.append(response_200_item)

        return response_200

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
) -> Response[Problem | list[ApiToken]]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | list[ApiToken]]:
    """List my API tokens

     Lists the API tokens owned by the calling user (user-attributable machine tokens, D2/188). An admin
    manages all tokens via /v1/tokens. Any signed-in user; needs a session (an API token is 401).
    Revoked tokens are listed with `revokedAt`; a value is never returned (R2-025).

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[ApiToken]]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | list[ApiToken] | None:
    """List my API tokens

     Lists the API tokens owned by the calling user (user-attributable machine tokens, D2/188). An admin
    manages all tokens via /v1/tokens. Any signed-in user; needs a session (an API token is 401).
    Revoked tokens are listed with `revokedAt`; a value is never returned (R2-025).

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[ApiToken]
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | list[ApiToken]]:
    """List my API tokens

     Lists the API tokens owned by the calling user (user-attributable machine tokens, D2/188). An admin
    manages all tokens via /v1/tokens. Any signed-in user; needs a session (an API token is 401).
    Revoked tokens are listed with `revokedAt`; a value is never returned (R2-025).

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[ApiToken]]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | list[ApiToken] | None:
    """List my API tokens

     Lists the API tokens owned by the calling user (user-attributable machine tokens, D2/188). An admin
    manages all tokens via /v1/tokens. Any signed-in user; needs a session (an API token is 401).
    Revoked tokens are listed with `revokedAt`; a value is never returned (R2-025).

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[ApiToken]
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
