from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_received_packages_type import ListReceivedPackagesType
from ...models.problem import Problem
from ...models.received_package_page import ReceivedPackagePage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    type_: ListReceivedPackagesType | Unset = UNSET,
    sender_instance_id: str | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_type_: str | Unset = UNSET
    if not isinstance(type_, Unset):
        json_type_ = type_.value

    params["type"] = json_type_

    params["senderInstanceId"] = sender_instance_id

    params["acknowledged"] = acknowledged

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/exchange/received",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ReceivedPackagePage | None:
    if response.status_code == 200:
        response_200 = ReceivedPackagePage.from_dict(response.json())

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
) -> Response[Problem | ReceivedPackagePage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    type_: ListReceivedPackagesType | Unset = UNSET,
    sender_instance_id: str | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | ReceivedPackagePage]:
    """List received packages

     Packages addressed to the caller (per ACL and TLP).

    **Scope:** `read`.

    Args:
        type_ (ListReceivedPackagesType | Unset):
        sender_instance_id (str | Unset):
        acknowledged (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReceivedPackagePage]
    """

    kwargs = _get_kwargs(
        type_=type_,
        sender_instance_id=sender_instance_id,
        acknowledged=acknowledged,
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
    type_: ListReceivedPackagesType | Unset = UNSET,
    sender_instance_id: str | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | ReceivedPackagePage | None:
    """List received packages

     Packages addressed to the caller (per ACL and TLP).

    **Scope:** `read`.

    Args:
        type_ (ListReceivedPackagesType | Unset):
        sender_instance_id (str | Unset):
        acknowledged (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReceivedPackagePage
    """

    return sync_detailed(
        client=client,
        type_=type_,
        sender_instance_id=sender_instance_id,
        acknowledged=acknowledged,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    type_: ListReceivedPackagesType | Unset = UNSET,
    sender_instance_id: str | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | ReceivedPackagePage]:
    """List received packages

     Packages addressed to the caller (per ACL and TLP).

    **Scope:** `read`.

    Args:
        type_ (ListReceivedPackagesType | Unset):
        sender_instance_id (str | Unset):
        acknowledged (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReceivedPackagePage]
    """

    kwargs = _get_kwargs(
        type_=type_,
        sender_instance_id=sender_instance_id,
        acknowledged=acknowledged,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    type_: ListReceivedPackagesType | Unset = UNSET,
    sender_instance_id: str | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | ReceivedPackagePage | None:
    """List received packages

     Packages addressed to the caller (per ACL and TLP).

    **Scope:** `read`.

    Args:
        type_ (ListReceivedPackagesType | Unset):
        sender_instance_id (str | Unset):
        acknowledged (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReceivedPackagePage
    """

    return (
        await asyncio_detailed(
            client=client,
            type_=type_,
            sender_instance_id=sender_instance_id,
            acknowledged=acknowledged,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
