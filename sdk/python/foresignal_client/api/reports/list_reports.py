from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.list_reports_scope import ListReportsScope
from ...models.list_reports_status import ListReportsStatus
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: ListReportsStatus | Unset = UNSET,
    scope: ListReportsScope | Unset = ListReportsScope.ALL,
    source_ref: str | Unset = UNSET,
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

    params["sourceRef"] = source_ref

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/reports",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: ListReportsStatus | Unset = UNSET,
    scope: ListReportsScope | Unset = ListReportsScope.ALL,
    source_ref: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem]:
    """List reports

     Reports the caller owns, authors or that are shared with them.

    **Scope:** `read`.

    Args:
        status (ListReportsStatus | Unset):
        scope (ListReportsScope | Unset):  Default: ListReportsScope.ALL.
        source_ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-
            Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the
            external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`:
            read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        scope=scope,
        source_ref=source_ref,
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
    status: ListReportsStatus | Unset = UNSET,
    scope: ListReportsScope | Unset = ListReportsScope.ALL,
    source_ref: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | None:
    """List reports

     Reports the caller owns, authors or that are shared with them.

    **Scope:** `read`.

    Args:
        status (ListReportsStatus | Unset):
        scope (ListReportsScope | Unset):  Default: ListReportsScope.ALL.
        source_ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-
            Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the
            external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`:
            read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        status=status,
        scope=scope,
        source_ref=source_ref,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: ListReportsStatus | Unset = UNSET,
    scope: ListReportsScope | Unset = ListReportsScope.ALL,
    source_ref: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem]:
    """List reports

     Reports the caller owns, authors or that are shared with them.

    **Scope:** `read`.

    Args:
        status (ListReportsStatus | Unset):
        scope (ListReportsScope | Unset):  Default: ListReportsScope.ALL.
        source_ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-
            Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the
            external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`:
            read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        scope=scope,
        source_ref=source_ref,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: ListReportsStatus | Unset = UNSET,
    scope: ListReportsScope | Unset = ListReportsScope.ALL,
    source_ref: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | None:
    """List reports

     Reports the caller owns, authors or that are shared with them.

    **Scope:** `read`.

    Args:
        status (ListReportsStatus | Unset):
        scope (ListReportsScope | Unset):  Default: ListReportsScope.ALL.
        source_ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-
            Contract.md §2). Record refs are `record:{sourceId}/{externalId}[@{seq}]` with the
            external id percent-encoded. Remote refs (132) are `remote:{instanceId}/{type}:{id}`:
            read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            scope=scope,
            source_ref=source_ref,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
