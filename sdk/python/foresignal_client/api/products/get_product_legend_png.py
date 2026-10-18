from http import HTTPStatus
from io import BytesIO
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_legend_png_orientation import GetProductLegendPngOrientation
from ...models.get_product_legend_png_theme import GetProductLegendPngTheme
from ...models.get_product_legend_png_units import GetProductLegendPngUnits
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    product_id: str,
    *,
    style: str | Unset = UNSET,
    units: GetProductLegendPngUnits | Unset = GetProductLegendPngUnits.METRIC,
    width: int | Unset = UNSET,
    orientation: GetProductLegendPngOrientation | Unset = UNSET,
    theme: GetProductLegendPngTheme | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["style"] = style

    json_units: str | Unset = UNSET
    if not isinstance(units, Unset):
        json_units = units.value

    params["units"] = json_units

    params["width"] = width

    json_orientation: str | Unset = UNSET
    if not isinstance(orientation, Unset):
        json_orientation = orientation.value

    params["orientation"] = json_orientation

    json_theme: str | Unset = UNSET
    if not isinstance(theme, Unset):
        json_theme = theme.value

    params["theme"] = json_theme

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/legend.png".format(
            product_id=quote(str(product_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> File | Problem | None:
    if response.status_code == 200:
        response_200 = File(payload=BytesIO(response.content))

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[File | Problem]:
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
    style: str | Unset = UNSET,
    units: GetProductLegendPngUnits | Unset = GetProductLegendPngUnits.METRIC,
    width: int | Unset = UNSET,
    orientation: GetProductLegendPngOrientation | Unset = UNSET,
    theme: GetProductLegendPngTheme | Unset = UNSET,
) -> Response[File | Problem]:
    """Get a product legend (PNG)

     Legend rendered with Java2D (JDK `SansSerif`), horizontal or vertical, for dark or light themes.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):  Example: nws-reflectivity@1.
        units (GetProductLegendPngUnits | Unset):  Default: GetProductLegendPngUnits.METRIC.
        width (int | Unset):
        orientation (GetProductLegendPngOrientation | Unset):
        theme (GetProductLegendPngTheme | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        style=style,
        units=units,
        width=width,
        orientation=orientation,
        theme=theme,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
    units: GetProductLegendPngUnits | Unset = GetProductLegendPngUnits.METRIC,
    width: int | Unset = UNSET,
    orientation: GetProductLegendPngOrientation | Unset = UNSET,
    theme: GetProductLegendPngTheme | Unset = UNSET,
) -> File | Problem | None:
    """Get a product legend (PNG)

     Legend rendered with Java2D (JDK `SansSerif`), horizontal or vertical, for dark or light themes.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):  Example: nws-reflectivity@1.
        units (GetProductLegendPngUnits | Unset):  Default: GetProductLegendPngUnits.METRIC.
        width (int | Unset):
        orientation (GetProductLegendPngOrientation | Unset):
        theme (GetProductLegendPngTheme | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return sync_detailed(
        product_id=product_id,
        client=client,
        style=style,
        units=units,
        width=width,
        orientation=orientation,
        theme=theme,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
    units: GetProductLegendPngUnits | Unset = GetProductLegendPngUnits.METRIC,
    width: int | Unset = UNSET,
    orientation: GetProductLegendPngOrientation | Unset = UNSET,
    theme: GetProductLegendPngTheme | Unset = UNSET,
) -> Response[File | Problem]:
    """Get a product legend (PNG)

     Legend rendered with Java2D (JDK `SansSerif`), horizontal or vertical, for dark or light themes.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):  Example: nws-reflectivity@1.
        units (GetProductLegendPngUnits | Unset):  Default: GetProductLegendPngUnits.METRIC.
        width (int | Unset):
        orientation (GetProductLegendPngOrientation | Unset):
        theme (GetProductLegendPngTheme | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        style=style,
        units=units,
        width=width,
        orientation=orientation,
        theme=theme,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
    units: GetProductLegendPngUnits | Unset = GetProductLegendPngUnits.METRIC,
    width: int | Unset = UNSET,
    orientation: GetProductLegendPngOrientation | Unset = UNSET,
    theme: GetProductLegendPngTheme | Unset = UNSET,
) -> File | Problem | None:
    """Get a product legend (PNG)

     Legend rendered with Java2D (JDK `SansSerif`), horizontal or vertical, for dark or light themes.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):  Example: nws-reflectivity@1.
        units (GetProductLegendPngUnits | Unset):  Default: GetProductLegendPngUnits.METRIC.
        width (int | Unset):
        orientation (GetProductLegendPngOrientation | Unset):
        theme (GetProductLegendPngTheme | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            client=client,
            style=style,
            units=units,
            width=width,
            orientation=orientation,
            theme=theme,
        )
    ).parsed
