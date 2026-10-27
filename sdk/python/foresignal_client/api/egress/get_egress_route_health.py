from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.egress_health import EgressHealth
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/egress/{id}/health".format(
            id=quote(str(id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EgressHealth | Problem | None:
    if response.status_code == 200:
        response_200 = EgressHealth.from_dict(response.json())

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
) -> Response[EgressHealth | Problem]:
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
) -> Response[EgressHealth | Problem]:
    """Get egress route health

     Returns reachability, latency, exit IP and backoff state for one route. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** health is held in memory and starts empty at restart; three consecutive failures mark
    the route unreachable, skip it in rotation and raise `system.egress_unhealthy`; a later success
    raises `system.egress_recovered`. `sources` lists the per-source holds on the route.

    Args:
        id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EgressHealth | Problem]
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
) -> EgressHealth | Problem | None:
    """Get egress route health

     Returns reachability, latency, exit IP and backoff state for one route. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** health is held in memory and starts empty at restart; three consecutive failures mark
    the route unreachable, skip it in rotation and raise `system.egress_unhealthy`; a later success
    raises `system.egress_recovered`. `sources` lists the per-source holds on the route.

    Args:
        id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EgressHealth | Problem
    """

    return sync_detailed(
        id=id,
        client=client,
    ).parsed


async def asyncio_detailed(
    id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[EgressHealth | Problem]:
    """Get egress route health

     Returns reachability, latency, exit IP and backoff state for one route. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** health is held in memory and starts empty at restart; three consecutive failures mark
    the route unreachable, skip it in rotation and raise `system.egress_unhealthy`; a later success
    raises `system.egress_recovered`. `sources` lists the per-source holds on the route.

    Args:
        id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EgressHealth | Problem]
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
) -> EgressHealth | Problem | None:
    """Get egress route health

     Returns reachability, latency, exit IP and backoff state for one route. Requires `network.manage`.

    **Scope:** `admin`.

    **R2-018:** health is held in memory and starts empty at restart; three consecutive failures mark
    the route unreachable, skip it in rotation and raise `system.egress_unhealthy`; a later success
    raises `system.egress_recovered`. `sources` lists the per-source holds on the route.

    Args:
        id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EgressHealth | Problem
    """

    return (
        await asyncio_detailed(
            id=id,
            client=client,
        )
    ).parsed
