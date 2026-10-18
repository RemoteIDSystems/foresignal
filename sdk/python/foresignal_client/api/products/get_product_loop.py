import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_loop_crs import GetProductLoopCrs
from ...models.problem import Problem
from ...models.product_loop import ProductLoop
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    products: list[str] | Unset = UNSET,
    past: str | Unset = UNSET,
    future: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    frames: int | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopCrs | Unset = GetProductLoopCrs.EPSG3857,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
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

    params["run"] = run

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/loop",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ProductLoop | None:
    if response.status_code == 200:
        response_200 = ProductLoop.from_dict(response.json())

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
) -> Response[Problem | ProductLoop]:
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
    frames: int | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopCrs | Unset = GetProductLoopCrs.EPSG3857,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
) -> Response[Problem | ProductLoop]:
    """Build an animation loop

     Snaps the bbox once and returns corner coordinates plus up to 24 frame URLs pointing at `/image`,
    for mobile and animation clients. `products` lists one or two products, or `past` and `future` name
    an observed and a forecast product for a seamless loop; forecast runs are never mixed. `place`
    resolves through places (404 UNKNOWN_PLACE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        past (str | Unset):
        future (str | Unset):
        bbox (str | Unset):
        place (str | Unset):
        radius_km (float | Unset):
        frames (int | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        width (int | Unset):
        crs (GetProductLoopCrs | Unset):  Default: GetProductLoopCrs.EPSG3857.
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProductLoop]
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
        run=run,
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
    frames: int | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopCrs | Unset = GetProductLoopCrs.EPSG3857,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
) -> Problem | ProductLoop | None:
    """Build an animation loop

     Snaps the bbox once and returns corner coordinates plus up to 24 frame URLs pointing at `/image`,
    for mobile and animation clients. `products` lists one or two products, or `past` and `future` name
    an observed and a forecast product for a seamless loop; forecast runs are never mixed. `place`
    resolves through places (404 UNKNOWN_PLACE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        past (str | Unset):
        future (str | Unset):
        bbox (str | Unset):
        place (str | Unset):
        radius_km (float | Unset):
        frames (int | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        width (int | Unset):
        crs (GetProductLoopCrs | Unset):  Default: GetProductLoopCrs.EPSG3857.
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProductLoop
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
        run=run,
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
    frames: int | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopCrs | Unset = GetProductLoopCrs.EPSG3857,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
) -> Response[Problem | ProductLoop]:
    """Build an animation loop

     Snaps the bbox once and returns corner coordinates plus up to 24 frame URLs pointing at `/image`,
    for mobile and animation clients. `products` lists one or two products, or `past` and `future` name
    an observed and a forecast product for a seamless loop; forecast runs are never mixed. `place`
    resolves through places (404 UNKNOWN_PLACE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        past (str | Unset):
        future (str | Unset):
        bbox (str | Unset):
        place (str | Unset):
        radius_km (float | Unset):
        frames (int | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        width (int | Unset):
        crs (GetProductLoopCrs | Unset):  Default: GetProductLoopCrs.EPSG3857.
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ProductLoop]
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
        run=run,
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
    frames: int | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    width: int | Unset = UNSET,
    crs: GetProductLoopCrs | Unset = GetProductLoopCrs.EPSG3857,
    style: str | Unset = UNSET,
    run: str | Unset = UNSET,
) -> Problem | ProductLoop | None:
    """Build an animation loop

     Snaps the bbox once and returns corner coordinates plus up to 24 frame URLs pointing at `/image`,
    for mobile and animation clients. `products` lists one or two products, or `past` and `future` name
    an observed and a forecast product for a seamless loop; forecast runs are never mixed. `place`
    resolves through places (404 UNKNOWN_PLACE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        past (str | Unset):
        future (str | Unset):
        bbox (str | Unset):
        place (str | Unset):
        radius_km (float | Unset):
        frames (int | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        width (int | Unset):
        crs (GetProductLoopCrs | Unset):  Default: GetProductLoopCrs.EPSG3857.
        style (str | Unset):  Example: nws-reflectivity@1.
        run (str | Unset):  Example: latest.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ProductLoop
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
            run=run,
        )
    ).parsed
