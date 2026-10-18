import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.entity_record_list import EntityRecordList
from ...models.list_entity_records_format import ListEntityRecordsFormat
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    entity_id: str,
    *,
    at: datetime.datetime | Unset = UNSET,
    include_closed: bool | Unset = False,
    format_: ListEntityRecordsFormat | Unset = ListEntityRecordsFormat.JSON,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_at: str | Unset = UNSET
    if not isinstance(at, Unset):
        json_at = at.isoformat()
    params["at"] = json_at

    params["includeClosed"] = include_closed

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/entities/{entity_id}/records".format(
            entity_id=quote(str(entity_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EntityRecordList | Problem | None:
    if response.status_code == 200:
        response_200 = EntityRecordList.from_dict(response.json())

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
) -> Response[EntityRecordList | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    entity_id: str,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime | Unset = UNSET,
    include_closed: bool | Unset = False,
    format_: ListEntityRecordsFormat | Unset = ListEntityRecordsFormat.JSON,
) -> Response[EntityRecordList | Problem]:
    """Current records linked to an entity

     Current records across sources (head ⋈ live), each with its link. Links from private sources are
    visible only to admin tokens.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        at (datetime.datetime | Unset):
        include_closed (bool | Unset):  Default: False.
        format_ (ListEntityRecordsFormat | Unset):  Default: ListEntityRecordsFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EntityRecordList | Problem]
    """

    kwargs = _get_kwargs(
        entity_id=entity_id,
        at=at,
        include_closed=include_closed,
        format_=format_,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    entity_id: str,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime | Unset = UNSET,
    include_closed: bool | Unset = False,
    format_: ListEntityRecordsFormat | Unset = ListEntityRecordsFormat.JSON,
) -> EntityRecordList | Problem | None:
    """Current records linked to an entity

     Current records across sources (head ⋈ live), each with its link. Links from private sources are
    visible only to admin tokens.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        at (datetime.datetime | Unset):
        include_closed (bool | Unset):  Default: False.
        format_ (ListEntityRecordsFormat | Unset):  Default: ListEntityRecordsFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EntityRecordList | Problem
    """

    return sync_detailed(
        entity_id=entity_id,
        client=client,
        at=at,
        include_closed=include_closed,
        format_=format_,
    ).parsed


async def asyncio_detailed(
    entity_id: str,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime | Unset = UNSET,
    include_closed: bool | Unset = False,
    format_: ListEntityRecordsFormat | Unset = ListEntityRecordsFormat.JSON,
) -> Response[EntityRecordList | Problem]:
    """Current records linked to an entity

     Current records across sources (head ⋈ live), each with its link. Links from private sources are
    visible only to admin tokens.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        at (datetime.datetime | Unset):
        include_closed (bool | Unset):  Default: False.
        format_ (ListEntityRecordsFormat | Unset):  Default: ListEntityRecordsFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EntityRecordList | Problem]
    """

    kwargs = _get_kwargs(
        entity_id=entity_id,
        at=at,
        include_closed=include_closed,
        format_=format_,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    entity_id: str,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime | Unset = UNSET,
    include_closed: bool | Unset = False,
    format_: ListEntityRecordsFormat | Unset = ListEntityRecordsFormat.JSON,
) -> EntityRecordList | Problem | None:
    """Current records linked to an entity

     Current records across sources (head ⋈ live), each with its link. Links from private sources are
    visible only to admin tokens.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        at (datetime.datetime | Unset):
        include_closed (bool | Unset):  Default: False.
        format_ (ListEntityRecordsFormat | Unset):  Default: ListEntityRecordsFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EntityRecordList | Problem
    """

    return (
        await asyncio_detailed(
            entity_id=entity_id,
            client=client,
            at=at,
            include_closed=include_closed,
            format_=format_,
        )
    ).parsed
