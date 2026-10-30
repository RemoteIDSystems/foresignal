from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.news_subscription_update import NewsSubscriptionUpdate
from ...models.news_subscription_view import NewsSubscriptionView
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: NewsSubscriptionUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/me/news-subscriptions",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> NewsSubscriptionView | Problem | None:
    if response.status_code == 200:
        response_200 = NewsSubscriptionView.from_dict(response.json())

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
    body: NewsSubscriptionUpdate,
) -> Response[NewsSubscriptionView | Problem]:
    """Update my news subscriptions

     Replaces the caller's news subscriptions; an empty list removes every restriction. An unknown outlet
    id or a malformed language tag is a 400. An API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Args:
        body (NewsSubscriptionUpdate): Replace the caller's news subscriptions. With no entry of a
            type there is no restriction of that type; with at least one, the caller's editions keep
            only news matching one of them (an outlet entry matches its outlet, a language entry an
            article in that language or translated into it, NP-002).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NewsSubscriptionView | Problem]
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
    body: NewsSubscriptionUpdate,
) -> NewsSubscriptionView | Problem | None:
    """Update my news subscriptions

     Replaces the caller's news subscriptions; an empty list removes every restriction. An unknown outlet
    id or a malformed language tag is a 400. An API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Args:
        body (NewsSubscriptionUpdate): Replace the caller's news subscriptions. With no entry of a
            type there is no restriction of that type; with at least one, the caller's editions keep
            only news matching one of them (an outlet entry matches its outlet, a language entry an
            article in that language or translated into it, NP-002).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NewsSubscriptionView | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: NewsSubscriptionUpdate,
) -> Response[NewsSubscriptionView | Problem]:
    """Update my news subscriptions

     Replaces the caller's news subscriptions; an empty list removes every restriction. An unknown outlet
    id or a malformed language tag is a 400. An API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Args:
        body (NewsSubscriptionUpdate): Replace the caller's news subscriptions. With no entry of a
            type there is no restriction of that type; with at least one, the caller's editions keep
            only news matching one of them (an outlet entry matches its outlet, a language entry an
            article in that language or translated into it, NP-002).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[NewsSubscriptionView | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: NewsSubscriptionUpdate,
) -> NewsSubscriptionView | Problem | None:
    """Update my news subscriptions

     Replaces the caller's news subscriptions; an empty list removes every restriction. An unknown outlet
    id or a malformed language tag is a 400. An API token gets `401`. Requires `self.sources`.

    **Scope:** `read`.

    Args:
        body (NewsSubscriptionUpdate): Replace the caller's news subscriptions. With no entry of a
            type there is no restriction of that type; with at least one, the caller's editions keep
            only news matching one of them (an outlet entry matches its outlet, a language entry an
            article in that language or translated into it, NP-002).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        NewsSubscriptionView | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
