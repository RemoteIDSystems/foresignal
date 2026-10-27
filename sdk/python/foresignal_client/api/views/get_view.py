from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.saved_view import SavedView
from ...types import Response


def _get_kwargs(
    view_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/views/{view_id}".format(
            view_id=quote(str(view_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SavedView | None:
    if response.status_code == 200:
        response_200 = SavedView.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SavedView]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    view_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SavedView]:
    """Get a saved view

     404 UNKNOWN_VIEW.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (per-user, R2-012):** a saved view is **shared** (`shared: true`, no owner; every view from
    before v2.0 and every view an API token creates) or **personal** (`ownerPrincipalId`, the owning
    user's principal). A signed-in user reaches the view operations with `self.views` (the reads also
    with `data.read`): they list and render their own views and the shared ones, and create, replace and
    delete their own; a view they create is theirs. Another user's view is 404 UNKNOWN_VIEW, like an
    unknown one; replacing or deleting a shared view needs `users.manage` (403 PERMISSION_DENIED
    otherwise). A session holding `users.manage` and every API token see and manage all views. Slugs
    stay unique across all views. The shapes change only by the additive read-only `shared` and
    `ownerPrincipalId`.

    Args:
        view_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SavedView]
    """

    kwargs = _get_kwargs(
        view_id=view_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    view_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SavedView | None:
    """Get a saved view

     404 UNKNOWN_VIEW.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (per-user, R2-012):** a saved view is **shared** (`shared: true`, no owner; every view from
    before v2.0 and every view an API token creates) or **personal** (`ownerPrincipalId`, the owning
    user's principal). A signed-in user reaches the view operations with `self.views` (the reads also
    with `data.read`): they list and render their own views and the shared ones, and create, replace and
    delete their own; a view they create is theirs. Another user's view is 404 UNKNOWN_VIEW, like an
    unknown one; replacing or deleting a shared view needs `users.manage` (403 PERMISSION_DENIED
    otherwise). A session holding `users.manage` and every API token see and manage all views. Slugs
    stay unique across all views. The shapes change only by the additive read-only `shared` and
    `ownerPrincipalId`.

    Args:
        view_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SavedView
    """

    return sync_detailed(
        view_id=view_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    view_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | SavedView]:
    """Get a saved view

     404 UNKNOWN_VIEW.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (per-user, R2-012):** a saved view is **shared** (`shared: true`, no owner; every view from
    before v2.0 and every view an API token creates) or **personal** (`ownerPrincipalId`, the owning
    user's principal). A signed-in user reaches the view operations with `self.views` (the reads also
    with `data.read`): they list and render their own views and the shared ones, and create, replace and
    delete their own; a view they create is theirs. Another user's view is 404 UNKNOWN_VIEW, like an
    unknown one; replacing or deleting a shared view needs `users.manage` (403 PERMISSION_DENIED
    otherwise). A session holding `users.manage` and every API token see and manage all views. Slugs
    stay unique across all views. The shapes change only by the additive read-only `shared` and
    `ownerPrincipalId`.

    Args:
        view_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SavedView]
    """

    kwargs = _get_kwargs(
        view_id=view_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    view_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | SavedView | None:
    """Get a saved view

     404 UNKNOWN_VIEW.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (per-user, R2-012):** a saved view is **shared** (`shared: true`, no owner; every view from
    before v2.0 and every view an API token creates) or **personal** (`ownerPrincipalId`, the owning
    user's principal). A signed-in user reaches the view operations with `self.views` (the reads also
    with `data.read`): they list and render their own views and the shared ones, and create, replace and
    delete their own; a view they create is theirs. Another user's view is 404 UNKNOWN_VIEW, like an
    unknown one; replacing or deleting a shared view needs `users.manage` (403 PERMISSION_DENIED
    otherwise). A session holding `users.manage` and every API token see and manage all views. Slugs
    stay unique across all views. The shapes change only by the additive read-only `shared` and
    `ownerPrincipalId`.

    Args:
        view_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SavedView
    """

    return (
        await asyncio_detailed(
            view_id=view_id,
            client=client,
        )
    ).parsed
