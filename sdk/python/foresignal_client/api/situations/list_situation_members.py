from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_situation_members_role import ListSituationMembersRole
from ...models.list_situation_members_type_item import ListSituationMembersTypeItem
from ...models.problem import Problem
from ...models.situation_member_page import SituationMemberPage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    situation_id: str,
    *,
    type_: list[ListSituationMembersTypeItem] | Unset = UNSET,
    role: ListSituationMembersRole | Unset = UNSET,
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

    json_role: str | Unset = UNSET
    if not isinstance(role, Unset):
        json_role = role.value

    params["role"] = json_role

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/situations/{situation_id}/members".format(
            situation_id=quote(str(situation_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SituationMemberPage | None:
    if response.status_code == 200:
        response_200 = SituationMemberPage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

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
) -> Response[Problem | SituationMemberPage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    type_: list[ListSituationMembersTypeItem] | Unset = UNSET,
    role: ListSituationMembersRole | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SituationMemberPage]:
    """List situation members

     Lists the pinned members with their join reasons and corroboration status, most recently observed
    first.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (list[ListSituationMembersTypeItem] | Unset):
        role (ListSituationMembersRole | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SituationMemberPage]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        type_=type_,
        role=role,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    type_: list[ListSituationMembersTypeItem] | Unset = UNSET,
    role: ListSituationMembersRole | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SituationMemberPage | None:
    """List situation members

     Lists the pinned members with their join reasons and corroboration status, most recently observed
    first.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (list[ListSituationMembersTypeItem] | Unset):
        role (ListSituationMembersRole | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SituationMemberPage
    """

    return sync_detailed(
        situation_id=situation_id,
        client=client,
        type_=type_,
        role=role,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    type_: list[ListSituationMembersTypeItem] | Unset = UNSET,
    role: ListSituationMembersRole | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | SituationMemberPage]:
    """List situation members

     Lists the pinned members with their join reasons and corroboration status, most recently observed
    first.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (list[ListSituationMembersTypeItem] | Unset):
        role (ListSituationMembersRole | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SituationMemberPage]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        type_=type_,
        role=role,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    type_: list[ListSituationMembersTypeItem] | Unset = UNSET,
    role: ListSituationMembersRole | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | SituationMemberPage | None:
    """List situation members

     Lists the pinned members with their join reasons and corroboration status, most recently observed
    first.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        type_ (list[ListSituationMembersTypeItem] | Unset):
        role (ListSituationMembersRole | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SituationMemberPage
    """

    return (
        await asyncio_detailed(
            situation_id=situation_id,
            client=client,
            type_=type_,
            role=role,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
