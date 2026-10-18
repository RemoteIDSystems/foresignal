from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.investigation_page import InvestigationPage
from ...models.list_investigations_scope import ListInvestigationsScope
from ...models.list_investigations_status import ListInvestigationsStatus
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: ListInvestigationsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    scope: ListInvestigationsScope | Unset = ListInvestigationsScope.ALL,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    params["cursor"] = cursor

    params["limit"] = limit

    json_scope: str | Unset = UNSET
    if not isinstance(scope, Unset):
        json_scope = scope.value

    params["scope"] = json_scope

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/investigations",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> InvestigationPage | Problem | None:
    if response.status_code == 200:
        response_200 = InvestigationPage.from_dict(response.json())

        return response_200

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
) -> Response[InvestigationPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: ListInvestigationsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    scope: ListInvestigationsScope | Unset = ListInvestigationsScope.ALL,
) -> Response[InvestigationPage | Problem]:
    """List investigations

     Investigations visible to the caller (130 adds `scope=mine|shared|all` and access lists).

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        status (ListInvestigationsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        scope (ListInvestigationsScope | Unset):  Default: ListInvestigationsScope.ALL.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InvestigationPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        cursor=cursor,
        limit=limit,
        scope=scope,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    status: ListInvestigationsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    scope: ListInvestigationsScope | Unset = ListInvestigationsScope.ALL,
) -> InvestigationPage | Problem | None:
    """List investigations

     Investigations visible to the caller (130 adds `scope=mine|shared|all` and access lists).

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        status (ListInvestigationsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        scope (ListInvestigationsScope | Unset):  Default: ListInvestigationsScope.ALL.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InvestigationPage | Problem
    """

    return sync_detailed(
        client=client,
        status=status,
        cursor=cursor,
        limit=limit,
        scope=scope,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: ListInvestigationsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    scope: ListInvestigationsScope | Unset = ListInvestigationsScope.ALL,
) -> Response[InvestigationPage | Problem]:
    """List investigations

     Investigations visible to the caller (130 adds `scope=mine|shared|all` and access lists).

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        status (ListInvestigationsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        scope (ListInvestigationsScope | Unset):  Default: ListInvestigationsScope.ALL.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InvestigationPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        cursor=cursor,
        limit=limit,
        scope=scope,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: ListInvestigationsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
    scope: ListInvestigationsScope | Unset = ListInvestigationsScope.ALL,
) -> InvestigationPage | Problem | None:
    """List investigations

     Investigations visible to the caller (130 adds `scope=mine|shared|all` and access lists).

    **Scope:** `read` (token required; not covered by `security.openReads`).

    Args:
        status (ListInvestigationsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.
        scope (ListInvestigationsScope | Unset):  Default: ListInvestigationsScope.ALL.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InvestigationPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            cursor=cursor,
            limit=limit,
            scope=scope,
        )
    ).parsed
