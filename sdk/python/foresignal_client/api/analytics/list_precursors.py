from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_precursors_kind import ListPrecursorsKind
from ...models.list_precursors_status_item import ListPrecursorsStatusItem
from ...models.precursor_page import PrecursorPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: list[ListPrecursorsStatusItem] | Unset = UNSET,
    kind: ListPrecursorsKind | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    family: list[str] | Unset = UNSET,
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

    json_kind: str | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind.value

    params["kind"] = json_kind

    json_region: list[str] | Unset = UNSET
    if not isinstance(region, Unset):
        json_region = region

    params["region"] = json_region

    json_family: list[str] | Unset = UNSET
    if not isinstance(family, Unset):
        json_family = family

    params["family"] = json_family

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/precursors",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PrecursorPage | Problem | None:
    if response.status_code == 200:
        response_200 = PrecursorPage.from_dict(response.json())

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
) -> Response[PrecursorPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: list[ListPrecursorsStatusItem] | Unset = UNSET,
    kind: ListPrecursorsKind | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    family: list[str] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[PrecursorPage | Problem]:
    """List precursors

     Lists the precursor library. Every entry states association in stored data, its batch size, both
    windows' statistics and the reverse test.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListPrecursorsStatusItem] | Unset):
        kind (ListPrecursorsKind | Unset):
        region (list[str] | Unset):
        family (list[str] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PrecursorPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        kind=kind,
        region=region,
        family=family,
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
    status: list[ListPrecursorsStatusItem] | Unset = UNSET,
    kind: ListPrecursorsKind | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    family: list[str] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> PrecursorPage | Problem | None:
    """List precursors

     Lists the precursor library. Every entry states association in stored data, its batch size, both
    windows' statistics and the reverse test.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListPrecursorsStatusItem] | Unset):
        kind (ListPrecursorsKind | Unset):
        region (list[str] | Unset):
        family (list[str] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PrecursorPage | Problem
    """

    return sync_detailed(
        client=client,
        status=status,
        kind=kind,
        region=region,
        family=family,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: list[ListPrecursorsStatusItem] | Unset = UNSET,
    kind: ListPrecursorsKind | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    family: list[str] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[PrecursorPage | Problem]:
    """List precursors

     Lists the precursor library. Every entry states association in stored data, its batch size, both
    windows' statistics and the reverse test.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListPrecursorsStatusItem] | Unset):
        kind (ListPrecursorsKind | Unset):
        region (list[str] | Unset):
        family (list[str] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PrecursorPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        kind=kind,
        region=region,
        family=family,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: list[ListPrecursorsStatusItem] | Unset = UNSET,
    kind: ListPrecursorsKind | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    family: list[str] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> PrecursorPage | Problem | None:
    """List precursors

     Lists the precursor library. Every entry states association in stored data, its batch size, both
    windows' statistics and the reverse test.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        status (list[ListPrecursorsStatusItem] | Unset):
        kind (ListPrecursorsKind | Unset):
        region (list[str] | Unset):
        family (list[str] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PrecursorPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            kind=kind,
            region=region,
            family=family,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
