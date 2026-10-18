import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analyst_note_page import AnalystNotePage
from ...models.list_analyst_notes_priority import ListAnalystNotesPriority
from ...models.list_analyst_notes_status import ListAnalystNotesStatus
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    profile_id: str | Unset = UNSET,
    status: ListAnalystNotesStatus | Unset = UNSET,
    priority: ListAnalystNotesPriority | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["profileId"] = profile_id

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    json_priority: str | Unset = UNSET
    if not isinstance(priority, Unset):
        json_priority = priority.value

    params["priority"] = json_priority

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analyst/notes",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalystNotePage | Problem | None:
    if response.status_code == 200:
        response_200 = AnalystNotePage.from_dict(response.json())

        return response_200

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
) -> Response[AnalystNotePage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    profile_id: str | Unset = UNSET,
    status: ListAnalystNotesStatus | Unset = UNSET,
    priority: ListAnalystNotesPriority | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AnalystNotePage | Problem]:
    """List analyst notes

     Newest first. Private notes are excluded for callers without private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (ListAnalystNotesStatus | Unset):
        priority (ListAnalystNotesPriority | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystNotePage | Problem]
    """

    kwargs = _get_kwargs(
        profile_id=profile_id,
        status=status,
        priority=priority,
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
    profile_id: str | Unset = UNSET,
    status: ListAnalystNotesStatus | Unset = UNSET,
    priority: ListAnalystNotesPriority | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AnalystNotePage | Problem | None:
    """List analyst notes

     Newest first. Private notes are excluded for callers without private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (ListAnalystNotesStatus | Unset):
        priority (ListAnalystNotesPriority | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystNotePage | Problem
    """

    return sync_detailed(
        client=client,
        profile_id=profile_id,
        status=status,
        priority=priority,
        since=since,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    profile_id: str | Unset = UNSET,
    status: ListAnalystNotesStatus | Unset = UNSET,
    priority: ListAnalystNotesPriority | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AnalystNotePage | Problem]:
    """List analyst notes

     Newest first. Private notes are excluded for callers without private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (ListAnalystNotesStatus | Unset):
        priority (ListAnalystNotesPriority | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystNotePage | Problem]
    """

    kwargs = _get_kwargs(
        profile_id=profile_id,
        status=status,
        priority=priority,
        since=since,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    profile_id: str | Unset = UNSET,
    status: ListAnalystNotesStatus | Unset = UNSET,
    priority: ListAnalystNotesPriority | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AnalystNotePage | Problem | None:
    """List analyst notes

     Newest first. Private notes are excluded for callers without private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        status (ListAnalystNotesStatus | Unset):
        priority (ListAnalystNotesPriority | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystNotePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            profile_id=profile_id,
            status=status,
            priority=priority,
            since=since,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
