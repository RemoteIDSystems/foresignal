from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.import_watchlist_mode import ImportWatchlistMode
from ...models.problem import Problem
from ...models.watchlist_import_result import WatchlistImportResult
from ...models.watchlist_item_input import WatchlistItemInput
from ...types import UNSET, Response, Unset


def _get_kwargs(
    watchlist_id: UUID,
    *,
    body: list[WatchlistItemInput],
    mode: ImportWatchlistMode | Unset = ImportWatchlistMode.MERGE,
    dry_run: bool | Unset = False,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    params: dict[str, Any] = {}

    json_mode: str | Unset = UNSET
    if not isinstance(mode, Unset):
        json_mode = mode.value

    params["mode"] = json_mode

    params["dryRun"] = dry_run

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/watchlists/{watchlist_id}/import".format(
            watchlist_id=quote(str(watchlist_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["json"] = []
    for body_item_data in body:
        body_item = body_item_data.to_dict()
        _kwargs["json"].append(body_item)

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

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if response.status_code == 415:
        response_415 = Problem.from_dict(response.json())

        return response_415

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
    body: list[WatchlistItemInput],
    mode: ImportWatchlistMode | Unset = ImportWatchlistMode.MERGE,
    dry_run: bool | Unset = False,
) -> Response[Problem | WatchlistImportResult]:
    """Import watchlist items from CSV or JSON

     `text/csv` with header `value[,label,notes,tags]` or a JSON array of items; `mode=merge|replace`;
    `dryRun=true` reports adds, updates, removals and invalid rows without changing anything. ≤ 5 MB; ≤
    100,000 items per list. Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        mode (ImportWatchlistMode | Unset):  Default: ImportWatchlistMode.MERGE.
        dry_run (bool | Unset):  Default: False.
        body (list[WatchlistItemInput]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WatchlistImportResult]
    """

    kwargs = _get_kwargs(
        watchlist_id=watchlist_id,
        body=body,
        mode=mode,
        dry_run=dry_run,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    body: list[WatchlistItemInput],
    mode: ImportWatchlistMode | Unset = ImportWatchlistMode.MERGE,
    dry_run: bool | Unset = False,
) -> Problem | WatchlistImportResult | None:
    """Import watchlist items from CSV or JSON

     `text/csv` with header `value[,label,notes,tags]` or a JSON array of items; `mode=merge|replace`;
    `dryRun=true` reports adds, updates, removals and invalid rows without changing anything. ≤ 5 MB; ≤
    100,000 items per list. Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        mode (ImportWatchlistMode | Unset):  Default: ImportWatchlistMode.MERGE.
        dry_run (bool | Unset):  Default: False.
        body (list[WatchlistItemInput]):

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
        mode=mode,
        dry_run=dry_run,
    ).parsed


async def asyncio_detailed(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    body: list[WatchlistItemInput],
    mode: ImportWatchlistMode | Unset = ImportWatchlistMode.MERGE,
    dry_run: bool | Unset = False,
) -> Response[Problem | WatchlistImportResult]:
    """Import watchlist items from CSV or JSON

     `text/csv` with header `value[,label,notes,tags]` or a JSON array of items; `mode=merge|replace`;
    `dryRun=true` reports adds, updates, removals and invalid rows without changing anything. ≤ 5 MB; ≤
    100,000 items per list. Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        mode (ImportWatchlistMode | Unset):  Default: ImportWatchlistMode.MERGE.
        dry_run (bool | Unset):  Default: False.
        body (list[WatchlistItemInput]):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WatchlistImportResult]
    """

    kwargs = _get_kwargs(
        watchlist_id=watchlist_id,
        body=body,
        mode=mode,
        dry_run=dry_run,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    watchlist_id: UUID,
    *,
    client: AuthenticatedClient,
    body: list[WatchlistItemInput],
    mode: ImportWatchlistMode | Unset = ImportWatchlistMode.MERGE,
    dry_run: bool | Unset = False,
) -> Problem | WatchlistImportResult | None:
    """Import watchlist items from CSV or JSON

     `text/csv` with header `value[,label,notes,tags]` or a JSON array of items; `mode=merge|replace`;
    `dryRun=true` reports adds, updates, removals and invalid rows without changing anything. ≤ 5 MB; ≤
    100,000 items per list. Every edit is audited.

    **Scope:** `agent`.

    Args:
        watchlist_id (UUID):
        mode (ImportWatchlistMode | Unset):  Default: ImportWatchlistMode.MERGE.
        dry_run (bool | Unset):  Default: False.
        body (list[WatchlistItemInput]):

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
            mode=mode,
            dry_run=dry_run,
        )
    ).parsed
