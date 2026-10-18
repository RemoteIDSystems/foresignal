import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_narratives_state_item import ListNarrativesStateItem
from ...models.list_narratives_status_item import ListNarrativesStatusItem
from ...models.narrative_page import NarrativePage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    topic: str | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    place: str | Unset = UNSET,
    status: list[ListNarrativesStatusItem] | Unset = UNSET,
    state: list[ListNarrativesStateItem] | Unset = UNSET,
    platform: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["topic"] = topic

    params["situationId"] = situation_id

    params["place"] = place

    json_status: list[str] | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = []
        for status_item_data in status:
            status_item = status_item_data.value
            json_status.append(status_item)

    params["status"] = json_status

    json_state: list[str] | Unset = UNSET
    if not isinstance(state, Unset):
        json_state = []
        for state_item_data in state:
            state_item = state_item_data.value
            json_state.append(state_item)

    params["state"] = json_state

    params["platform"] = platform

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/narratives",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NarrativePage | Problem | None:
    if response.status_code == 200:
        response_200 = NarrativePage.from_dict(response.json())

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
) -> Response[NarrativePage | Problem]:
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
    situation_id: str | Unset = UNSET,
    place: str | Unset = UNSET,
    status: list[ListNarrativesStatusItem] | Unset = UNSET,
    state: list[ListNarrativesStateItem] | Unset = UNSET,
    platform: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[NarrativePage | Problem]:
    """List narratives

     Narratives newest activity first with platforms, origin counts (evidence groups), origin
    concentration, velocity, status and state. Earliest posts are earliest observed, never originators.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        place (str | Unset):
        status (list[ListNarrativesStatusItem] | Unset):
        state (list[ListNarrativesStateItem] | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NarrativePage | Problem]
    """

    kwargs = _get_kwargs(
        topic=topic,
        situation_id=situation_id,
        place=place,
        status=status,
        state=state,
        platform=platform,
        since=since,
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
    situation_id: str | Unset = UNSET,
    place: str | Unset = UNSET,
    status: list[ListNarrativesStatusItem] | Unset = UNSET,
    state: list[ListNarrativesStateItem] | Unset = UNSET,
    platform: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> NarrativePage | Problem | None:
    """List narratives

     Narratives newest activity first with platforms, origin counts (evidence groups), origin
    concentration, velocity, status and state. Earliest posts are earliest observed, never originators.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        place (str | Unset):
        status (list[ListNarrativesStatusItem] | Unset):
        state (list[ListNarrativesStateItem] | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NarrativePage | Problem
    """

    return sync_detailed(
        client=client,
        topic=topic,
        situation_id=situation_id,
        place=place,
        status=status,
        state=state,
        platform=platform,
        since=since,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    place: str | Unset = UNSET,
    status: list[ListNarrativesStatusItem] | Unset = UNSET,
    state: list[ListNarrativesStateItem] | Unset = UNSET,
    platform: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[NarrativePage | Problem]:
    """List narratives

     Narratives newest activity first with platforms, origin counts (evidence groups), origin
    concentration, velocity, status and state. Earliest posts are earliest observed, never originators.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        place (str | Unset):
        status (list[ListNarrativesStatusItem] | Unset):
        state (list[ListNarrativesStateItem] | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NarrativePage | Problem]
    """

    kwargs = _get_kwargs(
        topic=topic,
        situation_id=situation_id,
        place=place,
        status=status,
        state=state,
        platform=platform,
        since=since,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    topic: str | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    place: str | Unset = UNSET,
    status: list[ListNarrativesStatusItem] | Unset = UNSET,
    state: list[ListNarrativesStateItem] | Unset = UNSET,
    platform: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> NarrativePage | Problem | None:
    """List narratives

     Narratives newest activity first with platforms, origin counts (evidence groups), origin
    concentration, velocity, status and state. Earliest posts are earliest observed, never originators.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topic (str | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        place (str | Unset):
        status (list[ListNarrativesStatusItem] | Unset):
        state (list[ListNarrativesStateItem] | Unset):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`,
            `threads`, `instagram`, or the platform an EDP provider (024, 138) declares Example:
            bluesky.
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NarrativePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            topic=topic,
            situation_id=situation_id,
            place=place,
            status=status,
            state=state,
            platform=platform,
            since=since,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
