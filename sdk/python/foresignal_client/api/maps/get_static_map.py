from http import HTTPStatus
from io import BytesIO
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_static_map_theme import GetStaticMapTheme
from ...models.get_static_map_units import GetStaticMapUnits
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    *,
    products: list[str] | Unset = UNSET,
    center: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    bbox: str | Unset = UNSET,
    time: str | Unset = "latest",
    run: str | Unset = UNSET,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    alert: int | Unset = UNSET,
    place: str | Unset = UNSET,
    style: list[str] | Unset = UNSET,
    theme: GetStaticMapTheme | Unset = UNSET,
    units: GetStaticMapUnits | Unset = GetStaticMapUnits.METRIC,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    json_products: list[str] | Unset = UNSET
    if not isinstance(products, Unset):
        json_products = products

    params["products"] = json_products

    params["center"] = center

    params["radiusKm"] = radius_km

    params["bbox"] = bbox

    params["time"] = time

    params["run"] = run

    params["width"] = width

    params["height"] = height

    params["alert"] = alert

    params["place"] = place

    json_style: list[str] | Unset = UNSET
    if not isinstance(style, Unset):
        json_style = style

    params["style"] = json_style

    json_theme: str | Unset = UNSET
    if not isinstance(theme, Unset):
        json_theme = theme.value

    params["theme"] = json_theme

    json_units: str | Unset = UNSET
    if not isinstance(units, Unset):
        json_units = units.value

    params["units"] = json_units

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/maps/static",
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
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    center: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    bbox: str | Unset = UNSET,
    time: str | Unset = "latest",
    run: str | Unset = UNSET,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    alert: int | Unset = UNSET,
    place: str | Unset = UNSET,
    style: list[str] | Unset = UNSET,
    theme: GetStaticMapTheme | Unset = UNSET,
    units: GetStaticMapUnits | Unset = GetStaticMapUnits.METRIC,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Render a static map

     Basemap-free server-rendered PNG: Natural Earth land, water and boundaries, product frames, alert
    geometry, place marker and range rings, scale bar, frame valid times (UTC plus the place's local
    time), legend, attribution and an "Automated — not an official warning" footer. The box is `bbox`,
    else `center` with `radiusKm` (default 50 km), else the alert's record geometry, else the place; it
    is snapped outwards to 0.01° once and widened to the map area's aspect, and `Foresignal-Image-
    Bounds` gives the result. Missing or stale frames are drawn as a labeled note ("… unavailable — not
    evidence of clear weather"), never omitted; a concrete ISO time at which an enabled product has no
    frame is 404 UNKNOWN_FRAME. Nothing fetches third-party tiles (an operator's LAN tile server,
    `FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE`, may replace the Natural Earth fills). Immutable only when
    every frame is pinned: a concrete ISO `time`, complete current frames, a pinned run for forecasts, a
    versioned style for every product and no `place`; otherwise a short max-age with an ETag checked
    before rendering. Rendered on the render pool within 2 s, else 503 RENDER_BUSY.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        center (str | Unset):
        radius_km (float | Unset):
        bbox (str | Unset):
        time (str | Unset):  Default: 'latest'. Example: -PT1H.
        run (str | Unset):  Example: latest.
        width (int | Unset):
        height (int | Unset):
        alert (int | Unset):
        place (str | Unset):
        style (list[str] | Unset):
        theme (GetStaticMapTheme | Unset):
        units (GetStaticMapUnits | Unset):  Default: GetStaticMapUnits.METRIC.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        products=products,
        center=center,
        radius_km=radius_km,
        bbox=bbox,
        time=time,
        run=run,
        width=width,
        height=height,
        alert=alert,
        place=place,
        style=style,
        theme=theme,
        units=units,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    center: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    bbox: str | Unset = UNSET,
    time: str | Unset = "latest",
    run: str | Unset = UNSET,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    alert: int | Unset = UNSET,
    place: str | Unset = UNSET,
    style: list[str] | Unset = UNSET,
    theme: GetStaticMapTheme | Unset = UNSET,
    units: GetStaticMapUnits | Unset = GetStaticMapUnits.METRIC,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Render a static map

     Basemap-free server-rendered PNG: Natural Earth land, water and boundaries, product frames, alert
    geometry, place marker and range rings, scale bar, frame valid times (UTC plus the place's local
    time), legend, attribution and an "Automated — not an official warning" footer. The box is `bbox`,
    else `center` with `radiusKm` (default 50 km), else the alert's record geometry, else the place; it
    is snapped outwards to 0.01° once and widened to the map area's aspect, and `Foresignal-Image-
    Bounds` gives the result. Missing or stale frames are drawn as a labeled note ("… unavailable — not
    evidence of clear weather"), never omitted; a concrete ISO time at which an enabled product has no
    frame is 404 UNKNOWN_FRAME. Nothing fetches third-party tiles (an operator's LAN tile server,
    `FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE`, may replace the Natural Earth fills). Immutable only when
    every frame is pinned: a concrete ISO `time`, complete current frames, a pinned run for forecasts, a
    versioned style for every product and no `place`; otherwise a short max-age with an ETag checked
    before rendering. Rendered on the render pool within 2 s, else 503 RENDER_BUSY.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        center (str | Unset):
        radius_km (float | Unset):
        bbox (str | Unset):
        time (str | Unset):  Default: 'latest'. Example: -PT1H.
        run (str | Unset):  Example: latest.
        width (int | Unset):
        height (int | Unset):
        alert (int | Unset):
        place (str | Unset):
        style (list[str] | Unset):
        theme (GetStaticMapTheme | Unset):
        units (GetStaticMapUnits | Unset):  Default: GetStaticMapUnits.METRIC.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return sync_detailed(
        client=client,
        products=products,
        center=center,
        radius_km=radius_km,
        bbox=bbox,
        time=time,
        run=run,
        width=width,
        height=height,
        alert=alert,
        place=place,
        style=style,
        theme=theme,
        units=units,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    center: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    bbox: str | Unset = UNSET,
    time: str | Unset = "latest",
    run: str | Unset = UNSET,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    alert: int | Unset = UNSET,
    place: str | Unset = UNSET,
    style: list[str] | Unset = UNSET,
    theme: GetStaticMapTheme | Unset = UNSET,
    units: GetStaticMapUnits | Unset = GetStaticMapUnits.METRIC,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Render a static map

     Basemap-free server-rendered PNG: Natural Earth land, water and boundaries, product frames, alert
    geometry, place marker and range rings, scale bar, frame valid times (UTC plus the place's local
    time), legend, attribution and an "Automated — not an official warning" footer. The box is `bbox`,
    else `center` with `radiusKm` (default 50 km), else the alert's record geometry, else the place; it
    is snapped outwards to 0.01° once and widened to the map area's aspect, and `Foresignal-Image-
    Bounds` gives the result. Missing or stale frames are drawn as a labeled note ("… unavailable — not
    evidence of clear weather"), never omitted; a concrete ISO time at which an enabled product has no
    frame is 404 UNKNOWN_FRAME. Nothing fetches third-party tiles (an operator's LAN tile server,
    `FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE`, may replace the Natural Earth fills). Immutable only when
    every frame is pinned: a concrete ISO `time`, complete current frames, a pinned run for forecasts, a
    versioned style for every product and no `place`; otherwise a short max-age with an ETag checked
    before rendering. Rendered on the render pool within 2 s, else 503 RENDER_BUSY.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        center (str | Unset):
        radius_km (float | Unset):
        bbox (str | Unset):
        time (str | Unset):  Default: 'latest'. Example: -PT1H.
        run (str | Unset):  Example: latest.
        width (int | Unset):
        height (int | Unset):
        alert (int | Unset):
        place (str | Unset):
        style (list[str] | Unset):
        theme (GetStaticMapTheme | Unset):
        units (GetStaticMapUnits | Unset):  Default: GetStaticMapUnits.METRIC.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        products=products,
        center=center,
        radius_km=radius_km,
        bbox=bbox,
        time=time,
        run=run,
        width=width,
        height=height,
        alert=alert,
        place=place,
        style=style,
        theme=theme,
        units=units,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    products: list[str] | Unset = UNSET,
    center: str | Unset = UNSET,
    radius_km: float | Unset = UNSET,
    bbox: str | Unset = UNSET,
    time: str | Unset = "latest",
    run: str | Unset = UNSET,
    width: int | Unset = UNSET,
    height: int | Unset = UNSET,
    alert: int | Unset = UNSET,
    place: str | Unset = UNSET,
    style: list[str] | Unset = UNSET,
    theme: GetStaticMapTheme | Unset = UNSET,
    units: GetStaticMapUnits | Unset = GetStaticMapUnits.METRIC,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Render a static map

     Basemap-free server-rendered PNG: Natural Earth land, water and boundaries, product frames, alert
    geometry, place marker and range rings, scale bar, frame valid times (UTC plus the place's local
    time), legend, attribution and an "Automated — not an official warning" footer. The box is `bbox`,
    else `center` with `radiusKm` (default 50 km), else the alert's record geometry, else the place; it
    is snapped outwards to 0.01° once and widened to the map area's aspect, and `Foresignal-Image-
    Bounds` gives the result. Missing or stale frames are drawn as a labeled note ("… unavailable — not
    evidence of clear weather"), never omitted; a concrete ISO time at which an enabled product has no
    frame is 404 UNKNOWN_FRAME. Nothing fetches third-party tiles (an operator's LAN tile server,
    `FORESIGNAL_MAPS_BASEMAP_URL_TEMPLATE`, may replace the Natural Earth fills). Immutable only when
    every frame is pinned: a concrete ISO `time`, complete current frames, a pinned run for forecasts, a
    versioned style for every product and no `place`; otherwise a short max-age with an ETag checked
    before rendering. Rendered on the render pool within 2 s, else 503 RENDER_BUSY.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str] | Unset):
        center (str | Unset):
        radius_km (float | Unset):
        bbox (str | Unset):
        time (str | Unset):  Default: 'latest'. Example: -PT1H.
        run (str | Unset):  Example: latest.
        width (int | Unset):
        height (int | Unset):
        alert (int | Unset):
        place (str | Unset):
        style (list[str] | Unset):
        theme (GetStaticMapTheme | Unset):
        units (GetStaticMapUnits | Unset):  Default: GetStaticMapUnits.METRIC.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            products=products,
            center=center,
            radius_km=radius_km,
            bbox=bbox,
            time=time,
            run=run,
            width=width,
            height=height,
            alert=alert,
            place=place,
            style=style,
            theme=theme,
            units=units,
            if_none_match=if_none_match,
        )
    ).parsed
