from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.patch_workspace_context_body import PatchWorkspaceContextBody
from ...models.problem import Problem
from ...models.workspace_context_state import WorkspaceContextState
from ...types import UNSET, Response, Unset


def _get_kwargs(
    workspace_id: str,
    *,
    body: PatchWorkspaceContextBody,
    if_match: str,
    foresignal_client_id: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    headers["If-Match"] = if_match

    if not isinstance(foresignal_client_id, Unset):
        headers["Foresignal-Client-Id"] = foresignal_client_id

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/workspaces/{workspace_id}/context".format(
            workspace_id=quote(str(workspace_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | WorkspaceContextState | None:
    if response.status_code == 200:
        response_200 = WorkspaceContextState.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 412:
        response_412 = Problem.from_dict(response.json())

        return response_412

    if response.status_code == 415:
        response_415 = Problem.from_dict(response.json())

        return response_415

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | WorkspaceContextState]:
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
    body: PatchWorkspaceContextBody,
    if_match: str,
    foresignal_client_id: str | Unset = UNSET,
) -> Response[Problem | WorkspaceContextState]:
    """Patch a workspace's shared context

     JSON merge patch with `If-Match: <contextVersion>`; 412 `WORKSPACE_CONTEXT_STALE` on a stale
    version. The new version is broadcast as `workspace.context` with the sender's `originClientId`.
    Validated per Client-Contract §5 (well-formed refs, ordered time range, ≤ 200 selected refs). The
    bytes it adds count against the `maxStorageBytes` quota of the workspace owner (409 QUOTA_EXCEEDED,
    R2-026).

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
        if_match (str):
        foresignal_client_id (str | Unset):
        body (PatchWorkspaceContextBody): JSON merge patch of `WorkspaceContext`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WorkspaceContextState]
    """

    kwargs = _get_kwargs(
        workspace_id=workspace_id,
        body=body,
        if_match=if_match,
        foresignal_client_id=foresignal_client_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
    body: PatchWorkspaceContextBody,
    if_match: str,
    foresignal_client_id: str | Unset = UNSET,
) -> Problem | WorkspaceContextState | None:
    """Patch a workspace's shared context

     JSON merge patch with `If-Match: <contextVersion>`; 412 `WORKSPACE_CONTEXT_STALE` on a stale
    version. The new version is broadcast as `workspace.context` with the sender's `originClientId`.
    Validated per Client-Contract §5 (well-formed refs, ordered time range, ≤ 200 selected refs). The
    bytes it adds count against the `maxStorageBytes` quota of the workspace owner (409 QUOTA_EXCEEDED,
    R2-026).

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
        if_match (str):
        foresignal_client_id (str | Unset):
        body (PatchWorkspaceContextBody): JSON merge patch of `WorkspaceContext`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | WorkspaceContextState
    """

    return sync_detailed(
        workspace_id=workspace_id,
        client=client,
        body=body,
        if_match=if_match,
        foresignal_client_id=foresignal_client_id,
    ).parsed


async def asyncio_detailed(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
    body: PatchWorkspaceContextBody,
    if_match: str,
    foresignal_client_id: str | Unset = UNSET,
) -> Response[Problem | WorkspaceContextState]:
    """Patch a workspace's shared context

     JSON merge patch with `If-Match: <contextVersion>`; 412 `WORKSPACE_CONTEXT_STALE` on a stale
    version. The new version is broadcast as `workspace.context` with the sender's `originClientId`.
    Validated per Client-Contract §5 (well-formed refs, ordered time range, ≤ 200 selected refs). The
    bytes it adds count against the `maxStorageBytes` quota of the workspace owner (409 QUOTA_EXCEEDED,
    R2-026).

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
        if_match (str):
        foresignal_client_id (str | Unset):
        body (PatchWorkspaceContextBody): JSON merge patch of `WorkspaceContext`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | WorkspaceContextState]
    """

    kwargs = _get_kwargs(
        workspace_id=workspace_id,
        body=body,
        if_match=if_match,
        foresignal_client_id=foresignal_client_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
    body: PatchWorkspaceContextBody,
    if_match: str,
    foresignal_client_id: str | Unset = UNSET,
) -> Problem | WorkspaceContextState | None:
    """Patch a workspace's shared context

     JSON merge patch with `If-Match: <contextVersion>`; 412 `WORKSPACE_CONTEXT_STALE` on a stale
    version. The new version is broadcast as `workspace.context` with the sender's `originClientId`.
    Validated per Client-Contract §5 (well-formed refs, ordered time range, ≤ 200 selected refs). The
    bytes it adds count against the `maxStorageBytes` quota of the workspace owner (409 QUOTA_EXCEEDED,
    R2-026).

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
        if_match (str):
        foresignal_client_id (str | Unset):
        body (PatchWorkspaceContextBody): JSON merge patch of `WorkspaceContext`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | WorkspaceContextState
    """

    return (
        await asyncio_detailed(
            workspace_id=workspace_id,
            client=client,
            body=body,
            if_match=if_match,
            foresignal_client_id=foresignal_client_id,
        )
    ).parsed
