from http import HTTPStatus
from io import BytesIO
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_image_crs import GetProductImageCrs
from ...models.get_product_image_foresignal_prefetch import (
    GetProductImageForesignalPrefetch,
)
from ...models.get_product_image_format import GetProductImageFormat
from ...models.get_product_image_prefetch import GetProductImagePrefetch
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    product_id: str,
    frame_key: str,
    format_: GetProductImageFormat,
    *,
    bbox: str,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    crs: GetProductImageCrs | Unset = UNSET,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    prefetch: GetProductImagePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductImageForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_prefetch, Unset):
        headers["Foresignal-Prefetch"] = str(foresignal_prefetch)

    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    params["bbox"] = bbox

    params["width"] = width

    params["height"] = height

    json_crs: str | Unset = UNSET
    if not isinstance(crs, Unset):
        json_crs = crs.value

    params["crs"] = json_crs

    params["style"] = style

    params["run"] = run

    json_prefetch: int | Unset = UNSET
    if not isinstance(prefetch, Unset):
        json_prefetch = prefetch.value

    params["prefetch"] = json_prefetch

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/image/{frame_key}.{format_}".format(
            product_id=quote(str(product_id), safe=""),
            frame_key=quote(str(frame_key), safe=""),
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
    format_: GetProductImageFormat,
    *,
    client: AuthenticatedClient,
    bbox: str,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    crs: GetProductImageCrs | Unset = UNSET,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    prefetch: GetProductImagePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductImageForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Render a single product image

     Renders one frame for a bbox, up to 2048 px a side, in EPSG:3857 or EPSG:4326, and returns the
    snapped bounds in `Foresignal-Image-Bounds`. Follows the same cache rule as tiles.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        format_ (GetProductImageFormat):
        bbox (str):
        width (int | Unset):
        height (int | Unset):
        crs (GetProductImageCrs | Unset):
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.
        prefetch (GetProductImagePrefetch | Unset):
        foresignal_prefetch (GetProductImageForesignalPrefetch | Unset):
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
        format_=format_,
        bbox=bbox,
        width=width,
        height=height,
        crs=crs,
        style=style,
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
    format_: GetProductImageFormat,
    *,
    client: AuthenticatedClient,
    bbox: str,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    crs: GetProductImageCrs | Unset = UNSET,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    prefetch: GetProductImagePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductImageForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Render a single product image

     Renders one frame for a bbox, up to 2048 px a side, in EPSG:3857 or EPSG:4326, and returns the
    snapped bounds in `Foresignal-Image-Bounds`. Follows the same cache rule as tiles.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        format_ (GetProductImageFormat):
        bbox (str):
        width (int | Unset):
        height (int | Unset):
        crs (GetProductImageCrs | Unset):
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.
        prefetch (GetProductImagePrefetch | Unset):
        foresignal_prefetch (GetProductImageForesignalPrefetch | Unset):
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
        format_=format_,
        client=client,
        bbox=bbox,
        width=width,
        height=height,
        crs=crs,
        style=style,
        run=run,
        prefetch=prefetch,
        foresignal_prefetch=foresignal_prefetch,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    frame_key: str,
    format_: GetProductImageFormat,
    *,
    client: AuthenticatedClient,
    bbox: str,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    crs: GetProductImageCrs | Unset = UNSET,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    prefetch: GetProductImagePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductImageForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Render a single product image

     Renders one frame for a bbox, up to 2048 px a side, in EPSG:3857 or EPSG:4326, and returns the
    snapped bounds in `Foresignal-Image-Bounds`. Follows the same cache rule as tiles.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        format_ (GetProductImageFormat):
        bbox (str):
        width (int | Unset):
        height (int | Unset):
        crs (GetProductImageCrs | Unset):
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.
        prefetch (GetProductImagePrefetch | Unset):
        foresignal_prefetch (GetProductImageForesignalPrefetch | Unset):
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
        format_=format_,
        bbox=bbox,
        width=width,
        height=height,
        crs=crs,
        style=style,
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
    format_: GetProductImageFormat,
    *,
    client: AuthenticatedClient,
    bbox: str,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    crs: GetProductImageCrs | Unset = UNSET,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
    prefetch: GetProductImagePrefetch | Unset = UNSET,
    foresignal_prefetch: GetProductImageForesignalPrefetch | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Render a single product image

     Renders one frame for a bbox, up to 2048 px a side, in EPSG:3857 or EPSG:4326, and returns the
    snapped bounds in `Foresignal-Image-Bounds`. Follows the same cache rule as tiles.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        format_ (GetProductImageFormat):
        bbox (str):
        width (int | Unset):
        height (int | Unset):
        crs (GetProductImageCrs | Unset):
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.
        prefetch (GetProductImagePrefetch | Unset):
        foresignal_prefetch (GetProductImageForesignalPrefetch | Unset):
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
            format_=format_,
            client=client,
            bbox=bbox,
            width=width,
            height=height,
            crs=crs,
            style=style,
            run=run,
            prefetch=prefetch,
            foresignal_prefetch=foresignal_prefetch,
            if_none_match=if_none_match,
        )
    ).parsed
