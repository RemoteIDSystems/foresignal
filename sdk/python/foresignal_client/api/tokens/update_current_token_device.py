from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.api_token import ApiToken
from ...models.device_info import DeviceInfo
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: DeviceInfo,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/tokens/current/device",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ApiToken | Problem | None:
    if response.status_code == 200:
        response_200 = ApiToken.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ApiToken | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: DeviceInfo,
) -> Response[ApiToken | Problem]:
    """Report device metadata for this token

     Sets device metadata on the calling token itself (`deviceName`, `platform`, `appVersion`) and stamps
    `reportedAt`. Push devices (032) and the inbox (033) use it to identify the device.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (DeviceInfo): Device metadata a client app reports for its own token (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ApiToken | Problem]
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
    body: DeviceInfo,
) -> ApiToken | Problem | None:
    """Report device metadata for this token

     Sets device metadata on the calling token itself (`deviceName`, `platform`, `appVersion`) and stamps
    `reportedAt`. Push devices (032) and the inbox (033) use it to identify the device.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (DeviceInfo): Device metadata a client app reports for its own token (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ApiToken | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: DeviceInfo,
) -> Response[ApiToken | Problem]:
    """Report device metadata for this token

     Sets device metadata on the calling token itself (`deviceName`, `platform`, `appVersion`) and stamps
    `reportedAt`. Push devices (032) and the inbox (033) use it to identify the device.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (DeviceInfo): Device metadata a client app reports for its own token (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ApiToken | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: DeviceInfo,
) -> ApiToken | Problem | None:
    """Report device metadata for this token

     Sets device metadata on the calling token itself (`deviceName`, `platform`, `appVersion`) and stamps
    `reportedAt`. Push devices (032) and the inbox (033) use it to identify the device.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (DeviceInfo): Device metadata a client app reports for its own token (023)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ApiToken | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
