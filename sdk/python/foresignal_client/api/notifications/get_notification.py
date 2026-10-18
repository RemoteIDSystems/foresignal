from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.notification_detail import NotificationDetail
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    notification_id: int,
    *,
    foresignal_device_id: UUID | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_device_id, Unset):
        headers["Foresignal-Device-Id"] = foresignal_device_id

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/notifications/{notification_id}".format(
            notification_id=quote(str(notification_id), safe=""),
        ),
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NotificationDetail | Problem | None:
    if response.status_code == 200:
        response_200 = NotificationDetail.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[NotificationDetail | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    notification_id: int,
    *,
    client: AuthenticatedClient,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[NotificationDetail | Problem]:
    """Get a notification with actions

     Adds `links` and `actions`: capability URLs minted for the calling device (12 h), so native apps
    never receive capabilities through Apple or Google.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        notification_id (int):
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationDetail | Problem]
    """

    kwargs = _get_kwargs(
        notification_id=notification_id,
        foresignal_device_id=foresignal_device_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    notification_id: int,
    *,
    client: AuthenticatedClient,
    foresignal_device_id: UUID | Unset = UNSET,
) -> NotificationDetail | Problem | None:
    """Get a notification with actions

     Adds `links` and `actions`: capability URLs minted for the calling device (12 h), so native apps
    never receive capabilities through Apple or Google.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        notification_id (int):
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationDetail | Problem
    """

    return sync_detailed(
        notification_id=notification_id,
        client=client,
        foresignal_device_id=foresignal_device_id,
    ).parsed


async def asyncio_detailed(
    notification_id: int,
    *,
    client: AuthenticatedClient,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[NotificationDetail | Problem]:
    """Get a notification with actions

     Adds `links` and `actions`: capability URLs minted for the calling device (12 h), so native apps
    never receive capabilities through Apple or Google.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        notification_id (int):
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationDetail | Problem]
    """

    kwargs = _get_kwargs(
        notification_id=notification_id,
        foresignal_device_id=foresignal_device_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    notification_id: int,
    *,
    client: AuthenticatedClient,
    foresignal_device_id: UUID | Unset = UNSET,
) -> NotificationDetail | Problem | None:
    """Get a notification with actions

     Adds `links` and `actions`: capability URLs minted for the calling device (12 h), so native apps
    never receive capabilities through Apple or Google.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        notification_id (int):
        foresignal_device_id (UUID | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationDetail | Problem
    """

    return (
        await asyncio_detailed(
            notification_id=notification_id,
            client=client,
            foresignal_device_id=foresignal_device_id,
        )
    ).parsed
