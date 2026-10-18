from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecaster_page import ForecasterPage
from ...models.list_forecasters_type_item import ListForecastersTypeItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    type_: list[ListForecastersTypeItem] | Unset = UNSET,
    active: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_type_: list[str] | Unset = UNSET
    if not isinstance(type_, Unset):
        json_type_ = []
        for type_item_data in type_:
            type_item = type_item_data.value
            json_type_.append(type_item)

    params["type"] = json_type_

    params["active"] = active

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/forecasts/forecasters",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecasterPage | Problem | None:
    if response.status_code == 200:
        response_200 = ForecasterPage.from_dict(response.json())

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
) -> Response[ForecasterPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    type_: list[ListForecastersTypeItem] | Unset = UNSET,
    active: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ForecasterPage | Problem]:
    """List forecasters

     Lists registered forecasters with their type, version and headline track record.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListForecastersTypeItem] | Unset):
        active (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecasterPage | Problem]
    """

    kwargs = _get_kwargs(
        type_=type_,
        active=active,
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
    type_: list[ListForecastersTypeItem] | Unset = UNSET,
    active: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ForecasterPage | Problem | None:
    """List forecasters

     Lists registered forecasters with their type, version and headline track record.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListForecastersTypeItem] | Unset):
        active (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecasterPage | Problem
    """

    return sync_detailed(
        client=client,
        type_=type_,
        active=active,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    type_: list[ListForecastersTypeItem] | Unset = UNSET,
    active: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ForecasterPage | Problem]:
    """List forecasters

     Lists registered forecasters with their type, version and headline track record.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListForecastersTypeItem] | Unset):
        active (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecasterPage | Problem]
    """

    kwargs = _get_kwargs(
        type_=type_,
        active=active,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    type_: list[ListForecastersTypeItem] | Unset = UNSET,
    active: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ForecasterPage | Problem | None:
    """List forecasters

     Lists registered forecasters with their type, version and headline track record.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListForecastersTypeItem] | Unset):
        active (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecasterPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            type_=type_,
            active=active,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
