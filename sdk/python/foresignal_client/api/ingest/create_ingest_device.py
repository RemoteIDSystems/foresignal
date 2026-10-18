from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ingest_device_created import IngestDeviceCreated
from ...models.ingest_device_input import IngestDeviceInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
    *,
    body: IngestDeviceInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/custom-sources/{source_id}/devices".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> IngestDeviceCreated | Problem | None:
    if response.status_code == 201:
        response_201 = IngestDeviceCreated.from_dict(response.json())

        return response_201

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
) -> Response[IngestDeviceCreated | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: IngestDeviceInput,
) -> Response[IngestDeviceCreated | Problem]:
    """Create a device-scoped ingest key

     A device key (`eid_`) authenticates only for this source and forces externalId to the bound id (a
    mismatching id → 403 DEVICE_ENTITY_MISMATCH). Returns the key once with OwnTracks, Overland, Traccar
    and GPSLogger setup data.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (IngestDeviceInput): Create a device-scoped ingest key bound to one entity (039)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IngestDeviceCreated | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: IngestDeviceInput,
) -> IngestDeviceCreated | Problem | None:
    """Create a device-scoped ingest key

     A device key (`eid_`) authenticates only for this source and forces externalId to the bound id (a
    mismatching id → 403 DEVICE_ENTITY_MISMATCH). Returns the key once with OwnTracks, Overland, Traccar
    and GPSLogger setup data.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (IngestDeviceInput): Create a device-scoped ingest key bound to one entity (039)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IngestDeviceCreated | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: IngestDeviceInput,
) -> Response[IngestDeviceCreated | Problem]:
    """Create a device-scoped ingest key

     A device key (`eid_`) authenticates only for this source and forces externalId to the bound id (a
    mismatching id → 403 DEVICE_ENTITY_MISMATCH). Returns the key once with OwnTracks, Overland, Traccar
    and GPSLogger setup data.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (IngestDeviceInput): Create a device-scoped ingest key bound to one entity (039)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IngestDeviceCreated | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: IngestDeviceInput,
) -> IngestDeviceCreated | Problem | None:
    """Create a device-scoped ingest key

     A device key (`eid_`) authenticates only for this source and forces externalId to the bound id (a
    mismatching id → 403 DEVICE_ENTITY_MISMATCH). Returns the key once with OwnTracks, Overland, Traccar
    and GPSLogger setup data.

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (IngestDeviceInput): Create a device-scoped ingest key bound to one entity (039)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IngestDeviceCreated | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
