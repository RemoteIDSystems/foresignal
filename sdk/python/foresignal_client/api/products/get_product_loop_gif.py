import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_loop_gif_crs import GetProductLoopGifCrs
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    products: list[str] | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    frames: int | Unset = 12,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopGifCrs | Unset = GetProductLoopGifCrs.EPSG3857,
    style: str | Unset = UNSET,
    delay: int | Unset = 500,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_products: list[str] | Unset = UNSET
    if not isinstance(products, Unset):
        json_products = products

    params["products"] = json_products

    params["past"] = past

    params["future"] = future

    params["bbox"] = bbox

    params["place"] = place

    params["radiusKm"] = radius_km

    params["frames"] = frames

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    params["width"] = width

    json_crs: str | Unset = UNSET
    if not isinstance(crs, Unset):
        json_crs = crs.value

    params["crs"] = json_crs

    params["style"] = style

    params["delay"] = delay

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/loop.gif",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    frames: int | Unset = 12,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopGifCrs | Unset = GetProductLoopGifCrs.EPSG3857,
    style: str | Unset = UNSET,
    delay: int | Unset = 500,
) -> Response[Problem]:
    """Animated GIF loop

     Takes the `/loop` parameters; palette from the style LUT; rendered at prefetch priority (503
    RENDER_BUSY applies). Cached only for content-addressed frame sets.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        past (str | Unset):
        future (str | Unset):
        bbox (str | Unset):
        place (str | Unset):
        radius_km (float | Unset):
        frames (int | Unset):  Default: 12.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        width (int | Unset):
        crs (GetProductLoopGifCrs | Unset):  Default: GetProductLoopGifCrs.EPSG3857.
        style (str | Unset):
        delay (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        products=products,
        past=past,
        future=future,
        bbox=bbox,
        place=place,
        radius_km=radius_km,
        frames=frames,
        from_=from_,
        to=to,
        width=width,
        crs=crs,
        style=style,
        delay=delay,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    frames: int | Unset = 12,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopGifCrs | Unset = GetProductLoopGifCrs.EPSG3857,
    style: str | Unset = UNSET,
    delay: int | Unset = 500,
) -> Problem | None:
    """Animated GIF loop

     Takes the `/loop` parameters; palette from the style LUT; rendered at prefetch priority (503
    RENDER_BUSY applies). Cached only for content-addressed frame sets.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        past (str | Unset):
        future (str | Unset):
        bbox (str | Unset):
        place (str | Unset):
        radius_km (float | Unset):
        frames (int | Unset):  Default: 12.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        width (int | Unset):
        crs (GetProductLoopGifCrs | Unset):  Default: GetProductLoopGifCrs.EPSG3857.
        style (str | Unset):
        delay (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        products=products,
        past=past,
        future=future,
        bbox=bbox,
        place=place,
        radius_km=radius_km,
        frames=frames,
        from_=from_,
        to=to,
        width=width,
        crs=crs,
        style=style,
        delay=delay,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    frames: int | Unset = 12,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopGifCrs | Unset = GetProductLoopGifCrs.EPSG3857,
    style: str | Unset = UNSET,
    delay: int | Unset = 500,
) -> Response[Problem]:
    """Animated GIF loop

     Takes the `/loop` parameters; palette from the style LUT; rendered at prefetch priority (503
    RENDER_BUSY applies). Cached only for content-addressed frame sets.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        past (str | Unset):
        future (str | Unset):
        bbox (str | Unset):
        place (str | Unset):
        radius_km (float | Unset):
        frames (int | Unset):  Default: 12.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        width (int | Unset):
        crs (GetProductLoopGifCrs | Unset):  Default: GetProductLoopGifCrs.EPSG3857.
        style (str | Unset):
        delay (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        products=products,
        past=past,
        future=future,
        bbox=bbox,
        place=place,
        radius_km=radius_km,
        frames=frames,
        from_=from_,
        to=to,
        width=width,
        crs=crs,
        style=style,
        delay=delay,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    frames: int | Unset = 12,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopGifCrs | Unset = GetProductLoopGifCrs.EPSG3857,
    style: str | Unset = UNSET,
    delay: int | Unset = 500,
) -> Problem | None:
    """Animated GIF loop

     Takes the `/loop` parameters; palette from the style LUT; rendered at prefetch priority (503
    RENDER_BUSY applies). Cached only for content-addressed frame sets.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        past (str | Unset):
        future (str | Unset):
        bbox (str | Unset):
        place (str | Unset):
        radius_km (float | Unset):
        frames (int | Unset):  Default: 12.
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        width (int | Unset):
        crs (GetProductLoopGifCrs | Unset):  Default: GetProductLoopGifCrs.EPSG3857.
        style (str | Unset):
        delay (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            products=products,
            past=past,
            future=future,
            bbox=bbox,
            place=place,
            radius_km=radius_km,
            frames=frames,
            from_=from_,
            to=to,
            width=width,
            crs=crs,
            style=style,
            delay=delay,
        )
    ).parsed
