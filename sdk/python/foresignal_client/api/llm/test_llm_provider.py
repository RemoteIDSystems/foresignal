from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.llm_test_result import LlmTestResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    provider_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/llm/providers/{provider_id}/test".format(
            provider_id=quote(str(provider_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> LlmTestResult | Problem | None:
    if response.status_code == 200:
        response_200 = LlmTestResult.from_dict(response.json())

        return response_200

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
) -> Response[LlmTestResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    provider_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[LlmTestResult | Problem]:
    """Test an LLM provider

     Sends a tiny chat request ("Reply with the single word: ok", 16 output tokens, purpose `test`) and
    an embedding of "test" when the provider has an embedding model, and reports latency, model, token
    usage (cached tokens included) and the normalized stop reason. Consumes budget. A provider failure
    or refusal is reported in the result (`success: false`, `error`, `refusalCategory`); only a budget
    refusal is an error status. A destination refused by the outbound URL policy at send time answers
    `success: false` with an `error` containing URL_NOT_ALLOWED (035).

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

    Args:
        provider_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmTestResult | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    provider_id: str,
    *,
    client: AuthenticatedClient,
) -> LlmTestResult | Problem | None:
    """Test an LLM provider

     Sends a tiny chat request ("Reply with the single word: ok", 16 output tokens, purpose `test`) and
    an embedding of "test" when the provider has an embedding model, and reports latency, model, token
    usage (cached tokens included) and the normalized stop reason. Consumes budget. A provider failure
    or refusal is reported in the result (`success: false`, `error`, `refusalCategory`); only a budget
    refusal is an error status. A destination refused by the outbound URL policy at send time answers
    `success: false` with an `error` containing URL_NOT_ALLOWED (035).

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

    Args:
        provider_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmTestResult | Problem
    """

    return sync_detailed(
        provider_id=provider_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    provider_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[LlmTestResult | Problem]:
    """Test an LLM provider

     Sends a tiny chat request ("Reply with the single word: ok", 16 output tokens, purpose `test`) and
    an embedding of "test" when the provider has an embedding model, and reports latency, model, token
    usage (cached tokens included) and the normalized stop reason. Consumes budget. A provider failure
    or refusal is reported in the result (`success: false`, `error`, `refusalCategory`); only a budget
    refusal is an error status. A destination refused by the outbound URL policy at send time answers
    `success: false` with an `error` containing URL_NOT_ALLOWED (035).

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

    Args:
        provider_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmTestResult | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    provider_id: str,
    *,
    client: AuthenticatedClient,
) -> LlmTestResult | Problem | None:
    """Test an LLM provider

     Sends a tiny chat request ("Reply with the single word: ok", 16 output tokens, purpose `test`) and
    an embedding of "test" when the provider has an embedding model, and reports latency, model, token
    usage (cached tokens included) and the normalized stop reason. Consumes budget. A provider failure
    or refusal is reported in the result (`success: false`, `error`, `refusalCategory`); only a budget
    refusal is an error status. A destination refused by the outbound URL policy at send time answers
    `success: false` with an `error` containing URL_NOT_ALLOWED (035).

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

    Args:
        provider_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmTestResult | Problem
    """

    return (
        await asyncio_detailed(
            provider_id=provider_id,
            client=client,
        )
    ).parsed
