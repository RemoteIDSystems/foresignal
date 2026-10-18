from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_sample_units import GetSampleUnits
from ...models.problem import Problem
from ...models.sample_response import SampleResponse
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    products: list[str],
    time: str | Unset = "latest",
    units: GetSampleUnits | Unset = GetSampleUnits.METRIC,
    record: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["lon"] = lon

    params["lat"] = lat

    params["place"] = place

    json_products = products

    params["products"] = json_products

    params["time"] = time

    json_units: str | Unset = UNSET
    if not isinstance(units, Unset):
        json_units = units.value

    params["units"] = json_units

    params["record"] = record

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/sample",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SampleResponse | None:
    if response.status_code == 200:
        response_200 = SampleResponse.from_dict(response.json())

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
) -> Response[Problem | SampleResponse]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    products: list[str],
    time: str | Unset = "latest",
    units: GetSampleUnits | Unset = GetSampleUnits.METRIC,
    record: str | Unset = UNSET,
) -> Response[Problem | SampleResponse]:
    """Sample products at a point

     Per product: value (or u/v with derived speed and direction), units, label, frame key, valid time,
    local time, age, run, method, status and attribution. Non-ok statuses carry `statusText`; no-
    coverage reads "outside product coverage; this is not evidence of clear weather". Never triggers an
    upstream fetch. Time: `latest` is the newest frame (judged stale against now); `now`, an ISO time
    and a past offset choose the observation frame valid at or before the time; a future offset (and any
    time on a forecast product) uses the newest complete forecast run, taking the step valid at or
    before the time. Status order: unavailable (product disabled, source failing, no frame, frame
    expired), no-coverage, no-data, stale, ok; a non-ok value has `value: null`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        lon (float | Unset):
        lat (float | Unset):
        place (str | Unset):
        products (list[str]):
        time (str | Unset):  Default: 'latest'. Example: -PT1H.
        units (GetSampleUnits | Unset):  Default: GetSampleUnits.METRIC.
        record (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SampleResponse]
    """

    kwargs = _get_kwargs(
        lon=lon,
        lat=lat,
        place=place,
        products=products,
        time=time,
        units=units,
        record=record,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    products: list[str],
    time: str | Unset = "latest",
    units: GetSampleUnits | Unset = GetSampleUnits.METRIC,
    record: str | Unset = UNSET,
) -> Problem | SampleResponse | None:
    """Sample products at a point

     Per product: value (or u/v with derived speed and direction), units, label, frame key, valid time,
    local time, age, run, method, status and attribution. Non-ok statuses carry `statusText`; no-
    coverage reads "outside product coverage; this is not evidence of clear weather". Never triggers an
    upstream fetch. Time: `latest` is the newest frame (judged stale against now); `now`, an ISO time
    and a past offset choose the observation frame valid at or before the time; a future offset (and any
    time on a forecast product) uses the newest complete forecast run, taking the step valid at or
    before the time. Status order: unavailable (product disabled, source failing, no frame, frame
    expired), no-coverage, no-data, stale, ok; a non-ok value has `value: null`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        lon (float | Unset):
        lat (float | Unset):
        place (str | Unset):
        products (list[str]):
        time (str | Unset):  Default: 'latest'. Example: -PT1H.
        units (GetSampleUnits | Unset):  Default: GetSampleUnits.METRIC.
        record (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SampleResponse
    """

    return sync_detailed(
        client=client,
        lon=lon,
        lat=lat,
        place=place,
        products=products,
        time=time,
        units=units,
        record=record,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    products: list[str],
    time: str | Unset = "latest",
    units: GetSampleUnits | Unset = GetSampleUnits.METRIC,
    record: str | Unset = UNSET,
) -> Response[Problem | SampleResponse]:
    """Sample products at a point

     Per product: value (or u/v with derived speed and direction), units, label, frame key, valid time,
    local time, age, run, method, status and attribution. Non-ok statuses carry `statusText`; no-
    coverage reads "outside product coverage; this is not evidence of clear weather". Never triggers an
    upstream fetch. Time: `latest` is the newest frame (judged stale against now); `now`, an ISO time
    and a past offset choose the observation frame valid at or before the time; a future offset (and any
    time on a forecast product) uses the newest complete forecast run, taking the step valid at or
    before the time. Status order: unavailable (product disabled, source failing, no frame, frame
    expired), no-coverage, no-data, stale, ok; a non-ok value has `value: null`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        lon (float | Unset):
        lat (float | Unset):
        place (str | Unset):
        products (list[str]):
        time (str | Unset):  Default: 'latest'. Example: -PT1H.
        units (GetSampleUnits | Unset):  Default: GetSampleUnits.METRIC.
        record (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SampleResponse]
    """

    kwargs = _get_kwargs(
        lon=lon,
        lat=lat,
        place=place,
        products=products,
        time=time,
        units=units,
        record=record,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    lon: float | Unset = UNSET,
    lat: float | Unset = UNSET,
    place: str | Unset = UNSET,
    products: list[str],
    time: str | Unset = "latest",
    units: GetSampleUnits | Unset = GetSampleUnits.METRIC,
    record: str | Unset = UNSET,
) -> Problem | SampleResponse | None:
    """Sample products at a point

     Per product: value (or u/v with derived speed and direction), units, label, frame key, valid time,
    local time, age, run, method, status and attribution. Non-ok statuses carry `statusText`; no-
    coverage reads "outside product coverage; this is not evidence of clear weather". Never triggers an
    upstream fetch. Time: `latest` is the newest frame (judged stale against now); `now`, an ISO time
    and a past offset choose the observation frame valid at or before the time; a future offset (and any
    time on a forecast product) uses the newest complete forecast run, taking the step valid at or
    before the time. Status order: unavailable (product disabled, source failing, no frame, frame
    expired), no-coverage, no-data, stale, ok; a non-ok value has `value: null`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        lon (float | Unset):
        lat (float | Unset):
        place (str | Unset):
        products (list[str]):
        time (str | Unset):  Default: 'latest'. Example: -PT1H.
        units (GetSampleUnits | Unset):  Default: GetSampleUnits.METRIC.
        record (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SampleResponse
    """

    return (
        await asyncio_detailed(
            client=client,
            lon=lon,
            lat=lat,
            place=place,
            products=products,
            time=time,
            units=units,
            record=record,
        )
    ).parsed
