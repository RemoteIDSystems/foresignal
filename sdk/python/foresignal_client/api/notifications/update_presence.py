from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.client_presence import ClientPresence
from ...models.client_presence_input import ClientPresenceInput
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    body: ClientPresenceInput,
    foresignal_device_id: UUID | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_device_id, Unset):
        headers["Foresignal-Device-Id"] = foresignal_device_id

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/clients/presence",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ClientPresence | Problem | None:
    if response.status_code == 200:
        response_200 = ClientPresence.from_dict(response.json())

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
) -> Response[ClientPresence | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ClientPresenceInput,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[ClientPresence | Problem]:
    """Report client presence

     Upserts this device's presence (`ttlSeconds` ≤ 300). While any desktop is `active`, non-critical
    alerts on channels with `deferWhileActiveSeconds` are deferred and suppressed if read or
    acknowledged meanwhile. Critical alerts and system events are never deferred; stale presence fails
    open.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):
        body (ClientPresenceInput): Presence heartbeat from a client (033)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ClientPresence | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        foresignal_device_id=foresignal_device_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ClientPresenceInput,
    foresignal_device_id: UUID | Unset = UNSET,
) -> ClientPresence | Problem | None:
    """Report client presence

     Upserts this device's presence (`ttlSeconds` ≤ 300). While any desktop is `active`, non-critical
    alerts on channels with `deferWhileActiveSeconds` are deferred and suppressed if read or
    acknowledged meanwhile. Critical alerts and system events are never deferred; stale presence fails
    open.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):
        body (ClientPresenceInput): Presence heartbeat from a client (033)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ClientPresence | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
        foresignal_device_id=foresignal_device_id,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ClientPresenceInput,
    foresignal_device_id: UUID | Unset = UNSET,
) -> Response[ClientPresence | Problem]:
    """Report client presence

     Upserts this device's presence (`ttlSeconds` ≤ 300). While any desktop is `active`, non-critical
    alerts on channels with `deferWhileActiveSeconds` are deferred and suppressed if read or
    acknowledged meanwhile. Critical alerts and system events are never deferred; stale presence fails
    open.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):
        body (ClientPresenceInput): Presence heartbeat from a client (033)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ClientPresence | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
        foresignal_device_id=foresignal_device_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ClientPresenceInput,
    foresignal_device_id: UUID | Unset = UNSET,
) -> ClientPresence | Problem | None:
    """Report client presence

     Upserts this device's presence (`ttlSeconds` ≤ 300). While any desktop is `active`, non-critical
    alerts on channels with `deferWhileActiveSeconds` are deferred and suppressed if read or
    acknowledged meanwhile. Critical alerts and system events are never deferred; stale presence fails
    open.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        foresignal_device_id (UUID | Unset):
        body (ClientPresenceInput): Presence heartbeat from a client (033)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ClientPresence | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
            foresignal_device_id=foresignal_device_id,
        )
    ).parsed
