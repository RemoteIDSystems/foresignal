from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.social_post import SocialPost
from ...types import Response


def _get_kwargs(
    platform: str,
    post_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/posts/{platform}/{post_id}".format(
            platform=quote(str(platform), safe=""),
            post_id=quote(str(post_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SocialPost | None:
    if response.status_code == 200:
        response_200 = SocialPost.from_dict(response.json())

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
) -> Response[Problem | SocialPost]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    platform: str,
    post_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SocialPost]:
    """Get a social post

     The latest version of one post by network and platform post id (percent-encode ids that contain
    `/`).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        post_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialPost]
    """

    kwargs = _get_kwargs(
        platform=platform,
        post_id=post_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    platform: str,
    post_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SocialPost | None:
    """Get a social post

     The latest version of one post by network and platform post id (percent-encode ids that contain
    `/`).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        post_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialPost
    """

    return sync_detailed(
        platform=platform,
        post_id=post_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    platform: str,
    post_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SocialPost]:
    """Get a social post

     The latest version of one post by network and platform post id (percent-encode ids that contain
    `/`).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        post_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialPost]
    """

    kwargs = _get_kwargs(
        platform=platform,
        post_id=post_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    platform: str,
    post_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SocialPost | None:
    """Get a social post

     The latest version of one post by network and platform post id (percent-encode ids that contain
    `/`).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        post_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialPost
    """

    return (
        await asyncio_detailed(
            platform=platform,
            post_id=post_id,
            client=client,
        )
    ).parsed
