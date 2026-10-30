from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.news_subscription_view import NewsSubscriptionView
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/me/news-subscriptions",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NewsSubscriptionView | Problem | None:
    if response.status_code == 200:
        response_200 = NewsSubscriptionView.from_dict(response.json())

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
) -> Response[NewsSubscriptionView | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[NewsSubscriptionView | Problem]:
    """Get my news subscriptions

     The outlets and languages the caller follows, which scope the caller's editions (NP-003). A caller
    who never set any has none, which is no restriction. Subscriptions belong to a signed-in user, so an
    API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NewsSubscriptionView | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> NewsSubscriptionView | Problem | None:
    """Get my news subscriptions

     The outlets and languages the caller follows, which scope the caller's editions (NP-003). A caller
    who never set any has none, which is no restriction. Subscriptions belong to a signed-in user, so an
    API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NewsSubscriptionView | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[NewsSubscriptionView | Problem]:
    """Get my news subscriptions

     The outlets and languages the caller follows, which scope the caller's editions (NP-003). A caller
    who never set any has none, which is no restriction. Subscriptions belong to a signed-in user, so an
    API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NewsSubscriptionView | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> NewsSubscriptionView | Problem | None:
    """Get my news subscriptions

     The outlets and languages the caller follows, which scope the caller's editions (NP-003). A caller
    who never set any has none, which is no restriction. Subscriptions belong to a signed-in user, so an
    API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NewsSubscriptionView | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
