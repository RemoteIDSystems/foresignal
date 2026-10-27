from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.context_summary import ContextSummary
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    workspace_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/workspaces/{workspace_id}/summary".format(
            workspace_id=quote(str(workspace_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ContextSummary | Problem | None:
    if response.status_code == 200:
        response_200 = ContextSummary.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ContextSummary | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ContextSummary | Problem]:
    """Summarise a workspace's current context

     The context summary for the workspace's current viewport, time range, filters and layers.

    **Scope:** `read` (token required).

    **v2.0 (per-user, R2-012):** a workspace's `owner` is the principal of the caller that created it,
    which for a signed-in user is the user's principal, so a user's workspaces are their own. A signed-
    in user reaches the workspace operations with `self.workspaces`; every operation except
    createWorkspace and shareWorkspace also accepts `data.read`, so a user who may not own workspaces
    can still open the ones shared with them. Who may read, edit, share and delete one workspace is
    unchanged: ownership, membership and access-list grants decide. When a user is deleted their
    workspaces move to the receiving admin (deleteUser). The shapes are unchanged.

    Args:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ContextSummary | Problem]
    """

    kwargs = _get_kwargs(
        workspace_id=workspace_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
) -> ContextSummary | Problem | None:
    """Summarise a workspace's current context

     The context summary for the workspace's current viewport, time range, filters and layers.

    **Scope:** `read` (token required).

    **v2.0 (per-user, R2-012):** a workspace's `owner` is the principal of the caller that created it,
    which for a signed-in user is the user's principal, so a user's workspaces are their own. A signed-
    in user reaches the workspace operations with `self.workspaces`; every operation except
    createWorkspace and shareWorkspace also accepts `data.read`, so a user who may not own workspaces
    can still open the ones shared with them. Who may read, edit, share and delete one workspace is
    unchanged: ownership, membership and access-list grants decide. When a user is deleted their
    workspaces move to the receiving admin (deleteUser). The shapes are unchanged.

    Args:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ContextSummary | Problem
    """

    return sync_detailed(
        workspace_id=workspace_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ContextSummary | Problem]:
    """Summarise a workspace's current context

     The context summary for the workspace's current viewport, time range, filters and layers.

    **Scope:** `read` (token required).

    **v2.0 (per-user, R2-012):** a workspace's `owner` is the principal of the caller that created it,
    which for a signed-in user is the user's principal, so a user's workspaces are their own. A signed-
    in user reaches the workspace operations with `self.workspaces`; every operation except
    createWorkspace and shareWorkspace also accepts `data.read`, so a user who may not own workspaces
    can still open the ones shared with them. Who may read, edit, share and delete one workspace is
    unchanged: ownership, membership and access-list grants decide. When a user is deleted their
    workspaces move to the receiving admin (deleteUser). The shapes are unchanged.

    Args:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ContextSummary | Problem]
    """

    kwargs = _get_kwargs(
        workspace_id=workspace_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
) -> ContextSummary | Problem | None:
    """Summarise a workspace's current context

     The context summary for the workspace's current viewport, time range, filters and layers.

    **Scope:** `read` (token required).

    **v2.0 (per-user, R2-012):** a workspace's `owner` is the principal of the caller that created it,
    which for a signed-in user is the user's principal, so a user's workspaces are their own. A signed-
    in user reaches the workspace operations with `self.workspaces`; every operation except
    createWorkspace and shareWorkspace also accepts `data.read`, so a user who may not own workspaces
    can still open the ones shared with them. Who may read, edit, share and delete one workspace is
    unchanged: ownership, membership and access-list grants decide. When a user is deleted their
    workspaces move to the receiving admin (deleteUser). The shapes are unchanged.

    Args:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ContextSummary | Problem
    """

    return (
        await asyncio_detailed(
            workspace_id=workspace_id,
            client=client,
        )
    ).parsed
