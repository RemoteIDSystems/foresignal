from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.channel_airtime import ChannelAirtime
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    channel_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/channels/{channel_id}/airtime".format(
            channel_id=quote(str(channel_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChannelAirtime | Problem | None:
    if response.status_code == 200:
        response_200 = ChannelAirtime.from_dict(response.json())

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
) -> Response[ChannelAirtime | Problem]:
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
) -> Response[ChannelAirtime | Problem]:
    """LoRa airtime governor state

     One-hour sliding-window airtime estimate of a `meshtastic` or `meshcore` channel (Semtech time-on-
    air per preset), against the duty-cycle limit, with queued, folded and dropped counts.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChannelAirtime | Problem]
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
) -> ChannelAirtime | Problem | None:
    """LoRa airtime governor state

     One-hour sliding-window airtime estimate of a `meshtastic` or `meshcore` channel (Semtech time-on-
    air per preset), against the duty-cycle limit, with queued, folded and dropped counts.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChannelAirtime | Problem
    """

    return sync_detailed(
        channel_id=channel_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[ChannelAirtime | Problem]:
    """LoRa airtime governor state

     One-hour sliding-window airtime estimate of a `meshtastic` or `meshcore` channel (Semtech time-on-
    air per preset), against the duty-cycle limit, with queued, folded and dropped counts.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChannelAirtime | Problem]
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
) -> ChannelAirtime | Problem | None:
    """LoRa airtime governor state

     One-hour sliding-window airtime estimate of a `meshtastic` or `meshcore` channel (Semtech time-on-
    air per preset), against the duty-cycle limit, with queued, folded and dropped counts.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChannelAirtime | Problem
    """

    return (
        await asyncio_detailed(
            channel_id=channel_id,
            client=client,
        )
    ).parsed
