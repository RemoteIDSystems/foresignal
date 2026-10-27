from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.user_source_view import UserSourceView
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/me/sources",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | UserSourceView | None:
    if response.status_code == 200:
        response_200 = UserSourceView.from_dict(response.json())

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
) -> Response[Problem | UserSourceView]:
    """Get my enabled-source view

     Returns the caller's own source view over the shared collection: one entry per source the caller may
    see, with `enabled` and the caller's display overrides. A view is opt-out: a source the caller never
    turned off is `enabled` with no overrides. The instance-wide collection set is getCollectionSources.
    A view belongs to a signed-in user, so an API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserSourceView]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | UserSourceView | None:
    """Get my enabled-source view

     Returns the caller's own source view over the shared collection: one entry per source the caller may
    see, with `enabled` and the caller's display overrides. A view is opt-out: a source the caller never
    turned off is `enabled` with no overrides. The instance-wide collection set is getCollectionSources.
    A view belongs to a signed-in user, so an API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserSourceView
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | UserSourceView]:
    """Get my enabled-source view

     Returns the caller's own source view over the shared collection: one entry per source the caller may
    see, with `enabled` and the caller's display overrides. A view is opt-out: a source the caller never
    turned off is `enabled` with no overrides. The instance-wide collection set is getCollectionSources.
    A view belongs to a signed-in user, so an API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserSourceView]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | UserSourceView | None:
    """Get my enabled-source view

     Returns the caller's own source view over the shared collection: one entry per source the caller may
    see, with `enabled` and the caller's display overrides. A view is opt-out: a source the caller never
    turned off is `enabled` with no overrides. The instance-wide collection set is getCollectionSources.
    A view belongs to a signed-in user, so an API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserSourceView
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
