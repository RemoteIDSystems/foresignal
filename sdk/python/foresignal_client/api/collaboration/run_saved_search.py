from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.saved_search_result_page import SavedSearchResultPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    saved_search_id: str,
    *,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/saved-searches/{saved_search_id}/results".format(
            saved_search_id=quote(str(saved_search_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SavedSearchResultPage | None:
    if response.status_code == 200:
        response_200 = SavedSearchResultPage.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

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
) -> Response[Problem | SavedSearchResultPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    saved_search_id: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SavedSearchResultPage]:
    """Run a saved search

     Runs the saved search now (027 hybrid search plus structured filters over envelopes) and returns
    envelopes the caller can see.

    **Scope:** `read`.

    Args:
        saved_search_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SavedSearchResultPage]
    """

    kwargs = _get_kwargs(
        saved_search_id=saved_search_id,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    saved_search_id: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SavedSearchResultPage | None:
    """Run a saved search

     Runs the saved search now (027 hybrid search plus structured filters over envelopes) and returns
    envelopes the caller can see.

    **Scope:** `read`.

    Args:
        saved_search_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SavedSearchResultPage
    """

    return sync_detailed(
        saved_search_id=saved_search_id,
        client=client,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    saved_search_id: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SavedSearchResultPage]:
    """Run a saved search

     Runs the saved search now (027 hybrid search plus structured filters over envelopes) and returns
    envelopes the caller can see.

    **Scope:** `read`.

    Args:
        saved_search_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SavedSearchResultPage]
    """

    kwargs = _get_kwargs(
        saved_search_id=saved_search_id,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    saved_search_id: str,
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SavedSearchResultPage | None:
    """Run a saved search

     Runs the saved search now (027 hybrid search plus structured filters over envelopes) and returns
    envelopes the caller can see.

    **Scope:** `read`.

    Args:
        saved_search_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SavedSearchResultPage
    """

    return (
        await asyncio_detailed(
            saved_search_id=saved_search_id,
            client=client,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
