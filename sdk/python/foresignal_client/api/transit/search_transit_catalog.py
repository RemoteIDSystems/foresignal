from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.search_transit_catalog_entity_type import SearchTransitCatalogEntityType
from ...models.transit_catalog_page import TransitCatalogPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    country: str | Unset = UNSET,
    q: str | Unset = UNSET,
    entity_type: SearchTransitCatalogEntityType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["country"] = country

    params["q"] = q

    json_entity_type: str | Unset = UNSET
    if not isinstance(entity_type, Unset):
        json_entity_type = entity_type.value

    params["entityType"] = json_entity_type

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/transit/catalog",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TransitCatalogPage | None:
    if response.status_code == 200:
        response_200 = TransitCatalogPage.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | TransitCatalogPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    country: str | Unset = UNSET,
    q: str | Unset = UNSET,
    entity_type: SearchTransitCatalogEntityType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | TransitCatalogPage]:
    """Search the Mobility Database GTFS-RT snapshot

     Read-only search of the vendored CC0 MobilityData snapshot (keyless, active, https GTFS-RT feeds)
    with whether each feed is already enabled. Feeds are enabled only through the bootstrap property
    `foresignal.transit.catalog-feeds` and a restart; URLs are never accepted at runtime, so there is no
    POST. Rows are ordered by id; `entityType` matches a row that serves that entity type among others
    (`tu|vp` matches `vp`). An unknown cursor is `400 INVALID_CURSOR`.

    **Scope:** `admin`.

    Args:
        country (str | Unset):
        q (str | Unset):
        entity_type (SearchTransitCatalogEntityType | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TransitCatalogPage]
    """

    kwargs = _get_kwargs(
        country=country,
        q=q,
        entity_type=entity_type,
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
    country: str | Unset = UNSET,
    q: str | Unset = UNSET,
    entity_type: SearchTransitCatalogEntityType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | TransitCatalogPage | None:
    """Search the Mobility Database GTFS-RT snapshot

     Read-only search of the vendored CC0 MobilityData snapshot (keyless, active, https GTFS-RT feeds)
    with whether each feed is already enabled. Feeds are enabled only through the bootstrap property
    `foresignal.transit.catalog-feeds` and a restart; URLs are never accepted at runtime, so there is no
    POST. Rows are ordered by id; `entityType` matches a row that serves that entity type among others
    (`tu|vp` matches `vp`). An unknown cursor is `400 INVALID_CURSOR`.

    **Scope:** `admin`.

    Args:
        country (str | Unset):
        q (str | Unset):
        entity_type (SearchTransitCatalogEntityType | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TransitCatalogPage
    """

    return sync_detailed(
        client=client,
        country=country,
        q=q,
        entity_type=entity_type,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    country: str | Unset = UNSET,
    q: str | Unset = UNSET,
    entity_type: SearchTransitCatalogEntityType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | TransitCatalogPage]:
    """Search the Mobility Database GTFS-RT snapshot

     Read-only search of the vendored CC0 MobilityData snapshot (keyless, active, https GTFS-RT feeds)
    with whether each feed is already enabled. Feeds are enabled only through the bootstrap property
    `foresignal.transit.catalog-feeds` and a restart; URLs are never accepted at runtime, so there is no
    POST. Rows are ordered by id; `entityType` matches a row that serves that entity type among others
    (`tu|vp` matches `vp`). An unknown cursor is `400 INVALID_CURSOR`.

    **Scope:** `admin`.

    Args:
        country (str | Unset):
        q (str | Unset):
        entity_type (SearchTransitCatalogEntityType | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TransitCatalogPage]
    """

    kwargs = _get_kwargs(
        country=country,
        q=q,
        entity_type=entity_type,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    country: str | Unset = UNSET,
    q: str | Unset = UNSET,
    entity_type: SearchTransitCatalogEntityType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | TransitCatalogPage | None:
    """Search the Mobility Database GTFS-RT snapshot

     Read-only search of the vendored CC0 MobilityData snapshot (keyless, active, https GTFS-RT feeds)
    with whether each feed is already enabled. Feeds are enabled only through the bootstrap property
    `foresignal.transit.catalog-feeds` and a restart; URLs are never accepted at runtime, so there is no
    POST. Rows are ordered by id; `entityType` matches a row that serves that entity type among others
    (`tu|vp` matches `vp`). An unknown cursor is `400 INVALID_CURSOR`.

    **Scope:** `admin`.

    Args:
        country (str | Unset):
        q (str | Unset):
        entity_type (SearchTransitCatalogEntityType | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TransitCatalogPage
    """

    return (
        await asyncio_detailed(
            client=client,
            country=country,
            q=q,
            entity_type=entity_type,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
