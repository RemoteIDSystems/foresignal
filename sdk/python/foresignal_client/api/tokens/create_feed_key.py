from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.feed_key_created import FeedKeyCreated
from ...models.feed_key_input import FeedKeyInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: FeedKeyInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/feed-keys",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> FeedKeyCreated | Problem | None:
    if response.status_code == 201:
        response_201 = FeedKeyCreated.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[FeedKeyCreated | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: FeedKeyInput,
) -> Response[FeedKeyCreated | Problem]:
    """Create a feed key

     Creates an `fsf_` key for header-less clients (KML NetworkLinks, iCal, feed readers, Home Assistant
    geo_json_events, browser EventSource). Clients call `/v1/k/{key}/<rest>`, which is forwarded to
    `/v1/<rest>` for GET/HEAD when `/v1/<rest>` starts with one of `prefixes`; invalid key → 401
    INVALID_FEED_KEY, expired or revoked → 410. Returns the key and ready-made URLs once.

    **Scope:** `admin`.

    Args:
        body (FeedKeyInput): Create a path-bound feed key for header-less clients (035)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FeedKeyCreated | Problem]
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
    body: FeedKeyInput,
) -> FeedKeyCreated | Problem | None:
    """Create a feed key

     Creates an `fsf_` key for header-less clients (KML NetworkLinks, iCal, feed readers, Home Assistant
    geo_json_events, browser EventSource). Clients call `/v1/k/{key}/<rest>`, which is forwarded to
    `/v1/<rest>` for GET/HEAD when `/v1/<rest>` starts with one of `prefixes`; invalid key → 401
    INVALID_FEED_KEY, expired or revoked → 410. Returns the key and ready-made URLs once.

    **Scope:** `admin`.

    Args:
        body (FeedKeyInput): Create a path-bound feed key for header-less clients (035)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FeedKeyCreated | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: FeedKeyInput,
) -> Response[FeedKeyCreated | Problem]:
    """Create a feed key

     Creates an `fsf_` key for header-less clients (KML NetworkLinks, iCal, feed readers, Home Assistant
    geo_json_events, browser EventSource). Clients call `/v1/k/{key}/<rest>`, which is forwarded to
    `/v1/<rest>` for GET/HEAD when `/v1/<rest>` starts with one of `prefixes`; invalid key → 401
    INVALID_FEED_KEY, expired or revoked → 410. Returns the key and ready-made URLs once.

    **Scope:** `admin`.

    Args:
        body (FeedKeyInput): Create a path-bound feed key for header-less clients (035)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FeedKeyCreated | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: FeedKeyInput,
) -> FeedKeyCreated | Problem | None:
    """Create a feed key

     Creates an `fsf_` key for header-less clients (KML NetworkLinks, iCal, feed readers, Home Assistant
    geo_json_events, browser EventSource). Clients call `/v1/k/{key}/<rest>`, which is forwarded to
    `/v1/<rest>` for GET/HEAD when `/v1/<rest>` starts with one of `prefixes`; invalid key → 401
    INVALID_FEED_KEY, expired or revoked → 410. Returns the key and ready-made URLs once.

    **Scope:** `admin`.

    Args:
        body (FeedKeyInput): Create a path-bound feed key for header-less clients (035)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FeedKeyCreated | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
