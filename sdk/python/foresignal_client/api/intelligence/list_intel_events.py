import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.intel_event_page import IntelEventPage
from ...models.list_intel_events_status_item import ListIntelEventsStatusItem
from ...models.list_intel_events_type_item import ListIntelEventsTypeItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    type_: list[ListIntelEventsTypeItem] | Unset = UNSET,
    status: list[ListIntelEventsStatusItem] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_type_: list[str] | Unset = UNSET
    if not isinstance(type_, Unset):
        json_type_ = []
        for type_item_data in type_:
            type_item = type_item_data.value
            json_type_.append(type_item)

    params["type"] = json_type_

    json_status: list[str] | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = []
        for status_item_data in status:
            status_item = status_item_data.value
            json_status.append(status_item)

    params["status"] = json_status

    params["bbox"] = bbox

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["minConfidence"] = min_confidence

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/intel/events",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> IntelEventPage | Problem | None:
    if response.status_code == 200:
        response_200 = IntelEventPage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[IntelEventPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    type_: list[ListIntelEventsTypeItem] | Unset = UNSET,
    status: list[ListIntelEventsStatusItem] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[IntelEventPage | Problem]:
    """List intelligence events

     Keyset on (updatedAt, id). Private events are visible only to callers allowed private egress.
    Clients resync after SSE gaps with `since`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListIntelEventsTypeItem] | Unset):
        status (list[ListIntelEventsStatusItem] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        min_confidence (float | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelEventPage | Problem]
    """

    kwargs = _get_kwargs(
        type_=type_,
        status=status,
        bbox=bbox,
        since=since,
        min_confidence=min_confidence,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    type_: list[ListIntelEventsTypeItem] | Unset = UNSET,
    status: list[ListIntelEventsStatusItem] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> IntelEventPage | Problem | None:
    """List intelligence events

     Keyset on (updatedAt, id). Private events are visible only to callers allowed private egress.
    Clients resync after SSE gaps with `since`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListIntelEventsTypeItem] | Unset):
        status (list[ListIntelEventsStatusItem] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        min_confidence (float | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IntelEventPage | Problem
    """

    return sync_detailed(
        client=client,
        type_=type_,
        status=status,
        bbox=bbox,
        since=since,
        min_confidence=min_confidence,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    type_: list[ListIntelEventsTypeItem] | Unset = UNSET,
    status: list[ListIntelEventsStatusItem] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[IntelEventPage | Problem]:
    """List intelligence events

     Keyset on (updatedAt, id). Private events are visible only to callers allowed private egress.
    Clients resync after SSE gaps with `since`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListIntelEventsTypeItem] | Unset):
        status (list[ListIntelEventsStatusItem] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        min_confidence (float | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelEventPage | Problem]
    """

    kwargs = _get_kwargs(
        type_=type_,
        status=status,
        bbox=bbox,
        since=since,
        min_confidence=min_confidence,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    type_: list[ListIntelEventsTypeItem] | Unset = UNSET,
    status: list[ListIntelEventsStatusItem] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    min_confidence: float | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> IntelEventPage | Problem | None:
    """List intelligence events

     Keyset on (updatedAt, id). Private events are visible only to callers allowed private egress.
    Clients resync after SSE gaps with `since`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListIntelEventsTypeItem] | Unset):
        status (list[ListIntelEventsStatusItem] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        min_confidence (float | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IntelEventPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            type_=type_,
            status=status,
            bbox=bbox,
            since=since,
            min_confidence=min_confidence,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
