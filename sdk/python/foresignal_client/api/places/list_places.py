from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_places_kind import ListPlacesKind
from ...models.place_page import PlacePage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    kind: ListPlacesKind | Unset = UNSET,
    tag: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_kind: str | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind.value

    params["kind"] = json_kind

    params["tag"] = tag

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/places",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PlacePage | Problem | None:
    if response.status_code == 200:
        response_200 = PlacePage.from_dict(response.json())

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
) -> Response[PlacePage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    kind: ListPlacesKind | Unset = UNSET,
    tag: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[PlacePage | Problem]:
    """List places

     Filter by `kind` and `tag`. Places are sensitive: reads need a token even when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        kind (ListPlacesKind | Unset):
        tag (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PlacePage | Problem]
    """

    kwargs = _get_kwargs(
        kind=kind,
        tag=tag,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    kind: ListPlacesKind | Unset = UNSET,
    tag: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> PlacePage | Problem | None:
    """List places

     Filter by `kind` and `tag`. Places are sensitive: reads need a token even when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        kind (ListPlacesKind | Unset):
        tag (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PlacePage | Problem
    """

    return sync_detailed(
        client=client,
        kind=kind,
        tag=tag,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    kind: ListPlacesKind | Unset = UNSET,
    tag: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[PlacePage | Problem]:
    """List places

     Filter by `kind` and `tag`. Places are sensitive: reads need a token even when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        kind (ListPlacesKind | Unset):
        tag (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PlacePage | Problem]
    """

    kwargs = _get_kwargs(
        kind=kind,
        tag=tag,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    kind: ListPlacesKind | Unset = UNSET,
    tag: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> PlacePage | Problem | None:
    """List places

     Filter by `kind` and `tag`. Places are sensitive: reads need a token even when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        kind (ListPlacesKind | Unset):
        tag (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PlacePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            kind=kind,
            tag=tag,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
