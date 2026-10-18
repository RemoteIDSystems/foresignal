from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.settings_bundle import SettingsBundle
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    include_places: bool | Unset = False,
    include_watchlists: bool | Unset = False,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["includePlaces"] = include_places

    params["includeWatchlists"] = include_watchlists

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/settings/export",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SettingsBundle | None:
    if response.status_code == 200:
        response_200 = SettingsBundle.from_dict(response.json())

        return response_200

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
) -> Response[Problem | SettingsBundle]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    include_places: bool | Unset = False,
    include_watchlists: bool | Unset = False,
) -> Response[Problem | SettingsBundle]:
    """Export all settings

     Source settings (no credentials), retention with kinds, CORS, notification settings, watchdog,
    trigger rules with ids and channels without secrets, plus later sections (security, server,
    briefings, custom sources, LLM). Never includes secrets, credentials or ingest keys.

    **Scope:** `admin`.

    Args:
        include_places (bool | Unset):  Default: False.
        include_watchlists (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SettingsBundle]
    """

    kwargs = _get_kwargs(
        include_places=include_places,
        include_watchlists=include_watchlists,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    include_places: bool | Unset = False,
    include_watchlists: bool | Unset = False,
) -> Problem | SettingsBundle | None:
    """Export all settings

     Source settings (no credentials), retention with kinds, CORS, notification settings, watchdog,
    trigger rules with ids and channels without secrets, plus later sections (security, server,
    briefings, custom sources, LLM). Never includes secrets, credentials or ingest keys.

    **Scope:** `admin`.

    Args:
        include_places (bool | Unset):  Default: False.
        include_watchlists (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SettingsBundle
    """

    return sync_detailed(
        client=client,
        include_places=include_places,
        include_watchlists=include_watchlists,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    include_places: bool | Unset = False,
    include_watchlists: bool | Unset = False,
) -> Response[Problem | SettingsBundle]:
    """Export all settings

     Source settings (no credentials), retention with kinds, CORS, notification settings, watchdog,
    trigger rules with ids and channels without secrets, plus later sections (security, server,
    briefings, custom sources, LLM). Never includes secrets, credentials or ingest keys.

    **Scope:** `admin`.

    Args:
        include_places (bool | Unset):  Default: False.
        include_watchlists (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SettingsBundle]
    """

    kwargs = _get_kwargs(
        include_places=include_places,
        include_watchlists=include_watchlists,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    include_places: bool | Unset = False,
    include_watchlists: bool | Unset = False,
) -> Problem | SettingsBundle | None:
    """Export all settings

     Source settings (no credentials), retention with kinds, CORS, notification settings, watchdog,
    trigger rules with ids and channels without secrets, plus later sections (security, server,
    briefings, custom sources, LLM). Never includes secrets, credentials or ingest keys.

    **Scope:** `admin`.

    Args:
        include_places (bool | Unset):  Default: False.
        include_watchlists (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SettingsBundle
    """

    return (
        await asyncio_detailed(
            client=client,
            include_places=include_places,
            include_watchlists=include_watchlists,
        )
    ).parsed
