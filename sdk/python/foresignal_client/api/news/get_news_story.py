from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.news_story import NewsStory
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    story_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/news/stories/{story_id}".format(
            story_id=quote(str(story_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NewsStory | Problem | None:
    if response.status_code == 200:
        response_200 = NewsStory.from_dict(response.json())

        return response_200

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
) -> Response[NewsStory | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    story_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[NewsStory | Problem]:
    """Get a news story

     Articles grouped by outlet with each outlet's ratings and consensus, coverage statistics, linked
    situations and canonical events.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        story_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NewsStory | Problem]
    """

    kwargs = _get_kwargs(
        story_id=story_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    story_id: str,
    *,
    client: AuthenticatedClient,
) -> NewsStory | Problem | None:
    """Get a news story

     Articles grouped by outlet with each outlet's ratings and consensus, coverage statistics, linked
    situations and canonical events.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        story_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NewsStory | Problem
    """

    return sync_detailed(
        story_id=story_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    story_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[NewsStory | Problem]:
    """Get a news story

     Articles grouped by outlet with each outlet's ratings and consensus, coverage statistics, linked
    situations and canonical events.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        story_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NewsStory | Problem]
    """

    kwargs = _get_kwargs(
        story_id=story_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    story_id: str,
    *,
    client: AuthenticatedClient,
) -> NewsStory | Problem | None:
    """Get a news story

     Articles grouped by outlet with each outlet's ratings and consensus, coverage statistics, linked
    situations and canonical events.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        story_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NewsStory | Problem
    """

    return (
        await asyncio_detailed(
            story_id=story_id,
            client=client,
        )
    ).parsed
