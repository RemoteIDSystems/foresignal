from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.aggregator_test_result import AggregatorTestResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    domain: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/aggregators/{domain}/test".format(
            domain=quote(str(domain), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AggregatorTestResult | Problem | None:
    if response.status_code == 200:
        response_200 = AggregatorTestResult.from_dict(response.json())

        return response_200

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
) -> Response[AggregatorTestResult | Problem]:
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
) -> Response[AggregatorTestResult | Problem]:
    """Test a domain aggregator

     Probes the aggregator's local endpoint and reports reachability and a sample object count; nothing
    is persisted. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** reads the stored endpoint once through the aggregator's source (its budget and pinning
    apply). Always 200; `ok` false with a `message` when nothing is configured, the endpoint cannot be
    reached, or the answer is not readsb aircraft.json. `sampleCount` is the number of aircraft in it.

    **R2-020:** for ais, listens to the stored NMEA endpoint for 10 s through `local-ais-aggregator` and
    reports the vessel records decoded (at most 100); a live UDP listener, which cannot be bound twice,
    is reported from its statistics (distinct MMSIs in the last 10 minutes).

    **R2-021:** for datalink, listens to every selected read (ACARS/VDLM2/HFDL and APRS) at once for 10
    s and sums the messages decoded; `reachable` is false only when no selected read could be connected.

    **R2-022:** for satellite, runs the satdump-aggregator station for 10 s (its /api read and inbox
    scan) and counts the records it produced: station state, passes and products.

    **R2-023:** for ground-sensors, probes every enabled sensor's polling or streaming read at once for
    10 s and sums the records; a push source (seismic, same, the MeshCore bridge) or a live local-sonde
    UDP listener is reachable once it has received data. A failing sensor is named in `message`;
    `reachable` is false only when every sensor fails.

    Args:
        domain (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AggregatorTestResult | Problem]
    """

    kwargs = _get_kwargs(
        domain=domain,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    domain: str,
    *,
    client: AuthenticatedClient,
) -> AggregatorTestResult | Problem | None:
    """Test a domain aggregator

     Probes the aggregator's local endpoint and reports reachability and a sample object count; nothing
    is persisted. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** reads the stored endpoint once through the aggregator's source (its budget and pinning
    apply). Always 200; `ok` false with a `message` when nothing is configured, the endpoint cannot be
    reached, or the answer is not readsb aircraft.json. `sampleCount` is the number of aircraft in it.

    **R2-020:** for ais, listens to the stored NMEA endpoint for 10 s through `local-ais-aggregator` and
    reports the vessel records decoded (at most 100); a live UDP listener, which cannot be bound twice,
    is reported from its statistics (distinct MMSIs in the last 10 minutes).

    **R2-021:** for datalink, listens to every selected read (ACARS/VDLM2/HFDL and APRS) at once for 10
    s and sums the messages decoded; `reachable` is false only when no selected read could be connected.

    **R2-022:** for satellite, runs the satdump-aggregator station for 10 s (its /api read and inbox
    scan) and counts the records it produced: station state, passes and products.

    **R2-023:** for ground-sensors, probes every enabled sensor's polling or streaming read at once for
    10 s and sums the records; a push source (seismic, same, the MeshCore bridge) or a live local-sonde
    UDP listener is reachable once it has received data. A failing sensor is named in `message`;
    `reachable` is false only when every sensor fails.

    Args:
        domain (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AggregatorTestResult | Problem
    """

    return sync_detailed(
        domain=domain,
        client=client,
    ).parsed


async def asyncio_detailed(
    domain: str,
    *,
    client: AuthenticatedClient,
) -> Response[AggregatorTestResult | Problem]:
    """Test a domain aggregator

     Probes the aggregator's local endpoint and reports reachability and a sample object count; nothing
    is persisted. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** reads the stored endpoint once through the aggregator's source (its budget and pinning
    apply). Always 200; `ok` false with a `message` when nothing is configured, the endpoint cannot be
    reached, or the answer is not readsb aircraft.json. `sampleCount` is the number of aircraft in it.

    **R2-020:** for ais, listens to the stored NMEA endpoint for 10 s through `local-ais-aggregator` and
    reports the vessel records decoded (at most 100); a live UDP listener, which cannot be bound twice,
    is reported from its statistics (distinct MMSIs in the last 10 minutes).

    **R2-021:** for datalink, listens to every selected read (ACARS/VDLM2/HFDL and APRS) at once for 10
    s and sums the messages decoded; `reachable` is false only when no selected read could be connected.

    **R2-022:** for satellite, runs the satdump-aggregator station for 10 s (its /api read and inbox
    scan) and counts the records it produced: station state, passes and products.

    **R2-023:** for ground-sensors, probes every enabled sensor's polling or streaming read at once for
    10 s and sums the records; a push source (seismic, same, the MeshCore bridge) or a live local-sonde
    UDP listener is reachable once it has received data. A failing sensor is named in `message`;
    `reachable` is false only when every sensor fails.

    Args:
        domain (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AggregatorTestResult | Problem]
    """

    kwargs = _get_kwargs(
        domain=domain,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    domain: str,
    *,
    client: AuthenticatedClient,
) -> AggregatorTestResult | Problem | None:
    """Test a domain aggregator

     Probes the aggregator's local endpoint and reports reachability and a sample object count; nothing
    is persisted. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-019:** reads the stored endpoint once through the aggregator's source (its budget and pinning
    apply). Always 200; `ok` false with a `message` when nothing is configured, the endpoint cannot be
    reached, or the answer is not readsb aircraft.json. `sampleCount` is the number of aircraft in it.

    **R2-020:** for ais, listens to the stored NMEA endpoint for 10 s through `local-ais-aggregator` and
    reports the vessel records decoded (at most 100); a live UDP listener, which cannot be bound twice,
    is reported from its statistics (distinct MMSIs in the last 10 minutes).

    **R2-021:** for datalink, listens to every selected read (ACARS/VDLM2/HFDL and APRS) at once for 10
    s and sums the messages decoded; `reachable` is false only when no selected read could be connected.

    **R2-022:** for satellite, runs the satdump-aggregator station for 10 s (its /api read and inbox
    scan) and counts the records it produced: station state, passes and products.

    **R2-023:** for ground-sensors, probes every enabled sensor's polling or streaming read at once for
    10 s and sums the records; a push source (seismic, same, the MeshCore bridge) or a live local-sonde
    UDP listener is reachable once it has received data. A failing sensor is named in `message`;
    `reachable` is false only when every sensor fails.

    Args:
        domain (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AggregatorTestResult | Problem
    """

    return (
        await asyncio_detailed(
            domain=domain,
            client=client,
        )
    ).parsed
