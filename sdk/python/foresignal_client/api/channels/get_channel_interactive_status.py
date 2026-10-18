from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.channel_interactive_status import ChannelInteractiveStatus
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    channel_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/channels/{channel_id}/interactive".format(
            channel_id=quote(str(channel_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChannelInteractiveStatus | Problem | None:
    if response.status_code == 200:
        response_200 = ChannelInteractiveStatus.from_dict(response.json())

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
) -> Response[ChannelInteractiveStatus | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[ChannelInteractiveStatus | Problem]:
    """Interactive chat-command status

     Status of the chat-command connector of an interactive `telegram` or `matrix` channel: connection,
    last poll, last error, health (degraded when a Telegram bot has a foreign webhook without
    `takeOverWebhook`, or a Matrix room is encrypted; failing after 401/403), the discovery window, and
    the allowlisted chats with their scope and whether each is served (`active`, `reason`). A channel
    that is not served (not interactive, disabled, or `chatops.enabled` off) reports `status: idle` with
    the reason.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChannelInteractiveStatus | Problem]
    """

    kwargs = _get_kwargs(
        channel_id=channel_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
) -> ChannelInteractiveStatus | Problem | None:
    """Interactive chat-command status

     Status of the chat-command connector of an interactive `telegram` or `matrix` channel: connection,
    last poll, last error, health (degraded when a Telegram bot has a foreign webhook without
    `takeOverWebhook`, or a Matrix room is encrypted; failing after 401/403), the discovery window, and
    the allowlisted chats with their scope and whether each is served (`active`, `reason`). A channel
    that is not served (not interactive, disabled, or `chatops.enabled` off) reports `status: idle` with
    the reason.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChannelInteractiveStatus | Problem
    """

    return sync_detailed(
        channel_id=channel_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[ChannelInteractiveStatus | Problem]:
    """Interactive chat-command status

     Status of the chat-command connector of an interactive `telegram` or `matrix` channel: connection,
    last poll, last error, health (degraded when a Telegram bot has a foreign webhook without
    `takeOverWebhook`, or a Matrix room is encrypted; failing after 401/403), the discovery window, and
    the allowlisted chats with their scope and whether each is served (`active`, `reason`). A channel
    that is not served (not interactive, disabled, or `chatops.enabled` off) reports `status: idle` with
    the reason.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChannelInteractiveStatus | Problem]
    """

    kwargs = _get_kwargs(
        channel_id=channel_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
) -> ChannelInteractiveStatus | Problem | None:
    """Interactive chat-command status

     Status of the chat-command connector of an interactive `telegram` or `matrix` channel: connection,
    last poll, last error, health (degraded when a Telegram bot has a foreign webhook without
    `takeOverWebhook`, or a Matrix room is encrypted; failing after 401/403), the discovery window, and
    the allowlisted chats with their scope and whether each is served (`active`, `reason`). A channel
    that is not served (not interactive, disabled, or `chatops.enabled` off) reports `status: idle` with
    the reason.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChannelInteractiveStatus | Problem
    """

    return (
        await asyncio_detailed(
            channel_id=channel_id,
            client=client,
        )
    ).parsed
