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
    briefing_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/briefings/{briefing_id}/podcast.rss".format(
            briefing_id=quote(str(briefing_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

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
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[Any | Problem]:
    """Briefing audio podcast feed

     RSS 2.0 feed (058 feed writer family) of the briefing's runs that have audio (`audio.status` `ok`
    and a media row that still exists), newest first, at most 50 items. Each `<item>` has a non-
    permalink `<guid>`, the run's HTML rendition as `<link>`, its plain text as `<description>`, and an
    `<enclosure url length type>` to the 043 media (`audio/mpeg` or `audio/ogg`), whose detached JWS is
    the run's `audio.signature` (109). Feed keys work through
    `/v1/k/{key}/briefings/{briefingId}/podcast.rss`; the enclosure URLs then carry the same key, so
    create the key with prefixes covering both the feed and `/v1/media/`. Same visibility as the run
    feeds: 404 unless the briefing is visible to the caller, runs citing private sources only behind the
    private gate. Strong ETag and `Last-Modified`; 304 on a match.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Any | Problem | None:
    """Briefing audio podcast feed

     RSS 2.0 feed (058 feed writer family) of the briefing's runs that have audio (`audio.status` `ok`
    and a media row that still exists), newest first, at most 50 items. Each `<item>` has a non-
    permalink `<guid>`, the run's HTML rendition as `<link>`, its plain text as `<description>`, and an
    `<enclosure url length type>` to the 043 media (`audio/mpeg` or `audio/ogg`), whose detached JWS is
    the run's `audio.signature` (109). Feed keys work through
    `/v1/k/{key}/briefings/{briefingId}/podcast.rss`; the enclosure URLs then carry the same key, so
    create the key with prefixes covering both the feed and `/v1/media/`. Same visibility as the run
    feeds: 404 unless the briefing is visible to the caller, runs citing private sources only behind the
    private gate. Strong ETag and `Last-Modified`; 304 on a match.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        briefing_id=briefing_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[Any | Problem]:
    """Briefing audio podcast feed

     RSS 2.0 feed (058 feed writer family) of the briefing's runs that have audio (`audio.status` `ok`
    and a media row that still exists), newest first, at most 50 items. Each `<item>` has a non-
    permalink `<guid>`, the run's HTML rendition as `<link>`, its plain text as `<description>`, and an
    `<enclosure url length type>` to the 043 media (`audio/mpeg` or `audio/ogg`), whose detached JWS is
    the run's `audio.signature` (109). Feed keys work through
    `/v1/k/{key}/briefings/{briefingId}/podcast.rss`; the enclosure URLs then carry the same key, so
    create the key with prefixes covering both the feed and `/v1/media/`. Same visibility as the run
    feeds: 404 unless the briefing is visible to the caller, runs citing private sources only behind the
    private gate. Strong ETag and `Last-Modified`; 304 on a match.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Any | Problem | None:
    """Briefing audio podcast feed

     RSS 2.0 feed (058 feed writer family) of the briefing's runs that have audio (`audio.status` `ok`
    and a media row that still exists), newest first, at most 50 items. Each `<item>` has a non-
    permalink `<guid>`, the run's HTML rendition as `<link>`, its plain text as `<description>`, and an
    `<enclosure url length type>` to the 043 media (`audio/mpeg` or `audio/ogg`), whose detached JWS is
    the run's `audio.signature` (109). Feed keys work through
    `/v1/k/{key}/briefings/{briefingId}/podcast.rss`; the enclosure URLs then carry the same key, so
    create the key with prefixes covering both the feed and `/v1/media/`. Same visibility as the run
    feeds: 404 unless the briefing is visible to the caller, runs citing private sources only behind the
    private gate. Strong ETag and `Last-Modified`; 304 on a match.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            briefing_id=briefing_id,
            client=client,
        )
    ).parsed
