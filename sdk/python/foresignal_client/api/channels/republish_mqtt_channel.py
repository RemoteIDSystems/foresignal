from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    channel_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/channels/{channel_id}/mqtt/republish".format(
            channel_id=quote(str(channel_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 202:
        response_202 = cast(Any, None)
        return response_202

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
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
) -> Response[Any | Problem]:
    """Re-publish MQTT state and HA discovery

     Forces re-publication of every retained state topic (`{prefix}/rules/{ruleId}/state`,
    `{prefix}/sources/{sourceId}/health`, `{prefix}/summary`) and, when `haDiscovery=true`, the Home
    Assistant device config. `{prefix}/status` is re-published as `online` on every (re)connect.
    Publishing happens asynchronously on the channel's connection.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
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
) -> Any | Problem | None:
    """Re-publish MQTT state and HA discovery

     Forces re-publication of every retained state topic (`{prefix}/rules/{ruleId}/state`,
    `{prefix}/sources/{sourceId}/health`, `{prefix}/summary`) and, when `haDiscovery=true`, the Home
    Assistant device config. `{prefix}/status` is re-published as `online` on every (re)connect.
    Publishing happens asynchronously on the channel's connection.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        channel_id=channel_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    channel_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[Any | Problem]:
    """Re-publish MQTT state and HA discovery

     Forces re-publication of every retained state topic (`{prefix}/rules/{ruleId}/state`,
    `{prefix}/sources/{sourceId}/health`, `{prefix}/summary`) and, when `haDiscovery=true`, the Home
    Assistant device config. `{prefix}/status` is re-published as `online` on every (re)connect.
    Publishing happens asynchronously on the channel's connection.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
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
) -> Any | Problem | None:
    """Re-publish MQTT state and HA discovery

     Forces re-publication of every retained state topic (`{prefix}/rules/{ruleId}/state`,
    `{prefix}/sources/{sourceId}/health`, `{prefix}/summary`) and, when `haDiscovery=true`, the Home
    Assistant device config. `{prefix}/status` is re-published as `online` on every (re)connect.
    Publishing happens asynchronously on the channel's connection.

    **Scope:** `admin`.

    Args:
        channel_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            channel_id=channel_id,
            client=client,
        )
    ).parsed
