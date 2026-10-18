from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_channel_deliveries_status import ListChannelDeliveriesStatus
from ...models.notification_delivery_page import NotificationDeliveryPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    channel_id: UUID,
    *,
    status: ListChannelDeliveriesStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/channels/{channel_id}/deliveries".format(
            channel_id=quote(str(channel_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NotificationDeliveryPage | Problem | None:
    if response.status_code == 200:
        response_200 = NotificationDeliveryPage.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[NotificationDeliveryPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
    status: ListChannelDeliveriesStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> Response[NotificationDeliveryPage | Problem]:
    """Recent deliveries

     Deliveries newest first (keyset cursor) with notificationId, kind (send, update, followup, digest),
    deferredReason and edited.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):
        status (ListChannelDeliveriesStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationDeliveryPage | Problem]
    """

    kwargs = _get_kwargs(
        channel_id=channel_id,
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
    status: ListChannelDeliveriesStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> NotificationDeliveryPage | Problem | None:
    """Recent deliveries

     Deliveries newest first (keyset cursor) with notificationId, kind (send, update, followup, digest),
    deferredReason and edited.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):
        status (ListChannelDeliveriesStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationDeliveryPage | Problem
    """

    return sync_detailed(
        channel_id=channel_id,
        client=client,
        status=status,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
    status: ListChannelDeliveriesStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> Response[NotificationDeliveryPage | Problem]:
    """Recent deliveries

     Deliveries newest first (keyset cursor) with notificationId, kind (send, update, followup, digest),
    deferredReason and edited.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):
        status (ListChannelDeliveriesStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationDeliveryPage | Problem]
    """

    kwargs = _get_kwargs(
        channel_id=channel_id,
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
    status: ListChannelDeliveriesStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
) -> NotificationDeliveryPage | Problem | None:
    """Recent deliveries

     Deliveries newest first (keyset cursor) with notificationId, kind (send, update, followup, digest),
    deferredReason and edited.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):
        status (ListChannelDeliveriesStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationDeliveryPage | Problem
    """

    return (
        await asyncio_detailed(
            channel_id=channel_id,
            client=client,
            status=status,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
