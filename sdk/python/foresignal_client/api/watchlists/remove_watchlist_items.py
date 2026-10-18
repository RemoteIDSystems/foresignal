from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.watchlist_import_result import WatchlistImportResult
from ...types import UNSET, Response


def _get_kwargs(
    watchlist_id: UUID,
    *,
    value: list[str],
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_value = value

    params["value"] = json_value

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/watchlists/{watchlist_id}/items".format(
            watchlist_id=quote(str(watchlist_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | WatchlistImportResult | None:
    if response.status_code == 200:
        response_200 = WatchlistImportResult.from_dict(response.json())

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
    value: list[str],
) -> Response[Problem | WatchlistImportResult]:
    """Remove watchlist items

     Removes the given values. Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        value (list[str]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WatchlistImportResult]
    """

    kwargs = _get_kwargs(
        watchlist_id=watchlist_id,
        value=value,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    value: list[str],
) -> Problem | WatchlistImportResult | None:
    """Remove watchlist items

     Removes the given values. Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        value (list[str]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | WatchlistImportResult
    """

    return sync_detailed(
        watchlist_id=watchlist_id,
        client=client,
        value=value,
    ).parsed


async def asyncio_detailed(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    value: list[str],
) -> Response[Problem | WatchlistImportResult]:
    """Remove watchlist items

     Removes the given values. Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        value (list[str]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WatchlistImportResult]
    """

    kwargs = _get_kwargs(
        watchlist_id=watchlist_id,
        value=value,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    value: list[str],
) -> Problem | WatchlistImportResult | None:
    """Remove watchlist items

     Removes the given values. Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        value (list[str]):

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
            value=value,
        )
    ).parsed
