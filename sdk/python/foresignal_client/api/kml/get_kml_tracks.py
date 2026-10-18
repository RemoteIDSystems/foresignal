import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_kml_tracks_format import GetKmlTracksFormat
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    source_id: str | Unset = UNSET,
    external_id: str | Unset = UNSET,
    kind: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    format_: GetKmlTracksFormat | Unset = GetKmlTracksFormat.KML,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["sourceId"] = source_id

    params["externalId"] = external_id

    params["kind"] = kind

    params["bbox"] = bbox

    json_from_: str | Unset = UNSET
    if not isinstance(from_, Unset):
        json_from_ = from_.isoformat()
    params["from"] = json_from_

    json_to: str | Unset = UNSET
    if not isinstance(to, Unset):
        json_to = to.isoformat()
    params["to"] = json_to

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/kml/tracks.kml",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    source_id: str | Unset = UNSET,
    external_id: str | Unset = UNSET,
    kind: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    format_: GetKmlTracksFormat | Unset = GetKmlTracksFormat.KML,
) -> Response[Problem]:
    """Entity tracks as KML gx:Track

     One `Placemark` with `gx:Track` per entity (`when` plus `gx:coord "lon lat alt"`; altitude from the
    payload altitude field, else 0 with `clampToGround`), from 037's TrackService unsimplified. Select
    one entity with `sourceId`+`externalId`, or many with `kind`+`bbox`. Limits: ≤ 100 entities and ≤
    5,000 points per track, with truncation flags in `ExtendedData`. All upstream strings are escaped
    for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text. The Document
    `description` carries the included sources' attributions. Private filtering and
    `EgressPolicy.allows(FEED)` apply; keys in URLs are redacted from logs.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str | Unset):
        external_id (str | Unset):
        kind (str | Unset):
        bbox (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        format_ (GetKmlTracksFormat | Unset):  Default: GetKmlTracksFormat.KML.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        kind=kind,
        bbox=bbox,
        from_=from_,
        to=to,
        format_=format_,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    source_id: str | Unset = UNSET,
    external_id: str | Unset = UNSET,
    kind: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    format_: GetKmlTracksFormat | Unset = GetKmlTracksFormat.KML,
) -> Problem | None:
    """Entity tracks as KML gx:Track

     One `Placemark` with `gx:Track` per entity (`when` plus `gx:coord "lon lat alt"`; altitude from the
    payload altitude field, else 0 with `clampToGround`), from 037's TrackService unsimplified. Select
    one entity with `sourceId`+`externalId`, or many with `kind`+`bbox`. Limits: ≤ 100 entities and ≤
    5,000 points per track, with truncation flags in `ExtendedData`. All upstream strings are escaped
    for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text. The Document
    `description` carries the included sources' attributions. Private filtering and
    `EgressPolicy.allows(FEED)` apply; keys in URLs are redacted from logs.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str | Unset):
        external_id (str | Unset):
        kind (str | Unset):
        bbox (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        format_ (GetKmlTracksFormat | Unset):  Default: GetKmlTracksFormat.KML.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        source_id=source_id,
        external_id=external_id,
        kind=kind,
        bbox=bbox,
        from_=from_,
        to=to,
        format_=format_,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    source_id: str | Unset = UNSET,
    external_id: str | Unset = UNSET,
    kind: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    format_: GetKmlTracksFormat | Unset = GetKmlTracksFormat.KML,
) -> Response[Problem]:
    """Entity tracks as KML gx:Track

     One `Placemark` with `gx:Track` per entity (`when` plus `gx:coord "lon lat alt"`; altitude from the
    payload altitude field, else 0 with `clampToGround`), from 037's TrackService unsimplified. Select
    one entity with `sourceId`+`externalId`, or many with `kind`+`bbox`. Limits: ≤ 100 entities and ≤
    5,000 points per track, with truncation flags in `ExtendedData`. All upstream strings are escaped
    for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text. The Document
    `description` carries the included sources' attributions. Private filtering and
    `EgressPolicy.allows(FEED)` apply; keys in URLs are redacted from logs.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str | Unset):
        external_id (str | Unset):
        kind (str | Unset):
        bbox (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        format_ (GetKmlTracksFormat | Unset):  Default: GetKmlTracksFormat.KML.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        external_id=external_id,
        kind=kind,
        bbox=bbox,
        from_=from_,
        to=to,
        format_=format_,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    source_id: str | Unset = UNSET,
    external_id: str | Unset = UNSET,
    kind: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    from_: datetime.datetime | Unset = UNSET,
    to: datetime.datetime | Unset = UNSET,
    format_: GetKmlTracksFormat | Unset = GetKmlTracksFormat.KML,
) -> Problem | None:
    """Entity tracks as KML gx:Track

     One `Placemark` with `gx:Track` per entity (`when` plus `gx:coord "lon lat alt"`; altitude from the
    payload altitude field, else 0 with `clampToGround`), from 037's TrackService unsimplified. Select
    one entity with `sourceId`+`externalId`, or many with `kind`+`bbox`. Limits: ≤ 100 entities and ≤
    5,000 points per track, with truncation flags in `ExtendedData`. All upstream strings are escaped
    for XML and HTML (Google Earth renders balloons as HTML); no CDATA with upstream text. The Document
    `description` carries the included sources' attributions. Private filtering and
    `EgressPolicy.allows(FEED)` apply; keys in URLs are redacted from logs.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str | Unset):
        external_id (str | Unset):
        kind (str | Unset):
        bbox (str | Unset):
        from_ (datetime.datetime | Unset):
        to (datetime.datetime | Unset):
        format_ (GetKmlTracksFormat | Unset):  Default: GetKmlTracksFormat.KML.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            source_id=source_id,
            external_id=external_id,
            kind=kind,
            bbox=bbox,
            from_=from_,
            to=to,
            format_=format_,
        )
    ).parsed
