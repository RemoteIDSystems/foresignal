from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ingest_device_created import IngestDeviceCreated
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
    device_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/custom-sources/{source_id}/devices/{device_id}/key/rotate".format(
            source_id=quote(str(source_id), safe=""),
            device_id=quote(str(device_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> IngestDeviceCreated | Problem | None:
    if response.status_code == 200:
        response_200 = IngestDeviceCreated.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[IngestDeviceCreated | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    device_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[IngestDeviceCreated | Problem]:
    """Rotate a device key

     Returns the new key and setup data once; the old key stops working.

    **Scope:** `admin`.

    Args:
        source_id (str):
        device_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IngestDeviceCreated | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        device_id=device_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    device_id: UUID,
    *,
    client: AuthenticatedClient,
) -> IngestDeviceCreated | Problem | None:
    """Rotate a device key

     Returns the new key and setup data once; the old key stops working.

    **Scope:** `admin`.

    Args:
        source_id (str):
        device_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IngestDeviceCreated | Problem
    """

    return sync_detailed(
        source_id=source_id,
        device_id=device_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    device_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[IngestDeviceCreated | Problem]:
    """Rotate a device key

     Returns the new key and setup data once; the old key stops working.

    **Scope:** `admin`.

    Args:
        source_id (str):
        device_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IngestDeviceCreated | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        device_id=device_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    device_id: UUID,
    *,
    client: AuthenticatedClient,
) -> IngestDeviceCreated | Problem | None:
    """Rotate a device key

     Returns the new key and setup data once; the old key stops working.

    **Scope:** `admin`.

    Args:
        source_id (str):
        device_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IngestDeviceCreated | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            device_id=device_id,
            client=client,
        )
    ).parsed
