from http import HTTPStatus
from io import BytesIO
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_tile_tms_empty import GetProductTileTmsEmpty
from ...models.get_product_tile_tms_foresignal_prefetch import (
    GetProductTileTmsForesignalPrefetch,
)
from ...models.get_product_tile_tms_format import GetProductTileTmsFormat
from ...models.get_product_tile_tms_on_expired import GetProductTileTmsOnExpired
from ...models.get_product_tile_tms_prefetch import GetProductTileTmsPrefetch
from ...models.get_product_tile_tms_tile_matrix_set_id import (
    GetProductTileTmsTileMatrixSetId,
)
from ...models.get_product_tile_tms_tile_size import GetProductTileTmsTileSize
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    product_id: str,
    frame_key: str,
    tile_matrix_set_id: GetProductTileTmsTileMatrixSetId,
    z: int,
    x: int,
    y: str,
    format_: GetProductTileTmsFormat,
    *,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    tile_size: GetProductTileTmsTileSize | Unset = GetProductTileTmsTileSize.VALUE_256,
    empty: GetProductTileTmsEmpty | Unset = UNSET,
    on_expired: GetProductTileTmsOnExpired | Unset = UNSET,
    prefetch: GetProductTileTmsPrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductTileTmsForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_prefetch, Unset):
        headers["Foresignal-Prefetch"] = str(foresignal_prefetch)

    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    params["style"] = style

    params["run"] = run

    json_tile_size: int | Unset = UNSET
    if not isinstance(tile_size, Unset):
        json_tile_size = tile_size.value

    params["tileSize"] = json_tile_size

    json_empty: str | Unset = UNSET
    if not isinstance(empty, Unset):
        json_empty = empty.value

    params["empty"] = json_empty

    json_on_expired: str | Unset = UNSET
    if not isinstance(on_expired, Unset):
        json_on_expired = on_expired.value

    params["onExpired"] = json_on_expired

    json_prefetch: int | Unset = UNSET
    if not isinstance(prefetch, Unset):
        json_prefetch = prefetch.value

    params["prefetch"] = json_prefetch

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/tiles/{frame_key}/{tile_matrix_set_id}/{z}/{x}/{y}.{format_}".format(
            product_id=quote(str(product_id), safe=""),
            frame_key=quote(str(frame_key), safe=""),
            tile_matrix_set_id=quote(str(tile_matrix_set_id), safe=""),
            z=quote(str(z), safe=""),
            x=quote(str(x), safe=""),
            y=quote(str(y), safe=""),
            format_=quote(str(format_), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | File | Problem | None:
    if response.status_code == 200:
        response_200 = File(payload=BytesIO(response.content))

        return response_200

    if response.status_code == 204:
        response_204 = cast(Any, None)
        return response_204

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 406:
        response_406 = Problem.from_dict(response.json())

        return response_406

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | File | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    product_id: str,
    frame_key: str,
    tile_matrix_set_id: GetProductTileTmsTileMatrixSetId,
    z: int,
    x: int,
    y: str,
    format_: GetProductTileTmsFormat,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    tile_size: GetProductTileTmsTileSize | Unset = GetProductTileTmsTileSize.VALUE_256,
    empty: GetProductTileTmsEmpty | Unset = UNSET,
    on_expired: GetProductTileTmsOnExpired | Unset = UNSET,
    prefetch: GetProductTileTmsPrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductTileTmsForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Product tile in a tile matrix set

     Renders a tile in WebMercatorQuad or WorldCRS84Quad (EPSG:4326, 2×1 tiles at z0) as PNG or WebP.
    Same caching as tiles: content-addressed URLs are immutable.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        tile_matrix_set_id (GetProductTileTmsTileMatrixSetId):
        z (int):
        x (int):
        y (str):
        format_ (GetProductTileTmsFormat):
        style (str | Unset):
        run (str | Unset):
        tile_size (GetProductTileTmsTileSize | Unset):  Default:
            GetProductTileTmsTileSize.VALUE_256.
        empty (GetProductTileTmsEmpty | Unset):
        on_expired (GetProductTileTmsOnExpired | Unset):
        prefetch (GetProductTileTmsPrefetch | Unset):
        foresignal_prefetch (GetProductTileTmsForesignalPrefetch | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        tile_matrix_set_id=tile_matrix_set_id,
        z=z,
        x=x,
        y=y,
        format_=format_,
        style=style,
        run=run,
        tile_size=tile_size,
        empty=empty,
        on_expired=on_expired,
        prefetch=prefetch,
        foresignal_prefetch=foresignal_prefetch,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    frame_key: str,
    tile_matrix_set_id: GetProductTileTmsTileMatrixSetId,
    z: int,
    x: int,
    y: str,
    format_: GetProductTileTmsFormat,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    tile_size: GetProductTileTmsTileSize | Unset = GetProductTileTmsTileSize.VALUE_256,
    empty: GetProductTileTmsEmpty | Unset = UNSET,
    on_expired: GetProductTileTmsOnExpired | Unset = UNSET,
    prefetch: GetProductTileTmsPrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductTileTmsForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Product tile in a tile matrix set

     Renders a tile in WebMercatorQuad or WorldCRS84Quad (EPSG:4326, 2×1 tiles at z0) as PNG or WebP.
    Same caching as tiles: content-addressed URLs are immutable.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        tile_matrix_set_id (GetProductTileTmsTileMatrixSetId):
        z (int):
        x (int):
        y (str):
        format_ (GetProductTileTmsFormat):
        style (str | Unset):
        run (str | Unset):
        tile_size (GetProductTileTmsTileSize | Unset):  Default:
            GetProductTileTmsTileSize.VALUE_256.
        empty (GetProductTileTmsEmpty | Unset):
        on_expired (GetProductTileTmsOnExpired | Unset):
        prefetch (GetProductTileTmsPrefetch | Unset):
        foresignal_prefetch (GetProductTileTmsForesignalPrefetch | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return sync_detailed(
        product_id=product_id,
        frame_key=frame_key,
        tile_matrix_set_id=tile_matrix_set_id,
        z=z,
        x=x,
        y=y,
        format_=format_,
        client=client,
        style=style,
        run=run,
        tile_size=tile_size,
        empty=empty,
        on_expired=on_expired,
        prefetch=prefetch,
        foresignal_prefetch=foresignal_prefetch,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    frame_key: str,
    tile_matrix_set_id: GetProductTileTmsTileMatrixSetId,
    z: int,
    x: int,
    y: str,
    format_: GetProductTileTmsFormat,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    tile_size: GetProductTileTmsTileSize | Unset = GetProductTileTmsTileSize.VALUE_256,
    empty: GetProductTileTmsEmpty | Unset = UNSET,
    on_expired: GetProductTileTmsOnExpired | Unset = UNSET,
    prefetch: GetProductTileTmsPrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductTileTmsForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Product tile in a tile matrix set

     Renders a tile in WebMercatorQuad or WorldCRS84Quad (EPSG:4326, 2×1 tiles at z0) as PNG or WebP.
    Same caching as tiles: content-addressed URLs are immutable.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        tile_matrix_set_id (GetProductTileTmsTileMatrixSetId):
        z (int):
        x (int):
        y (str):
        format_ (GetProductTileTmsFormat):
        style (str | Unset):
        run (str | Unset):
        tile_size (GetProductTileTmsTileSize | Unset):  Default:
            GetProductTileTmsTileSize.VALUE_256.
        empty (GetProductTileTmsEmpty | Unset):
        on_expired (GetProductTileTmsOnExpired | Unset):
        prefetch (GetProductTileTmsPrefetch | Unset):
        foresignal_prefetch (GetProductTileTmsForesignalPrefetch | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        tile_matrix_set_id=tile_matrix_set_id,
        z=z,
        x=x,
        y=y,
        format_=format_,
        style=style,
        run=run,
        tile_size=tile_size,
        empty=empty,
        on_expired=on_expired,
        prefetch=prefetch,
        foresignal_prefetch=foresignal_prefetch,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    frame_key: str,
    tile_matrix_set_id: GetProductTileTmsTileMatrixSetId,
    z: int,
    x: int,
    y: str,
    format_: GetProductTileTmsFormat,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    tile_size: GetProductTileTmsTileSize | Unset = GetProductTileTmsTileSize.VALUE_256,
    empty: GetProductTileTmsEmpty | Unset = UNSET,
    on_expired: GetProductTileTmsOnExpired | Unset = UNSET,
    prefetch: GetProductTileTmsPrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductTileTmsForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Product tile in a tile matrix set

     Renders a tile in WebMercatorQuad or WorldCRS84Quad (EPSG:4326, 2×1 tiles at z0) as PNG or WebP.
    Same caching as tiles: content-addressed URLs are immutable.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        tile_matrix_set_id (GetProductTileTmsTileMatrixSetId):
        z (int):
        x (int):
        y (str):
        format_ (GetProductTileTmsFormat):
        style (str | Unset):
        run (str | Unset):
        tile_size (GetProductTileTmsTileSize | Unset):  Default:
            GetProductTileTmsTileSize.VALUE_256.
        empty (GetProductTileTmsEmpty | Unset):
        on_expired (GetProductTileTmsOnExpired | Unset):
        prefetch (GetProductTileTmsPrefetch | Unset):
        foresignal_prefetch (GetProductTileTmsForesignalPrefetch | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            frame_key=frame_key,
            tile_matrix_set_id=tile_matrix_set_id,
            z=z,
            x=x,
            y=y,
            format_=format_,
            client=client,
            style=style,
            run=run,
            tile_size=tile_size,
            empty=empty,
            on_expired=on_expired,
            prefetch=prefetch,
            foresignal_prefetch=foresignal_prefetch,
            if_none_match=if_none_match,
        )
    ).parsed
