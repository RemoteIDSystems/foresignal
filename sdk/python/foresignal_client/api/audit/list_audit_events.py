import datetime
from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.audit_event_page import AuditEventPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    token_id: UUID | Unset = UNSET,
    operation_id: str | Unset = UNSET,
    status: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    json_until: str | Unset = UNSET
    if not isinstance(until, Unset):
        json_until = until.isoformat()
    params["until"] = json_until

    json_token_id: str | Unset = UNSET
    if not isinstance(token_id, Unset):
        json_token_id = str(token_id)
    params["tokenId"] = json_token_id

    params["operationId"] = operation_id

    params["status"] = status

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/audit",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AuditEventPage | Problem | None:
    if response.status_code == 200:
        response_200 = AuditEventPage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AuditEventPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    token_id: UUID | Unset = UNSET,
    operation_id: str | Unset = UNSET,
    status: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AuditEventPage | Problem]:
    """List audit events

     Newest first, keyset paging. Covers authenticated writes, 401/403 on write paths, pairing claims,
    token revocations, MCP tool calls, notification actions, private-data reads by non-admins, exports
    and purges. `detail` never contains secrets or request bodies. Retention key `audit` (default 90d).

    **Scope:** `admin`.

    Args:
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        token_id (UUID | Unset):
        operation_id (str | Unset):
        status (int | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AuditEventPage | Problem]
    """

    kwargs = _get_kwargs(
        since=since,
        until=until,
        token_id=token_id,
        operation_id=operation_id,
        status=status,
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
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    token_id: UUID | Unset = UNSET,
    operation_id: str | Unset = UNSET,
    status: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AuditEventPage | Problem | None:
    """List audit events

     Newest first, keyset paging. Covers authenticated writes, 401/403 on write paths, pairing claims,
    token revocations, MCP tool calls, notification actions, private-data reads by non-admins, exports
    and purges. `detail` never contains secrets or request bodies. Retention key `audit` (default 90d).

    **Scope:** `admin`.

    Args:
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        token_id (UUID | Unset):
        operation_id (str | Unset):
        status (int | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AuditEventPage | Problem
    """

    return sync_detailed(
        client=client,
        since=since,
        until=until,
        token_id=token_id,
        operation_id=operation_id,
        status=status,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    token_id: UUID | Unset = UNSET,
    operation_id: str | Unset = UNSET,
    status: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AuditEventPage | Problem]:
    """List audit events

     Newest first, keyset paging. Covers authenticated writes, 401/403 on write paths, pairing claims,
    token revocations, MCP tool calls, notification actions, private-data reads by non-admins, exports
    and purges. `detail` never contains secrets or request bodies. Retention key `audit` (default 90d).

    **Scope:** `admin`.

    Args:
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        token_id (UUID | Unset):
        operation_id (str | Unset):
        status (int | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AuditEventPage | Problem]
    """

    kwargs = _get_kwargs(
        since=since,
        until=until,
        token_id=token_id,
        operation_id=operation_id,
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    until: datetime.datetime | Unset = UNSET,
    token_id: UUID | Unset = UNSET,
    operation_id: str | Unset = UNSET,
    status: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AuditEventPage | Problem | None:
    """List audit events

     Newest first, keyset paging. Covers authenticated writes, 401/403 on write paths, pairing claims,
    token revocations, MCP tool calls, notification actions, private-data reads by non-admins, exports
    and purges. `detail` never contains secrets or request bodies. Retention key `audit` (default 90d).

    **Scope:** `admin`.

    Args:
        since (datetime.datetime | Unset):
        until (datetime.datetime | Unset):
        token_id (UUID | Unset):
        operation_id (str | Unset):
        status (int | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AuditEventPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            since=since,
            until=until,
            token_id=token_id,
            operation_id=operation_id,
            status=status,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
