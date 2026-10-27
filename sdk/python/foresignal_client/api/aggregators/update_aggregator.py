from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.aggregator_config import AggregatorConfig
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    domain: str,
    *,
    body: AggregatorConfig,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/aggregators/{domain}".format(
            domain=quote(str(domain), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    domain: str,
    *,
    client: AuthenticatedClient,
    body: AggregatorConfig,
) -> Response[Problem]:
    """Configure a domain aggregator

     Enables and configures a domain's first-party aggregator and, by default, defaults the superseded
    public/mirror sources off (they stay in the catalog as opt-in fallbacks). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** the configuration is replaced whole. adsb writes `endpoint` (and `config.band`) to the
    `local-adsb-aggregator` source and turns it on while the aggregator is live (enabled with an
    endpoint), off otherwise. While live with `disableSupersededSources`, every registered `adsblol-*`,
    `adsbfi-*` and `airplaneslive-*` source and `gpsjam-daily` that is on is turned off, and seeding and
    `enable-core`/`enable-free` leave them off; when the aggregator stops being live, or
    `disableSupersededSources` is false, the sources it turned off are turned back on. 400 for a bad
    member, an endpoint that is not an http(s) aircraft.json URL (adsb) or a tcp:// or udp:// NMEA
    endpoint (ais, R2-020; udp:// needs config.allowSenders) or a tcp://host decoder host with at least
    one decoder selected (datalink, R2-021) or an http(s) SatDump URL without config.location
    (satellite, R2-022) or any endpoint, an unknown sensor or member, or no sensor enabled (ground-
    sensors, R2-023, where a source's own parameter error is reported at the sensor's member) or does
    not resolve to a local address, or `enabled: true` on a domain this server has no backend for. A
    live aggregator whose source starts failing raises `system.aggregator_down` (and
    `system.aggregator_recovered` when it is healthy again).

    **R2-023:** ground-sensors is live while enabled with a sensor enabled. Each enabled sensor's
    members are written into its local read, which is turned on while live; a read a previous
    configuration drove and this one does not is turned off, and one never driven is left alone. Only
    the enabled sensors' mirrors are superseded, and a configuration that supersedes less turns the rest
    back on.

    Args:
        domain (str):
        body (AggregatorConfig): Configure one domain's first-party aggregator

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        domain=domain,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    domain: str,
    *,
    client: AuthenticatedClient,
    body: AggregatorConfig,
) -> Problem | None:
    """Configure a domain aggregator

     Enables and configures a domain's first-party aggregator and, by default, defaults the superseded
    public/mirror sources off (they stay in the catalog as opt-in fallbacks). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** the configuration is replaced whole. adsb writes `endpoint` (and `config.band`) to the
    `local-adsb-aggregator` source and turns it on while the aggregator is live (enabled with an
    endpoint), off otherwise. While live with `disableSupersededSources`, every registered `adsblol-*`,
    `adsbfi-*` and `airplaneslive-*` source and `gpsjam-daily` that is on is turned off, and seeding and
    `enable-core`/`enable-free` leave them off; when the aggregator stops being live, or
    `disableSupersededSources` is false, the sources it turned off are turned back on. 400 for a bad
    member, an endpoint that is not an http(s) aircraft.json URL (adsb) or a tcp:// or udp:// NMEA
    endpoint (ais, R2-020; udp:// needs config.allowSenders) or a tcp://host decoder host with at least
    one decoder selected (datalink, R2-021) or an http(s) SatDump URL without config.location
    (satellite, R2-022) or any endpoint, an unknown sensor or member, or no sensor enabled (ground-
    sensors, R2-023, where a source's own parameter error is reported at the sensor's member) or does
    not resolve to a local address, or `enabled: true` on a domain this server has no backend for. A
    live aggregator whose source starts failing raises `system.aggregator_down` (and
    `system.aggregator_recovered` when it is healthy again).

    **R2-023:** ground-sensors is live while enabled with a sensor enabled. Each enabled sensor's
    members are written into its local read, which is turned on while live; a read a previous
    configuration drove and this one does not is turned off, and one never driven is left alone. Only
    the enabled sensors' mirrors are superseded, and a configuration that supersedes less turns the rest
    back on.

    Args:
        domain (str):
        body (AggregatorConfig): Configure one domain's first-party aggregator

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        domain=domain,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    domain: str,
    *,
    client: AuthenticatedClient,
    body: AggregatorConfig,
) -> Response[Problem]:
    """Configure a domain aggregator

     Enables and configures a domain's first-party aggregator and, by default, defaults the superseded
    public/mirror sources off (they stay in the catalog as opt-in fallbacks). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** the configuration is replaced whole. adsb writes `endpoint` (and `config.band`) to the
    `local-adsb-aggregator` source and turns it on while the aggregator is live (enabled with an
    endpoint), off otherwise. While live with `disableSupersededSources`, every registered `adsblol-*`,
    `adsbfi-*` and `airplaneslive-*` source and `gpsjam-daily` that is on is turned off, and seeding and
    `enable-core`/`enable-free` leave them off; when the aggregator stops being live, or
    `disableSupersededSources` is false, the sources it turned off are turned back on. 400 for a bad
    member, an endpoint that is not an http(s) aircraft.json URL (adsb) or a tcp:// or udp:// NMEA
    endpoint (ais, R2-020; udp:// needs config.allowSenders) or a tcp://host decoder host with at least
    one decoder selected (datalink, R2-021) or an http(s) SatDump URL without config.location
    (satellite, R2-022) or any endpoint, an unknown sensor or member, or no sensor enabled (ground-
    sensors, R2-023, where a source's own parameter error is reported at the sensor's member) or does
    not resolve to a local address, or `enabled: true` on a domain this server has no backend for. A
    live aggregator whose source starts failing raises `system.aggregator_down` (and
    `system.aggregator_recovered` when it is healthy again).

    **R2-023:** ground-sensors is live while enabled with a sensor enabled. Each enabled sensor's
    members are written into its local read, which is turned on while live; a read a previous
    configuration drove and this one does not is turned off, and one never driven is left alone. Only
    the enabled sensors' mirrors are superseded, and a configuration that supersedes less turns the rest
    back on.

    Args:
        domain (str):
        body (AggregatorConfig): Configure one domain's first-party aggregator

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        domain=domain,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    domain: str,
    *,
    client: AuthenticatedClient,
    body: AggregatorConfig,
) -> Problem | None:
    """Configure a domain aggregator

     Enables and configures a domain's first-party aggregator and, by default, defaults the superseded
    public/mirror sources off (they stay in the catalog as opt-in fallbacks). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** the configuration is replaced whole. adsb writes `endpoint` (and `config.band`) to the
    `local-adsb-aggregator` source and turns it on while the aggregator is live (enabled with an
    endpoint), off otherwise. While live with `disableSupersededSources`, every registered `adsblol-*`,
    `adsbfi-*` and `airplaneslive-*` source and `gpsjam-daily` that is on is turned off, and seeding and
    `enable-core`/`enable-free` leave them off; when the aggregator stops being live, or
    `disableSupersededSources` is false, the sources it turned off are turned back on. 400 for a bad
    member, an endpoint that is not an http(s) aircraft.json URL (adsb) or a tcp:// or udp:// NMEA
    endpoint (ais, R2-020; udp:// needs config.allowSenders) or a tcp://host decoder host with at least
    one decoder selected (datalink, R2-021) or an http(s) SatDump URL without config.location
    (satellite, R2-022) or any endpoint, an unknown sensor or member, or no sensor enabled (ground-
    sensors, R2-023, where a source's own parameter error is reported at the sensor's member) or does
    not resolve to a local address, or `enabled: true` on a domain this server has no backend for. A
    live aggregator whose source starts failing raises `system.aggregator_down` (and
    `system.aggregator_recovered` when it is healthy again).

    **R2-023:** ground-sensors is live while enabled with a sensor enabled. Each enabled sensor's
    members are written into its local read, which is turned on while live; a read a previous
    configuration drove and this one does not is turned off, and one never driven is left alone. Only
    the enabled sensors' mirrors are superseded, and a configuration that supersedes less turns the rest
    back on.

    Args:
        domain (str):
        body (AggregatorConfig): Configure one domain's first-party aggregator

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            domain=domain,
            client=client,
            body=body,
        )
    ).parsed
