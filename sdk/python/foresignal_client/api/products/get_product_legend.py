from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_legend_units import GetProductLegendUnits
from ...models.legend import Legend
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    product_id: str,
    *,
    style: str | Unset = UNSET,
    units: GetProductLegendUnits | Unset = GetProductLegendUnits.METRIC,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["style"] = style

    json_units: str | Unset = UNSET
    if not isinstance(units, Unset):
        json_units = units.value

    params["units"] = json_units

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/legend".format(
            product_id=quote(str(product_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Legend | Problem | None:
    if response.status_code == 200:
        response_200 = Legend.from_dict(response.json())

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
) -> Response[Legend | Problem]:
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
    units: GetProductLegendUnits | Unset = GetProductLegendUnits.METRIC,
) -> Response[Legend | Problem]:
    """Get a product legend (JSON)

     Legend for native clients: stops, ticks, categories, specials and plain-language meanings (e.g. "≥
    50 dBZ: heavy rain, hail possible"), converted to the requested unit system (mm/h↔in/h, K→°C/°F,
    m/s↔kt/mph, mm↔in).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):  Example: nws-reflectivity@1.
        units (GetProductLegendUnits | Unset):  Default: GetProductLegendUnits.METRIC.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Legend | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        style=style,
        units=units,
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
    units: GetProductLegendUnits | Unset = GetProductLegendUnits.METRIC,
) -> Legend | Problem | None:
    """Get a product legend (JSON)

     Legend for native clients: stops, ticks, categories, specials and plain-language meanings (e.g. "≥
    50 dBZ: heavy rain, hail possible"), converted to the requested unit system (mm/h↔in/h, K→°C/°F,
    m/s↔kt/mph, mm↔in).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):  Example: nws-reflectivity@1.
        units (GetProductLegendUnits | Unset):  Default: GetProductLegendUnits.METRIC.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Legend | Problem
    """

    return sync_detailed(
        product_id=product_id,
        client=client,
        style=style,
        units=units,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
    units: GetProductLegendUnits | Unset = GetProductLegendUnits.METRIC,
) -> Response[Legend | Problem]:
    """Get a product legend (JSON)

     Legend for native clients: stops, ticks, categories, specials and plain-language meanings (e.g. "≥
    50 dBZ: heavy rain, hail possible"), converted to the requested unit system (mm/h↔in/h, K→°C/°F,
    m/s↔kt/mph, mm↔in).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):  Example: nws-reflectivity@1.
        units (GetProductLegendUnits | Unset):  Default: GetProductLegendUnits.METRIC.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Legend | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        style=style,
        units=units,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
    units: GetProductLegendUnits | Unset = GetProductLegendUnits.METRIC,
) -> Legend | Problem | None:
    """Get a product legend (JSON)

     Legend for native clients: stops, ticks, categories, specials and plain-language meanings (e.g. "≥
    50 dBZ: heavy rain, hail possible"), converted to the requested unit system (mm/h↔in/h, K→°C/°F,
    m/s↔kt/mph, mm↔in).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):  Example: nws-reflectivity@1.
        units (GetProductLegendUnits | Unset):  Default: GetProductLegendUnits.METRIC.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Legend | Problem
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            client=client,
            style=style,
            units=units,
        )
    ).parsed
