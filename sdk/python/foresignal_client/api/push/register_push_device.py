from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.push_device import PushDevice
from ...models.push_device_input import PushDeviceInput
from ...types import Response


def _get_kwargs(
    *,
    body: PushDeviceInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/push/devices",
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

    if response.status_code == 201:
        response_201 = PushDevice.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

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
    *,
    client: AuthenticatedClient,
    body: PushDeviceInput,
) -> Response[Problem | PushDevice]:
    """Register a native push device

     Registers an APNs or FCM device token for the calling API token. Requires an enabled provider whose
    `appIds` contains `appId` (409 PUSH_PROVIDER_NOT_CONFIGURED) and, for APNs, a key environment
    covering `environment`. Upserts by (transport, token): re-registering moves the device to the
    calling token. App version and device name are copied from the token's device metadata (023) when
    present; `platform` is the request's (DeviceInfo has no `ipados`). APNs requires `environment` and
    an Apple platform (ios, ipados, macos); for FCM `environment` is ignored and stored as null. Cap 20
    devices per token (409 PUSH_LIMIT_REACHED).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (PushDeviceInput): Register a native device for push (032)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushDevice]
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
    body: PushDeviceInput,
) -> Problem | PushDevice | None:
    """Register a native push device

     Registers an APNs or FCM device token for the calling API token. Requires an enabled provider whose
    `appIds` contains `appId` (409 PUSH_PROVIDER_NOT_CONFIGURED) and, for APNs, a key environment
    covering `environment`. Upserts by (transport, token): re-registering moves the device to the
    calling token. App version and device name are copied from the token's device metadata (023) when
    present; `platform` is the request's (DeviceInfo has no `ipados`). APNs requires `environment` and
    an Apple platform (ios, ipados, macos); for FCM `environment` is ignored and stored as null. Cap 20
    devices per token (409 PUSH_LIMIT_REACHED).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (PushDeviceInput): Register a native device for push (032)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushDevice
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: PushDeviceInput,
) -> Response[Problem | PushDevice]:
    """Register a native push device

     Registers an APNs or FCM device token for the calling API token. Requires an enabled provider whose
    `appIds` contains `appId` (409 PUSH_PROVIDER_NOT_CONFIGURED) and, for APNs, a key environment
    covering `environment`. Upserts by (transport, token): re-registering moves the device to the
    calling token. App version and device name are copied from the token's device metadata (023) when
    present; `platform` is the request's (DeviceInfo has no `ipados`). APNs requires `environment` and
    an Apple platform (ios, ipados, macos); for FCM `environment` is ignored and stored as null. Cap 20
    devices per token (409 PUSH_LIMIT_REACHED).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (PushDeviceInput): Register a native device for push (032)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushDevice]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: PushDeviceInput,
) -> Problem | PushDevice | None:
    """Register a native push device

     Registers an APNs or FCM device token for the calling API token. Requires an enabled provider whose
    `appIds` contains `appId` (409 PUSH_PROVIDER_NOT_CONFIGURED) and, for APNs, a key environment
    covering `environment`. Upserts by (transport, token): re-registering moves the device to the
    calling token. App version and device name are copied from the token's device metadata (023) when
    present; `platform` is the request's (DeviceInfo has no `ipados`). APNs requires `environment` and
    an Apple platform (ios, ipados, macos); for FCM `environment` is ignored and stored as null. Cap 20
    devices per token (409 PUSH_LIMIT_REACHED).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (PushDeviceInput): Register a native device for push (032)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushDevice
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
