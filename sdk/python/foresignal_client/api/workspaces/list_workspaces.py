from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.workspace_page import WorkspacePage
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/workspaces",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | WorkspacePage | None:
    if response.status_code == 200:
        response_200 = WorkspacePage.from_dict(response.json())

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
) -> Response[Problem | WorkspacePage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | WorkspacePage]:
    """List the caller's workspaces

     Workspaces the caller owns or is a member of.

    **Scope:** `read` (token required).

    **v2.0 (per-user, R2-012):** a workspace's `owner` is the principal of the caller that created it,
    which for a signed-in user is the user's principal, so a user's workspaces are their own. A signed-
    in user reaches the workspace operations with `self.workspaces`; every operation except
    createWorkspace and shareWorkspace also accepts `data.read`, so a user who may not own workspaces
    can still open the ones shared with them. Who may read, edit, share and delete one workspace is
    unchanged: ownership, membership and access-list grants decide. When a user is deleted their
    workspaces move to the receiving admin (deleteUser). The shapes are unchanged.

    Args:
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WorkspacePage]
    """

    kwargs = _get_kwargs(
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
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | WorkspacePage | None:
    """List the caller's workspaces

     Workspaces the caller owns or is a member of.

    **Scope:** `read` (token required).

    **v2.0 (per-user, R2-012):** a workspace's `owner` is the principal of the caller that created it,
    which for a signed-in user is the user's principal, so a user's workspaces are their own. A signed-
    in user reaches the workspace operations with `self.workspaces`; every operation except
    createWorkspace and shareWorkspace also accepts `data.read`, so a user who may not own workspaces
    can still open the ones shared with them. Who may read, edit, share and delete one workspace is
    unchanged: ownership, membership and access-list grants decide. When a user is deleted their
    workspaces move to the receiving admin (deleteUser). The shapes are unchanged.

    Args:
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | WorkspacePage
    """

    return sync_detailed(
        client=client,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[Problem | WorkspacePage]:
    """List the caller's workspaces

     Workspaces the caller owns or is a member of.

    **Scope:** `read` (token required).

    **v2.0 (per-user, R2-012):** a workspace's `owner` is the principal of the caller that created it,
    which for a signed-in user is the user's principal, so a user's workspaces are their own. A signed-
    in user reaches the workspace operations with `self.workspaces`; every operation except
    createWorkspace and shareWorkspace also accepts `data.read`, so a user who may not own workspaces
    can still open the ones shared with them. Who may read, edit, share and delete one workspace is
    unchanged: ownership, membership and access-list grants decide. When a user is deleted their
    workspaces move to the receiving admin (deleteUser). The shapes are unchanged.

    Args:
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WorkspacePage]
    """

    kwargs = _get_kwargs(
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Problem | WorkspacePage | None:
    """List the caller's workspaces

     Workspaces the caller owns or is a member of.

    **Scope:** `read` (token required).

    **v2.0 (per-user, R2-012):** a workspace's `owner` is the principal of the caller that created it,
    which for a signed-in user is the user's principal, so a user's workspaces are their own. A signed-
    in user reaches the workspace operations with `self.workspaces`; every operation except
    createWorkspace and shareWorkspace also accepts `data.read`, so a user who may not own workspaces
    can still open the ones shared with them. Who may read, edit, share and delete one workspace is
    unchanged: ownership, membership and access-list grants decide. When a user is deleted their
    workspaces move to the receiving admin (deleteUser). The shapes are unchanged.

    Args:
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | WorkspacePage
    """

    return (
        await asyncio_detailed(
            client=client,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
