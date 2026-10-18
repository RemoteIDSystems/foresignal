from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.federation_item import FederationItem
from ...models.heartbeat_event import HeartbeatEvent
from ...models.live_event import LiveEvent
from ...models.problem import Problem
from ...models.reset_event import ResetEvent
from ...models.resync_event import ResyncEvent
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    after: int | Unset = UNSET,
    live: bool | Unset = False,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(last_event_id, Unset):
        headers["Last-Event-ID"] = last_event_id

    params: dict[str, Any] = {}

    params["after"] = after

    params["live"] = live

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params["bbox"] = bbox

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/federation/stream",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> (
    FederationItem
    | HeartbeatEvent
    | LiveEvent
    | ResetEvent
    | ResyncEvent
    | Problem
    | None
):
    if response.status_code == 200:

        def _parse_response_200(
            data: object,
        ) -> FederationItem | HeartbeatEvent | LiveEvent | ResetEvent | ResyncEvent:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = FederationItem.from_dict(data)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_1 = LiveEvent.from_dict(data)

                return response_200_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_2 = HeartbeatEvent.from_dict(data)

                return response_200_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_3 = ResyncEvent.from_dict(data)

                return response_200_type_3
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            response_200_type_4 = ResetEvent.from_dict(data)

            return response_200_type_4

        response_200 = _parse_response_200(response.text)

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[
    FederationItem | HeartbeatEvent | LiveEvent | ResetEvent | ResyncEvent | Problem
]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    live: bool | Unset = False,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> Response[
    FederationItem | HeartbeatEvent | LiveEvent | ResetEvent | ResyncEvent | Problem
]:
    """Federation change stream (SSE)

     SSE with the 015 lanes: `change` (full FederationItem, id = seq), optional `live` with `live=true`,
    `heartbeat`, `resync`, and `reset` below the purge horizon. Resumes from `Last-Event-ID` or `after`.
    Per record: FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers,
    MLAT positions), private sources and exercise records (unless the token sets includeExercise) are
    excluded. Restriction columns of the token still apply.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        after (int | Unset):
        live (bool | Unset):  Default: False.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationItem | HeartbeatEvent | LiveEvent | ResetEvent | ResyncEvent | Problem]
    """

    kwargs = _get_kwargs(
        after=after,
        live=live,
        source=source,
        kind=kind,
        bbox=bbox,
        last_event_id=last_event_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    live: bool | Unset = False,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> (
    FederationItem
    | HeartbeatEvent
    | LiveEvent
    | ResetEvent
    | ResyncEvent
    | Problem
    | None
):
    """Federation change stream (SSE)

     SSE with the 015 lanes: `change` (full FederationItem, id = seq), optional `live` with `live=true`,
    `heartbeat`, `resync`, and `reset` below the purge horizon. Resumes from `Last-Event-ID` or `after`.
    Per record: FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers,
    MLAT positions), private sources and exercise records (unless the token sets includeExercise) are
    excluded. Restriction columns of the token still apply.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        after (int | Unset):
        live (bool | Unset):  Default: False.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationItem | HeartbeatEvent | LiveEvent | ResetEvent | ResyncEvent | Problem
    """

    return sync_detailed(
        client=client,
        after=after,
        live=live,
        source=source,
        kind=kind,
        bbox=bbox,
        last_event_id=last_event_id,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    live: bool | Unset = False,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> Response[
    FederationItem | HeartbeatEvent | LiveEvent | ResetEvent | ResyncEvent | Problem
]:
    """Federation change stream (SSE)

     SSE with the 015 lanes: `change` (full FederationItem, id = seq), optional `live` with `live=true`,
    `heartbeat`, `resync`, and `reset` below the purge horizon. Resumes from `Last-Event-ID` or `after`.
    Per record: FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers,
    MLAT positions), private sources and exercise records (unless the token sets includeExercise) are
    excluded. Restriction columns of the token still apply.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        after (int | Unset):
        live (bool | Unset):  Default: False.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationItem | HeartbeatEvent | LiveEvent | ResetEvent | ResyncEvent | Problem]
    """

    kwargs = _get_kwargs(
        after=after,
        live=live,
        source=source,
        kind=kind,
        bbox=bbox,
        last_event_id=last_event_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    live: bool | Unset = False,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> (
    FederationItem
    | HeartbeatEvent
    | LiveEvent
    | ResetEvent
    | ResyncEvent
    | Problem
    | None
):
    """Federation change stream (SSE)

     SSE with the 015 lanes: `change` (full FederationItem, id = seq), optional `live` with `live=true`,
    `heartbeat`, `resync`, and `reset` below the purge horizon. Resumes from `Last-Event-ID` or `after`.
    Per record: FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers,
    MLAT positions), private sources and exercise records (unless the token sets includeExercise) are
    excluded. Restriction columns of the token still apply.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        after (int | Unset):
        live (bool | Unset):  Default: False.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationItem | HeartbeatEvent | LiveEvent | ResetEvent | ResyncEvent | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            after=after,
            live=live,
            source=source,
            kind=kind,
            bbox=bbox,
            last_event_id=last_event_id,
        )
    ).parsed
