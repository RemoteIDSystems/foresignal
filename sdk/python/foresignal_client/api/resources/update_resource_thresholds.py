from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.resource_thresholds import ResourceThresholds
from ...types import Response


def _get_kwargs(
    *,
    body: ResourceThresholds,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/thresholds",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ResourceThresholds | None:
    if response.status_code == 200:
        response_200 = ResourceThresholds.from_dict(response.json())

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
) -> Response[Problem | ResourceThresholds]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ResourceThresholds,
) -> Response[Problem | ResourceThresholds]:
    """Update resource thresholds

     Replaces the thresholds; a breach raises /v1/system warnings and fires alerts through the channel
    system. Each alert is edge-triggered: entering `warn` or `critical` publishes one `system`
    notification (`system.disk_low`, `system.disk_critical`, `system.memory_low`,
    `system.memory_critical`, `system.source_storm`, `system.collector_backlog`,
    `system.user_rate_limited`) and returning to normal publishes one recovery (`system.disk_recovered`,
    `system.memory_recovered`, `system.source_storm_cleared`, `system.collector_backlog_cleared`,
    `system.user_rate_limit_cleared`). Requires `storage.manage`.

    **Scope:** `admin`.

    Args:
        body (ResourceThresholds): Admin thresholds that raise /v1/system warnings and fire
            channel alerts when breached (B1/177). A PUT replaces the whole value: a missing member
            takes its default (disk 15 % warn / 5 % critical free, memory off, a storm at 5 failing
            sources, a backlog of 10,000 seq for 300 s) and an explicit null turns it off.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ResourceThresholds]
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
    body: ResourceThresholds,
) -> Problem | ResourceThresholds | None:
    """Update resource thresholds

     Replaces the thresholds; a breach raises /v1/system warnings and fires alerts through the channel
    system. Each alert is edge-triggered: entering `warn` or `critical` publishes one `system`
    notification (`system.disk_low`, `system.disk_critical`, `system.memory_low`,
    `system.memory_critical`, `system.source_storm`, `system.collector_backlog`,
    `system.user_rate_limited`) and returning to normal publishes one recovery (`system.disk_recovered`,
    `system.memory_recovered`, `system.source_storm_cleared`, `system.collector_backlog_cleared`,
    `system.user_rate_limit_cleared`). Requires `storage.manage`.

    **Scope:** `admin`.

    Args:
        body (ResourceThresholds): Admin thresholds that raise /v1/system warnings and fire
            channel alerts when breached (B1/177). A PUT replaces the whole value: a missing member
            takes its default (disk 15 % warn / 5 % critical free, memory off, a storm at 5 failing
            sources, a backlog of 10,000 seq for 300 s) and an explicit null turns it off.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ResourceThresholds
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ResourceThresholds,
) -> Response[Problem | ResourceThresholds]:
    """Update resource thresholds

     Replaces the thresholds; a breach raises /v1/system warnings and fires alerts through the channel
    system. Each alert is edge-triggered: entering `warn` or `critical` publishes one `system`
    notification (`system.disk_low`, `system.disk_critical`, `system.memory_low`,
    `system.memory_critical`, `system.source_storm`, `system.collector_backlog`,
    `system.user_rate_limited`) and returning to normal publishes one recovery (`system.disk_recovered`,
    `system.memory_recovered`, `system.source_storm_cleared`, `system.collector_backlog_cleared`,
    `system.user_rate_limit_cleared`). Requires `storage.manage`.

    **Scope:** `admin`.

    Args:
        body (ResourceThresholds): Admin thresholds that raise /v1/system warnings and fire
            channel alerts when breached (B1/177). A PUT replaces the whole value: a missing member
            takes its default (disk 15 % warn / 5 % critical free, memory off, a storm at 5 failing
            sources, a backlog of 10,000 seq for 300 s) and an explicit null turns it off.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ResourceThresholds]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ResourceThresholds,
) -> Problem | ResourceThresholds | None:
    """Update resource thresholds

     Replaces the thresholds; a breach raises /v1/system warnings and fires alerts through the channel
    system. Each alert is edge-triggered: entering `warn` or `critical` publishes one `system`
    notification (`system.disk_low`, `system.disk_critical`, `system.memory_low`,
    `system.memory_critical`, `system.source_storm`, `system.collector_backlog`,
    `system.user_rate_limited`) and returning to normal publishes one recovery (`system.disk_recovered`,
    `system.memory_recovered`, `system.source_storm_cleared`, `system.collector_backlog_cleared`,
    `system.user_rate_limit_cleared`). Requires `storage.manage`.

    **Scope:** `admin`.

    Args:
        body (ResourceThresholds): Admin thresholds that raise /v1/system warnings and fire
            channel alerts when breached (B1/177). A PUT replaces the whole value: a missing member
            takes its default (disk 15 % warn / 5 % critical free, memory off, a storm at 5 failing
            sources, a backlog of 10,000 seq for 300 s) and an explicit null turns it off.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ResourceThresholds
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
