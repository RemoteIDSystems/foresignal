from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_page import ChroniclePage
from ...models.list_chronicles_scope import ListChroniclesScope
from ...models.list_chronicles_status import ListChroniclesStatus
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: ListChroniclesStatus | Unset = UNSET,
    scope: ListChroniclesScope | Unset = ListChroniclesScope.ALL,
    situation_id: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    json_scope: str | Unset = UNSET
    if not isinstance(scope, Unset):
        json_scope = scope.value

    params["scope"] = json_scope

    params["situationId"] = situation_id

    params["q"] = q

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicles",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChroniclePage | Problem | None:
    if response.status_code == 200:
        response_200 = ChroniclePage.from_dict(response.json())

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
) -> Response[ChroniclePage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: ListChroniclesStatus | Unset = UNSET,
    scope: ListChroniclesScope | Unset = ListChroniclesScope.ALL,
    situation_id: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ChroniclePage | Problem]:
    """List chronicles

     Chronicles the caller owns or that are shared with them (access lists and TLP, 130).

    **Scope:** `read`.

    Args:
        status (ListChroniclesStatus | Unset):
        scope (ListChroniclesScope | Unset):  Default: ListChroniclesScope.ALL.
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChroniclePage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        scope=scope,
        situation_id=situation_id,
        q=q,
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
    status: ListChroniclesStatus | Unset = UNSET,
    scope: ListChroniclesScope | Unset = ListChroniclesScope.ALL,
    situation_id: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ChroniclePage | Problem | None:
    """List chronicles

     Chronicles the caller owns or that are shared with them (access lists and TLP, 130).

    **Scope:** `read`.

    Args:
        status (ListChroniclesStatus | Unset):
        scope (ListChroniclesScope | Unset):  Default: ListChroniclesScope.ALL.
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChroniclePage | Problem
    """

    return sync_detailed(
        client=client,
        status=status,
        scope=scope,
        situation_id=situation_id,
        q=q,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: ListChroniclesStatus | Unset = UNSET,
    scope: ListChroniclesScope | Unset = ListChroniclesScope.ALL,
    situation_id: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ChroniclePage | Problem]:
    """List chronicles

     Chronicles the caller owns or that are shared with them (access lists and TLP, 130).

    **Scope:** `read`.

    Args:
        status (ListChroniclesStatus | Unset):
        scope (ListChroniclesScope | Unset):  Default: ListChroniclesScope.ALL.
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChroniclePage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        scope=scope,
        situation_id=situation_id,
        q=q,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: ListChroniclesStatus | Unset = UNSET,
    scope: ListChroniclesScope | Unset = ListChroniclesScope.ALL,
    situation_id: str | Unset = UNSET,
    q: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ChroniclePage | Problem | None:
    """List chronicles

     Chronicles the caller owns or that are shared with them (access lists and TLP, 130).

    **Scope:** `read`.

    Args:
        status (ListChroniclesStatus | Unset):
        scope (ListChroniclesScope | Unset):  Default: ListChroniclesScope.ALL.
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        q (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChroniclePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            scope=scope,
            situation_id=situation_id,
            q=q,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
