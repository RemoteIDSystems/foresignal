import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.canonical_event_page import CanonicalEventPage
from ...models.list_canonical_events_flags_item import ListCanonicalEventsFlagsItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    flags: list[ListCanonicalEventsFlagsItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["family"] = family

    params["region"] = region

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    json_flags: list[str] | Unset = UNSET
    if not isinstance(flags, Unset):
        json_flags = []
        for flags_item_data in flags:
            flags_item = flags_item_data.value
            json_flags.append(flags_item)

    params["flags"] = json_flags

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/evidence/events",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CanonicalEventPage | Problem | None:
    if response.status_code == 200:
        response_200 = CanonicalEventPage.from_dict(response.json())

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
) -> Response[CanonicalEventPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    flags: list[ListCanonicalEventsFlagsItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[CanonicalEventPage | Problem]:
    """List canonical events

     Canonical events (summaries) with observation and evidence-group counts and quality flags.
    Independence is counted in evidence groups, never records or sources.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (str | Unset):
        region (str | Unset):
        since (datetime.datetime | Unset):
        flags (list[ListCanonicalEventsFlagsItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CanonicalEventPage | Problem]
    """

    kwargs = _get_kwargs(
        family=family,
        region=region,
        since=since,
        flags=flags,
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
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    flags: list[ListCanonicalEventsFlagsItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> CanonicalEventPage | Problem | None:
    """List canonical events

     Canonical events (summaries) with observation and evidence-group counts and quality flags.
    Independence is counted in evidence groups, never records or sources.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (str | Unset):
        region (str | Unset):
        since (datetime.datetime | Unset):
        flags (list[ListCanonicalEventsFlagsItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CanonicalEventPage | Problem
    """

    return sync_detailed(
        client=client,
        family=family,
        region=region,
        since=since,
        flags=flags,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    flags: list[ListCanonicalEventsFlagsItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[CanonicalEventPage | Problem]:
    """List canonical events

     Canonical events (summaries) with observation and evidence-group counts and quality flags.
    Independence is counted in evidence groups, never records or sources.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (str | Unset):
        region (str | Unset):
        since (datetime.datetime | Unset):
        flags (list[ListCanonicalEventsFlagsItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CanonicalEventPage | Problem]
    """

    kwargs = _get_kwargs(
        family=family,
        region=region,
        since=since,
        flags=flags,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    family: str | Unset = UNSET,
    region: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    flags: list[ListCanonicalEventsFlagsItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> CanonicalEventPage | Problem | None:
    """List canonical events

     Canonical events (summaries) with observation and evidence-group counts and quality flags.
    Independence is counted in evidence groups, never records or sources.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (str | Unset):
        region (str | Unset):
        since (datetime.datetime | Unset):
        flags (list[ListCanonicalEventsFlagsItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CanonicalEventPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            family=family,
            region=region,
            since=since,
            flags=flags,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
