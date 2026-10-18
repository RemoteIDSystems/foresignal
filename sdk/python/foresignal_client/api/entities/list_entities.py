import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.entity_page import EntityPage
from ...models.list_entities_type import ListEntitiesType
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    q: str | Unset = UNSET,
    type_: ListEntitiesType | Unset = UNSET,
    key: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 100,
    after: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["q"] = q

    json_type_: str | Unset = UNSET
    if not isinstance(type_, Unset):
        json_type_ = type_.value

    params["type"] = json_type_

    params["key"] = key

    json_at: str | Unset = UNSET
    if not isinstance(at, Unset):
        json_at = at.isoformat()
    params["at"] = json_at

    params["limit"] = limit

    params["after"] = after

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/entities",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EntityPage | Problem | None:
    if response.status_code == 200:
        response_200 = EntityPage.from_dict(response.json())

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
) -> Response[EntityPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    type_: ListEntitiesType | Unset = UNSET,
    key: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 100,
    after: str | Unset = UNSET,
) -> Response[EntityPage | Problem]:
    """Search entities

     Prefix search over aliases and display names, or exact lookup by key. Deterministic keys only; no
    fuzzy matching.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str | Unset):
        type_ (ListEntitiesType | Unset):
        key (str | Unset):  Example: icao24:ae1234.
        at (datetime.datetime | Unset):
        limit (int | Unset):  Default: 100.
        after (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EntityPage | Problem]
    """

    kwargs = _get_kwargs(
        q=q,
        type_=type_,
        key=key,
        at=at,
        limit=limit,
        after=after,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    type_: ListEntitiesType | Unset = UNSET,
    key: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 100,
    after: str | Unset = UNSET,
) -> EntityPage | Problem | None:
    """Search entities

     Prefix search over aliases and display names, or exact lookup by key. Deterministic keys only; no
    fuzzy matching.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str | Unset):
        type_ (ListEntitiesType | Unset):
        key (str | Unset):  Example: icao24:ae1234.
        at (datetime.datetime | Unset):
        limit (int | Unset):  Default: 100.
        after (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EntityPage | Problem
    """

    return sync_detailed(
        client=client,
        q=q,
        type_=type_,
        key=key,
        at=at,
        limit=limit,
        after=after,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    type_: ListEntitiesType | Unset = UNSET,
    key: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 100,
    after: str | Unset = UNSET,
) -> Response[EntityPage | Problem]:
    """Search entities

     Prefix search over aliases and display names, or exact lookup by key. Deterministic keys only; no
    fuzzy matching.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str | Unset):
        type_ (ListEntitiesType | Unset):
        key (str | Unset):  Example: icao24:ae1234.
        at (datetime.datetime | Unset):
        limit (int | Unset):  Default: 100.
        after (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EntityPage | Problem]
    """

    kwargs = _get_kwargs(
        q=q,
        type_=type_,
        key=key,
        at=at,
        limit=limit,
        after=after,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    type_: ListEntitiesType | Unset = UNSET,
    key: str | Unset = UNSET,
    at: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 100,
    after: str | Unset = UNSET,
) -> EntityPage | Problem | None:
    """Search entities

     Prefix search over aliases and display names, or exact lookup by key. Deterministic keys only; no
    fuzzy matching.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str | Unset):
        type_ (ListEntitiesType | Unset):
        key (str | Unset):  Example: icao24:ae1234.
        at (datetime.datetime | Unset):
        limit (int | Unset):  Default: 100.
        after (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EntityPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            q=q,
            type_=type_,
            key=key,
            at=at,
            limit=limit,
            after=after,
        )
    ).parsed
