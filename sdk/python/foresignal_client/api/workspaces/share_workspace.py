from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.share_link import ShareLink
from ...models.workspace_share_input import WorkspaceShareInput
from ...types import UNSET, Response, Unset


def _get_kwargs(
    workspace_id: str,
    *,
    body: WorkspaceShareInput | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/workspaces/{workspace_id}/share".format(
            workspace_id=quote(str(workspace_id), safe=""),
        ),
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ShareLink | None:
    if response.status_code == 201:
        response_201 = ShareLink.from_dict(response.json())

        return response_201

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ShareLink]:
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
    body: WorkspaceShareInput | Unset = UNSET,
) -> Response[Problem | ShareLink]:
    """Share a workspace context read-only

     Issues a read-only 112 share link (`/v1/k/{key}/…`) to the workspace context; the key is shown once.
    A share link is public, so the workspace's TLP label must be CLEAR (130, `TLP_VIOLATION`).

    **Scope:** `agent`.

    Args:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (WorkspaceShareInput | Unset): Options for a read-only share link to a workspace
            context

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ShareLink]
    """

    kwargs = _get_kwargs(
        workspace_id=workspace_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
    body: WorkspaceShareInput | Unset = UNSET,
) -> Problem | ShareLink | None:
    """Share a workspace context read-only

     Issues a read-only 112 share link (`/v1/k/{key}/…`) to the workspace context; the key is shown once.
    A share link is public, so the workspace's TLP label must be CLEAR (130, `TLP_VIOLATION`).

    **Scope:** `agent`.

    Args:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (WorkspaceShareInput | Unset): Options for a read-only share link to a workspace
            context

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ShareLink
    """

    return sync_detailed(
        workspace_id=workspace_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
    body: WorkspaceShareInput | Unset = UNSET,
) -> Response[Problem | ShareLink]:
    """Share a workspace context read-only

     Issues a read-only 112 share link (`/v1/k/{key}/…`) to the workspace context; the key is shown once.
    A share link is public, so the workspace's TLP label must be CLEAR (130, `TLP_VIOLATION`).

    **Scope:** `agent`.

    Args:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (WorkspaceShareInput | Unset): Options for a read-only share link to a workspace
            context

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ShareLink]
    """

    kwargs = _get_kwargs(
        workspace_id=workspace_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    workspace_id: str,
    *,
    client: AuthenticatedClient,
    body: WorkspaceShareInput | Unset = UNSET,
) -> Problem | ShareLink | None:
    """Share a workspace context read-only

     Issues a read-only 112 share link (`/v1/k/{key}/…`) to the workspace context; the key is shown once.
    A share link is public, so the workspace's TLP label must be CLEAR (130, `TLP_VIOLATION`).

    **Scope:** `agent`.

    Args:
        workspace_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (WorkspaceShareInput | Unset): Options for a read-only share link to a workspace
            context

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ShareLink
    """

    return (
        await asyncio_detailed(
            workspace_id=workspace_id,
            client=client,
            body=body,
        )
    ).parsed
