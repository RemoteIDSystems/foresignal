import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.situation_page import SituationPage
from ...models.situation_status import SituationStatus
from ...models.situation_theme import SituationTheme
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: list[SituationStatus] | Unset = UNSET,
    theme: list[SituationTheme] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
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

    json_theme: list[str] | Unset = UNSET
    if not isinstance(theme, Unset):
        json_theme = []
        for theme_item_data in theme:
            theme_item = theme_item_data.value
            json_theme.append(theme_item)

    params["theme"] = json_theme

    json_region: list[str] | Unset = UNSET
    if not isinstance(region, Unset):
        json_region = region

    params["region"] = json_region

    params["bbox"] = bbox

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["q"] = q

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/situations",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SituationPage | None:
    if response.status_code == 200:
        response_200 = SituationPage.from_dict(response.json())

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
) -> Response[Problem | SituationPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: list[SituationStatus] | Unset = UNSET,
    theme: list[SituationTheme] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SituationPage]:
    """List situations

     Lists situations by latest activity (keyset on `lastActivityAt`, `id`). Private situations are
    returned only to callers allowed private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[SituationStatus] | Unset):
        theme (list[SituationTheme] | Unset):
        region (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SituationPage]
    """

    kwargs = _get_kwargs(
        status=status,
        theme=theme,
        region=region,
        bbox=bbox,
        since=since,
        q=q,
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
    status: list[SituationStatus] | Unset = UNSET,
    theme: list[SituationTheme] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SituationPage | None:
    """List situations

     Lists situations by latest activity (keyset on `lastActivityAt`, `id`). Private situations are
    returned only to callers allowed private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[SituationStatus] | Unset):
        theme (list[SituationTheme] | Unset):
        region (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SituationPage
    """

    return sync_detailed(
        client=client,
        status=status,
        theme=theme,
        region=region,
        bbox=bbox,
        since=since,
        q=q,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: list[SituationStatus] | Unset = UNSET,
    theme: list[SituationTheme] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SituationPage]:
    """List situations

     Lists situations by latest activity (keyset on `lastActivityAt`, `id`). Private situations are
    returned only to callers allowed private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[SituationStatus] | Unset):
        theme (list[SituationTheme] | Unset):
        region (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SituationPage]
    """

    kwargs = _get_kwargs(
        status=status,
        theme=theme,
        region=region,
        bbox=bbox,
        since=since,
        q=q,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: list[SituationStatus] | Unset = UNSET,
    theme: list[SituationTheme] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SituationPage | None:
    """List situations

     Lists situations by latest activity (keyset on `lastActivityAt`, `id`). Private situations are
    returned only to callers allowed private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[SituationStatus] | Unset):
        theme (list[SituationTheme] | Unset):
        region (list[str] | Unset):
        bbox (str | Unset):
        since (datetime.datetime | Unset):
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SituationPage
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            theme=theme,
            region=region,
            bbox=bbox,
            since=since,
            q=q,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
