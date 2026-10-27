from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.user_source_view import UserSourceView
from ...models.user_source_view_update import UserSourceViewUpdate
from ...types import Response


def _get_kwargs(
    *,
    body: UserSourceViewUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/me/sources",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | UserSourceView | None:
    if response.status_code == 200:
        response_200 = UserSourceView.from_dict(response.json())

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
) -> Response[Problem | UserSourceView]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: UserSourceViewUpdate,
) -> Response[Problem | UserSourceView]:
    """Update my enabled-source view

     Replaces the caller's source view; a source left out returns to the default (enabled, no overrides).
    A source may be listed whether or not the instance collects it; the preference takes effect while it
    is in the collection set. The collector pulls a source once while any active user's view keeps it,
    so a view never adds a second pull. Afterwards the caller's reads (records, changes, tiles, search,
    statistics) show only the sources in their view. An API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Args:
        body (UserSourceViewUpdate): Replace the caller's enabled-source view

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserSourceView]
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
    body: UserSourceViewUpdate,
) -> Problem | UserSourceView | None:
    """Update my enabled-source view

     Replaces the caller's source view; a source left out returns to the default (enabled, no overrides).
    A source may be listed whether or not the instance collects it; the preference takes effect while it
    is in the collection set. The collector pulls a source once while any active user's view keeps it,
    so a view never adds a second pull. Afterwards the caller's reads (records, changes, tiles, search,
    statistics) show only the sources in their view. An API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Args:
        body (UserSourceViewUpdate): Replace the caller's enabled-source view

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserSourceView
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: UserSourceViewUpdate,
) -> Response[Problem | UserSourceView]:
    """Update my enabled-source view

     Replaces the caller's source view; a source left out returns to the default (enabled, no overrides).
    A source may be listed whether or not the instance collects it; the preference takes effect while it
    is in the collection set. The collector pulls a source once while any active user's view keeps it,
    so a view never adds a second pull. Afterwards the caller's reads (records, changes, tiles, search,
    statistics) show only the sources in their view. An API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Args:
        body (UserSourceViewUpdate): Replace the caller's enabled-source view

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserSourceView]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: UserSourceViewUpdate,
) -> Problem | UserSourceView | None:
    """Update my enabled-source view

     Replaces the caller's source view; a source left out returns to the default (enabled, no overrides).
    A source may be listed whether or not the instance collects it; the preference takes effect while it
    is in the collection set. The collector pulls a source once while any active user's view keeps it,
    so a view never adds a second pull. Afterwards the caller's reads (records, changes, tiles, search,
    statistics) show only the sources in their view. An API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Args:
        body (UserSourceViewUpdate): Replace the caller's enabled-source view

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserSourceView
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
