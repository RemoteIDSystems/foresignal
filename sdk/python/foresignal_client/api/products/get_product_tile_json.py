from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_tile_json_encoding import GetProductTileJsonEncoding
from ...models.get_product_tile_json_format import GetProductTileJsonFormat
from ...models.get_product_tile_json_tile_size import GetProductTileJsonTileSize
from ...models.get_product_tile_json_tms import GetProductTileJsonTms
from ...models.problem import Problem
from ...models.tile_json import TileJson
from ...types import UNSET, Response, Unset


def _get_kwargs(
    product_id: str,
    *,
    frame_key: str | Unset = "latest",
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    tms: GetProductTileJsonTms | Unset = GetProductTileJsonTms.WEBMERCATORQUAD,
    tile_size: GetProductTileJsonTileSize
    | Unset = GetProductTileJsonTileSize.VALUE_256,
    encoding: GetProductTileJsonEncoding | Unset = UNSET,
    format_: GetProductTileJsonFormat | Unset = GetProductTileJsonFormat.PNG,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    params["frameKey"] = frame_key

    params["style"] = style

    params["run"] = run

    params["includePartial"] = include_partial

    json_tms: str | Unset = UNSET
    if not isinstance(tms, Unset):
        json_tms = tms.value

    params["tms"] = json_tms

    json_tile_size: int | Unset = UNSET
    if not isinstance(tile_size, Unset):
        json_tile_size = tile_size.value

    params["tileSize"] = json_tile_size

    json_encoding: str | Unset = UNSET
    if not isinstance(encoding, Unset):
        json_encoding = encoding.value

    params["encoding"] = json_encoding

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/tilejson".format(
            product_id=quote(str(product_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | TileJson | None:
    if response.status_code == 200:
        response_200 = TileJson.from_dict(response.json())

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
) -> Response[Any | Problem | TileJson]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    frame_key: str | Unset = "latest",
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    tms: GetProductTileJsonTms | Unset = GetProductTileJsonTms.WEBMERCATORQUAD,
    tile_size: GetProductTileJsonTileSize
    | Unset = GetProductTileJsonTileSize.VALUE_256,
    encoding: GetProductTileJsonEncoding | Unset = UNSET,
    format_: GetProductTileJsonFormat | Unset = GetProductTileJsonFormat.PNG,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | TileJson]:
    """Get TileJSON for a product frame

     TileJSON 3.0.0 with a concrete content-addressed template (aliases resolved at generation),
    `bounds`, native `minzoom`/`maxzoom`, `center`, `tileSize: 256`, the `@2x` variant and attribution
    text. The document itself has a short max-age.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str | Unset):  Default: 'latest'.
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.
        include_partial (bool | Unset):  Default: False.
        tms (GetProductTileJsonTms | Unset):  Default: GetProductTileJsonTms.WEBMERCATORQUAD.
        tile_size (GetProductTileJsonTileSize | Unset):  Default:
            GetProductTileJsonTileSize.VALUE_256.
        encoding (GetProductTileJsonEncoding | Unset):
        format_ (GetProductTileJsonFormat | Unset):  Default: GetProductTileJsonFormat.PNG.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | TileJson]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        style=style,
        run=run,
        include_partial=include_partial,
        tms=tms,
        tile_size=tile_size,
        encoding=encoding,
        format_=format_,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    *,
    client: AuthenticatedClient,
    frame_key: str | Unset = "latest",
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    tms: GetProductTileJsonTms | Unset = GetProductTileJsonTms.WEBMERCATORQUAD,
    tile_size: GetProductTileJsonTileSize
    | Unset = GetProductTileJsonTileSize.VALUE_256,
    encoding: GetProductTileJsonEncoding | Unset = UNSET,
    format_: GetProductTileJsonFormat | Unset = GetProductTileJsonFormat.PNG,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | TileJson | None:
    """Get TileJSON for a product frame

     TileJSON 3.0.0 with a concrete content-addressed template (aliases resolved at generation),
    `bounds`, native `minzoom`/`maxzoom`, `center`, `tileSize: 256`, the `@2x` variant and attribution
    text. The document itself has a short max-age.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str | Unset):  Default: 'latest'.
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.
        include_partial (bool | Unset):  Default: False.
        tms (GetProductTileJsonTms | Unset):  Default: GetProductTileJsonTms.WEBMERCATORQUAD.
        tile_size (GetProductTileJsonTileSize | Unset):  Default:
            GetProductTileJsonTileSize.VALUE_256.
        encoding (GetProductTileJsonEncoding | Unset):
        format_ (GetProductTileJsonFormat | Unset):  Default: GetProductTileJsonFormat.PNG.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | TileJson
    """

    return sync_detailed(
        product_id=product_id,
        client=client,
        frame_key=frame_key,
        style=style,
        run=run,
        include_partial=include_partial,
        tms=tms,
        tile_size=tile_size,
        encoding=encoding,
        format_=format_,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    frame_key: str | Unset = "latest",
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    tms: GetProductTileJsonTms | Unset = GetProductTileJsonTms.WEBMERCATORQUAD,
    tile_size: GetProductTileJsonTileSize
    | Unset = GetProductTileJsonTileSize.VALUE_256,
    encoding: GetProductTileJsonEncoding | Unset = UNSET,
    format_: GetProductTileJsonFormat | Unset = GetProductTileJsonFormat.PNG,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | TileJson]:
    """Get TileJSON for a product frame

     TileJSON 3.0.0 with a concrete content-addressed template (aliases resolved at generation),
    `bounds`, native `minzoom`/`maxzoom`, `center`, `tileSize: 256`, the `@2x` variant and attribution
    text. The document itself has a short max-age.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str | Unset):  Default: 'latest'.
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.
        include_partial (bool | Unset):  Default: False.
        tms (GetProductTileJsonTms | Unset):  Default: GetProductTileJsonTms.WEBMERCATORQUAD.
        tile_size (GetProductTileJsonTileSize | Unset):  Default:
            GetProductTileJsonTileSize.VALUE_256.
        encoding (GetProductTileJsonEncoding | Unset):
        format_ (GetProductTileJsonFormat | Unset):  Default: GetProductTileJsonFormat.PNG.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | TileJson]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        style=style,
        run=run,
        include_partial=include_partial,
        tms=tms,
        tile_size=tile_size,
        encoding=encoding,
        format_=format_,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    *,
    client: AuthenticatedClient,
    frame_key: str | Unset = "latest",
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    tms: GetProductTileJsonTms | Unset = GetProductTileJsonTms.WEBMERCATORQUAD,
    tile_size: GetProductTileJsonTileSize
    | Unset = GetProductTileJsonTileSize.VALUE_256,
    encoding: GetProductTileJsonEncoding | Unset = UNSET,
    format_: GetProductTileJsonFormat | Unset = GetProductTileJsonFormat.PNG,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | TileJson | None:
    """Get TileJSON for a product frame

     TileJSON 3.0.0 with a concrete content-addressed template (aliases resolved at generation),
    `bounds`, native `minzoom`/`maxzoom`, `center`, `tileSize: 256`, the `@2x` variant and attribution
    text. The document itself has a short max-age.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str | Unset):  Default: 'latest'.
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.
        include_partial (bool | Unset):  Default: False.
        tms (GetProductTileJsonTms | Unset):  Default: GetProductTileJsonTms.WEBMERCATORQUAD.
        tile_size (GetProductTileJsonTileSize | Unset):  Default:
            GetProductTileJsonTileSize.VALUE_256.
        encoding (GetProductTileJsonEncoding | Unset):
        format_ (GetProductTileJsonFormat | Unset):  Default: GetProductTileJsonFormat.PNG.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | TileJson
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            client=client,
            frame_key=frame_key,
            style=style,
            run=run,
            include_partial=include_partial,
            tms=tms,
            tile_size=tile_size,
            encoding=encoding,
            format_=format_,
            if_none_match=if_none_match,
        )
    ).parsed
