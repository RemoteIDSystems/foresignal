from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.user_preferences import UserPreferences
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/me/preferences",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | UserPreferences | None:
    if response.status_code == 200:
        response_200 = UserPreferences.from_dict(response.json())

        return response_200

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
) -> Response[Problem | UserPreferences]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | UserPreferences]:
    """Get my preferences

     Returns the calling user's preferences with the defaults filled in: an unset `timezone` reads as the
    server's (getServerSettings), an unset `theme` as `system`, an unset `locale` as the instance
    notification language (`notify.defaultLanguage`). `notifications` summarizes the caller's
    notification preferences (listMyNotificationPreferences): `emailEnabled` is false only when the
    caller has email channels and every one is off, `pushEnabled` likewise for push channels (ntfy,
    gotify, pushover, webpush), and `quietHours` is the window every preference shares, or null when
    they differ or have none. Needs a signed-in session (an API token is 401). Requires
    `self.preferences`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserPreferences]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | UserPreferences | None:
    """Get my preferences

     Returns the calling user's preferences with the defaults filled in: an unset `timezone` reads as the
    server's (getServerSettings), an unset `theme` as `system`, an unset `locale` as the instance
    notification language (`notify.defaultLanguage`). `notifications` summarizes the caller's
    notification preferences (listMyNotificationPreferences): `emailEnabled` is false only when the
    caller has email channels and every one is off, `pushEnabled` likewise for push channels (ntfy,
    gotify, pushover, webpush), and `quietHours` is the window every preference shares, or null when
    they differ or have none. Needs a signed-in session (an API token is 401). Requires
    `self.preferences`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserPreferences
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | UserPreferences]:
    """Get my preferences

     Returns the calling user's preferences with the defaults filled in: an unset `timezone` reads as the
    server's (getServerSettings), an unset `theme` as `system`, an unset `locale` as the instance
    notification language (`notify.defaultLanguage`). `notifications` summarizes the caller's
    notification preferences (listMyNotificationPreferences): `emailEnabled` is false only when the
    caller has email channels and every one is off, `pushEnabled` likewise for push channels (ntfy,
    gotify, pushover, webpush), and `quietHours` is the window every preference shares, or null when
    they differ or have none. Needs a signed-in session (an API token is 401). Requires
    `self.preferences`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserPreferences]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | UserPreferences | None:
    """Get my preferences

     Returns the calling user's preferences with the defaults filled in: an unset `timezone` reads as the
    server's (getServerSettings), an unset `theme` as `system`, an unset `locale` as the instance
    notification language (`notify.defaultLanguage`). `notifications` summarizes the caller's
    notification preferences (listMyNotificationPreferences): `emailEnabled` is false only when the
    caller has email channels and every one is off, `pushEnabled` likewise for push channels (ntfy,
    gotify, pushover, webpush), and `quietHours` is the window every preference shares, or null when
    they differ or have none. Needs a signed-in session (an API token is 401). Requires
    `self.preferences`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserPreferences
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
