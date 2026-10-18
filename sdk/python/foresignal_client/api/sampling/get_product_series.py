import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_series_units import GetProductSeriesUnits
from ...models.problem import Problem
from ...models.series import Series
from ...types import UNSET, Response, Unset


def _get_kwargs(
    product_id: str,
    *,
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    units: GetProductSeriesUnits | Unset = GetProductSeriesUnits.METRIC,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["lon"] = lon

    params["lat"] = lat

    params["place"] = place

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    params["run"] = run

    json_units: str | Unset = UNSET
    if not isinstance(units, Unset):
        json_units = units.value

    params["units"] = json_units

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/series".format(
            product_id=quote(str(product_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Series | None:
    if response.status_code == 200:
        response_200 = Series.from_dict(response.json())

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
) -> Response[Problem | Series]:
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
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    units: GetProductSeriesUnits | Unset = GetProductSeriesUnits.METRIC,
) -> Response[Problem | Series]:
    """Get a point time series

     Observed past plus forecast future at a point, each step with status, frame key and run (one run,
    never mixed). At most 500 steps; larger ranges → 400 VALIDATION_FAILED. Observation products:
    complete frames in `from`..`to` (default the last 6 h, never after now), `kind: observed`. Forecast
    products: the steps of one run (newest complete, or `run`) in `from`..`to`, `kind: forecast`. A
    disabled product's steps are `unavailable`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        lon (float | Unset):
        lat (float | Unset):
        place (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        run (str | Unset):  Example: latest.
        units (GetProductSeriesUnits | Unset):  Default: GetProductSeriesUnits.METRIC.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Series]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        lon=lon,
        lat=lat,
        place=place,
        from_=from_,
        to=to,
        run=run,
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
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    units: GetProductSeriesUnits | Unset = GetProductSeriesUnits.METRIC,
) -> Problem | Series | None:
    """Get a point time series

     Observed past plus forecast future at a point, each step with status, frame key and run (one run,
    never mixed). At most 500 steps; larger ranges → 400 VALIDATION_FAILED. Observation products:
    complete frames in `from`..`to` (default the last 6 h, never after now), `kind: observed`. Forecast
    products: the steps of one run (newest complete, or `run`) in `from`..`to`, `kind: forecast`. A
    disabled product's steps are `unavailable`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        lon (float | Unset):
        lat (float | Unset):
        place (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        run (str | Unset):  Example: latest.
        units (GetProductSeriesUnits | Unset):  Default: GetProductSeriesUnits.METRIC.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Series
    """

    return sync_detailed(
        product_id=product_id,
        client=client,
        lon=lon,
        lat=lat,
        place=place,
        from_=from_,
        to=to,
        run=run,
        units=units,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    units: GetProductSeriesUnits | Unset = GetProductSeriesUnits.METRIC,
) -> Response[Problem | Series]:
    """Get a point time series

     Observed past plus forecast future at a point, each step with status, frame key and run (one run,
    never mixed). At most 500 steps; larger ranges → 400 VALIDATION_FAILED. Observation products:
    complete frames in `from`..`to` (default the last 6 h, never after now), `kind: observed`. Forecast
    products: the steps of one run (newest complete, or `run`) in `from`..`to`, `kind: forecast`. A
    disabled product's steps are `unavailable`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        lon (float | Unset):
        lat (float | Unset):
        place (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        run (str | Unset):  Example: latest.
        units (GetProductSeriesUnits | Unset):  Default: GetProductSeriesUnits.METRIC.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Series]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        lon=lon,
        lat=lat,
        place=place,
        from_=from_,
        to=to,
        run=run,
        units=units,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    *,
    client: AuthenticatedClient,
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    run: str | Unset = UNSET,
    units: GetProductSeriesUnits | Unset = GetProductSeriesUnits.METRIC,
) -> Problem | Series | None:
    """Get a point time series

     Observed past plus forecast future at a point, each step with status, frame key and run (one run,
    never mixed). At most 500 steps; larger ranges → 400 VALIDATION_FAILED. Observation products:
    complete frames in `from`..`to` (default the last 6 h, never after now), `kind: observed`. Forecast
    products: the steps of one run (newest complete, or `run`) in `from`..`to`, `kind: forecast`. A
    disabled product's steps are `unavailable`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        lon (float | Unset):
        lat (float | Unset):
        place (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        run (str | Unset):  Example: latest.
        units (GetProductSeriesUnits | Unset):  Default: GetProductSeriesUnits.METRIC.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Series
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            client=client,
            lon=lon,
            lat=lat,
            place=place,
            from_=from_,
            to=to,
            run=run,
            units=units,
        )
    ).parsed
