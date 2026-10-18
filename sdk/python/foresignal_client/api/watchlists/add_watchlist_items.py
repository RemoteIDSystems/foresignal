from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.watchlist_import_result import WatchlistImportResult
from ...models.watchlist_items_input import WatchlistItemsInput
from ...types import Response


def _get_kwargs(
    watchlist_id: UUID,
    *,
    body: WatchlistItemsInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/watchlists/{watchlist_id}/items".format(
            watchlist_id=quote(str(watchlist_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | WatchlistImportResult | None:
    if response.status_code == 200:
        response_200 = WatchlistImportResult.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | WatchlistImportResult]:
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
    body: WatchlistItemsInput,
) -> Response[Problem | WatchlistImportResult]:
    """Add or update watchlist items

     Validates and normalises values per identity; edits take effect on each entity's next observation
    (no retroactive alerts). Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        body (WatchlistItemsInput): Items to add or update (036)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WatchlistImportResult]
    """

    kwargs = _get_kwargs(
        watchlist_id=watchlist_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    body: WatchlistItemsInput,
) -> Problem | WatchlistImportResult | None:
    """Add or update watchlist items

     Validates and normalises values per identity; edits take effect on each entity's next observation
    (no retroactive alerts). Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        body (WatchlistItemsInput): Items to add or update (036)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | WatchlistImportResult
    """

    return sync_detailed(
        watchlist_id=watchlist_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    body: WatchlistItemsInput,
) -> Response[Problem | WatchlistImportResult]:
    """Add or update watchlist items

     Validates and normalises values per identity; edits take effect on each entity's next observation
    (no retroactive alerts). Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        body (WatchlistItemsInput): Items to add or update (036)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WatchlistImportResult]
    """

    kwargs = _get_kwargs(
        watchlist_id=watchlist_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    body: WatchlistItemsInput,
) -> Problem | WatchlistImportResult | None:
    """Add or update watchlist items

     Validates and normalises values per identity; edits take effect on each entity's next observation
    (no retroactive alerts). Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        body (WatchlistItemsInput): Items to add or update (036)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | WatchlistImportResult
    """

    return (
        await asyncio_detailed(
            watchlist_id=watchlist_id,
            client=client,
            body=body,
        )
    ).parsed
