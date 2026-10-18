from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.push_subscription import PushSubscription
from ...models.push_subscription_verify import PushSubscriptionVerify
from ...types import Response


def _get_kwargs(
    subscription_id: UUID,
    *,
    body: PushSubscriptionVerify,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/push/subscriptions/{subscription_id}/verify".format(
            subscription_id=quote(str(subscription_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | PushSubscription | None:
    if response.status_code == 200:
        response_200 = PushSubscription.from_dict(response.json())

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
) -> Response[Problem | PushSubscription]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    subscription_id: UUID,
    *,
    client: AuthenticatedClient,
    body: PushSubscriptionVerify,
) -> Response[Problem | PushSubscription]:
    """Verify a push subscription

     Echo of the code from the `verify` push; a match activates the subscription. Mismatch → 400
    VERIFY_FAILED (after 5 failures the row is deleted); pending rows expire after 10 minutes. Only the
    creating token. Verifying an already active subscription returns it unchanged.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        subscription_id (UUID):
        body (PushSubscriptionVerify): Verification code echoed by the client (031)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushSubscription]
    """

    kwargs = _get_kwargs(
        subscription_id=subscription_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    subscription_id: UUID,
    *,
    client: AuthenticatedClient,
    body: PushSubscriptionVerify,
) -> Problem | PushSubscription | None:
    """Verify a push subscription

     Echo of the code from the `verify` push; a match activates the subscription. Mismatch → 400
    VERIFY_FAILED (after 5 failures the row is deleted); pending rows expire after 10 minutes. Only the
    creating token. Verifying an already active subscription returns it unchanged.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        subscription_id (UUID):
        body (PushSubscriptionVerify): Verification code echoed by the client (031)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushSubscription
    """

    return sync_detailed(
        subscription_id=subscription_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    subscription_id: UUID,
    *,
    client: AuthenticatedClient,
    body: PushSubscriptionVerify,
) -> Response[Problem | PushSubscription]:
    """Verify a push subscription

     Echo of the code from the `verify` push; a match activates the subscription. Mismatch → 400
    VERIFY_FAILED (after 5 failures the row is deleted); pending rows expire after 10 minutes. Only the
    creating token. Verifying an already active subscription returns it unchanged.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        subscription_id (UUID):
        body (PushSubscriptionVerify): Verification code echoed by the client (031)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushSubscription]
    """

    kwargs = _get_kwargs(
        subscription_id=subscription_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    subscription_id: UUID,
    *,
    client: AuthenticatedClient,
    body: PushSubscriptionVerify,
) -> Problem | PushSubscription | None:
    """Verify a push subscription

     Echo of the code from the `verify` push; a match activates the subscription. Mismatch → 400
    VERIFY_FAILED (after 5 failures the row is deleted); pending rows expire after 10 minutes. Only the
    creating token. Verifying an already active subscription returns it unchanged.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        subscription_id (UUID):
        body (PushSubscriptionVerify): Verification code echoed by the client (031)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushSubscription
    """

    return (
        await asyncio_detailed(
            subscription_id=subscription_id,
            client=client,
            body=body,
        )
    ).parsed
