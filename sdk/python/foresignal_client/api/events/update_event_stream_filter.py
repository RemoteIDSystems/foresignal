from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.event_stream_filter import EventStreamFilter
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    stream_id: str,
    *,
    body: EventStreamFilter,
    foresignal_stream_key: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(foresignal_stream_key, Unset):
        headers["Foresignal-Stream-Key"] = foresignal_stream_key

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/events/stream/{stream_id}/filter".format(
            stream_id=quote(str(stream_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EventStreamFilter | Problem | None:
    if response.status_code == 200:
        response_200 = EventStreamFilter.from_dict(response.json())

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
) -> Response[EventStreamFilter | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    stream_id: str,
    *,
    client: AuthenticatedClient,
    body: EventStreamFilter,
    foresignal_stream_key: str | Unset = UNSET,
) -> Response[EventStreamFilter | Problem]:
    """Swap an open stream's filter

     Validates the whole body, then swaps the filter atomically on the stream's sender: the stream emits
    `filter-applied {atSeq, filterVersion}` and every later event uses the new filter. Omitted topic
    blocks are unchanged; an added topic starts live from now without replay; there is no spatial delta
    replay (load the newly exposed area with /v1/records or tiles). Only the owning principal may patch
    (anonymous callers send `Foresignal-Stream-Key`); others get 404.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        stream_id (str):
        foresignal_stream_key (str | Unset):
        body (EventStreamFilter): Filter of a multiplexed event stream; on PATCH, omitted topic
            blocks are unchanged and an added topic starts live from now Example: {'changes': {'bbox':
            '-97.5,32.5,-96.5,33.2', 'live': True}}.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EventStreamFilter | Problem]
    """

    kwargs = _get_kwargs(
        stream_id=stream_id,
        body=body,
        foresignal_stream_key=foresignal_stream_key,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    stream_id: str,
    *,
    client: AuthenticatedClient,
    body: EventStreamFilter,
    foresignal_stream_key: str | Unset = UNSET,
) -> EventStreamFilter | Problem | None:
    """Swap an open stream's filter

     Validates the whole body, then swaps the filter atomically on the stream's sender: the stream emits
    `filter-applied {atSeq, filterVersion}` and every later event uses the new filter. Omitted topic
    blocks are unchanged; an added topic starts live from now without replay; there is no spatial delta
    replay (load the newly exposed area with /v1/records or tiles). Only the owning principal may patch
    (anonymous callers send `Foresignal-Stream-Key`); others get 404.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        stream_id (str):
        foresignal_stream_key (str | Unset):
        body (EventStreamFilter): Filter of a multiplexed event stream; on PATCH, omitted topic
            blocks are unchanged and an added topic starts live from now Example: {'changes': {'bbox':
            '-97.5,32.5,-96.5,33.2', 'live': True}}.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EventStreamFilter | Problem
    """

    return sync_detailed(
        stream_id=stream_id,
        client=client,
        body=body,
        foresignal_stream_key=foresignal_stream_key,
    ).parsed


async def asyncio_detailed(
    stream_id: str,
    *,
    client: AuthenticatedClient,
    body: EventStreamFilter,
    foresignal_stream_key: str | Unset = UNSET,
) -> Response[EventStreamFilter | Problem]:
    """Swap an open stream's filter

     Validates the whole body, then swaps the filter atomically on the stream's sender: the stream emits
    `filter-applied {atSeq, filterVersion}` and every later event uses the new filter. Omitted topic
    blocks are unchanged; an added topic starts live from now without replay; there is no spatial delta
    replay (load the newly exposed area with /v1/records or tiles). Only the owning principal may patch
    (anonymous callers send `Foresignal-Stream-Key`); others get 404.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        stream_id (str):
        foresignal_stream_key (str | Unset):
        body (EventStreamFilter): Filter of a multiplexed event stream; on PATCH, omitted topic
            blocks are unchanged and an added topic starts live from now Example: {'changes': {'bbox':
            '-97.5,32.5,-96.5,33.2', 'live': True}}.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EventStreamFilter | Problem]
    """

    kwargs = _get_kwargs(
        stream_id=stream_id,
        body=body,
        foresignal_stream_key=foresignal_stream_key,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    stream_id: str,
    *,
    client: AuthenticatedClient,
    body: EventStreamFilter,
    foresignal_stream_key: str | Unset = UNSET,
) -> EventStreamFilter | Problem | None:
    """Swap an open stream's filter

     Validates the whole body, then swaps the filter atomically on the stream's sender: the stream emits
    `filter-applied {atSeq, filterVersion}` and every later event uses the new filter. Omitted topic
    blocks are unchanged; an added topic starts live from now without replay; there is no spatial delta
    replay (load the newly exposed area with /v1/records or tiles). Only the owning principal may patch
    (anonymous callers send `Foresignal-Stream-Key`); others get 404.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        stream_id (str):
        foresignal_stream_key (str | Unset):
        body (EventStreamFilter): Filter of a multiplexed event stream; on PATCH, omitted topic
            blocks are unchanged and an added topic starts live from now Example: {'changes': {'bbox':
            '-97.5,32.5,-96.5,33.2', 'live': True}}.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EventStreamFilter | Problem
    """

    return (
        await asyncio_detailed(
            stream_id=stream_id,
            client=client,
            body=body,
            foresignal_stream_key=foresignal_stream_key,
        )
    ).parsed
