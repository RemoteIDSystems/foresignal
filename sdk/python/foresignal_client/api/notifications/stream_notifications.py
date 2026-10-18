from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.heartbeat_event import HeartbeatEvent
from ...models.notification import Notification
from ...models.notification_badge import NotificationBadge
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_device_id: UUID | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(last_event_id, Unset):
        headers["Last-Event-ID"] = last_event_id

    if not isinstance(foresignal_device_id, Unset):
        headers["Foresignal-Device-Id"] = foresignal_device_id

    params: dict[str, Any] = {}

    params["ticket"] = ticket

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/notifications/stream",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> HeartbeatEvent | Notification | NotificationBadge | Problem | None:
    if response.status_code == 200:

        def _parse_response_200(
            data: object,
        ) -> HeartbeatEvent | Notification | NotificationBadge:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = Notification.from_dict(data)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_1 = NotificationBadge.from_dict(data)

                return response_200_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            response_200_type_2 = HeartbeatEvent.from_dict(data)

            return response_200_type_2

        response_200 = _parse_response_200(response.text)

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
) -> Response[HeartbeatEvent | Notification | NotificationBadge | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[HeartbeatEvent | Notification | NotificationBadge | Problem]:
    """Inbox event stream (SSE)

     Server-Sent Events through the 015 channel: `notification` (id = notification id; replayed from the
    table after `Last-Event-ID`) and `badge` (when this device's cursor moves). An authenticated open
    stream counts as `background` presence.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        ticket (str | Unset):
        last_event_id (str | Unset):
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HeartbeatEvent | Notification | NotificationBadge | Problem]
    """

    kwargs = _get_kwargs(
        ticket=ticket,
        last_event_id=last_event_id,
        foresignal_device_id=foresignal_device_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_device_id: UUID | Unset = UNSET,
) -> HeartbeatEvent | Notification | NotificationBadge | Problem | None:
    """Inbox event stream (SSE)

     Server-Sent Events through the 015 channel: `notification` (id = notification id; replayed from the
    table after `Last-Event-ID`) and `badge` (when this device's cursor moves). An authenticated open
    stream counts as `background` presence.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        ticket (str | Unset):
        last_event_id (str | Unset):
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HeartbeatEvent | Notification | NotificationBadge | Problem
    """

    return sync_detailed(
        client=client,
        ticket=ticket,
        last_event_id=last_event_id,
        foresignal_device_id=foresignal_device_id,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[HeartbeatEvent | Notification | NotificationBadge | Problem]:
    """Inbox event stream (SSE)

     Server-Sent Events through the 015 channel: `notification` (id = notification id; replayed from the
    table after `Last-Event-ID`) and `badge` (when this device's cursor moves). An authenticated open
    stream counts as `background` presence.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        ticket (str | Unset):
        last_event_id (str | Unset):
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[HeartbeatEvent | Notification | NotificationBadge | Problem]
    """

    kwargs = _get_kwargs(
        ticket=ticket,
        last_event_id=last_event_id,
        foresignal_device_id=foresignal_device_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
    foresignal_device_id: UUID | Unset = UNSET,
) -> HeartbeatEvent | Notification | NotificationBadge | Problem | None:
    """Inbox event stream (SSE)

     Server-Sent Events through the 015 channel: `notification` (id = notification id; replayed from the
    table after `Last-Event-ID`) and `badge` (when this device's cursor moves). An authenticated open
    stream counts as `background` presence.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        ticket (str | Unset):
        last_event_id (str | Unset):
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        HeartbeatEvent | Notification | NotificationBadge | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            ticket=ticket,
            last_event_id=last_event_id,
            foresignal_device_id=foresignal_device_id,
        )
    ).parsed
