from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.channel_input import ChannelInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ChannelInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/channels",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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
    *,
    client: AuthenticatedClient,
    body: ChannelInput,
) -> Response[Problem]:
    """Create a notification channel

     Create a notification channel A signed-in user creating a personal channel is refused past their
    `maxChannels` quota (409 QUOTA_EXCEEDED, R2-026).

    **Scope:** `admin`.

    **v2.0 (per-user, R2-009):** a channel is **personal** (`ownerPrincipalId`, the owning user's
    principal) or **global** (`global: true`, no owner; every channel from before v2.0). A signed-in
    user reaches these operations with `self.channels` or `channels.global`: they see their own channels
    and the global ones, manage their own, and manage global channels only with `channels.global`
    (changing one without it is 403 PERMISSION_DENIED); another user's channel is 404, like an unknown
    one. A session holding `users.manage` and every API token see and manage all channels. On create,
    `global` picks the kind: absent, a user holding `self.channels` gets a personal channel and anyone
    else a global one; an API token always creates global channels. A user's alerts go to their own
    channels and to the global channels whose `ruleIds` name the rule. This is a deliberate semantic
    change from the single-operator v1.x behavior; the shapes are unchanged except the additive
    `ownerPrincipalId` and `global`.

    Args:
        body (ChannelInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
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
    body: ChannelInput,
) -> Problem | None:
    """Create a notification channel

     Create a notification channel A signed-in user creating a personal channel is refused past their
    `maxChannels` quota (409 QUOTA_EXCEEDED, R2-026).

    **Scope:** `admin`.

    **v2.0 (per-user, R2-009):** a channel is **personal** (`ownerPrincipalId`, the owning user's
    principal) or **global** (`global: true`, no owner; every channel from before v2.0). A signed-in
    user reaches these operations with `self.channels` or `channels.global`: they see their own channels
    and the global ones, manage their own, and manage global channels only with `channels.global`
    (changing one without it is 403 PERMISSION_DENIED); another user's channel is 404, like an unknown
    one. A session holding `users.manage` and every API token see and manage all channels. On create,
    `global` picks the kind: absent, a user holding `self.channels` gets a personal channel and anyone
    else a global one; an API token always creates global channels. A user's alerts go to their own
    channels and to the global channels whose `ruleIds` name the rule. This is a deliberate semantic
    change from the single-operator v1.x behavior; the shapes are unchanged except the additive
    `ownerPrincipalId` and `global`.

    Args:
        body (ChannelInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ChannelInput,
) -> Response[Problem]:
    """Create a notification channel

     Create a notification channel A signed-in user creating a personal channel is refused past their
    `maxChannels` quota (409 QUOTA_EXCEEDED, R2-026).

    **Scope:** `admin`.

    **v2.0 (per-user, R2-009):** a channel is **personal** (`ownerPrincipalId`, the owning user's
    principal) or **global** (`global: true`, no owner; every channel from before v2.0). A signed-in
    user reaches these operations with `self.channels` or `channels.global`: they see their own channels
    and the global ones, manage their own, and manage global channels only with `channels.global`
    (changing one without it is 403 PERMISSION_DENIED); another user's channel is 404, like an unknown
    one. A session holding `users.manage` and every API token see and manage all channels. On create,
    `global` picks the kind: absent, a user holding `self.channels` gets a personal channel and anyone
    else a global one; an API token always creates global channels. A user's alerts go to their own
    channels and to the global channels whose `ruleIds` name the rule. This is a deliberate semantic
    change from the single-operator v1.x behavior; the shapes are unchanged except the additive
    `ownerPrincipalId` and `global`.

    Args:
        body (ChannelInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ChannelInput,
) -> Problem | None:
    """Create a notification channel

     Create a notification channel A signed-in user creating a personal channel is refused past their
    `maxChannels` quota (409 QUOTA_EXCEEDED, R2-026).

    **Scope:** `admin`.

    **v2.0 (per-user, R2-009):** a channel is **personal** (`ownerPrincipalId`, the owning user's
    principal) or **global** (`global: true`, no owner; every channel from before v2.0). A signed-in
    user reaches these operations with `self.channels` or `channels.global`: they see their own channels
    and the global ones, manage their own, and manage global channels only with `channels.global`
    (changing one without it is 403 PERMISSION_DENIED); another user's channel is 404, like an unknown
    one. A session holding `users.manage` and every API token see and manage all channels. On create,
    `global` picks the kind: absent, a user holding `self.channels` gets a personal channel and anyone
    else a global one; an API token always creates global channels. A user's alerts go to their own
    channels and to the global channels whose `ruleIds` name the rule. This is a deliberate semantic
    change from the single-operator v1.x behavior; the shapes are unchanged except the additive
    `ownerPrincipalId` and `global`.

    Args:
        body (ChannelInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
