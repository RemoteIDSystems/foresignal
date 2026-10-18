from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.narrative_post_page import NarrativePostPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    narrative_id: str,
    *,
    platform: str | Unset = UNSET,
    evidence_group: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["platform"] = platform

    params["evidenceGroup"] = evidence_group

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/narratives/{narrative_id}/posts".format(
            narrative_id=quote(str(narrative_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NarrativePostPage | Problem | None:
    if response.status_code == 200:
        response_200 = NarrativePostPage.from_dict(response.json())

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
) -> Response[NarrativePostPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
    platform: str | Unset = UNSET,
    evidence_group: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[NarrativePostPage | Problem]:
    """List a narrative's posts

     Member posts with their link reason (`seed`, `repost`, `quote`, `text`, `link`, `media`) and
    evidence group, newest first.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        evidence_group (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NarrativePostPage | Problem]
    """

    kwargs = _get_kwargs(
        narrative_id=narrative_id,
        platform=platform,
        evidence_group=evidence_group,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
    platform: str | Unset = UNSET,
    evidence_group: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> NarrativePostPage | Problem | None:
    """List a narrative's posts

     Member posts with their link reason (`seed`, `repost`, `quote`, `text`, `link`, `media`) and
    evidence group, newest first.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        evidence_group (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NarrativePostPage | Problem
    """

    return sync_detailed(
        narrative_id=narrative_id,
        client=client,
        platform=platform,
        evidence_group=evidence_group,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
    platform: str | Unset = UNSET,
    evidence_group: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[NarrativePostPage | Problem]:
    """List a narrative's posts

     Member posts with their link reason (`seed`, `repost`, `quote`, `text`, `link`, `media`) and
    evidence group, newest first.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        evidence_group (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NarrativePostPage | Problem]
    """

    kwargs = _get_kwargs(
        narrative_id=narrative_id,
        platform=platform,
        evidence_group=evidence_group,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
    platform: str | Unset = UNSET,
    evidence_group: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> NarrativePostPage | Problem | None:
    """List a narrative's posts

     Member posts with their link reason (`seed`, `repost`, `quote`, `text`, `link`, `media`) and
    evidence group, newest first.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        evidence_group (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NarrativePostPage | Problem
    """

    return (
        await asyncio_detailed(
            narrative_id=narrative_id,
            client=client,
            platform=platform,
            evidence_group=evidence_group,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
