from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.saved_search import SavedSearch
from ...models.saved_search_input import SavedSearchInput
from ...types import Response


def _get_kwargs(
    *,
    body: SavedSearchInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/saved-searches",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SavedSearch | None:
    if response.status_code == 201:
        response_201 = SavedSearch.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SavedSearch]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: SavedSearchInput,
) -> Response[Problem | SavedSearch]:
    """Create a saved search

     Creates a saved search owned by the caller; with `notify`, new matches reach the owner's inbox at
    most hourly.

    **Scope:** `read`.

    Args:
        body (SavedSearchInput): Create a saved search

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SavedSearch]
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
    body: SavedSearchInput,
) -> Problem | SavedSearch | None:
    """Create a saved search

     Creates a saved search owned by the caller; with `notify`, new matches reach the owner's inbox at
    most hourly.

    **Scope:** `read`.

    Args:
        body (SavedSearchInput): Create a saved search

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SavedSearch
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: SavedSearchInput,
) -> Response[Problem | SavedSearch]:
    """Create a saved search

     Creates a saved search owned by the caller; with `notify`, new matches reach the owner's inbox at
    most hourly.

    **Scope:** `read`.

    Args:
        body (SavedSearchInput): Create a saved search

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SavedSearch]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: SavedSearchInput,
) -> Problem | SavedSearch | None:
    """Create a saved search

     Creates a saved search owned by the caller; with `notify`, new matches reach the owner's inbox at
    most hourly.

    **Scope:** `read`.

    Args:
        body (SavedSearchInput): Create a saved search

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SavedSearch
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
