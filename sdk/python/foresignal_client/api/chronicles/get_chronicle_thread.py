import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_thread_page import ChronicleThreadPage
from ...models.get_chronicle_thread_types_item import GetChronicleThreadTypesItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    chronicle_id: str,
    *,
    after: datetime.datetime | Unset = UNSET,
    types: list[GetChronicleThreadTypesItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_after: str | Unset = UNSET
    if not isinstance(after, Unset):
        json_after = after.isoformat()
    params["after"] = json_after

    json_types: list[str] | Unset = UNSET
    if not isinstance(types, Unset):
        json_types = []
        for types_item_data in types:
            types_item = types_item_data.value
            json_types.append(types_item)

    params["types"] = json_types

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicles/{chronicle_id}/thread".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChronicleThreadPage | Problem | None:
    if response.status_code == 200:
        response_200 = ChronicleThreadPage.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ChronicleThreadPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    after: datetime.datetime | Unset = UNSET,
    types: list[GetChronicleThreadTypesItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ChronicleThreadPage | Problem]:
    """A chronicle's thread

     Chronological feed of notable items (alerts, findings, situation changes, hypothesis band changes,
    forecast resolutions, first appearances, layer edits, capture gaps), each with its envelope, plus
    the caller's unread count. Live updates: SSE topic `chronicle`.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        after (datetime.datetime | Unset):
        types (list[GetChronicleThreadTypesItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleThreadPage | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        after=after,
        types=types,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    after: datetime.datetime | Unset = UNSET,
    types: list[GetChronicleThreadTypesItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ChronicleThreadPage | Problem | None:
    """A chronicle's thread

     Chronological feed of notable items (alerts, findings, situation changes, hypothesis band changes,
    forecast resolutions, first appearances, layer edits, capture gaps), each with its envelope, plus
    the caller's unread count. Live updates: SSE topic `chronicle`.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        after (datetime.datetime | Unset):
        types (list[GetChronicleThreadTypesItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleThreadPage | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
        after=after,
        types=types,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    after: datetime.datetime | Unset = UNSET,
    types: list[GetChronicleThreadTypesItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ChronicleThreadPage | Problem]:
    """A chronicle's thread

     Chronological feed of notable items (alerts, findings, situation changes, hypothesis band changes,
    forecast resolutions, first appearances, layer edits, capture gaps), each with its envelope, plus
    the caller's unread count. Live updates: SSE topic `chronicle`.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        after (datetime.datetime | Unset):
        types (list[GetChronicleThreadTypesItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleThreadPage | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        after=after,
        types=types,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    after: datetime.datetime | Unset = UNSET,
    types: list[GetChronicleThreadTypesItem] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ChronicleThreadPage | Problem | None:
    """A chronicle's thread

     Chronological feed of notable items (alerts, findings, situation changes, hypothesis band changes,
    forecast resolutions, first appearances, layer edits, capture gaps), each with its envelope, plus
    the caller's unread count. Live updates: SSE topic `chronicle`.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        after (datetime.datetime | Unset):
        types (list[GetChronicleThreadTypesItem] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleThreadPage | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
            after=after,
            types=types,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
