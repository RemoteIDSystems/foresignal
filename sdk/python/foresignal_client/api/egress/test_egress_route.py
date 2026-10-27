from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.egress_test_result import EgressTestResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/egress/{id}/test".format(
            id=quote(str(id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EgressTestResult | Problem | None:
    if response.status_code == 200:
        response_200 = EgressTestResult.from_dict(response.json())

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
) -> Response[EgressTestResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[EgressTestResult | Problem]:
    """Test an egress route

     Makes a probe call through the route and reports the observed public exit IP and latency; nothing is
    persisted beyond the route's health (C1/181). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** one `GET` of the configured exit-IP service (`foresignal.egress.probe-url`, default
    `https://checkip.amazonaws.com/`, checked against the public-only outbound policy) through the
    route. Always 200: `ok` false with a `message` when the route is unavailable or the call fails. A
    passing test records `exitIp` on the route.

    Args:
        id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EgressTestResult | Problem]
    """

    kwargs = _get_kwargs(
        id=id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    id: UUID,
    *,
    client: AuthenticatedClient,
) -> EgressTestResult | Problem | None:
    """Test an egress route

     Makes a probe call through the route and reports the observed public exit IP and latency; nothing is
    persisted beyond the route's health (C1/181). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** one `GET` of the configured exit-IP service (`foresignal.egress.probe-url`, default
    `https://checkip.amazonaws.com/`, checked against the public-only outbound policy) through the
    route. Always 200: `ok` false with a `message` when the route is unavailable or the call fails. A
    passing test records `exitIp` on the route.

    Args:
        id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EgressTestResult | Problem
    """

    return sync_detailed(
        id=id,
        client=client,
    ).parsed


async def asyncio_detailed(
    id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[EgressTestResult | Problem]:
    """Test an egress route

     Makes a probe call through the route and reports the observed public exit IP and latency; nothing is
    persisted beyond the route's health (C1/181). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** one `GET` of the configured exit-IP service (`foresignal.egress.probe-url`, default
    `https://checkip.amazonaws.com/`, checked against the public-only outbound policy) through the
    route. Always 200: `ok` false with a `message` when the route is unavailable or the call fails. A
    passing test records `exitIp` on the route.

    Args:
        id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EgressTestResult | Problem]
    """

    kwargs = _get_kwargs(
        id=id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    id: UUID,
    *,
    client: AuthenticatedClient,
) -> EgressTestResult | Problem | None:
    """Test an egress route

     Makes a probe call through the route and reports the observed public exit IP and latency; nothing is
    persisted beyond the route's health (C1/181). Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** one `GET` of the configured exit-IP service (`foresignal.egress.probe-url`, default
    `https://checkip.amazonaws.com/`, checked against the public-only outbound policy) through the
    route. Always 200: `ok` false with a `message` when the route is unavailable or the call fails. A
    passing test records `exitIp` on the route.

    Args:
        id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EgressTestResult | Problem
    """

    return (
        await asyncio_detailed(
            id=id,
            client=client,
        )
    ).parsed
