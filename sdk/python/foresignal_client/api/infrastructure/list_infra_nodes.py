from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.infra_node_page import InfraNodePage
from ...models.list_infra_nodes_format import ListInfraNodesFormat
from ...models.list_infra_nodes_type_item import ListInfraNodesTypeItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    bbox: str | Unset = UNSET,
    type_: list[ListInfraNodesTypeItem] | Unset = UNSET,
    q: str | Unset = UNSET,
    format_: ListInfraNodesFormat | Unset = ListInfraNodesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["bbox"] = bbox

    json_type_: list[str] | Unset = UNSET
    if not isinstance(type_, Unset):
        json_type_ = []
        for type_item_data in type_:
            type_item = type_item_data.value
            json_type_.append(type_item)

    params["type"] = json_type_

    params["q"] = q

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/infra/nodes",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> InfraNodePage | Problem | None:
    if response.status_code == 200:
        response_200 = InfraNodePage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[InfraNodePage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    type_: list[ListInfraNodesTypeItem] | Unset = UNSET,
    q: str | Unset = UNSET,
    format_: ListInfraNodesFormat | Unset = ListInfraNodesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[InfraNodePage | Problem]:
    """List infrastructure nodes

     Facility nodes by area, type and name. Responses with OSM-derived data carry "© OpenStreetMap
    contributors".

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        type_ (list[ListInfraNodesTypeItem] | Unset):
        q (str | Unset):
        format_ (ListInfraNodesFormat | Unset):  Default: ListInfraNodesFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraNodePage | Problem]
    """

    kwargs = _get_kwargs(
        bbox=bbox,
        type_=type_,
        q=q,
        format_=format_,
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
    bbox: str | Unset = UNSET,
    type_: list[ListInfraNodesTypeItem] | Unset = UNSET,
    q: str | Unset = UNSET,
    format_: ListInfraNodesFormat | Unset = ListInfraNodesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> InfraNodePage | Problem | None:
    """List infrastructure nodes

     Facility nodes by area, type and name. Responses with OSM-derived data carry "© OpenStreetMap
    contributors".

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        type_ (list[ListInfraNodesTypeItem] | Unset):
        q (str | Unset):
        format_ (ListInfraNodesFormat | Unset):  Default: ListInfraNodesFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraNodePage | Problem
    """

    return sync_detailed(
        client=client,
        bbox=bbox,
        type_=type_,
        q=q,
        format_=format_,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    type_: list[ListInfraNodesTypeItem] | Unset = UNSET,
    q: str | Unset = UNSET,
    format_: ListInfraNodesFormat | Unset = ListInfraNodesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[InfraNodePage | Problem]:
    """List infrastructure nodes

     Facility nodes by area, type and name. Responses with OSM-derived data carry "© OpenStreetMap
    contributors".

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        type_ (list[ListInfraNodesTypeItem] | Unset):
        q (str | Unset):
        format_ (ListInfraNodesFormat | Unset):  Default: ListInfraNodesFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraNodePage | Problem]
    """

    kwargs = _get_kwargs(
        bbox=bbox,
        type_=type_,
        q=q,
        format_=format_,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    bbox: str | Unset = UNSET,
    type_: list[ListInfraNodesTypeItem] | Unset = UNSET,
    q: str | Unset = UNSET,
    format_: ListInfraNodesFormat | Unset = ListInfraNodesFormat.JSON,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> InfraNodePage | Problem | None:
    """List infrastructure nodes

     Facility nodes by area, type and name. Responses with OSM-derived data carry "© OpenStreetMap
    contributors".

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        bbox (str | Unset):
        type_ (list[ListInfraNodesTypeItem] | Unset):
        q (str | Unset):
        format_ (ListInfraNodesFormat | Unset):  Default: ListInfraNodesFormat.JSON.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraNodePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            bbox=bbox,
            type_=type_,
            q=q,
            format_=format_,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
