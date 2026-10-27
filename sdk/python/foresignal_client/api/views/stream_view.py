from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    view_id: str,
    *,
    after: int | Unset = UNSET,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(last_event_id, Unset):
        headers["Last-Event-ID"] = last_event_id

    params: dict[str, Any] = {}

    params["after"] = after

    params["ticket"] = ticket

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/views/{view_id}/stream".format(
            view_id=quote(str(view_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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
) -> Response[Problem]:
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
    after: int | Unset = UNSET,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> Response[Problem]:
    """Change stream of a saved view (SSE)

     015 SSE semantics (replay from `Last-Event-ID`/`after`, live and touch lanes, resync and reset;
    `leftFilter` unchanged) filtered by the view's predicate. View data follows normal read rules unless
    the view references a place or watchlist, in which case a token is required (401).

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
        after (int | Unset):
        ticket (str | Unset):
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        view_id=view_id,
        after=after,
        ticket=ticket,
        last_event_id=last_event_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    view_id: str,
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> Problem | None:
    """Change stream of a saved view (SSE)

     015 SSE semantics (replay from `Last-Event-ID`/`after`, live and touch lanes, resync and reset;
    `leftFilter` unchanged) filtered by the view's predicate. View data follows normal read rules unless
    the view references a place or watchlist, in which case a token is required (401).

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
        after (int | Unset):
        ticket (str | Unset):
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        view_id=view_id,
        client=client,
        after=after,
        ticket=ticket,
        last_event_id=last_event_id,
    ).parsed


async def asyncio_detailed(
    view_id: str,
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> Response[Problem]:
    """Change stream of a saved view (SSE)

     015 SSE semantics (replay from `Last-Event-ID`/`after`, live and touch lanes, resync and reset;
    `leftFilter` unchanged) filtered by the view's predicate. View data follows normal read rules unless
    the view references a place or watchlist, in which case a token is required (401).

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
        after (int | Unset):
        ticket (str | Unset):
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        view_id=view_id,
        after=after,
        ticket=ticket,
        last_event_id=last_event_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    view_id: str,
    *,
    client: AuthenticatedClient,
    after: int | Unset = UNSET,
    ticket: str | Unset = UNSET,
    last_event_id: str | Unset = UNSET,
) -> Problem | None:
    """Change stream of a saved view (SSE)

     015 SSE semantics (replay from `Last-Event-ID`/`after`, live and touch lanes, resync and reset;
    `leftFilter` unchanged) filtered by the view's predicate. View data follows normal read rules unless
    the view references a place or watchlist, in which case a token is required (401).

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
        after (int | Unset):
        ticket (str | Unset):
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            view_id=view_id,
            client=client,
            after=after,
            ticket=ticket,
            last_event_id=last_event_id,
        )
    ).parsed
