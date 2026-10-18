from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.llm_provider import LlmProvider
from ...models.llm_provider_input import LlmProviderInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: LlmProviderInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/llm/providers",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> LlmProvider | Problem | None:
    if response.status_code == 201:
        response_201 = LlmProvider.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[LlmProvider | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: LlmProviderInput,
) -> Response[LlmProvider | Problem]:
    """Add an LLM provider

     Add an LLM provider. Defaults: `anthropic` → `baseUrl` https://api.anthropic.com, `chatModel`
    claude-opus-5-5, `options.effort` medium, `options.maxTokens` 16000, and `apiKey` is required;
    `ollama` → `baseUrl` http://ollama:11434; `openai-compatible` must name `baseUrl`. `baseUrl` must be
    http or https with a host and no user info or fragment; private-network URLs are allowed (035 adds
    OutboundUrlPolicy LAN_ALLOWED). The key is encrypted at rest and never returned (`apiKeySet`).

    **Scope:** `admin`.

    Args:
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmProvider | Problem]
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
    body: LlmProviderInput,
) -> LlmProvider | Problem | None:
    """Add an LLM provider

     Add an LLM provider. Defaults: `anthropic` → `baseUrl` https://api.anthropic.com, `chatModel`
    claude-opus-5-5, `options.effort` medium, `options.maxTokens` 16000, and `apiKey` is required;
    `ollama` → `baseUrl` http://ollama:11434; `openai-compatible` must name `baseUrl`. `baseUrl` must be
    http or https with a host and no user info or fragment; private-network URLs are allowed (035 adds
    OutboundUrlPolicy LAN_ALLOWED). The key is encrypted at rest and never returned (`apiKeySet`).

    **Scope:** `admin`.

    Args:
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmProvider | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: LlmProviderInput,
) -> Response[LlmProvider | Problem]:
    """Add an LLM provider

     Add an LLM provider. Defaults: `anthropic` → `baseUrl` https://api.anthropic.com, `chatModel`
    claude-opus-5-5, `options.effort` medium, `options.maxTokens` 16000, and `apiKey` is required;
    `ollama` → `baseUrl` http://ollama:11434; `openai-compatible` must name `baseUrl`. `baseUrl` must be
    http or https with a host and no user info or fragment; private-network URLs are allowed (035 adds
    OutboundUrlPolicy LAN_ALLOWED). The key is encrypted at rest and never returned (`apiKeySet`).

    **Scope:** `admin`.

    Args:
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmProvider | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: LlmProviderInput,
) -> LlmProvider | Problem | None:
    """Add an LLM provider

     Add an LLM provider. Defaults: `anthropic` → `baseUrl` https://api.anthropic.com, `chatModel`
    claude-opus-5-5, `options.effort` medium, `options.maxTokens` 16000, and `apiKey` is required;
    `ollama` → `baseUrl` http://ollama:11434; `openai-compatible` must name `baseUrl`. `baseUrl` must be
    http or https with a host and no user info or fragment; private-network URLs are allowed (035 adds
    OutboundUrlPolicy LAN_ALLOWED). The key is encrypted at rest and never returned (`apiKeySet`).

    **Scope:** `admin`.

    Args:
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmProvider | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
