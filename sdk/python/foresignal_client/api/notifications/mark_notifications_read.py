from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.notification_badge import NotificationBadge
from ...models.notification_read_request_type_0 import NotificationReadRequestType0
from ...models.notification_read_request_type_1 import NotificationReadRequestType1
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    body: NotificationReadRequestType0 | NotificationReadRequestType1,
    foresignal_device_id: UUID | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_device_id, Unset):
        headers["Foresignal-Device-Id"] = foresignal_device_id

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/notifications/read",
    }

    if isinstance(body, NotificationReadRequestType0):
        _kwargs["json"] = body.to_dict()
    else:
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NotificationBadge | Problem | None:
    if response.status_code == 200:
        response_200 = NotificationBadge.from_dict(response.json())

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
) -> Response[NotificationBadge | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: NotificationReadRequestType0 | NotificationReadRequestType1,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[NotificationBadge | Problem]:
    """Mark notifications read

     Moves this device's read cursor (`upToId`) or records sparse reads (`ids`). Reading never
    acknowledges an alert. Other windows of the same device get a `badge` event.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):
        body (NotificationReadRequestType0 | NotificationReadRequestType1): Mark notifications
            read on the calling device (033): move the cursor (`upToId`) or add sparse reads (`ids`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationBadge | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        foresignal_device_id=foresignal_device_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: NotificationReadRequestType0 | NotificationReadRequestType1,
    foresignal_device_id: UUID | Unset = UNSET,
) -> NotificationBadge | Problem | None:
    """Mark notifications read

     Moves this device's read cursor (`upToId`) or records sparse reads (`ids`). Reading never
    acknowledges an alert. Other windows of the same device get a `badge` event.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):
        body (NotificationReadRequestType0 | NotificationReadRequestType1): Mark notifications
            read on the calling device (033): move the cursor (`upToId`) or add sparse reads (`ids`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationBadge | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
        foresignal_device_id=foresignal_device_id,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: NotificationReadRequestType0 | NotificationReadRequestType1,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[NotificationBadge | Problem]:
    """Mark notifications read

     Moves this device's read cursor (`upToId`) or records sparse reads (`ids`). Reading never
    acknowledges an alert. Other windows of the same device get a `badge` event.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):
        body (NotificationReadRequestType0 | NotificationReadRequestType1): Mark notifications
            read on the calling device (033): move the cursor (`upToId`) or add sparse reads (`ids`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationBadge | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        foresignal_device_id=foresignal_device_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: NotificationReadRequestType0 | NotificationReadRequestType1,
    foresignal_device_id: UUID | Unset = UNSET,
) -> NotificationBadge | Problem | None:
    """Mark notifications read

     Moves this device's read cursor (`upToId`) or records sparse reads (`ids`). Reading never
    acknowledges an alert. Other windows of the same device get a `badge` event.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):
        body (NotificationReadRequestType0 | NotificationReadRequestType1): Mark notifications
            read on the calling device (033): move the cursor (`upToId`) or add sparse reads (`ids`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationBadge | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
            foresignal_device_id=foresignal_device_id,
        )
    ).parsed
