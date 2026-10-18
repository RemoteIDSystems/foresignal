import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_stats_grid_measure import GetStatsGridMeasure
from ...models.get_stats_grid_mode import GetStatsGridMode
from ...models.problem import Problem
from ...models.stats_grid import StatsGrid
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str,
    from_: datetime.datetime,
    to: datetime.datetime,
    measure: GetStatsGridMeasure | Unset = GetStatsGridMeasure.VERSIONS,
    res: int | Unset = UNSET,
    mode: GetStatsGridMode | Unset = GetStatsGridMode.ROLLUP,
    size_km: float | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
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

    json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to = to.isoformat()
    params["to"] = json_to

    json_measure: str | Unset = UNSET
    if not isinstance(measure, Unset):
        json_measure = measure.value

    params["measure"] = json_measure

    params["res"] = res

    json_mode: str | Unset = UNSET
    if not isinstance(mode, Unset):
        json_mode = mode.value

    params["mode"] = json_mode

    params["sizeKm"] = size_km

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

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/stats/grid",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | StatsGrid | None:
    if response.status_code == 200:
        response_200 = StatsGrid.from_dict(response.json())

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
) -> Response[Problem | StatsGrid]:
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
    bbox: str,
    from_: datetime.datetime,
    to: datetime.datetime,
    measure: GetStatsGridMeasure | Unset = GetStatsGridMeasure.VERSIONS,
    res: int | Unset = UNSET,
    mode: GetStatsGridMode | Unset = GetStatsGridMode.ROLLUP,
    size_km: float | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
) -> Response[Problem | StatsGrid]:
    """Spatial activity grid

     Rollup mode (hourly rows for ranges up to 7 days, daily rows beyond; closed buckets only): H3 cells
    aggregated to `res` (≤ cellRes) with boundaries split at the antimeridian. Raw mode
    (`mode=raw&sizeKm=`, range ≤ 7d): hexagons in EPSG:3857. ≤ 10,000 cells (400 STATS_TOO_LARGE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str):
        from_ (datetime.datetime):
        to (datetime.datetime):
        measure (GetStatsGridMeasure | Unset):  Default: GetStatsGridMeasure.VERSIONS.
        res (int | Unset):
        mode (GetStatsGridMode | Unset):  Default: GetStatsGridMode.ROLLUP.
        size_km (float | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsGrid]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        from_=from_,
        to=to,
        measure=measure,
        res=res,
        mode=mode,
        size_km=size_km,
        country=country,
        admin1=admin1,
        county=county,
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
    bbox: str,
    from_: datetime.datetime,
    to: datetime.datetime,
    measure: GetStatsGridMeasure | Unset = GetStatsGridMeasure.VERSIONS,
    res: int | Unset = UNSET,
    mode: GetStatsGridMode | Unset = GetStatsGridMode.ROLLUP,
    size_km: float | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
) -> Problem | StatsGrid | None:
    """Spatial activity grid

     Rollup mode (hourly rows for ranges up to 7 days, daily rows beyond; closed buckets only): H3 cells
    aggregated to `res` (≤ cellRes) with boundaries split at the antimeridian. Raw mode
    (`mode=raw&sizeKm=`, range ≤ 7d): hexagons in EPSG:3857. ≤ 10,000 cells (400 STATS_TOO_LARGE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str):
        from_ (datetime.datetime):
        to (datetime.datetime):
        measure (GetStatsGridMeasure | Unset):  Default: GetStatsGridMeasure.VERSIONS.
        res (int | Unset):
        mode (GetStatsGridMode | Unset):  Default: GetStatsGridMode.ROLLUP.
        size_km (float | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsGrid
    """

    return sync_detailed(
        client=client,
        source=source,
        kind=kind,
        bbox=bbox,
        from_=from_,
        to=to,
        measure=measure,
        res=res,
        mode=mode,
        size_km=size_km,
        country=country,
        admin1=admin1,
        county=county,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str,
    from_: datetime.datetime,
    to: datetime.datetime,
    measure: GetStatsGridMeasure | Unset = GetStatsGridMeasure.VERSIONS,
    res: int | Unset = UNSET,
    mode: GetStatsGridMode | Unset = GetStatsGridMode.ROLLUP,
    size_km: float | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
) -> Response[Problem | StatsGrid]:
    """Spatial activity grid

     Rollup mode (hourly rows for ranges up to 7 days, daily rows beyond; closed buckets only): H3 cells
    aggregated to `res` (≤ cellRes) with boundaries split at the antimeridian. Raw mode
    (`mode=raw&sizeKm=`, range ≤ 7d): hexagons in EPSG:3857. ≤ 10,000 cells (400 STATS_TOO_LARGE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str):
        from_ (datetime.datetime):
        to (datetime.datetime):
        measure (GetStatsGridMeasure | Unset):  Default: GetStatsGridMeasure.VERSIONS.
        res (int | Unset):
        mode (GetStatsGridMode | Unset):  Default: GetStatsGridMode.ROLLUP.
        size_km (float | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsGrid]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        from_=from_,
        to=to,
        measure=measure,
        res=res,
        mode=mode,
        size_km=size_km,
        country=country,
        admin1=admin1,
        county=county,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str,
    from_: datetime.datetime,
    to: datetime.datetime,
    measure: GetStatsGridMeasure | Unset = GetStatsGridMeasure.VERSIONS,
    res: int | Unset = UNSET,
    mode: GetStatsGridMode | Unset = GetStatsGridMode.ROLLUP,
    size_km: float | Unset = UNSET,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
) -> Problem | StatsGrid | None:
    """Spatial activity grid

     Rollup mode (hourly rows for ranges up to 7 days, daily rows beyond; closed buckets only): H3 cells
    aggregated to `res` (≤ cellRes) with boundaries split at the antimeridian. Raw mode
    (`mode=raw&sizeKm=`, range ≤ 7d): hexagons in EPSG:3857. ≤ 10,000 cells (400 STATS_TOO_LARGE).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str):
        from_ (datetime.datetime):
        to (datetime.datetime):
        measure (GetStatsGridMeasure | Unset):  Default: GetStatsGridMeasure.VERSIONS.
        res (int | Unset):
        mode (GetStatsGridMode | Unset):  Default: GetStatsGridMode.ROLLUP.
        size_km (float | Unset):
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsGrid
    """

    return (
        await asyncio_detailed(
            client=client,
            source=source,
            kind=kind,
            bbox=bbox,
            from_=from_,
            to=to,
            measure=measure,
            res=res,
            mode=mode,
            size_km=size_km,
            country=country,
            admin1=admin1,
            county=county,
        )
    ).parsed
