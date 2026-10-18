from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.social_platform import SocialPlatform
from ...models.social_platform_id import SocialPlatformId
from ...types import Response


def _get_kwargs(
    platform_id: SocialPlatformId,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/platforms/{platform_id}".format(
            platform_id=quote(str(platform_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SocialPlatform | None:
    if response.status_code == 200:
        response_200 = SocialPlatform.from_dict(response.json())

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
) -> Response[Problem | SocialPlatform]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    platform_id: SocialPlatformId,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SocialPlatform]:
    """Get a social platform adapter

     One adapter with health, quotas, spend and settings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        platform_id (SocialPlatformId): Social platform adapter (a 004 source with descriptor,
            budget, health and credentials). Only `bluesky-jetstream` is on by default (135)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialPlatform]
    """

    kwargs = _get_kwargs(
        platform_id=platform_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    platform_id: SocialPlatformId,
    *,
    client: AuthenticatedClient,
) -> Problem | SocialPlatform | None:
    """Get a social platform adapter

     One adapter with health, quotas, spend and settings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        platform_id (SocialPlatformId): Social platform adapter (a 004 source with descriptor,
            budget, health and credentials). Only `bluesky-jetstream` is on by default (135)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialPlatform
    """

    return sync_detailed(
        platform_id=platform_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    platform_id: SocialPlatformId,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SocialPlatform]:
    """Get a social platform adapter

     One adapter with health, quotas, spend and settings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        platform_id (SocialPlatformId): Social platform adapter (a 004 source with descriptor,
            budget, health and credentials). Only `bluesky-jetstream` is on by default (135)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialPlatform]
    """

    kwargs = _get_kwargs(
        platform_id=platform_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    platform_id: SocialPlatformId,
    *,
    client: AuthenticatedClient,
) -> Problem | SocialPlatform | None:
    """Get a social platform adapter

     One adapter with health, quotas, spend and settings.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        platform_id (SocialPlatformId): Social platform adapter (a 004 source with descriptor,
            budget, health and credentials). Only `bluesky-jetstream` is on by default (135)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialPlatform
    """

    return (
        await asyncio_detailed(
            platform_id=platform_id,
            client=client,
        )
    ).parsed
