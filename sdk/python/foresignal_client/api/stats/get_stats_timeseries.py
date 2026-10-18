import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_stats_timeseries_bucket import GetStatsTimeseriesBucket
from ...models.get_stats_timeseries_group_by import GetStatsTimeseriesGroupBy
from ...models.get_stats_timeseries_measure import GetStatsTimeseriesMeasure
from ...models.get_stats_timeseries_time_field import GetStatsTimeseriesTimeField
from ...models.problem import Problem
from ...models.stats_series import StatsSeries
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    bucket: GetStatsTimeseriesBucket | Unset = GetStatsTimeseriesBucket.AUTO,
    measure: GetStatsTimeseriesMeasure | Unset = GetStatsTimeseriesMeasure.VERSIONS,
    group_by: GetStatsTimeseriesGroupBy | Unset = GetStatsTimeseriesGroupBy.NONE,
    time_field: GetStatsTimeseriesTimeField
    | Unset = GetStatsTimeseriesTimeField.INGESTED,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params["bbox"] = bbox

    json_place: list[str] | Unset = UNSET
    if not isinstance(place, Unset):
        json_place = place

    params["place"] = json_place

    json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to = to.isoformat()
    params["to"] = json_to

    json_bucket: str | Unset = UNSET
    if not isinstance(bucket, Unset):
        json_bucket = bucket.value

    params["bucket"] = json_bucket

    json_measure: str | Unset = UNSET
    if not isinstance(measure, Unset):
        json_measure = measure.value

    params["measure"] = json_measure

    json_group_by: str | Unset = UNSET
    if not isinstance(group_by, Unset):
        json_group_by = group_by.value

    params["groupBy"] = json_group_by

    json_time_field: str | Unset = UNSET
    if not isinstance(time_field, Unset):
        json_time_field = time_field.value

    params["timeField"] = json_time_field

    json_country: list[str] | Unset = UNSET
    if not isinstance(country, Unset):
        json_country = country

    params["country"] = json_country

    json_admin1: list[str] | Unset = UNSET
    if not isinstance(admin1, Unset):
        json_admin1 = admin1

    params["admin1"] = json_admin1

    json_county: list[str] | Unset = UNSET
    if not isinstance(county, Unset):
        json_county = county

    params["county"] = json_county

    json_region: list[str] | Unset = UNSET
    if not isinstance(region, Unset):
        json_region = region

    params["region"] = json_region

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/stats/timeseries",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | StatsSeries | None:
    if response.status_code == 200:
        response_200 = StatsSeries.from_dict(response.json())

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
) -> Response[Problem | StatsSeries]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    bucket: GetStatsTimeseriesBucket | Unset = GetStatsTimeseriesBucket.AUTO,
    measure: GetStatsTimeseriesMeasure | Unset = GetStatsTimeseriesMeasure.VERSIONS,
    group_by: GetStatsTimeseriesGroupBy | Unset = GetStatsTimeseriesGroupBy.NONE,
    time_field: GetStatsTimeseriesTimeField
    | Unset = GetStatsTimeseriesTimeField.INGESTED,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> Response[Problem | StatsSeries]:
    """Activity time series

     `timeField=ingested` reads the rollup (plus raw rows for the open hour); other time fields read raw
    history and need `from ≥ retainedFrom`; `alerts` reads alert fire times. ≤ 2,000 buckets. 10 s
    timeout (503 QUERY_TIMEOUT).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (GetStatsTimeseriesBucket | Unset):  Default: GetStatsTimeseriesBucket.AUTO.
        measure (GetStatsTimeseriesMeasure | Unset):  Default: GetStatsTimeseriesMeasure.VERSIONS.
        group_by (GetStatsTimeseriesGroupBy | Unset):  Default: GetStatsTimeseriesGroupBy.NONE.
        time_field (GetStatsTimeseriesTimeField | Unset):  Default:
            GetStatsTimeseriesTimeField.INGESTED.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        region (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsSeries]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        place=place,
        from_=from_,
        to=to,
        bucket=bucket,
        measure=measure,
        group_by=group_by,
        time_field=time_field,
        country=country,
        admin1=admin1,
        county=county,
        region=region,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    bucket: GetStatsTimeseriesBucket | Unset = GetStatsTimeseriesBucket.AUTO,
    measure: GetStatsTimeseriesMeasure | Unset = GetStatsTimeseriesMeasure.VERSIONS,
    group_by: GetStatsTimeseriesGroupBy | Unset = GetStatsTimeseriesGroupBy.NONE,
    time_field: GetStatsTimeseriesTimeField
    | Unset = GetStatsTimeseriesTimeField.INGESTED,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> Problem | StatsSeries | None:
    """Activity time series

     `timeField=ingested` reads the rollup (plus raw rows for the open hour); other time fields read raw
    history and need `from ≥ retainedFrom`; `alerts` reads alert fire times. ≤ 2,000 buckets. 10 s
    timeout (503 QUERY_TIMEOUT).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (GetStatsTimeseriesBucket | Unset):  Default: GetStatsTimeseriesBucket.AUTO.
        measure (GetStatsTimeseriesMeasure | Unset):  Default: GetStatsTimeseriesMeasure.VERSIONS.
        group_by (GetStatsTimeseriesGroupBy | Unset):  Default: GetStatsTimeseriesGroupBy.NONE.
        time_field (GetStatsTimeseriesTimeField | Unset):  Default:
            GetStatsTimeseriesTimeField.INGESTED.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        region (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsSeries
    """

    return sync_detailed(
        client=client,
        source=source,
        kind=kind,
        bbox=bbox,
        place=place,
        from_=from_,
        to=to,
        bucket=bucket,
        measure=measure,
        group_by=group_by,
        time_field=time_field,
        country=country,
        admin1=admin1,
        county=county,
        region=region,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    bucket: GetStatsTimeseriesBucket | Unset = GetStatsTimeseriesBucket.AUTO,
    measure: GetStatsTimeseriesMeasure | Unset = GetStatsTimeseriesMeasure.VERSIONS,
    group_by: GetStatsTimeseriesGroupBy | Unset = GetStatsTimeseriesGroupBy.NONE,
    time_field: GetStatsTimeseriesTimeField
    | Unset = GetStatsTimeseriesTimeField.INGESTED,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> Response[Problem | StatsSeries]:
    """Activity time series

     `timeField=ingested` reads the rollup (plus raw rows for the open hour); other time fields read raw
    history and need `from ≥ retainedFrom`; `alerts` reads alert fire times. ≤ 2,000 buckets. 10 s
    timeout (503 QUERY_TIMEOUT).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (GetStatsTimeseriesBucket | Unset):  Default: GetStatsTimeseriesBucket.AUTO.
        measure (GetStatsTimeseriesMeasure | Unset):  Default: GetStatsTimeseriesMeasure.VERSIONS.
        group_by (GetStatsTimeseriesGroupBy | Unset):  Default: GetStatsTimeseriesGroupBy.NONE.
        time_field (GetStatsTimeseriesTimeField | Unset):  Default:
            GetStatsTimeseriesTimeField.INGESTED.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        region (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsSeries]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        place=place,
        from_=from_,
        to=to,
        bucket=bucket,
        measure=measure,
        group_by=group_by,
        time_field=time_field,
        country=country,
        admin1=admin1,
        county=county,
        region=region,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    bucket: GetStatsTimeseriesBucket | Unset = GetStatsTimeseriesBucket.AUTO,
    measure: GetStatsTimeseriesMeasure | Unset = GetStatsTimeseriesMeasure.VERSIONS,
    group_by: GetStatsTimeseriesGroupBy | Unset = GetStatsTimeseriesGroupBy.NONE,
    time_field: GetStatsTimeseriesTimeField
    | Unset = GetStatsTimeseriesTimeField.INGESTED,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> Problem | StatsSeries | None:
    """Activity time series

     `timeField=ingested` reads the rollup (plus raw rows for the open hour); other time fields read raw
    history and need `from ≥ retainedFrom`; `alerts` reads alert fire times. ≤ 2,000 buckets. 10 s
    timeout (503 QUERY_TIMEOUT).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (GetStatsTimeseriesBucket | Unset):  Default: GetStatsTimeseriesBucket.AUTO.
        measure (GetStatsTimeseriesMeasure | Unset):  Default: GetStatsTimeseriesMeasure.VERSIONS.
        group_by (GetStatsTimeseriesGroupBy | Unset):  Default: GetStatsTimeseriesGroupBy.NONE.
        time_field (GetStatsTimeseriesTimeField | Unset):  Default:
            GetStatsTimeseriesTimeField.INGESTED.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        region (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsSeries
    """

    return (
        await asyncio_detailed(
            client=client,
            source=source,
            kind=kind,
            bbox=bbox,
            place=place,
            from_=from_,
            to=to,
            bucket=bucket,
            measure=measure,
            group_by=group_by,
            time_field=time_field,
            country=country,
            admin1=admin1,
            county=county,
            region=region,
        )
    ).parsed
