import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.social_post_page import SocialPostPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    topic: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cue_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["topic"] = topic

    params["platform"] = platform

    params["q"] = q

    params["cueId"] = cue_id

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    json_until: str | Unset = UNSET
    if not isinstance(until, Unset):
        json_until = until.isoformat()
    params["until"] = json_until

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/posts",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SocialPostPage | None:
    if response.status_code == 200:
        response_200 = SocialPostPage.from_dict(response.json())

        return response_200

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
) -> Response[Problem | SocialPostPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cue_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SocialPostPage]:
    """Search social posts

     Stored `social-post` records newest first, filtered by topic, platform, text and time. Deleted posts
    appear as tombstones without text or media links; posts beyond their platform's retention are gone.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        q (str | Unset):
        cue_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialPostPage]
    """

    kwargs = _get_kwargs(
        topic=topic,
        platform=platform,
        q=q,
        cue_id=cue_id,
        since=since,
        until=until,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cue_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SocialPostPage | None:
    """Search social posts

     Stored `social-post` records newest first, filtered by topic, platform, text and time. Deleted posts
    appear as tombstones without text or media links; posts beyond their platform's retention are gone.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        q (str | Unset):
        cue_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialPostPage
    """

    return sync_detailed(
        client=client,
        topic=topic,
        platform=platform,
        q=q,
        cue_id=cue_id,
        since=since,
        until=until,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cue_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SocialPostPage]:
    """Search social posts

     Stored `social-post` records newest first, filtered by topic, platform, text and time. Deleted posts
    appear as tombstones without text or media links; posts beyond their platform's retention are gone.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        q (str | Unset):
        cue_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialPostPage]
    """

    kwargs = _get_kwargs(
        topic=topic,
        platform=platform,
        q=q,
        cue_id=cue_id,
        since=since,
        until=until,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    platform: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cue_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SocialPostPage | None:
    """Search social posts

     Stored `social-post` records newest first, filtered by topic, platform, text and time. Deleted posts
    appear as tombstones without text or media links; posts beyond their platform's retention are gone.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        q (str | Unset):
        cue_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialPostPage
    """

    return (
        await asyncio_detailed(
            client=client,
            topic=topic,
            platform=platform,
            q=q,
            cue_id=cue_id,
            since=since,
            until=until,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
