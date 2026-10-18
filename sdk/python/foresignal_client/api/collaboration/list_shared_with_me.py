from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.shareable_type import ShareableType
from ...models.shared_item_page import SharedItemPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    type_: ShareableType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_type_: str | Unset = UNSET
    if not isinstance(type_, Unset):
        json_type_ = type_.value

    params["type"] = json_type_

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/shared-with-me",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SharedItemPage | None:
    if response.status_code == 200:
        response_200 = SharedItemPage.from_dict(response.json())

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
) -> Response[Problem | SharedItemPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    type_: ShareableType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SharedItemPage]:
    """Objects shared with me

     Work products shared with the caller directly, through a group or with the instance, newest share
    first.

    **Scope:** `read`.

    Args:
        type_ (ShareableType | Unset): Object types that carry an owner, visibility, access list
            and TLP (Client-Contract §10)
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SharedItemPage]
    """

    kwargs = _get_kwargs(
        type_=type_,
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
    type_: ShareableType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SharedItemPage | None:
    """Objects shared with me

     Work products shared with the caller directly, through a group or with the instance, newest share
    first.

    **Scope:** `read`.

    Args:
        type_ (ShareableType | Unset): Object types that carry an owner, visibility, access list
            and TLP (Client-Contract §10)
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SharedItemPage
    """

    return sync_detailed(
        client=client,
        type_=type_,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    type_: ShareableType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SharedItemPage]:
    """Objects shared with me

     Work products shared with the caller directly, through a group or with the instance, newest share
    first.

    **Scope:** `read`.

    Args:
        type_ (ShareableType | Unset): Object types that carry an owner, visibility, access list
            and TLP (Client-Contract §10)
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SharedItemPage]
    """

    kwargs = _get_kwargs(
        type_=type_,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    type_: ShareableType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SharedItemPage | None:
    """Objects shared with me

     Work products shared with the caller directly, through a group or with the instance, newest share
    first.

    **Scope:** `read`.

    Args:
        type_ (ShareableType | Unset): Object types that carry an owner, visibility, access list
            and TLP (Client-Contract §10)
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SharedItemPage
    """

    return (
        await asyncio_detailed(
            client=client,
            type_=type_,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
