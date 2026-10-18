import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_social_cues_status_item import ListSocialCuesStatusItem
from ...models.problem import Problem
from ...models.social_cue_page import SocialCuePage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: list[ListSocialCuesStatusItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_status: list[str] | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = []
        for status_item_data in status:
            status_item = status_item_data.value
            json_status.append(status_item)

    params["status"] = json_status

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/cues",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SocialCuePage | None:
    if response.status_code == 200:
        response_200 = SocialCuePage.from_dict(response.json())

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
) -> Response[Problem | SocialCuePage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: list[ListSocialCuesStatusItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SocialCuePage]:
    """List social cues

     Automatic and manual cues newest first with their exact queries, platforms, status, results and
    cost.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListSocialCuesStatusItem] | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialCuePage]
    """

    kwargs = _get_kwargs(
        status=status,
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
    status: list[ListSocialCuesStatusItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SocialCuePage | None:
    """List social cues

     Automatic and manual cues newest first with their exact queries, platforms, status, results and
    cost.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListSocialCuesStatusItem] | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialCuePage
    """

    return sync_detailed(
        client=client,
        status=status,
        since=since,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: list[ListSocialCuesStatusItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SocialCuePage]:
    """List social cues

     Automatic and manual cues newest first with their exact queries, platforms, status, results and
    cost.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListSocialCuesStatusItem] | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SocialCuePage]
    """

    kwargs = _get_kwargs(
        status=status,
        since=since,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: list[ListSocialCuesStatusItem] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SocialCuePage | None:
    """List social cues

     Automatic and manual cues newest first with their exact queries, platforms, status, results and
    cost.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListSocialCuesStatusItem] | Unset):
        since (datetime.datetime | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SocialCuePage
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            since=since,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
