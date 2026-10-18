from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.notification_settings import NotificationSettings
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: NotificationSettings,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/notifications",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NotificationSettings | Problem | None:
    if response.status_code == 200:
        response_200 = NotificationSettings.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[NotificationSettings | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: NotificationSettings,
) -> Response[NotificationSettings | Problem]:
    """Set notification pause settings

     Replaces the settings. Deliveries deferred by a pause are released as one digest per channel when it
    ends; messages at or above `pauseBypassSeverity` are sent anyway.

    **Scope:** `admin`.

    Args:
        body (NotificationSettings): Global notification pause (`app_setting['notifications']`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationSettings | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: NotificationSettings,
) -> NotificationSettings | Problem | None:
    """Set notification pause settings

     Replaces the settings. Deliveries deferred by a pause are released as one digest per channel when it
    ends; messages at or above `pauseBypassSeverity` are sent anyway.

    **Scope:** `admin`.

    Args:
        body (NotificationSettings): Global notification pause (`app_setting['notifications']`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationSettings | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: NotificationSettings,
) -> Response[NotificationSettings | Problem]:
    """Set notification pause settings

     Replaces the settings. Deliveries deferred by a pause are released as one digest per channel when it
    ends; messages at or above `pauseBypassSeverity` are sent anyway.

    **Scope:** `admin`.

    Args:
        body (NotificationSettings): Global notification pause (`app_setting['notifications']`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NotificationSettings | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: NotificationSettings,
) -> NotificationSettings | Problem | None:
    """Set notification pause settings

     Replaces the settings. Deliveries deferred by a pause are released as one digest per channel when it
    ends; messages at or above `pauseBypassSeverity` are sent anyway.

    **Scope:** `admin`.

    Args:
        body (NotificationSettings): Global notification pause (`app_setting['notifications']`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NotificationSettings | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
