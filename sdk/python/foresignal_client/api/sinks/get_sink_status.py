from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.sink_status import SinkStatus
from ...types import Response


def _get_kwargs(
    sink_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/sinks/{sink_id}/status".format(
            sink_id=quote(str(sink_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SinkStatus | None:
    if response.status_code == 200:
        response_200 = SinkStatus.from_dict(response.json())

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
) -> Response[Problem | SinkStatus]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    sink_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SinkStatus]:
    """Sink cursor, lag and counters

     Cursor, lag (`lastPublishedSeq − lastSeq`), state, connected clients (SERVE), delivered, dropped
    (conflated) and denied counts by reason and source.

    **Scope:** `admin`.

    Args:
        sink_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SinkStatus]
    """

    kwargs = _get_kwargs(
        sink_id=sink_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    sink_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SinkStatus | None:
    """Sink cursor, lag and counters

     Cursor, lag (`lastPublishedSeq − lastSeq`), state, connected clients (SERVE), delivered, dropped
    (conflated) and denied counts by reason and source.

    **Scope:** `admin`.

    Args:
        sink_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SinkStatus
    """

    return sync_detailed(
        sink_id=sink_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    sink_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SinkStatus]:
    """Sink cursor, lag and counters

     Cursor, lag (`lastPublishedSeq − lastSeq`), state, connected clients (SERVE), delivered, dropped
    (conflated) and denied counts by reason and source.

    **Scope:** `admin`.

    Args:
        sink_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SinkStatus]
    """

    kwargs = _get_kwargs(
        sink_id=sink_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    sink_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SinkStatus | None:
    """Sink cursor, lag and counters

     Cursor, lag (`lastPublishedSeq − lastSeq`), state, connected clients (SERVE), delivered, dropped
    (conflated) and denied counts by reason and source.

    **Scope:** `admin`.

    Args:
        sink_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SinkStatus
    """

    return (
        await asyncio_detailed(
            sink_id=sink_id,
            client=client,
        )
    ).parsed
