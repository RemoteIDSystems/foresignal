from http import HTTPStatus
from io import BytesIO
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_vector_tile_foresignal_prefetch import (
    GetProductVectorTileForesignalPrefetch,
)
from ...models.get_product_vector_tile_prefetch import GetProductVectorTilePrefetch
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    product_id: str,
    frame_key: str,
    z: int,
    x: int,
    y: int,
    *,
    run: str | Unset = UNSET,
    prefetch: GetProductVectorTilePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductVectorTileForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_prefetch, Unset):
        headers["Foresignal-Prefetch"] = str(foresignal_prefetch)

    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    params["run"] = run

    json_prefetch: int | Unset = UNSET
    if not isinstance(prefetch, Unset):
        json_prefetch = prefetch.value

    params["prefetch"] = json_prefetch

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/vectors/{frame_key}/{z}/{x}/{y}.mvt".format(
            product_id=quote(str(product_id), safe=""),
            frame_key=quote(str(frame_key), safe=""),
            z=quote(str(z), safe=""),
            x=quote(str(x), safe=""),
            y=quote(str(y), safe=""),
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

    if response.status_code == 301:
        response_301 = cast(Any, None)
        return response_301

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

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
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    prefetch: GetProductVectorTilePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductVectorTileForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Get a wind barb vector tile

     Mapbox Vector Tile (layer `wind`, extent 4096) of wind points for the u/v products (`gfs`, `hrrr`):
    the frame sampled bilinearly on a 32 px lattice inside the 256 px tile (8×8 points at pixel centers,
    so the on-screen density is the same at every zoom), each with `speedMs` (m/s, 0.1), `fromDeg` (the
    direction the wind blows from, `(270 − atan2(v,u)·180/π) mod 360`, integer) and `barbKt` (knots
    rounded to a 5-kt bucket). HRRR's grid-relative winds are rotated to earth-relative at ingest. Built
    by PostGIS `ST_AsMVT`; an empty tile is an empty body. Cached and render-queued like raster tiles,
    with the same cache rule; zooms above the native max zoom are 400 TILE_OUT_OF_RANGE (clients
    overzoom).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        z (int):
        x (int):
        y (int):
        run (str | Unset):  Example: latest.
        prefetch (GetProductVectorTilePrefetch | Unset):
        foresignal_prefetch (GetProductVectorTileForesignalPrefetch | Unset):
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
        z=z,
        x=x,
        y=y,
        run=run,
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
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    prefetch: GetProductVectorTilePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductVectorTileForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Get a wind barb vector tile

     Mapbox Vector Tile (layer `wind`, extent 4096) of wind points for the u/v products (`gfs`, `hrrr`):
    the frame sampled bilinearly on a 32 px lattice inside the 256 px tile (8×8 points at pixel centers,
    so the on-screen density is the same at every zoom), each with `speedMs` (m/s, 0.1), `fromDeg` (the
    direction the wind blows from, `(270 − atan2(v,u)·180/π) mod 360`, integer) and `barbKt` (knots
    rounded to a 5-kt bucket). HRRR's grid-relative winds are rotated to earth-relative at ingest. Built
    by PostGIS `ST_AsMVT`; an empty tile is an empty body. Cached and render-queued like raster tiles,
    with the same cache rule; zooms above the native max zoom are 400 TILE_OUT_OF_RANGE (clients
    overzoom).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        z (int):
        x (int):
        y (int):
        run (str | Unset):  Example: latest.
        prefetch (GetProductVectorTilePrefetch | Unset):
        foresignal_prefetch (GetProductVectorTileForesignalPrefetch | Unset):
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
        z=z,
        x=x,
        y=y,
        client=client,
        run=run,
        prefetch=prefetch,
        foresignal_prefetch=foresignal_prefetch,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    frame_key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    prefetch: GetProductVectorTilePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductVectorTileForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Get a wind barb vector tile

     Mapbox Vector Tile (layer `wind`, extent 4096) of wind points for the u/v products (`gfs`, `hrrr`):
    the frame sampled bilinearly on a 32 px lattice inside the 256 px tile (8×8 points at pixel centers,
    so the on-screen density is the same at every zoom), each with `speedMs` (m/s, 0.1), `fromDeg` (the
    direction the wind blows from, `(270 − atan2(v,u)·180/π) mod 360`, integer) and `barbKt` (knots
    rounded to a 5-kt bucket). HRRR's grid-relative winds are rotated to earth-relative at ingest. Built
    by PostGIS `ST_AsMVT`; an empty tile is an empty body. Cached and render-queued like raster tiles,
    with the same cache rule; zooms above the native max zoom are 400 TILE_OUT_OF_RANGE (clients
    overzoom).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        z (int):
        x (int):
        y (int):
        run (str | Unset):  Example: latest.
        prefetch (GetProductVectorTilePrefetch | Unset):
        foresignal_prefetch (GetProductVectorTileForesignalPrefetch | Unset):
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
        z=z,
        x=x,
        y=y,
        run=run,
        prefetch=prefetch,
        foresignal_prefetch=foresignal_prefetch,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    frame_key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    prefetch: GetProductVectorTilePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductVectorTileForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Get a wind barb vector tile

     Mapbox Vector Tile (layer `wind`, extent 4096) of wind points for the u/v products (`gfs`, `hrrr`):
    the frame sampled bilinearly on a 32 px lattice inside the 256 px tile (8×8 points at pixel centers,
    so the on-screen density is the same at every zoom), each with `speedMs` (m/s, 0.1), `fromDeg` (the
    direction the wind blows from, `(270 − atan2(v,u)·180/π) mod 360`, integer) and `barbKt` (knots
    rounded to a 5-kt bucket). HRRR's grid-relative winds are rotated to earth-relative at ingest. Built
    by PostGIS `ST_AsMVT`; an empty tile is an empty body. Cached and render-queued like raster tiles,
    with the same cache rule; zooms above the native max zoom are 400 TILE_OUT_OF_RANGE (clients
    overzoom).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        z (int):
        x (int):
        y (int):
        run (str | Unset):  Example: latest.
        prefetch (GetProductVectorTilePrefetch | Unset):
        foresignal_prefetch (GetProductVectorTileForesignalPrefetch | Unset):
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
            z=z,
            x=x,
            y=y,
            client=client,
            run=run,
            prefetch=prefetch,
            foresignal_prefetch=foresignal_prefetch,
            if_none_match=if_none_match,
        )
    ).parsed
