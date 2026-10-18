import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_news_stories_blindspot import ListNewsStoriesBlindspot
from ...models.news_story_page import NewsStoryPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    topic: str | Unset = UNSET,
    region: str | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    blindspot: ListNewsStoriesBlindspot | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["topic"] = topic

    params["region"] = region

    params["situationId"] = situation_id

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    json_blindspot: str | Unset = UNSET
    if not isinstance(blindspot, Unset):
        json_blindspot = blindspot.value

    params["blindspot"] = json_blindspot

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/news/stories",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NewsStoryPage | Problem | None:
    if response.status_code == 200:
        response_200 = NewsStoryPage.from_dict(response.json())

        return response_200

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
) -> Response[NewsStoryPage | Problem]:
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
    region: str | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    blindspot: ListNewsStoriesBlindspot | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[NewsStoryPage | Problem]:
    """List news stories

     Stories with coverage balance, blindspot and corroboration. LOCAL_ONLY ratings only reach callers
    allowed local egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        region (str | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):
        blindspot (ListNewsStoriesBlindspot | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NewsStoryPage | Problem]
    """

    kwargs = _get_kwargs(
        topic=topic,
        region=region,
        situation_id=situation_id,
        since=since,
        blindspot=blindspot,
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
    region: str | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    blindspot: ListNewsStoriesBlindspot | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> NewsStoryPage | Problem | None:
    """List news stories

     Stories with coverage balance, blindspot and corroboration. LOCAL_ONLY ratings only reach callers
    allowed local egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        region (str | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):
        blindspot (ListNewsStoriesBlindspot | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NewsStoryPage | Problem
    """

    return sync_detailed(
        client=client,
        topic=topic,
        region=region,
        situation_id=situation_id,
        since=since,
        blindspot=blindspot,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    region: str | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    blindspot: ListNewsStoriesBlindspot | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[NewsStoryPage | Problem]:
    """List news stories

     Stories with coverage balance, blindspot and corroboration. LOCAL_ONLY ratings only reach callers
    allowed local egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        region (str | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):
        blindspot (ListNewsStoriesBlindspot | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NewsStoryPage | Problem]
    """

    kwargs = _get_kwargs(
        topic=topic,
        region=region,
        situation_id=situation_id,
        since=since,
        blindspot=blindspot,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    region: str | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    blindspot: ListNewsStoriesBlindspot | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> NewsStoryPage | Problem | None:
    """List news stories

     Stories with coverage balance, blindspot and corroboration. LOCAL_ONLY ratings only reach callers
    allowed local egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        region (str | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):
        blindspot (ListNewsStoriesBlindspot | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NewsStoryPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            topic=topic,
            region=region,
            situation_id=situation_id,
            since=since,
            blindspot=blindspot,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
