from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.llm_provider import LlmProvider
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/llm/providers",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | list[LlmProvider] | None:
    if response.status_code == 200:
        response_200 = []
        _response_200 = response.json()
        for response_200_item_data in _response_200:
            response_200_item = LlmProvider.from_dict(response_200_item_data)

            response_200.append(response_200_item)

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | list[LlmProvider]]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | list[LlmProvider]]:
    """List LLM providers

     API keys are never returned.

    **Scope:** `admin`.

    **v2.0 (shared and personal, R2-011):** a provider is **shared** (`shared: true`, no owner; every
    provider from before v2.0, managed with `llm.shared.manage`) or **personal** (`ownerPrincipalId`,
    the owning user's principal, managed by that user with `self.llm`). A signed-in user reaches the
    provider operations with either permission: they see their own providers and the shared ones; they
    change, delete and test their own, list its models and read its budget; on a shared provider those
    need `llm.shared.manage` (403 PERMISSION_DENIED otherwise), and another user's provider is 404, like
    an unknown one. A session holding `users.manage` and every API token see and manage all providers.
    On create, `shared` picks the kind: absent, a user holding `self.llm` gets a personal provider and
    anyone else a shared one; an API token always creates shared providers; a replace keeps the kind.
    Only a shared provider can hold an instance role (getLlmSettings) or carry `perUser*CapUsd`, the
    caps on each user's own spend on it. A personal provider is never `local` for LLM egress. Calls made
    for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive
    `shared`, `ownerPrincipalId` and `perUser*CapUsd`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[LlmProvider]]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | list[LlmProvider] | None:
    """List LLM providers

     API keys are never returned.

    **Scope:** `admin`.

    **v2.0 (shared and personal, R2-011):** a provider is **shared** (`shared: true`, no owner; every
    provider from before v2.0, managed with `llm.shared.manage`) or **personal** (`ownerPrincipalId`,
    the owning user's principal, managed by that user with `self.llm`). A signed-in user reaches the
    provider operations with either permission: they see their own providers and the shared ones; they
    change, delete and test their own, list its models and read its budget; on a shared provider those
    need `llm.shared.manage` (403 PERMISSION_DENIED otherwise), and another user's provider is 404, like
    an unknown one. A session holding `users.manage` and every API token see and manage all providers.
    On create, `shared` picks the kind: absent, a user holding `self.llm` gets a personal provider and
    anyone else a shared one; an API token always creates shared providers; a replace keeps the kind.
    Only a shared provider can hold an instance role (getLlmSettings) or carry `perUser*CapUsd`, the
    caps on each user's own spend on it. A personal provider is never `local` for LLM egress. Calls made
    for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive
    `shared`, `ownerPrincipalId` and `perUser*CapUsd`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[LlmProvider]
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | list[LlmProvider]]:
    """List LLM providers

     API keys are never returned.

    **Scope:** `admin`.

    **v2.0 (shared and personal, R2-011):** a provider is **shared** (`shared: true`, no owner; every
    provider from before v2.0, managed with `llm.shared.manage`) or **personal** (`ownerPrincipalId`,
    the owning user's principal, managed by that user with `self.llm`). A signed-in user reaches the
    provider operations with either permission: they see their own providers and the shared ones; they
    change, delete and test their own, list its models and read its budget; on a shared provider those
    need `llm.shared.manage` (403 PERMISSION_DENIED otherwise), and another user's provider is 404, like
    an unknown one. A session holding `users.manage` and every API token see and manage all providers.
    On create, `shared` picks the kind: absent, a user holding `self.llm` gets a personal provider and
    anyone else a shared one; an API token always creates shared providers; a replace keeps the kind.
    Only a shared provider can hold an instance role (getLlmSettings) or carry `perUser*CapUsd`, the
    caps on each user's own spend on it. A personal provider is never `local` for LLM egress. Calls made
    for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive
    `shared`, `ownerPrincipalId` and `perUser*CapUsd`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[LlmProvider]]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | list[LlmProvider] | None:
    """List LLM providers

     API keys are never returned.

    **Scope:** `admin`.

    **v2.0 (shared and personal, R2-011):** a provider is **shared** (`shared: true`, no owner; every
    provider from before v2.0, managed with `llm.shared.manage`) or **personal** (`ownerPrincipalId`,
    the owning user's principal, managed by that user with `self.llm`). A signed-in user reaches the
    provider operations with either permission: they see their own providers and the shared ones; they
    change, delete and test their own, list its models and read its budget; on a shared provider those
    need `llm.shared.manage` (403 PERMISSION_DENIED otherwise), and another user's provider is 404, like
    an unknown one. A session holding `users.manage` and every API token see and manage all providers.
    On create, `shared` picks the kind: absent, a user holding `self.llm` gets a personal provider and
    anyone else a shared one; an API token always creates shared providers; a replace keeps the kind.
    Only a shared provider can hold an instance role (getLlmSettings) or carry `perUser*CapUsd`, the
    caps on each user's own spend on it. A personal provider is never `local` for LLM egress. Calls made
    for a user are metered to that user as well (getMyLlmBudget). The shapes change only by the additive
    `shared`, `ownerPrincipalId` and `perUser*CapUsd`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[LlmProvider]
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
