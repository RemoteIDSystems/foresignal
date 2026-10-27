from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.user_preferences import UserPreferences
from ...types import Response


def _get_kwargs(
    *,
    body: UserPreferences,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/me/preferences",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | UserPreferences | None:
    if response.status_code == 200:
        response_200 = UserPreferences.from_dict(response.json())

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
    body: UserPreferences,
) -> Response[Problem | UserPreferences]:
    """Update my preferences

     Replaces the calling user's preferences. `timezone` (an IANA zone the server knows), `locale` (a BCP
    47 tag naming an ISO 639 language) and `theme` (`system`, `light` or `dark`) are replaced; absent or
    null resets each to its default, and an invalid value is 400 VALIDATION_FAILED. `ui` is replaced
    whole (at most 64 KiB of JSON). The timezone is the user's own (`Principal.timezone`): the user's
    personal notification channels render their quiet hours in it, and the Ask assistant writes the
    user's local time in it; stored times stay UTC. `notifications` writes through to the caller's
    notification preferences rather than a second copy: `emailEnabled` and `pushEnabled` turn every
    email or push channel on or off, `quietHours` (`HH:MM-HH:MM`, or null to clear) sets the window on
    every preference; a member left out, or the whole object, leaves those preferences as they are.
    Needs a signed-in session (an API token is 401). Requires `self.preferences`.

    **Scope:** `read`.

    Args:
        body (UserPreferences): The calling user's preferences (timezone, UI and notification
            defaults, 175)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserPreferences]
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
    body: UserPreferences,
) -> Problem | UserPreferences | None:
    """Update my preferences

     Replaces the calling user's preferences. `timezone` (an IANA zone the server knows), `locale` (a BCP
    47 tag naming an ISO 639 language) and `theme` (`system`, `light` or `dark`) are replaced; absent or
    null resets each to its default, and an invalid value is 400 VALIDATION_FAILED. `ui` is replaced
    whole (at most 64 KiB of JSON). The timezone is the user's own (`Principal.timezone`): the user's
    personal notification channels render their quiet hours in it, and the Ask assistant writes the
    user's local time in it; stored times stay UTC. `notifications` writes through to the caller's
    notification preferences rather than a second copy: `emailEnabled` and `pushEnabled` turn every
    email or push channel on or off, `quietHours` (`HH:MM-HH:MM`, or null to clear) sets the window on
    every preference; a member left out, or the whole object, leaves those preferences as they are.
    Needs a signed-in session (an API token is 401). Requires `self.preferences`.

    **Scope:** `read`.

    Args:
        body (UserPreferences): The calling user's preferences (timezone, UI and notification
            defaults, 175)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserPreferences
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: UserPreferences,
) -> Response[Problem | UserPreferences]:
    """Update my preferences

     Replaces the calling user's preferences. `timezone` (an IANA zone the server knows), `locale` (a BCP
    47 tag naming an ISO 639 language) and `theme` (`system`, `light` or `dark`) are replaced; absent or
    null resets each to its default, and an invalid value is 400 VALIDATION_FAILED. `ui` is replaced
    whole (at most 64 KiB of JSON). The timezone is the user's own (`Principal.timezone`): the user's
    personal notification channels render their quiet hours in it, and the Ask assistant writes the
    user's local time in it; stored times stay UTC. `notifications` writes through to the caller's
    notification preferences rather than a second copy: `emailEnabled` and `pushEnabled` turn every
    email or push channel on or off, `quietHours` (`HH:MM-HH:MM`, or null to clear) sets the window on
    every preference; a member left out, or the whole object, leaves those preferences as they are.
    Needs a signed-in session (an API token is 401). Requires `self.preferences`.

    **Scope:** `read`.

    Args:
        body (UserPreferences): The calling user's preferences (timezone, UI and notification
            defaults, 175)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserPreferences]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: UserPreferences,
) -> Problem | UserPreferences | None:
    """Update my preferences

     Replaces the calling user's preferences. `timezone` (an IANA zone the server knows), `locale` (a BCP
    47 tag naming an ISO 639 language) and `theme` (`system`, `light` or `dark`) are replaced; absent or
    null resets each to its default, and an invalid value is 400 VALIDATION_FAILED. `ui` is replaced
    whole (at most 64 KiB of JSON). The timezone is the user's own (`Principal.timezone`): the user's
    personal notification channels render their quiet hours in it, and the Ask assistant writes the
    user's local time in it; stored times stay UTC. `notifications` writes through to the caller's
    notification preferences rather than a second copy: `emailEnabled` and `pushEnabled` turn every
    email or push channel on or off, `quietHours` (`HH:MM-HH:MM`, or null to clear) sets the window on
    every preference; a member left out, or the whole object, leaves those preferences as they are.
    Needs a signed-in session (an API token is 401). Requires `self.preferences`.

    **Scope:** `read`.

    Args:
        body (UserPreferences): The calling user's preferences (timezone, UI and notification
            defaults, 175)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserPreferences
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
