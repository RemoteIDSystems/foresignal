from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.network_salt_input import NetworkSaltInput
from ...models.network_salt_status import NetworkSaltStatus
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: NetworkSaltInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/chronicle-network/salts",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NetworkSaltStatus | Problem | None:
    if response.status_code == 200:
        response_200 = NetworkSaltStatus.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[NetworkSaltStatus | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: NetworkSaltInput,
) -> Response[NetworkSaltStatus | Problem]:
    """Install a network discovery salt

     Stores a per-network salt agreed out of band by peer admins (requires `network.federated=true`). The
    salt is kept with the instance's secret material (encrypted), never exported or returned, and used
    for 90 days; installing a new salt for the network retires the previous one and forgets the sketches
    received under it, so linkage restarts with new sketch ids.

    **Scope:** `admin`.

    Args:
        body (NetworkSaltInput): Install a per-network discovery salt agreed out of band (stored
            with secret material, never exported)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NetworkSaltStatus | Problem]
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
    body: NetworkSaltInput,
) -> NetworkSaltStatus | Problem | None:
    """Install a network discovery salt

     Stores a per-network salt agreed out of band by peer admins (requires `network.federated=true`). The
    salt is kept with the instance's secret material (encrypted), never exported or returned, and used
    for 90 days; installing a new salt for the network retires the previous one and forgets the sketches
    received under it, so linkage restarts with new sketch ids.

    **Scope:** `admin`.

    Args:
        body (NetworkSaltInput): Install a per-network discovery salt agreed out of band (stored
            with secret material, never exported)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NetworkSaltStatus | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: NetworkSaltInput,
) -> Response[NetworkSaltStatus | Problem]:
    """Install a network discovery salt

     Stores a per-network salt agreed out of band by peer admins (requires `network.federated=true`). The
    salt is kept with the instance's secret material (encrypted), never exported or returned, and used
    for 90 days; installing a new salt for the network retires the previous one and forgets the sketches
    received under it, so linkage restarts with new sketch ids.

    **Scope:** `admin`.

    Args:
        body (NetworkSaltInput): Install a per-network discovery salt agreed out of band (stored
            with secret material, never exported)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NetworkSaltStatus | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: NetworkSaltInput,
) -> NetworkSaltStatus | Problem | None:
    """Install a network discovery salt

     Stores a per-network salt agreed out of band by peer admins (requires `network.federated=true`). The
    salt is kept with the instance's secret material (encrypted), never exported or returned, and used
    for 90 days; installing a new salt for the network retires the previous one and forgets the sketches
    received under it, so linkage restarts with new sketch ids.

    **Scope:** `admin`.

    Args:
        body (NetworkSaltInput): Install a per-network discovery salt agreed out of band (stored
            with secret material, never exported)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NetworkSaltStatus | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
