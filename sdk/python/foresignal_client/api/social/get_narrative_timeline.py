from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_narrative_timeline_interval import GetNarrativeTimelineInterval
from ...models.narrative_timeline import NarrativeTimeline
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    narrative_id: str,
    *,
    interval: GetNarrativeTimelineInterval | Unset = GetNarrativeTimelineInterval.PT1H,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_interval: str | Unset = UNSET
    if not isinstance(interval, Unset):
        json_interval = interval.value

    params["interval"] = json_interval

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/narratives/{narrative_id}/timeline".format(
            narrative_id=quote(str(narrative_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NarrativeTimeline | Problem | None:
    if response.status_code == 200:
        response_200 = NarrativeTimeline.from_dict(response.json())

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
) -> Response[NarrativeTimeline | Problem]:
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
    interval: GetNarrativeTimelineInterval | Unset = GetNarrativeTimelineInterval.PT1H,
) -> Response[NarrativeTimeline | Problem]:
    """Get a narrative's timeline

     Posts and unique authors per platform per interval over the narrative's life.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        interval (GetNarrativeTimelineInterval | Unset):  Default:
            GetNarrativeTimelineInterval.PT1H.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NarrativeTimeline | Problem]
    """

    kwargs = _get_kwargs(
        narrative_id=narrative_id,
        interval=interval,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
    interval: GetNarrativeTimelineInterval | Unset = GetNarrativeTimelineInterval.PT1H,
) -> NarrativeTimeline | Problem | None:
    """Get a narrative's timeline

     Posts and unique authors per platform per interval over the narrative's life.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        interval (GetNarrativeTimelineInterval | Unset):  Default:
            GetNarrativeTimelineInterval.PT1H.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NarrativeTimeline | Problem
    """

    return sync_detailed(
        narrative_id=narrative_id,
        client=client,
        interval=interval,
    ).parsed


async def asyncio_detailed(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
    interval: GetNarrativeTimelineInterval | Unset = GetNarrativeTimelineInterval.PT1H,
) -> Response[NarrativeTimeline | Problem]:
    """Get a narrative's timeline

     Posts and unique authors per platform per interval over the narrative's life.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        interval (GetNarrativeTimelineInterval | Unset):  Default:
            GetNarrativeTimelineInterval.PT1H.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NarrativeTimeline | Problem]
    """

    kwargs = _get_kwargs(
        narrative_id=narrative_id,
        interval=interval,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
    interval: GetNarrativeTimelineInterval | Unset = GetNarrativeTimelineInterval.PT1H,
) -> NarrativeTimeline | Problem | None:
    """Get a narrative's timeline

     Posts and unique authors per platform per interval over the narrative's life.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        interval (GetNarrativeTimelineInterval | Unset):  Default:
            GetNarrativeTimelineInterval.PT1H.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NarrativeTimeline | Problem
    """

    return (
        await asyncio_detailed(
            narrative_id=narrative_id,
            client=client,
            interval=interval,
        )
    ).parsed
