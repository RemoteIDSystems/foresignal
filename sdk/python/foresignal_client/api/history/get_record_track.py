import datetime
from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_record_track_format import GetRecordTrackFormat
from ...models.problem import Problem
from ...models.track import Track
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    external_id: str,
    *,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    tolerance: str | Unset = "auto",
    max_points: int | Unset = 2000,
    gap: str | Unset = UNSET,
    include_live: bool | Unset = True,
    fields: list[str] | Unset = UNSET,
    format_: GetRecordTrackFormat | Unset = GetRecordTrackFormat.JSON,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    params["tolerance"] = tolerance

    params["maxPoints"] = max_points

    params["gap"] = gap

    params["includeLive"] = include_live

    json_fields: list[str] | Unset = UNSET
    if not isinstance(fields, Unset):
        json_fields = fields

    params["fields"] = json_fields

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/records/{source_id}/{external_id}/track".format(
            source_id=quote(str(source_id), safe=""),
            external_id=quote(str(external_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | Track | None:
    if response.status_code == 200:
        response_200 = Track.from_dict(response.json())

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
) -> Response[Any | Problem | Track]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    tolerance: str | Unset = "auto",
    max_points: int | Unset = 2000,
    gap: str | Unset = UNSET,
    include_live: bool | Unset = True,
    fields: list[str] | Unset = UNSET,
    format_: GetRecordTrackFormat | Unset = GetRecordTrackFormat.JSON,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | Track]:
    """Simplified track of one entity

     Versions in [from, to] by seq (≤ 200,000 rows scanned, else `truncated`), POSITION_STALE carry-
    forwards skipped, new segment after a tombstone or a gap > `gap`, the live position appended when
    newer, Douglas–Peucker simplification keeping endpoints and FIRST/REAPPEAR/SIGNIFICANT/TRIGGER_EDGE
    points, split at the antimeridian. Window larger than the source's retention → 400
    TRACK_WINDOW_TOO_LARGE. Caching as asOf tiles, keyed on `to`; a response with an appended live point
    is never immutable. Simplification is Douglas–Peucker with the synchronized Euclidean distance (each
    point against the time-interpolated position on the simplified segment, in meters on a local
    equirectangular projection); versions outside the caller's restriction break the track. A segment
    with a single point repeats it, so every segment is a valid LineString.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        tolerance (str | Unset):  Default: 'auto'.
        max_points (int | Unset):  Default: 2000.
        gap (str | Unset): Segment break; default max(ttl, 10m) Example: 10m.
        include_live (bool | Unset):  Default: True.
        fields (list[str] | Unset):
        format_ (GetRecordTrackFormat | Unset):  Default: GetRecordTrackFormat.JSON.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | Track]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        from_=from_,
        to=to,
        tolerance=tolerance,
        max_points=max_points,
        gap=gap,
        include_live=include_live,
        fields=fields,
        format_=format_,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    tolerance: str | Unset = "auto",
    max_points: int | Unset = 2000,
    gap: str | Unset = UNSET,
    include_live: bool | Unset = True,
    fields: list[str] | Unset = UNSET,
    format_: GetRecordTrackFormat | Unset = GetRecordTrackFormat.JSON,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | Track | None:
    """Simplified track of one entity

     Versions in [from, to] by seq (≤ 200,000 rows scanned, else `truncated`), POSITION_STALE carry-
    forwards skipped, new segment after a tombstone or a gap > `gap`, the live position appended when
    newer, Douglas–Peucker simplification keeping endpoints and FIRST/REAPPEAR/SIGNIFICANT/TRIGGER_EDGE
    points, split at the antimeridian. Window larger than the source's retention → 400
    TRACK_WINDOW_TOO_LARGE. Caching as asOf tiles, keyed on `to`; a response with an appended live point
    is never immutable. Simplification is Douglas–Peucker with the synchronized Euclidean distance (each
    point against the time-interpolated position on the simplified segment, in meters on a local
    equirectangular projection); versions outside the caller's restriction break the track. A segment
    with a single point repeats it, so every segment is a valid LineString.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        tolerance (str | Unset):  Default: 'auto'.
        max_points (int | Unset):  Default: 2000.
        gap (str | Unset): Segment break; default max(ttl, 10m) Example: 10m.
        include_live (bool | Unset):  Default: True.
        fields (list[str] | Unset):
        format_ (GetRecordTrackFormat | Unset):  Default: GetRecordTrackFormat.JSON.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | Track
    """

    return sync_detailed(
        source_id=source_id,
        external_id=external_id,
        client=client,
        from_=from_,
        to=to,
        tolerance=tolerance,
        max_points=max_points,
        gap=gap,
        include_live=include_live,
        fields=fields,
        format_=format_,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    tolerance: str | Unset = "auto",
    max_points: int | Unset = 2000,
    gap: str | Unset = UNSET,
    include_live: bool | Unset = True,
    fields: list[str] | Unset = UNSET,
    format_: GetRecordTrackFormat | Unset = GetRecordTrackFormat.JSON,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | Track]:
    """Simplified track of one entity

     Versions in [from, to] by seq (≤ 200,000 rows scanned, else `truncated`), POSITION_STALE carry-
    forwards skipped, new segment after a tombstone or a gap > `gap`, the live position appended when
    newer, Douglas–Peucker simplification keeping endpoints and FIRST/REAPPEAR/SIGNIFICANT/TRIGGER_EDGE
    points, split at the antimeridian. Window larger than the source's retention → 400
    TRACK_WINDOW_TOO_LARGE. Caching as asOf tiles, keyed on `to`; a response with an appended live point
    is never immutable. Simplification is Douglas–Peucker with the synchronized Euclidean distance (each
    point against the time-interpolated position on the simplified segment, in meters on a local
    equirectangular projection); versions outside the caller's restriction break the track. A segment
    with a single point repeats it, so every segment is a valid LineString.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        tolerance (str | Unset):  Default: 'auto'.
        max_points (int | Unset):  Default: 2000.
        gap (str | Unset): Segment break; default max(ttl, 10m) Example: 10m.
        include_live (bool | Unset):  Default: True.
        fields (list[str] | Unset):
        format_ (GetRecordTrackFormat | Unset):  Default: GetRecordTrackFormat.JSON.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | Track]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        from_=from_,
        to=to,
        tolerance=tolerance,
        max_points=max_points,
        gap=gap,
        include_live=include_live,
        fields=fields,
        format_=format_,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    external_id: str,
    *,
    client: AuthenticatedClient,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    tolerance: str | Unset = "auto",
    max_points: int | Unset = 2000,
    gap: str | Unset = UNSET,
    include_live: bool | Unset = True,
    fields: list[str] | Unset = UNSET,
    format_: GetRecordTrackFormat | Unset = GetRecordTrackFormat.JSON,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | Track | None:
    """Simplified track of one entity

     Versions in [from, to] by seq (≤ 200,000 rows scanned, else `truncated`), POSITION_STALE carry-
    forwards skipped, new segment after a tombstone or a gap > `gap`, the live position appended when
    newer, Douglas–Peucker simplification keeping endpoints and FIRST/REAPPEAR/SIGNIFICANT/TRIGGER_EDGE
    points, split at the antimeridian. Window larger than the source's retention → 400
    TRACK_WINDOW_TOO_LARGE. Caching as asOf tiles, keyed on `to`; a response with an appended live point
    is never immutable. Simplification is Douglas–Peucker with the synchronized Euclidean distance (each
    point against the time-interpolated position on the simplified segment, in meters on a local
    equirectangular projection); versions outside the caller's restriction break the track. A segment
    with a single point repeats it, so every segment is a valid LineString.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):
        external_id (str):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        tolerance (str | Unset):  Default: 'auto'.
        max_points (int | Unset):  Default: 2000.
        gap (str | Unset): Segment break; default max(ttl, 10m) Example: 10m.
        include_live (bool | Unset):  Default: True.
        fields (list[str] | Unset):
        format_ (GetRecordTrackFormat | Unset):  Default: GetRecordTrackFormat.JSON.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | Track
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            external_id=external_id,
            client=client,
            from_=from_,
            to=to,
            tolerance=tolerance,
            max_points=max_points,
            gap=gap,
            include_live=include_live,
            fields=fields,
            format_=format_,
            if_none_match=if_none_match,
        )
    ).parsed
