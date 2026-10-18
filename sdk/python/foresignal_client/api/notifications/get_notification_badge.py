from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.notification_badge import NotificationBadge
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    foresignal_device_id: UUID | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_device_id, Unset):
        headers["Foresignal-Device-Id"] = foresignal_device_id

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/notifications/badge",
    }

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
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[NotificationBadge | Problem]:
    """Unread badge counts

     Unread alert and system notifications for this device, counting alerts that are still firing. The
    same count feeds native `aps.badge`.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationBadge | Problem]
    """

    kwargs = _get_kwargs(
        foresignal_device_id=foresignal_device_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    foresignal_device_id: UUID | Unset = UNSET,
) -> NotificationBadge | Problem | None:
    """Unread badge counts

     Unread alert and system notifications for this device, counting alerts that are still firing. The
    same count feeds native `aps.badge`.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationBadge | Problem
    """

    return sync_detailed(
        client=client,
        foresignal_device_id=foresignal_device_id,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[NotificationBadge | Problem]:
    """Unread badge counts

     Unread alert and system notifications for this device, counting alerts that are still firing. The
    same count feeds native `aps.badge`.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationBadge | Problem]
    """

    kwargs = _get_kwargs(
        foresignal_device_id=foresignal_device_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    foresignal_device_id: UUID | Unset = UNSET,
) -> NotificationBadge | Problem | None:
    """Unread badge counts

     Unread alert and system notifications for this device, counting alerts that are still firing. The
    same count feeds native `aps.badge`.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationBadge | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            foresignal_device_id=foresignal_device_id,
        )
    ).parsed
