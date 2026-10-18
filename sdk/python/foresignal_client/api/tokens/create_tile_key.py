from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.tile_key_create import TileKeyCreate
from ...models.tile_key_created import TileKeyCreated
from ...types import Response


def _get_kwargs(
    *,
    body: TileKeyCreate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/tile-keys",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TileKeyCreated | None:
    if response.status_code == 201:
        response_201 = TileKeyCreated.from_dict(response.json())

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
) -> Response[Problem | TileKeyCreated]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: TileKeyCreate,
) -> Response[Problem | TileKeyCreated]:
    """Create a tile key

     Mints a read-only `fsk_…` key for header-less map clients, usable as `/v1/k/{key}/…`, limited to the
    allowed canonical prefixes and optional restrictions, with a TTL of at most 30 days. Returns the
    plaintext key and `urlBase` once. Tile keys never grant access to private sources, carry no private
    grant and are refused `restrictions.excludePrivate: false`, `endpoints` and `placeIds`; an unknown,
    expired or revoked key on `/v1/k/**` is 401 INVALID_TILE_KEY, a path outside its prefixes 403
    INSUFFICIENT_SCOPE, and a method other than GET/HEAD 405. Responses through the key follow the
    canonical path's cache rule (`public` only while `openReads` covers it and the key has no data
    restriction).

    **Scope:** `agent`.

    Args:
        body (TileKeyCreate): Tile key creation request

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TileKeyCreated]
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
    body: TileKeyCreate,
) -> Problem | TileKeyCreated | None:
    """Create a tile key

     Mints a read-only `fsk_…` key for header-less map clients, usable as `/v1/k/{key}/…`, limited to the
    allowed canonical prefixes and optional restrictions, with a TTL of at most 30 days. Returns the
    plaintext key and `urlBase` once. Tile keys never grant access to private sources, carry no private
    grant and are refused `restrictions.excludePrivate: false`, `endpoints` and `placeIds`; an unknown,
    expired or revoked key on `/v1/k/**` is 401 INVALID_TILE_KEY, a path outside its prefixes 403
    INSUFFICIENT_SCOPE, and a method other than GET/HEAD 405. Responses through the key follow the
    canonical path's cache rule (`public` only while `openReads` covers it and the key has no data
    restriction).

    **Scope:** `agent`.

    Args:
        body (TileKeyCreate): Tile key creation request

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TileKeyCreated
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: TileKeyCreate,
) -> Response[Problem | TileKeyCreated]:
    """Create a tile key

     Mints a read-only `fsk_…` key for header-less map clients, usable as `/v1/k/{key}/…`, limited to the
    allowed canonical prefixes and optional restrictions, with a TTL of at most 30 days. Returns the
    plaintext key and `urlBase` once. Tile keys never grant access to private sources, carry no private
    grant and are refused `restrictions.excludePrivate: false`, `endpoints` and `placeIds`; an unknown,
    expired or revoked key on `/v1/k/**` is 401 INVALID_TILE_KEY, a path outside its prefixes 403
    INSUFFICIENT_SCOPE, and a method other than GET/HEAD 405. Responses through the key follow the
    canonical path's cache rule (`public` only while `openReads` covers it and the key has no data
    restriction).

    **Scope:** `agent`.

    Args:
        body (TileKeyCreate): Tile key creation request

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TileKeyCreated]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: TileKeyCreate,
) -> Problem | TileKeyCreated | None:
    """Create a tile key

     Mints a read-only `fsk_…` key for header-less map clients, usable as `/v1/k/{key}/…`, limited to the
    allowed canonical prefixes and optional restrictions, with a TTL of at most 30 days. Returns the
    plaintext key and `urlBase` once. Tile keys never grant access to private sources, carry no private
    grant and are refused `restrictions.excludePrivate: false`, `endpoints` and `placeIds`; an unknown,
    expired or revoked key on `/v1/k/**` is 401 INVALID_TILE_KEY, a path outside its prefixes 403
    INSUFFICIENT_SCOPE, and a method other than GET/HEAD 405. Responses through the key follow the
    canonical path's cache rule (`public` only while `openReads` covers it and the key has no data
    restriction).

    **Scope:** `agent`.

    Args:
        body (TileKeyCreate): Tile key creation request

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TileKeyCreated
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
