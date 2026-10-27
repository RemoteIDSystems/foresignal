from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.saved_view import SavedView
from ...models.saved_view_input import SavedViewInput
from ...types import Response


def _get_kwargs(
    *,
    body: SavedViewInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/views",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SavedView | None:
    if response.status_code == 201:
        response_201 = SavedView.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

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
) -> Response[Problem | SavedView]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: SavedViewInput,
) -> Response[Problem | SavedView]:
    """Create a saved view

     Compiles the view to one `RecordPredicate` shared with rules (SQL and in-memory evaluation agree on
    the operator truth table). A signed-in user creating their own view is refused past their
    `maxSavedViews` quota (409 QUOTA_EXCEEDED, R2-026).

    **Scope:** `agent`.

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
        body (SavedViewInput): Create or replace a saved view (036)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SavedView]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: SavedViewInput,
) -> Problem | SavedView | None:
    """Create a saved view

     Compiles the view to one `RecordPredicate` shared with rules (SQL and in-memory evaluation agree on
    the operator truth table). A signed-in user creating their own view is refused past their
    `maxSavedViews` quota (409 QUOTA_EXCEEDED, R2-026).

    **Scope:** `agent`.

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
        body (SavedViewInput): Create or replace a saved view (036)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SavedView
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: SavedViewInput,
) -> Response[Problem | SavedView]:
    """Create a saved view

     Compiles the view to one `RecordPredicate` shared with rules (SQL and in-memory evaluation agree on
    the operator truth table). A signed-in user creating their own view is refused past their
    `maxSavedViews` quota (409 QUOTA_EXCEEDED, R2-026).

    **Scope:** `agent`.

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
        body (SavedViewInput): Create or replace a saved view (036)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SavedView]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: SavedViewInput,
) -> Problem | SavedView | None:
    """Create a saved view

     Compiles the view to one `RecordPredicate` shared with rules (SQL and in-memory evaluation agree on
    the operator truth table). A signed-in user creating their own view is refused past their
    `maxSavedViews` quota (409 QUOTA_EXCEEDED, R2-026).

    **Scope:** `agent`.

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
        body (SavedViewInput): Create or replace a saved view (036)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SavedView
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
