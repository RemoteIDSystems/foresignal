from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.notification_preference import NotificationPreference
from ...models.notification_preferences_input import NotificationPreferencesInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: NotificationPreferencesInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/me/notification-preferences",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | list[NotificationPreference] | None:
    if response.status_code == 200:
        response_200 = []
        _response_200 = response.json()
        for response_200_item_data in _response_200:
            response_200_item = NotificationPreference.from_dict(response_200_item_data)

            response_200.append(response_200_item)

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
) -> Response[Problem | list[NotificationPreference]]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: NotificationPreferencesInput,
) -> Response[Problem | list[NotificationPreference]]:
    """Replace my notification preferences

     Replaces the calling principal's notification preferences. Each preference owns a 018 channel
    definition (own ntfy topic, email, Matrix room, webhook…); deliveries go through 018's worker.
    Channel URLs pass the 035 outbound URL policy (`URL_NOT_ALLOWED`).

    **Scope:** `read`.

    Args:
        body (NotificationPreferencesInput): Replace the calling principal's notification
            preferences

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[NotificationPreference]]
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
    body: NotificationPreferencesInput,
) -> Problem | list[NotificationPreference] | None:
    """Replace my notification preferences

     Replaces the calling principal's notification preferences. Each preference owns a 018 channel
    definition (own ntfy topic, email, Matrix room, webhook…); deliveries go through 018's worker.
    Channel URLs pass the 035 outbound URL policy (`URL_NOT_ALLOWED`).

    **Scope:** `read`.

    Args:
        body (NotificationPreferencesInput): Replace the calling principal's notification
            preferences

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[NotificationPreference]
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: NotificationPreferencesInput,
) -> Response[Problem | list[NotificationPreference]]:
    """Replace my notification preferences

     Replaces the calling principal's notification preferences. Each preference owns a 018 channel
    definition (own ntfy topic, email, Matrix room, webhook…); deliveries go through 018's worker.
    Channel URLs pass the 035 outbound URL policy (`URL_NOT_ALLOWED`).

    **Scope:** `read`.

    Args:
        body (NotificationPreferencesInput): Replace the calling principal's notification
            preferences

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[NotificationPreference]]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: NotificationPreferencesInput,
) -> Problem | list[NotificationPreference] | None:
    """Replace my notification preferences

     Replaces the calling principal's notification preferences. Each preference owns a 018 channel
    definition (own ntfy topic, email, Matrix room, webhook…); deliveries go through 018's worker.
    Channel URLs pass the 035 outbound URL policy (`URL_NOT_ALLOWED`).

    **Scope:** `read`.

    Args:
        body (NotificationPreferencesInput): Replace the calling principal's notification
            preferences

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[NotificationPreference]
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
