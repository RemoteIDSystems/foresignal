import datetime
from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.tile_layer_index import TileLayerIndex
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    ticket: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    params["ticket"] = ticket

    json_as_of: str | Unset = UNSET
    if not isinstance(as_of, Unset):
        json_as_of = as_of.isoformat()
    params["asOf"] = json_as_of

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/tiles.json",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | TileLayerIndex | None:
    if response.status_code == 200:
        response_200 = TileLayerIndex.from_dict(response.json())

        return response_200

    if response.status_code == 301:
        response_301 = cast(Any, None)
        return response_301

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem | TileLayerIndex]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | TileLayerIndex]:
    """Vector-tile layer index

     Every registered source id and every kind with at least one source the caller can see (private
    sources only for token callers; a kind spelled like a source id is left out because the name
    resolves to the source), each with its TileJSON URL (under `server.publicBaseUrl`, else the
    request's scheme, host and port) and aggBelowZoom (null = never aggregate). Strong ETag; `Cache-
    Control: public, max-age=300` (`private` when openReads is off). With `asOf` (037) every TileJSON
    URL carries the canonical `asOf`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        ticket (str | Unset):
        as_of (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | TileLayerIndex]
    """

    kwargs = _get_kwargs(
        ticket=ticket,
        as_of=as_of,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | TileLayerIndex | None:
    """Vector-tile layer index

     Every registered source id and every kind with at least one source the caller can see (private
    sources only for token callers; a kind spelled like a source id is left out because the name
    resolves to the source), each with its TileJSON URL (under `server.publicBaseUrl`, else the
    request's scheme, host and port) and aggBelowZoom (null = never aggregate). Strong ETag; `Cache-
    Control: public, max-age=300` (`private` when openReads is off). With `asOf` (037) every TileJSON
    URL carries the canonical `asOf`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        ticket (str | Unset):
        as_of (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | TileLayerIndex
    """

    return sync_detailed(
        client=client,
        ticket=ticket,
        as_of=as_of,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | TileLayerIndex]:
    """Vector-tile layer index

     Every registered source id and every kind with at least one source the caller can see (private
    sources only for token callers; a kind spelled like a source id is left out because the name
    resolves to the source), each with its TileJSON URL (under `server.publicBaseUrl`, else the
    request's scheme, host and port) and aggBelowZoom (null = never aggregate). Strong ETag; `Cache-
    Control: public, max-age=300` (`private` when openReads is off). With `asOf` (037) every TileJSON
    URL carries the canonical `asOf`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        ticket (str | Unset):
        as_of (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | TileLayerIndex]
    """

    kwargs = _get_kwargs(
        ticket=ticket,
        as_of=as_of,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | TileLayerIndex | None:
    """Vector-tile layer index

     Every registered source id and every kind with at least one source the caller can see (private
    sources only for token callers; a kind spelled like a source id is left out because the name
    resolves to the source), each with its TileJSON URL (under `server.publicBaseUrl`, else the
    request's scheme, host and port) and aggBelowZoom (null = never aggregate). Strong ETag; `Cache-
    Control: public, max-age=300` (`private` when openReads is off). With `asOf` (037) every TileJSON
    URL carries the canonical `asOf`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        ticket (str | Unset):
        as_of (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | TileLayerIndex
    """

    return (
        await asyncio_detailed(
            client=client,
            ticket=ticket,
            as_of=as_of,
            if_none_match=if_none_match,
        )
    ).parsed
