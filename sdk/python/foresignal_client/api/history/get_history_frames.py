import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_history_frames_format import GetHistoryFramesFormat
from ...models.get_history_frames_geometry import GetHistoryFramesGeometry
from ...models.history_frames import HistoryFrames
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    place: list[str] | Unset = UNSET,
    from_: datetime.datetime,
    to: datetime.datetime,
    step: str,
    max_frames: int | Unset = 240,
    geometry: GetHistoryFramesGeometry | Unset = GetHistoryFramesGeometry.POINT,
    format_: GetHistoryFramesFormat | Unset = GetHistoryFramesFormat.JSON,
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

    params["step"] = step

    params["maxFrames"] = max_frames

    json_geometry: str | Unset = UNSET
    if not isinstance(geometry, Unset):
        json_geometry = geometry.value

    params["geometry"] = json_geometry

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/history/frames",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> HistoryFrames | Problem | None:
    if response.status_code == 200:
        response_200 = HistoryFrames.from_dict(response.json())

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
) -> Response[HistoryFrames | Problem]:
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
    step: str,
    max_frames: int | Unset = 240,
    geometry: GetHistoryFramesGeometry | Unset = GetHistoryFramesGeometry.POINT,
    format_: GetHistoryFramesFormat | Unset = GetHistoryFramesFormat.JSON,
) -> Response[HistoryFrames | Problem]:
    """Keyframe and delta frames for playback

     Keyframe = asOf state at `from`; each frame holds upserts and removals (tombstone, expiry, leaving
    the bbox) for its interval, built in one ordered pass on the transaction axis. More than 20,000
    entities or 2,000,000 scanned rows → 400 FRAMES_TOO_LARGE (narrow bbox or kind). ndjson sends the
    keyframe line first (the envelope without `frames`), then one line per frame. Frame ends are t_i =
    min(from + i·step, to); (to − from)/step frames must not exceed maxFrames. With a bbox only entities
    with a version inside it in the range are read; the caller's restriction and `place` judge each
    version, and an entity whose newest version leaves them is a `left` removal.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        step (str): ≥ 10 s Example: 1m.
        max_frames (int | Unset):  Default: 240.
        geometry (GetHistoryFramesGeometry | Unset):  Default: GetHistoryFramesGeometry.POINT.
        format_ (GetHistoryFramesFormat | Unset):  Default: GetHistoryFramesFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HistoryFrames | Problem]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        place=place,
        from_=from_,
        to=to,
        step=step,
        max_frames=max_frames,
        geometry=geometry,
        format_=format_,
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
    step: str,
    max_frames: int | Unset = 240,
    geometry: GetHistoryFramesGeometry | Unset = GetHistoryFramesGeometry.POINT,
    format_: GetHistoryFramesFormat | Unset = GetHistoryFramesFormat.JSON,
) -> HistoryFrames | Problem | None:
    """Keyframe and delta frames for playback

     Keyframe = asOf state at `from`; each frame holds upserts and removals (tombstone, expiry, leaving
    the bbox) for its interval, built in one ordered pass on the transaction axis. More than 20,000
    entities or 2,000,000 scanned rows → 400 FRAMES_TOO_LARGE (narrow bbox or kind). ndjson sends the
    keyframe line first (the envelope without `frames`), then one line per frame. Frame ends are t_i =
    min(from + i·step, to); (to − from)/step frames must not exceed maxFrames. With a bbox only entities
    with a version inside it in the range are read; the caller's restriction and `place` judge each
    version, and an entity whose newest version leaves them is a `left` removal.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        step (str): ≥ 10 s Example: 1m.
        max_frames (int | Unset):  Default: 240.
        geometry (GetHistoryFramesGeometry | Unset):  Default: GetHistoryFramesGeometry.POINT.
        format_ (GetHistoryFramesFormat | Unset):  Default: GetHistoryFramesFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HistoryFrames | Problem
    """

    return sync_detailed(
        client=client,
        source=source,
        kind=kind,
        bbox=bbox,
        place=place,
        from_=from_,
        to=to,
        step=step,
        max_frames=max_frames,
        geometry=geometry,
        format_=format_,
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
    step: str,
    max_frames: int | Unset = 240,
    geometry: GetHistoryFramesGeometry | Unset = GetHistoryFramesGeometry.POINT,
    format_: GetHistoryFramesFormat | Unset = GetHistoryFramesFormat.JSON,
) -> Response[HistoryFrames | Problem]:
    """Keyframe and delta frames for playback

     Keyframe = asOf state at `from`; each frame holds upserts and removals (tombstone, expiry, leaving
    the bbox) for its interval, built in one ordered pass on the transaction axis. More than 20,000
    entities or 2,000,000 scanned rows → 400 FRAMES_TOO_LARGE (narrow bbox or kind). ndjson sends the
    keyframe line first (the envelope without `frames`), then one line per frame. Frame ends are t_i =
    min(from + i·step, to); (to − from)/step frames must not exceed maxFrames. With a bbox only entities
    with a version inside it in the range are read; the caller's restriction and `place` judge each
    version, and an entity whose newest version leaves them is a `left` removal.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        step (str): ≥ 10 s Example: 1m.
        max_frames (int | Unset):  Default: 240.
        geometry (GetHistoryFramesGeometry | Unset):  Default: GetHistoryFramesGeometry.POINT.
        format_ (GetHistoryFramesFormat | Unset):  Default: GetHistoryFramesFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HistoryFrames | Problem]
    """

    kwargs = _get_kwargs(
        source=source,
        kind=kind,
        bbox=bbox,
        place=place,
        from_=from_,
        to=to,
        step=step,
        max_frames=max_frames,
        geometry=geometry,
        format_=format_,
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
    step: str,
    max_frames: int | Unset = 240,
    geometry: GetHistoryFramesGeometry | Unset = GetHistoryFramesGeometry.POINT,
    format_: GetHistoryFramesFormat | Unset = GetHistoryFramesFormat.JSON,
) -> HistoryFrames | Problem | None:
    """Keyframe and delta frames for playback

     Keyframe = asOf state at `from`; each frame holds upserts and removals (tombstone, expiry, leaving
    the bbox) for its interval, built in one ordered pass on the transaction axis. More than 20,000
    entities or 2,000,000 scanned rows → 400 FRAMES_TOO_LARGE (narrow bbox or kind). ndjson sends the
    keyframe line first (the envelope without `frames`), then one line per frame. Frame ends are t_i =
    min(from + i·step, to); (to − from)/step frames must not exceed maxFrames. With a bbox only entities
    with a version inside it in the range are read; the caller's restriction and `place` judge each
    version, and an entity whose newest version leaves them is a `left` removal.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        place (list[str] | Unset):
        from_ (datetime.datetime):
        to (datetime.datetime):
        step (str): ≥ 10 s Example: 1m.
        max_frames (int | Unset):  Default: 240.
        geometry (GetHistoryFramesGeometry | Unset):  Default: GetHistoryFramesGeometry.POINT.
        format_ (GetHistoryFramesFormat | Unset):  Default: GetHistoryFramesFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HistoryFrames | Problem
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
            step=step,
            max_frames=max_frames,
            geometry=geometry,
            format_=format_,
        )
    ).parsed
