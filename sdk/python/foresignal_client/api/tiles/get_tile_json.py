import datetime
from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.tile_json import TileJson
from ...types import UNSET, Response, Unset


def _get_kwargs(
    layer: str,
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
        "url": "/v1/tiles/{layer}.json".format(
            layer=quote(str(layer), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | TileJson | None:
    if response.status_code == 200:
        response_200 = TileJson.from_dict(response.json())

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
) -> Response[Any | Problem | TileJson]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    layer: str,
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | TileJson]:
    """TileJSON for one layer

     TileJSON 3.0.0 for a source id or kind: `tilejson`, `name`, `description`, `version` (Foresignal
    version), `attribution` (distinct descriptor attributions joined with " · "), `scheme: xyz`, `tiles`
    under `server.publicBaseUrl` (else the request's scheme, host and port; X-Forwarded headers are not
    trusted until 023), `minzoom` 0, `maxzoom` 16, `bounds`, and one `vector_layers` entry whose
    `fields` describe every feature property (fieldSpecs label and unit, else String/Number/Boolean),
    plus the extension keys `foresignal:aggBelowZoom` (null = never) and `foresignal:properties`. Strong
    ETag; `Cache-Control: public, max-age=300`. Unknown layer, or a private source asked anonymously →
    404 UNKNOWN_LAYER. With `asOf` (037) the document is the layer as known at that instant: the `tiles`
    template is concrete with the canonical `asOf` baked in, `live` is not among the properties, and
    `foresignal:asOf` echoes the instant.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str):
        ticket (str | Unset):
        as_of (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | TileJson]
    """

    kwargs = _get_kwargs(
        layer=layer,
        ticket=ticket,
        as_of=as_of,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    layer: str,
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | TileJson | None:
    """TileJSON for one layer

     TileJSON 3.0.0 for a source id or kind: `tilejson`, `name`, `description`, `version` (Foresignal
    version), `attribution` (distinct descriptor attributions joined with " · "), `scheme: xyz`, `tiles`
    under `server.publicBaseUrl` (else the request's scheme, host and port; X-Forwarded headers are not
    trusted until 023), `minzoom` 0, `maxzoom` 16, `bounds`, and one `vector_layers` entry whose
    `fields` describe every feature property (fieldSpecs label and unit, else String/Number/Boolean),
    plus the extension keys `foresignal:aggBelowZoom` (null = never) and `foresignal:properties`. Strong
    ETag; `Cache-Control: public, max-age=300`. Unknown layer, or a private source asked anonymously →
    404 UNKNOWN_LAYER. With `asOf` (037) the document is the layer as known at that instant: the `tiles`
    template is concrete with the canonical `asOf` baked in, `live` is not among the properties, and
    `foresignal:asOf` echoes the instant.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str):
        ticket (str | Unset):
        as_of (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | TileJson
    """

    return sync_detailed(
        layer=layer,
        client=client,
        ticket=ticket,
        as_of=as_of,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    layer: str,
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | TileJson]:
    """TileJSON for one layer

     TileJSON 3.0.0 for a source id or kind: `tilejson`, `name`, `description`, `version` (Foresignal
    version), `attribution` (distinct descriptor attributions joined with " · "), `scheme: xyz`, `tiles`
    under `server.publicBaseUrl` (else the request's scheme, host and port; X-Forwarded headers are not
    trusted until 023), `minzoom` 0, `maxzoom` 16, `bounds`, and one `vector_layers` entry whose
    `fields` describe every feature property (fieldSpecs label and unit, else String/Number/Boolean),
    plus the extension keys `foresignal:aggBelowZoom` (null = never) and `foresignal:properties`. Strong
    ETag; `Cache-Control: public, max-age=300`. Unknown layer, or a private source asked anonymously →
    404 UNKNOWN_LAYER. With `asOf` (037) the document is the layer as known at that instant: the `tiles`
    template is concrete with the canonical `asOf` baked in, `live` is not among the properties, and
    `foresignal:asOf` echoes the instant.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str):
        ticket (str | Unset):
        as_of (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | TileJson]
    """

    kwargs = _get_kwargs(
        layer=layer,
        ticket=ticket,
        as_of=as_of,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    layer: str,
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
    as_of: datetime.datetime | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | TileJson | None:
    """TileJSON for one layer

     TileJSON 3.0.0 for a source id or kind: `tilejson`, `name`, `description`, `version` (Foresignal
    version), `attribution` (distinct descriptor attributions joined with " · "), `scheme: xyz`, `tiles`
    under `server.publicBaseUrl` (else the request's scheme, host and port; X-Forwarded headers are not
    trusted until 023), `minzoom` 0, `maxzoom` 16, `bounds`, and one `vector_layers` entry whose
    `fields` describe every feature property (fieldSpecs label and unit, else String/Number/Boolean),
    plus the extension keys `foresignal:aggBelowZoom` (null = never) and `foresignal:properties`. Strong
    ETag; `Cache-Control: public, max-age=300`. Unknown layer, or a private source asked anonymously →
    404 UNKNOWN_LAYER. With `asOf` (037) the document is the layer as known at that instant: the `tiles`
    template is concrete with the canonical `asOf` baked in, `live` is not among the properties, and
    `foresignal:asOf` echoes the instant.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layer (str):
        ticket (str | Unset):
        as_of (datetime.datetime | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | TileJson
    """

    return (
        await asyncio_detailed(
            layer=layer,
            client=client,
            ticket=ticket,
            as_of=as_of,
            if_none_match=if_none_match,
        )
    ).parsed
