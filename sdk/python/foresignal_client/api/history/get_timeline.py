import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_timeline_group_by import GetTimelineGroupBy
from ...models.get_timeline_measure import GetTimelineMeasure
from ...models.get_timeline_time_field import GetTimelineTimeField
from ...models.problem import Problem
from ...models.timeline import Timeline
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    bucket: str | Unset = "auto",
    time_field: GetTimelineTimeField | Unset = GetTimelineTimeField.OBSERVED,
    measure: GetTimelineMeasure | Unset = GetTimelineMeasure.VERSIONS,
    group_by: GetTimelineGroupBy | Unset = GetTimelineGroupBy.NONE,
    include_keyframes: bool | Unset = False,
    include_alerts: bool | Unset = False,
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

    params["bucket"] = bucket

    json_time_field: str | Unset = UNSET
    if not isinstance(time_field, Unset):
        json_time_field = time_field.value

    params["timeField"] = json_time_field

    json_measure: str | Unset = UNSET
    if not isinstance(measure, Unset):
        json_measure = measure.value

    params["measure"] = json_measure

    json_group_by: str | Unset = UNSET
    if not isinstance(group_by, Unset):
        json_group_by = group_by.value

    params["groupBy"] = json_group_by

    params["includeKeyframes"] = include_keyframes

    params["includeAlerts"] = include_alerts

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/timeline",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Timeline | None:
    if response.status_code == 200:
        response_200 = Timeline.from_dict(response.json())

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

    if response.status_code == 422:
        response_422 = Problem.from_dict(response.json())

        return response_422

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
) -> Response[Problem | Timeline]:
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
    bucket: str | Unset = "auto",
    time_field: GetTimelineTimeField | Unset = GetTimelineTimeField.OBSERVED,
    measure: GetTimelineMeasure | Unset = GetTimelineMeasure.VERSIONS,
    group_by: GetTimelineGroupBy | Unset = GetTimelineGroupBy.NONE,
    include_keyframes: bool | Unset = False,
    include_alerts: bool | Unset = False,
) -> Response[Problem | Timeline]:
    """Event-density timeline

     `date_bin` histogram over record versions (HEARTBEAT and KEYFRAME excluded unless
    `includeKeyframes`), optionally grouped and with alert counts. ≤ 1,000 buckets (400
    TIMELINE_TOO_LARGE); 10 s timeout (503 QUERY_TIMEOUT). A range whose raw read the hourly activity
    rollup estimates above 1,000,000 record versions (200,000 with `timeField=ingested`, which counts
    far slower), from the later of `from` and `retainedFrom` for the involved sources and kinds, is
    refused up front with 422 TIMELINE_RANGE_UNSUPPORTED instead of running into the timeout; narrow the
    range, the sources or the kinds, or use getStatsTimeseries, which serves long ranges from the
    rollups. `bucket=auto` picks the smallest of 1m, 5m, 10m, 15m, 30m, 1h, 2h, 3h, 6h, 12h, 1d, 2d, 7d,
    14d, 30d giving at most 200 buckets; buckets start at `from` and every bucket in range is returned,
    empty ones included. Without `source` or `kind`, every source the caller may see is counted.
    `timeField=ingested` is translated to seq bounds through seq_clock. `retainedFrom` is the latest of
    now − window over the involved sources. Not the same as /v1/products/timeline (051).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (str | Unset):  Default: 'auto'. Example: 1h.
        time_field (GetTimelineTimeField | Unset):  Default: GetTimelineTimeField.OBSERVED.
        measure (GetTimelineMeasure | Unset):  Default: GetTimelineMeasure.VERSIONS.
        group_by (GetTimelineGroupBy | Unset):  Default: GetTimelineGroupBy.NONE.
        include_keyframes (bool | Unset):  Default: False.
        include_alerts (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Timeline]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        place=place,
        from_=from_,
        to=to,
        bucket=bucket,
        time_field=time_field,
        measure=measure,
        group_by=group_by,
        include_keyframes=include_keyframes,
        include_alerts=include_alerts,
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
    bucket: str | Unset = "auto",
    time_field: GetTimelineTimeField | Unset = GetTimelineTimeField.OBSERVED,
    measure: GetTimelineMeasure | Unset = GetTimelineMeasure.VERSIONS,
    group_by: GetTimelineGroupBy | Unset = GetTimelineGroupBy.NONE,
    include_keyframes: bool | Unset = False,
    include_alerts: bool | Unset = False,
) -> Problem | Timeline | None:
    """Event-density timeline

     `date_bin` histogram over record versions (HEARTBEAT and KEYFRAME excluded unless
    `includeKeyframes`), optionally grouped and with alert counts. ≤ 1,000 buckets (400
    TIMELINE_TOO_LARGE); 10 s timeout (503 QUERY_TIMEOUT). A range whose raw read the hourly activity
    rollup estimates above 1,000,000 record versions (200,000 with `timeField=ingested`, which counts
    far slower), from the later of `from` and `retainedFrom` for the involved sources and kinds, is
    refused up front with 422 TIMELINE_RANGE_UNSUPPORTED instead of running into the timeout; narrow the
    range, the sources or the kinds, or use getStatsTimeseries, which serves long ranges from the
    rollups. `bucket=auto` picks the smallest of 1m, 5m, 10m, 15m, 30m, 1h, 2h, 3h, 6h, 12h, 1d, 2d, 7d,
    14d, 30d giving at most 200 buckets; buckets start at `from` and every bucket in range is returned,
    empty ones included. Without `source` or `kind`, every source the caller may see is counted.
    `timeField=ingested` is translated to seq bounds through seq_clock. `retainedFrom` is the latest of
    now − window over the involved sources. Not the same as /v1/products/timeline (051).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (str | Unset):  Default: 'auto'. Example: 1h.
        time_field (GetTimelineTimeField | Unset):  Default: GetTimelineTimeField.OBSERVED.
        measure (GetTimelineMeasure | Unset):  Default: GetTimelineMeasure.VERSIONS.
        group_by (GetTimelineGroupBy | Unset):  Default: GetTimelineGroupBy.NONE.
        include_keyframes (bool | Unset):  Default: False.
        include_alerts (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Timeline
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
        time_field=time_field,
        measure=measure,
        group_by=group_by,
        include_keyframes=include_keyframes,
        include_alerts=include_alerts,
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
    bucket: str | Unset = "auto",
    time_field: GetTimelineTimeField | Unset = GetTimelineTimeField.OBSERVED,
    measure: GetTimelineMeasure | Unset = GetTimelineMeasure.VERSIONS,
    group_by: GetTimelineGroupBy | Unset = GetTimelineGroupBy.NONE,
    include_keyframes: bool | Unset = False,
    include_alerts: bool | Unset = False,
) -> Response[Problem | Timeline]:
    """Event-density timeline

     `date_bin` histogram over record versions (HEARTBEAT and KEYFRAME excluded unless
    `includeKeyframes`), optionally grouped and with alert counts. ≤ 1,000 buckets (400
    TIMELINE_TOO_LARGE); 10 s timeout (503 QUERY_TIMEOUT). A range whose raw read the hourly activity
    rollup estimates above 1,000,000 record versions (200,000 with `timeField=ingested`, which counts
    far slower), from the later of `from` and `retainedFrom` for the involved sources and kinds, is
    refused up front with 422 TIMELINE_RANGE_UNSUPPORTED instead of running into the timeout; narrow the
    range, the sources or the kinds, or use getStatsTimeseries, which serves long ranges from the
    rollups. `bucket=auto` picks the smallest of 1m, 5m, 10m, 15m, 30m, 1h, 2h, 3h, 6h, 12h, 1d, 2d, 7d,
    14d, 30d giving at most 200 buckets; buckets start at `from` and every bucket in range is returned,
    empty ones included. Without `source` or `kind`, every source the caller may see is counted.
    `timeField=ingested` is translated to seq bounds through seq_clock. `retainedFrom` is the latest of
    now − window over the involved sources. Not the same as /v1/products/timeline (051).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (str | Unset):  Default: 'auto'. Example: 1h.
        time_field (GetTimelineTimeField | Unset):  Default: GetTimelineTimeField.OBSERVED.
        measure (GetTimelineMeasure | Unset):  Default: GetTimelineMeasure.VERSIONS.
        group_by (GetTimelineGroupBy | Unset):  Default: GetTimelineGroupBy.NONE.
        include_keyframes (bool | Unset):  Default: False.
        include_alerts (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Timeline]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        place=place,
        from_=from_,
        to=to,
        bucket=bucket,
        time_field=time_field,
        measure=measure,
        group_by=group_by,
        include_keyframes=include_keyframes,
        include_alerts=include_alerts,
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
    bucket: str | Unset = "auto",
    time_field: GetTimelineTimeField | Unset = GetTimelineTimeField.OBSERVED,
    measure: GetTimelineMeasure | Unset = GetTimelineMeasure.VERSIONS,
    group_by: GetTimelineGroupBy | Unset = GetTimelineGroupBy.NONE,
    include_keyframes: bool | Unset = False,
    include_alerts: bool | Unset = False,
) -> Problem | Timeline | None:
    """Event-density timeline

     `date_bin` histogram over record versions (HEARTBEAT and KEYFRAME excluded unless
    `includeKeyframes`), optionally grouped and with alert counts. ≤ 1,000 buckets (400
    TIMELINE_TOO_LARGE); 10 s timeout (503 QUERY_TIMEOUT). A range whose raw read the hourly activity
    rollup estimates above 1,000,000 record versions (200,000 with `timeField=ingested`, which counts
    far slower), from the later of `from` and `retainedFrom` for the involved sources and kinds, is
    refused up front with 422 TIMELINE_RANGE_UNSUPPORTED instead of running into the timeout; narrow the
    range, the sources or the kinds, or use getStatsTimeseries, which serves long ranges from the
    rollups. `bucket=auto` picks the smallest of 1m, 5m, 10m, 15m, 30m, 1h, 2h, 3h, 6h, 12h, 1d, 2d, 7d,
    14d, 30d giving at most 200 buckets; buckets start at `from` and every bucket in range is returned,
    empty ones included. Without `source` or `kind`, every source the caller may see is counted.
    `timeField=ingested` is translated to seq bounds through seq_clock. `retainedFrom` is the latest of
    now − window over the involved sources. Not the same as /v1/products/timeline (051).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        bucket (str | Unset):  Default: 'auto'. Example: 1h.
        time_field (GetTimelineTimeField | Unset):  Default: GetTimelineTimeField.OBSERVED.
        measure (GetTimelineMeasure | Unset):  Default: GetTimelineMeasure.VERSIONS.
        group_by (GetTimelineGroupBy | Unset):  Default: GetTimelineGroupBy.NONE.
        include_keyframes (bool | Unset):  Default: False.
        include_alerts (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Timeline
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
            time_field=time_field,
            measure=measure,
            group_by=group_by,
            include_keyframes=include_keyframes,
            include_alerts=include_alerts,
        )
    ).parsed
