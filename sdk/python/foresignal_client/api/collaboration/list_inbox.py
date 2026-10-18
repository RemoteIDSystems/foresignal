import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.inbox_page import InboxPage
from ...models.list_inbox_category import ListInboxCategory
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    unread: bool | Unset = UNSET,
    category: ListInboxCategory | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["unread"] = unread

    json_category: str | Unset = UNSET
    if not isinstance(category, Unset):
        json_category = category.value

    params["category"] = json_category

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/inbox",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> InboxPage | Problem | None:
    if response.status_code == 200:
        response_200 = InboxPage.from_dict(response.json())

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
) -> Response[InboxPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    unread: bool | Unset = UNSET,
    category: ListInboxCategory | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[InboxPage | Problem]:
    """The caller's personal inbox

     Per-principal view of the 033 inbox: mentions, shares, received reports and packages, chronicle
    proposals and warnings, access requests, saved-search matches and assessments addressed to the
    caller, newest first. Reading here never acknowledges an alert.

    **Scope:** `read`.

    Args:
        unread (bool | Unset):
        category (ListInboxCategory | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InboxPage | Problem]
    """

    kwargs = _get_kwargs(
        unread=unread,
        category=category,
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
    unread: bool | Unset = UNSET,
    category: ListInboxCategory | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> InboxPage | Problem | None:
    """The caller's personal inbox

     Per-principal view of the 033 inbox: mentions, shares, received reports and packages, chronicle
    proposals and warnings, access requests, saved-search matches and assessments addressed to the
    caller, newest first. Reading here never acknowledges an alert.

    **Scope:** `read`.

    Args:
        unread (bool | Unset):
        category (ListInboxCategory | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InboxPage | Problem
    """

    return sync_detailed(
        client=client,
        unread=unread,
        category=category,
        since=since,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    unread: bool | Unset = UNSET,
    category: ListInboxCategory | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[InboxPage | Problem]:
    """The caller's personal inbox

     Per-principal view of the 033 inbox: mentions, shares, received reports and packages, chronicle
    proposals and warnings, access requests, saved-search matches and assessments addressed to the
    caller, newest first. Reading here never acknowledges an alert.

    **Scope:** `read`.

    Args:
        unread (bool | Unset):
        category (ListInboxCategory | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InboxPage | Problem]
    """

    kwargs = _get_kwargs(
        unread=unread,
        category=category,
        since=since,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    unread: bool | Unset = UNSET,
    category: ListInboxCategory | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> InboxPage | Problem | None:
    """The caller's personal inbox

     Per-principal view of the 033 inbox: mentions, shares, received reports and packages, chronicle
    proposals and warnings, access requests, saved-search matches and assessments addressed to the
    caller, newest first. Reading here never acknowledges an alert.

    **Scope:** `read`.

    Args:
        unread (bool | Unset):
        category (ListInboxCategory | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InboxPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            unread=unread,
            category=category,
            since=since,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
