import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_stats_top_by import GetStatsTopBy
from ...models.get_stats_top_measure import GetStatsTopMeasure
from ...models.problem import Problem
from ...models.stats_top import StatsTop
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    by: GetStatsTopBy | Unset = GetStatsTopBy.SOURCE,
    measure: GetStatsTopMeasure | Unset = GetStatsTopMeasure.VERSIONS,
    from_: datetime.datetime,
    to: datetime.datetime,
    limit: int | Unset = 10,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_by: str | Unset = UNSET
    if not isinstance(by, Unset):
        json_by = by.value

    params["by"] = json_by

    json_measure: str | Unset = UNSET
    if not isinstance(measure, Unset):
        json_measure = measure.value

    params["measure"] = json_measure

    json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to = to.isoformat()
    params["to"] = json_to

    params["limit"] = limit

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

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
        "url": "/v1/stats/top",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | StatsTop | None:
    if response.status_code == 200:
        response_200 = StatsTop.from_dict(response.json())

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
) -> Response[Problem | StatsTop]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    by: GetStatsTopBy | Unset = GetStatsTopBy.SOURCE,
    measure: GetStatsTopMeasure | Unset = GetStatsTopMeasure.VERSIONS,
    from_: datetime.datetime,
    to: datetime.datetime,
    limit: int | Unset = 10,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> Response[Problem | StatsTop]:
    """Top sources, kinds, cells or entities

     `by=entity` reads raw history inside retention and returns titles.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        by (GetStatsTopBy | Unset):  Default: GetStatsTopBy.SOURCE.
        measure (GetStatsTopMeasure | Unset):  Default: GetStatsTopMeasure.VERSIONS.
        from_ (datetime.datetime):
        to (datetime.datetime):
        limit (int | Unset):  Default: 10.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        region (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsTop]
    """

    kwargs = _get_kwargs(
        by=by,
        measure=measure,
        from_=from_,
        to=to,
        limit=limit,
        source=source,
        kind=kind,
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
    by: GetStatsTopBy | Unset = GetStatsTopBy.SOURCE,
    measure: GetStatsTopMeasure | Unset = GetStatsTopMeasure.VERSIONS,
    from_: datetime.datetime,
    to: datetime.datetime,
    limit: int | Unset = 10,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> Problem | StatsTop | None:
    """Top sources, kinds, cells or entities

     `by=entity` reads raw history inside retention and returns titles.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        by (GetStatsTopBy | Unset):  Default: GetStatsTopBy.SOURCE.
        measure (GetStatsTopMeasure | Unset):  Default: GetStatsTopMeasure.VERSIONS.
        from_ (datetime.datetime):
        to (datetime.datetime):
        limit (int | Unset):  Default: 10.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        region (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsTop
    """

    return sync_detailed(
        client=client,
        by=by,
        measure=measure,
        from_=from_,
        to=to,
        limit=limit,
        source=source,
        kind=kind,
        country=country,
        admin1=admin1,
        county=county,
        region=region,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    by: GetStatsTopBy | Unset = GetStatsTopBy.SOURCE,
    measure: GetStatsTopMeasure | Unset = GetStatsTopMeasure.VERSIONS,
    from_: datetime.datetime,
    to: datetime.datetime,
    limit: int | Unset = 10,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> Response[Problem | StatsTop]:
    """Top sources, kinds, cells or entities

     `by=entity` reads raw history inside retention and returns titles.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        by (GetStatsTopBy | Unset):  Default: GetStatsTopBy.SOURCE.
        measure (GetStatsTopMeasure | Unset):  Default: GetStatsTopMeasure.VERSIONS.
        from_ (datetime.datetime):
        to (datetime.datetime):
        limit (int | Unset):  Default: 10.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        region (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsTop]
    """

    kwargs = _get_kwargs(
        by=by,
        measure=measure,
        from_=from_,
        to=to,
        limit=limit,
        source=source,
        kind=kind,
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
    by: GetStatsTopBy | Unset = GetStatsTopBy.SOURCE,
    measure: GetStatsTopMeasure | Unset = GetStatsTopMeasure.VERSIONS,
    from_: datetime.datetime,
    to: datetime.datetime,
    limit: int | Unset = 10,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
) -> Problem | StatsTop | None:
    """Top sources, kinds, cells or entities

     `by=entity` reads raw history inside retention and returns titles.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        by (GetStatsTopBy | Unset):  Default: GetStatsTopBy.SOURCE.
        measure (GetStatsTopMeasure | Unset):  Default: GetStatsTopMeasure.VERSIONS.
        from_ (datetime.datetime):
        to (datetime.datetime):
        limit (int | Unset):  Default: 10.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        region (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsTop
    """

    return (
        await asyncio_detailed(
            client=client,
            by=by,
            measure=measure,
            from_=from_,
            to=to,
            limit=limit,
            source=source,
            kind=kind,
            country=country,
            admin1=admin1,
            county=county,
            region=region,
        )
    ).parsed
