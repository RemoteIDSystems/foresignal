from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.enable_core_sources_result import EnableCoreSourcesResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/sources/enable-core",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EnableCoreSourcesResult | Problem | None:
    if response.status_code == 200:
        response_200 = EnableCoreSourcesResult.from_dict(response.json())

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
) -> Response[EnableCoreSourcesResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[EnableCoreSourcesResult | Problem]:
    """Enable the core sources

     Enables every currently disabled source of the core set that runs without operator input: the small
    live-tracking set a fresh install seeds enabled (prompt 150): `adsblol-world`, `adsblol-mil`,
    `airplaneslive-mil`, `adsbfi-blindspots`, `airplaneslive-blindspots`, `opensky-states`, `derived-
    aircraft`, `celestrak-gp`, `usgs-earthquakes`, `nws-alerts`, `nasa-firms`, `meshtastic-map` and
    `meshtastic-mqtt`. A core source that needs operator input (`nasa-firms` without its MAP_KEY) is
    skipped, as is every source outside the core set. Each source is enabled through the settings-update
    path, so the collector picks it up within seconds. Disables nothing; enabled sources and custom or
    external sources are left untouched. Idempotent: a second call enables nothing. This is how an
    install seeded before prompt 150 adopts the core default without re-seeding.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EnableCoreSourcesResult | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> EnableCoreSourcesResult | Problem | None:
    """Enable the core sources

     Enables every currently disabled source of the core set that runs without operator input: the small
    live-tracking set a fresh install seeds enabled (prompt 150): `adsblol-world`, `adsblol-mil`,
    `airplaneslive-mil`, `adsbfi-blindspots`, `airplaneslive-blindspots`, `opensky-states`, `derived-
    aircraft`, `celestrak-gp`, `usgs-earthquakes`, `nws-alerts`, `nasa-firms`, `meshtastic-map` and
    `meshtastic-mqtt`. A core source that needs operator input (`nasa-firms` without its MAP_KEY) is
    skipped, as is every source outside the core set. Each source is enabled through the settings-update
    path, so the collector picks it up within seconds. Disables nothing; enabled sources and custom or
    external sources are left untouched. Idempotent: a second call enables nothing. This is how an
    install seeded before prompt 150 adopts the core default without re-seeding.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EnableCoreSourcesResult | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[EnableCoreSourcesResult | Problem]:
    """Enable the core sources

     Enables every currently disabled source of the core set that runs without operator input: the small
    live-tracking set a fresh install seeds enabled (prompt 150): `adsblol-world`, `adsblol-mil`,
    `airplaneslive-mil`, `adsbfi-blindspots`, `airplaneslive-blindspots`, `opensky-states`, `derived-
    aircraft`, `celestrak-gp`, `usgs-earthquakes`, `nws-alerts`, `nasa-firms`, `meshtastic-map` and
    `meshtastic-mqtt`. A core source that needs operator input (`nasa-firms` without its MAP_KEY) is
    skipped, as is every source outside the core set. Each source is enabled through the settings-update
    path, so the collector picks it up within seconds. Disables nothing; enabled sources and custom or
    external sources are left untouched. Idempotent: a second call enables nothing. This is how an
    install seeded before prompt 150 adopts the core default without re-seeding.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EnableCoreSourcesResult | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> EnableCoreSourcesResult | Problem | None:
    """Enable the core sources

     Enables every currently disabled source of the core set that runs without operator input: the small
    live-tracking set a fresh install seeds enabled (prompt 150): `adsblol-world`, `adsblol-mil`,
    `airplaneslive-mil`, `adsbfi-blindspots`, `airplaneslive-blindspots`, `opensky-states`, `derived-
    aircraft`, `celestrak-gp`, `usgs-earthquakes`, `nws-alerts`, `nasa-firms`, `meshtastic-map` and
    `meshtastic-mqtt`. A core source that needs operator input (`nasa-firms` without its MAP_KEY) is
    skipped, as is every source outside the core set. Each source is enabled through the settings-update
    path, so the collector picks it up within seconds. Disables nothing; enabled sources and custom or
    external sources are left untouched. Idempotent: a second call enables nothing. This is how an
    install seeded before prompt 150 adopts the core default without re-seeding.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EnableCoreSourcesResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
