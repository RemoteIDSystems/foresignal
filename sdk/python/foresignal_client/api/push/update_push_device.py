from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.push_device import PushDevice
from ...models.push_device_update import PushDeviceUpdate
from ...types import Response


def _get_kwargs(
    device_id: UUID,
    *,
    body: PushDeviceUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/push/devices/{device_id}".format(
            device_id=quote(str(device_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | PushDevice | None:
    if response.status_code == 200:
        response_200 = PushDevice.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | PushDevice]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    device_id: UUID,
    *,
    client: AuthenticatedClient,
    body: PushDeviceUpdate,
) -> Response[Problem | PushDevice]:
    """Update a push device

     Changes name, filters, content level and language. Owner or admin; otherwise 404.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        device_id (UUID):
        body (PushDeviceUpdate): Editable fields of a registered device (032)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushDevice]
    """

    kwargs = _get_kwargs(
        device_id=device_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    device_id: UUID,
    *,
    client: AuthenticatedClient,
    body: PushDeviceUpdate,
) -> Problem | PushDevice | None:
    """Update a push device

     Changes name, filters, content level and language. Owner or admin; otherwise 404.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        device_id (UUID):
        body (PushDeviceUpdate): Editable fields of a registered device (032)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushDevice
    """

    return sync_detailed(
        device_id=device_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    device_id: UUID,
    *,
    client: AuthenticatedClient,
    body: PushDeviceUpdate,
) -> Response[Problem | PushDevice]:
    """Update a push device

     Changes name, filters, content level and language. Owner or admin; otherwise 404.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        device_id (UUID):
        body (PushDeviceUpdate): Editable fields of a registered device (032)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushDevice]
    """

    kwargs = _get_kwargs(
        device_id=device_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    device_id: UUID,
    *,
    client: AuthenticatedClient,
    body: PushDeviceUpdate,
) -> Problem | PushDevice | None:
    """Update a push device

     Changes name, filters, content level and language. Owner or admin; otherwise 404.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        device_id (UUID):
        body (PushDeviceUpdate): Editable fields of a registered device (032)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushDevice
    """

    return (
        await asyncio_detailed(
            device_id=device_id,
            client=client,
            body=body,
        )
    ).parsed
