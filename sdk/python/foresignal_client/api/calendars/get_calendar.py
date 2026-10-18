from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_calendar_feed import GetCalendarFeed
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    feed: GetCalendarFeed,
    *,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    params["bbox"] = bbox

    params["placeId"] = place_id

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/calendars/{feed}.ics".format(
            feed=quote(str(feed), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | str | None:
    if response.status_code == 200:
        response_200 = response.text
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
) -> Response[Any | Problem | str]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    feed: GetCalendarFeed,
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | str]:
    """Subscribe to an iCalendar feed

     RFC 5545 feed: `launches` (064), `tfr` (087), `nws` (047), `briefings` (next 30 days of occurrences)
    or `custom` (any kind whose descriptor declares startField/endField; `source` and `kind` required).
    UID `{sourceId}:{externalId}@{instanceId}`, SEQUENCE = record version; tombstoned or expired records
    stay 7 days as CANCELLED; at most 2,000 events. ETag and conditional GET; feed keys via
    `/v1/k/{key}/…`. Errors: `400 ICAL_FIELDS_MISSING`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        feed (GetCalendarFeed):
        bbox (str | Unset):
        place_id (str | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | str]
    """

    kwargs = _get_kwargs(
        feed=feed,
        bbox=bbox,
        place_id=place_id,
        source=source,
        kind=kind,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    feed: GetCalendarFeed,
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | str | None:
    """Subscribe to an iCalendar feed

     RFC 5545 feed: `launches` (064), `tfr` (087), `nws` (047), `briefings` (next 30 days of occurrences)
    or `custom` (any kind whose descriptor declares startField/endField; `source` and `kind` required).
    UID `{sourceId}:{externalId}@{instanceId}`, SEQUENCE = record version; tombstoned or expired records
    stay 7 days as CANCELLED; at most 2,000 events. ETag and conditional GET; feed keys via
    `/v1/k/{key}/…`. Errors: `400 ICAL_FIELDS_MISSING`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        feed (GetCalendarFeed):
        bbox (str | Unset):
        place_id (str | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | str
    """

    return sync_detailed(
        feed=feed,
        client=client,
        bbox=bbox,
        place_id=place_id,
        source=source,
        kind=kind,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    feed: GetCalendarFeed,
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | str]:
    """Subscribe to an iCalendar feed

     RFC 5545 feed: `launches` (064), `tfr` (087), `nws` (047), `briefings` (next 30 days of occurrences)
    or `custom` (any kind whose descriptor declares startField/endField; `source` and `kind` required).
    UID `{sourceId}:{externalId}@{instanceId}`, SEQUENCE = record version; tombstoned or expired records
    stay 7 days as CANCELLED; at most 2,000 events. ETag and conditional GET; feed keys via
    `/v1/k/{key}/…`. Errors: `400 ICAL_FIELDS_MISSING`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        feed (GetCalendarFeed):
        bbox (str | Unset):
        place_id (str | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | str]
    """

    kwargs = _get_kwargs(
        feed=feed,
        bbox=bbox,
        place_id=place_id,
        source=source,
        kind=kind,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    feed: GetCalendarFeed,
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    place_id: str | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | str | None:
    """Subscribe to an iCalendar feed

     RFC 5545 feed: `launches` (064), `tfr` (087), `nws` (047), `briefings` (next 30 days of occurrences)
    or `custom` (any kind whose descriptor declares startField/endField; `source` and `kind` required).
    UID `{sourceId}:{externalId}@{instanceId}`, SEQUENCE = record version; tombstoned or expired records
    stay 7 days as CANCELLED; at most 2,000 events. ETag and conditional GET; feed keys via
    `/v1/k/{key}/…`. Errors: `400 ICAL_FIELDS_MISSING`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        feed (GetCalendarFeed):
        bbox (str | Unset):
        place_id (str | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | str
    """

    return (
        await asyncio_detailed(
            feed=feed,
            client=client,
            bbox=bbox,
            place_id=place_id,
            source=source,
            kind=kind,
            if_none_match=if_none_match,
        )
    ).parsed
