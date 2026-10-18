from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.attention_topic_page import AttentionTopicPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["enabled"] = enabled

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/attention/topics",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AttentionTopicPage | Problem | None:
    if response.status_code == 200:
        response_200 = AttentionTopicPage.from_dict(response.json())

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
) -> Response[AttentionTopicPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AttentionTopicPage | Problem]:
    """List attention topics

     Operator-defined topics that drive wikimedia-pageviews and gdelt-timelines.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        enabled (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AttentionTopicPage | Problem]
    """

    kwargs = _get_kwargs(
        enabled=enabled,
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
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AttentionTopicPage | Problem | None:
    """List attention topics

     Operator-defined topics that drive wikimedia-pageviews and gdelt-timelines.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        enabled (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AttentionTopicPage | Problem
    """

    return sync_detailed(
        client=client,
        enabled=enabled,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AttentionTopicPage | Problem]:
    """List attention topics

     Operator-defined topics that drive wikimedia-pageviews and gdelt-timelines.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        enabled (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AttentionTopicPage | Problem]
    """

    kwargs = _get_kwargs(
        enabled=enabled,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    enabled: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AttentionTopicPage | Problem | None:
    """List attention topics

     Operator-defined topics that drive wikimedia-pageviews and gdelt-timelines.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        enabled (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AttentionTopicPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            enabled=enabled,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
