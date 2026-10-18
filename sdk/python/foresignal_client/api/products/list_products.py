from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_products_category import ListProductsCategory
from ...models.list_products_kind import ListProductsKind
from ...models.problem import Problem
from ...models.product_page import ProductPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    category: ListProductsCategory | Unset = UNSET,
    kind: ListProductsKind | Unset = UNSET,
    bbox: str | Unset = UNSET,
    source: str | Unset = UNSET,
    q: str | Unset = UNSET,
    compact: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    json_category: str | Unset = UNSET
    if not isinstance(category, Unset):
        json_category = category.value

    params["category"] = json_category

    json_kind: str | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind.value

    params["kind"] = json_kind

    params["bbox"] = bbox

    params["source"] = source

    params["q"] = q

    params["compact"] = compact

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | ProductPage | None:
    if response.status_code == 200:
        response_200 = ProductPage.from_dict(response.json())

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

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
) -> Response[Any | Problem | ProductPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    category: ListProductsCategory | Unset = UNSET,
    kind: ListProductsKind | Unset = UNSET,
    bbox: str | Unset = UNSET,
    source: str | Unset = UNSET,
    q: str | Unset = UNSET,
    compact: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | ProductPage]:
    """List raster, field and external products

     The product catalogue with coverage meaning, temporal and spatial facts, rendering defaults, legend
    links, structured attribution and current status. Supports `compact=true` and conditional GET
    (ETag); `kind=external` lists client-direct catalogue entries.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        category (ListProductsCategory | Unset):
        kind (ListProductsKind | Unset):
        bbox (str | Unset):
        source (str | Unset):
        q (str | Unset):
        compact (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | ProductPage]
    """

    kwargs = _get_kwargs(
        category=category,
        kind=kind,
        bbox=bbox,
        source=source,
        q=q,
        compact=compact,
        cursor=cursor,
        limit=limit,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    category: ListProductsCategory | Unset = UNSET,
    kind: ListProductsKind | Unset = UNSET,
    bbox: str | Unset = UNSET,
    source: str | Unset = UNSET,
    q: str | Unset = UNSET,
    compact: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | ProductPage | None:
    """List raster, field and external products

     The product catalogue with coverage meaning, temporal and spatial facts, rendering defaults, legend
    links, structured attribution and current status. Supports `compact=true` and conditional GET
    (ETag); `kind=external` lists client-direct catalogue entries.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        category (ListProductsCategory | Unset):
        kind (ListProductsKind | Unset):
        bbox (str | Unset):
        source (str | Unset):
        q (str | Unset):
        compact (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | ProductPage
    """

    return sync_detailed(
        client=client,
        category=category,
        kind=kind,
        bbox=bbox,
        source=source,
        q=q,
        compact=compact,
        cursor=cursor,
        limit=limit,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    category: ListProductsCategory | Unset = UNSET,
    kind: ListProductsKind | Unset = UNSET,
    bbox: str | Unset = UNSET,
    source: str | Unset = UNSET,
    q: str | Unset = UNSET,
    compact: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | ProductPage]:
    """List raster, field and external products

     The product catalogue with coverage meaning, temporal and spatial facts, rendering defaults, legend
    links, structured attribution and current status. Supports `compact=true` and conditional GET
    (ETag); `kind=external` lists client-direct catalogue entries.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        category (ListProductsCategory | Unset):
        kind (ListProductsKind | Unset):
        bbox (str | Unset):
        source (str | Unset):
        q (str | Unset):
        compact (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | ProductPage]
    """

    kwargs = _get_kwargs(
        category=category,
        kind=kind,
        bbox=bbox,
        source=source,
        q=q,
        compact=compact,
        cursor=cursor,
        limit=limit,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    category: ListProductsCategory | Unset = UNSET,
    kind: ListProductsKind | Unset = UNSET,
    bbox: str | Unset = UNSET,
    source: str | Unset = UNSET,
    q: str | Unset = UNSET,
    compact: bool | Unset = False,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | ProductPage | None:
    """List raster, field and external products

     The product catalogue with coverage meaning, temporal and spatial facts, rendering defaults, legend
    links, structured attribution and current status. Supports `compact=true` and conditional GET
    (ETag); `kind=external` lists client-direct catalogue entries.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        category (ListProductsCategory | Unset):
        kind (ListProductsKind | Unset):
        bbox (str | Unset):
        source (str | Unset):
        q (str | Unset):
        compact (bool | Unset):  Default: False.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | ProductPage
    """

    return (
        await asyncio_detailed(
            client=client,
            category=category,
            kind=kind,
            bbox=bbox,
            source=source,
            q=q,
            compact=compact,
            cursor=cursor,
            limit=limit,
            if_none_match=if_none_match,
        )
    ).parsed
