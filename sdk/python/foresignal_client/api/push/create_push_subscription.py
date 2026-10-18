from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.push_subscription import PushSubscription
from ...models.push_subscription_input import PushSubscriptionInput
from ...types import Response


def _get_kwargs(
    *,
    body: PushSubscriptionInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/push/subscriptions",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | PushSubscription | None:
    if response.status_code == 201:
        response_201 = PushSubscription.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

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
) -> Response[Problem | PushSubscription]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: PushSubscriptionInput,
) -> Response[Problem | PushSubscription]:
    """Register a browser push subscription

     Registers a browser or UnifiedPush subscription as `pending` and immediately sends a `verify` push
    carrying a code; only after `POST …/{subscriptionId}/verify` does it receive notifications. The
    endpoint must be https and every resolved address globally routable unless allowed by
    `push.allowedPrivateHosts` (400 URL_NOT_ALLOWED). Limits: ≤ 5 pending per token, ≤ 200 in total (409
    PUSH_LIMIT_REACHED). A subscription to an endpoint that is already registered replaces it. The
    response never contains the endpoint, only `endpointHost`; a verification push that the push service
    refuses leaves the row pending (it then expires).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (PushSubscriptionInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushSubscription]
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
    body: PushSubscriptionInput,
) -> Problem | PushSubscription | None:
    """Register a browser push subscription

     Registers a browser or UnifiedPush subscription as `pending` and immediately sends a `verify` push
    carrying a code; only after `POST …/{subscriptionId}/verify` does it receive notifications. The
    endpoint must be https and every resolved address globally routable unless allowed by
    `push.allowedPrivateHosts` (400 URL_NOT_ALLOWED). Limits: ≤ 5 pending per token, ≤ 200 in total (409
    PUSH_LIMIT_REACHED). A subscription to an endpoint that is already registered replaces it. The
    response never contains the endpoint, only `endpointHost`; a verification push that the push service
    refuses leaves the row pending (it then expires).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (PushSubscriptionInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushSubscription
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: PushSubscriptionInput,
) -> Response[Problem | PushSubscription]:
    """Register a browser push subscription

     Registers a browser or UnifiedPush subscription as `pending` and immediately sends a `verify` push
    carrying a code; only after `POST …/{subscriptionId}/verify` does it receive notifications. The
    endpoint must be https and every resolved address globally routable unless allowed by
    `push.allowedPrivateHosts` (400 URL_NOT_ALLOWED). Limits: ≤ 5 pending per token, ≤ 200 in total (409
    PUSH_LIMIT_REACHED). A subscription to an endpoint that is already registered replaces it. The
    response never contains the endpoint, only `endpointHost`; a verification push that the push service
    refuses leaves the row pending (it then expires).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (PushSubscriptionInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushSubscription]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: PushSubscriptionInput,
) -> Problem | PushSubscription | None:
    """Register a browser push subscription

     Registers a browser or UnifiedPush subscription as `pending` and immediately sends a `verify` push
    carrying a code; only after `POST …/{subscriptionId}/verify` does it receive notifications. The
    endpoint must be https and every resolved address globally routable unless allowed by
    `push.allowedPrivateHosts` (400 URL_NOT_ALLOWED). Limits: ≤ 5 pending per token, ≤ 200 in total (409
    PUSH_LIMIT_REACHED). A subscription to an endpoint that is already registered replaces it. The
    response never contains the endpoint, only `endpointHost`; a verification push that the push service
    refuses leaves the row pending (it then expires).

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        body (PushSubscriptionInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushSubscription
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
