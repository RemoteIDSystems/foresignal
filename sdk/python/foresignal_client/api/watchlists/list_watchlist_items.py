from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.watchlist_item_page import WatchlistItemPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    watchlist_id: UUID,
    *,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["q"] = q

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/watchlists/{watchlist_id}/items".format(
            watchlist_id=quote(str(watchlist_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | WatchlistItemPage | None:
    if response.status_code == 200:
        response_200 = WatchlistItemPage.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | WatchlistItemPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | WatchlistItemPage]:
    """List watchlist items

     Keyset-paged by value.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WatchlistItemPage]
    """

    kwargs = _get_kwargs(
        watchlist_id=watchlist_id,
        q=q,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | WatchlistItemPage | None:
    """List watchlist items

     Keyset-paged by value.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | WatchlistItemPage
    """

    return sync_detailed(
        watchlist_id=watchlist_id,
        client=client,
        q=q,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | WatchlistItemPage]:
    """List watchlist items

     Keyset-paged by value.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WatchlistItemPage]
    """

    kwargs = _get_kwargs(
        watchlist_id=watchlist_id,
        q=q,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | WatchlistItemPage | None:
    """List watchlist items

     Keyset-paged by value.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | WatchlistItemPage
    """

    return (
        await asyncio_detailed(
            watchlist_id=watchlist_id,
            client=client,
            q=q,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
