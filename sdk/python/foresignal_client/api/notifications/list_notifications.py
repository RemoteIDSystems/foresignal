from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_notifications_events_item import ListNotificationsEventsItem
from ...models.list_notifications_min_severity import ListNotificationsMinSeverity
from ...models.notification_page import NotificationPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    after: int | Unset = UNSET,
    before: int | Unset = UNSET,
    events: list[ListNotificationsEventsItem] | Unset = UNSET,
    min_severity: ListNotificationsMinSeverity | Unset = UNSET,
    unread: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    foresignal_device_id: UUID | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_device_id, Unset):
        headers["Foresignal-Device-Id"] = foresignal_device_id

    params: dict[str, Any] = {}

    params["after"] = after

    params["before"] = before

    json_events: list[str] | Unset = UNSET
    if not isinstance(events, Unset):
        json_events = []
        for events_item_data in events:
            events_item = events_item_data.value
            json_events.append(events_item)

    params["events"] = json_events

    json_min_severity: str | Unset = UNSET
    if not isinstance(min_severity, Unset):
        json_min_severity = min_severity.value

    params["minSeverity"] = json_min_severity

    params["unread"] = unread

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/notifications",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NotificationPage | Problem | None:
    if response.status_code == 200:
        response_200 = NotificationPage.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[NotificationPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    before: int | Unset = UNSET,
    events: list[ListNotificationsEventsItem] | Unset = UNSET,
    min_severity: ListNotificationsMinSeverity | Unset = UNSET,
    unread: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[NotificationPage | Problem]:
    """List inbox notifications

     Newest first, keyset on id: `before` pages to older items, `after` returns only newer ones. Read
    state is per device (token device metadata, else the `Foresignal-Device-Id` header combined with the
    token id; else 400 DEVICE_ID_REQUIRED). Includes system events. Reading never acknowledges an alert.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        after (int | Unset):
        before (int | Unset):
        events (list[ListNotificationsEventsItem] | Unset):
        min_severity (ListNotificationsMinSeverity | Unset):
        unread (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationPage | Problem]
    """

    kwargs = _get_kwargs(
        after=after,
        before=before,
        events=events,
        min_severity=min_severity,
        unread=unread,
        cursor=cursor,
        limit=limit,
        foresignal_device_id=foresignal_device_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    before: int | Unset = UNSET,
    events: list[ListNotificationsEventsItem] | Unset = UNSET,
    min_severity: ListNotificationsMinSeverity | Unset = UNSET,
    unread: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    foresignal_device_id: UUID | Unset = UNSET,
) -> NotificationPage | Problem | None:
    """List inbox notifications

     Newest first, keyset on id: `before` pages to older items, `after` returns only newer ones. Read
    state is per device (token device metadata, else the `Foresignal-Device-Id` header combined with the
    token id; else 400 DEVICE_ID_REQUIRED). Includes system events. Reading never acknowledges an alert.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        after (int | Unset):
        before (int | Unset):
        events (list[ListNotificationsEventsItem] | Unset):
        min_severity (ListNotificationsMinSeverity | Unset):
        unread (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationPage | Problem
    """

    return sync_detailed(
        client=client,
        after=after,
        before=before,
        events=events,
        min_severity=min_severity,
        unread=unread,
        cursor=cursor,
        limit=limit,
        foresignal_device_id=foresignal_device_id,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    before: int | Unset = UNSET,
    events: list[ListNotificationsEventsItem] | Unset = UNSET,
    min_severity: ListNotificationsMinSeverity | Unset = UNSET,
    unread: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[NotificationPage | Problem]:
    """List inbox notifications

     Newest first, keyset on id: `before` pages to older items, `after` returns only newer ones. Read
    state is per device (token device metadata, else the `Foresignal-Device-Id` header combined with the
    token id; else 400 DEVICE_ID_REQUIRED). Includes system events. Reading never acknowledges an alert.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        after (int | Unset):
        before (int | Unset):
        events (list[ListNotificationsEventsItem] | Unset):
        min_severity (ListNotificationsMinSeverity | Unset):
        unread (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationPage | Problem]
    """

    kwargs = _get_kwargs(
        after=after,
        before=before,
        events=events,
        min_severity=min_severity,
        unread=unread,
        cursor=cursor,
        limit=limit,
        foresignal_device_id=foresignal_device_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    before: int | Unset = UNSET,
    events: list[ListNotificationsEventsItem] | Unset = UNSET,
    min_severity: ListNotificationsMinSeverity | Unset = UNSET,
    unread: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    foresignal_device_id: UUID | Unset = UNSET,
) -> NotificationPage | Problem | None:
    """List inbox notifications

     Newest first, keyset on id: `before` pages to older items, `after` returns only newer ones. Read
    state is per device (token device metadata, else the `Foresignal-Device-Id` header combined with the
    token id; else 400 DEVICE_ID_REQUIRED). Includes system events. Reading never acknowledges an alert.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        after (int | Unset):
        before (int | Unset):
        events (list[ListNotificationsEventsItem] | Unset):
        min_severity (ListNotificationsMinSeverity | Unset):
        unread (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            after=after,
            before=before,
            events=events,
            min_severity=min_severity,
            unread=unread,
            cursor=cursor,
            limit=limit,
            foresignal_device_id=foresignal_device_id,
        )
    ).parsed
