from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.llm_provider import LlmProvider
from ...models.llm_provider_input import LlmProviderInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    provider_id: str,
    *,
    body: LlmProviderInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/llm/providers/{provider_id}".format(
            provider_id=quote(str(provider_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> LlmProvider | Problem | None:
    if response.status_code == 200:
        response_200 = LlmProvider.from_dict(response.json())

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
    provider_id: str,
    *,
    client: AuthenticatedClient,
    body: LlmProviderInput,
) -> Response[LlmProvider | Problem]:
    """Replace an LLM provider

     Omit `apiKey` to keep the stored key. Changing model or dimensions of the active embedding provider
    (or reassigning `embeddingProvider`) publishes an embedding-model change and starts a zero-downtime
    reindex (027). `baseUrl` passes OutboundUrlPolicy LAN_ALLOWED (035). A body `id`, when present, must
    equal the path id. The change is refused (400) when it would leave a role the provider holds
    unserved (disabled, no chatModel, or no embedding model).

    **Scope:** `admin`.

    Args:
        provider_id (str):
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmProvider | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    provider_id: str,
    *,
    client: AuthenticatedClient,
    body: LlmProviderInput,
) -> LlmProvider | Problem | None:
    """Replace an LLM provider

     Omit `apiKey` to keep the stored key. Changing model or dimensions of the active embedding provider
    (or reassigning `embeddingProvider`) publishes an embedding-model change and starts a zero-downtime
    reindex (027). `baseUrl` passes OutboundUrlPolicy LAN_ALLOWED (035). A body `id`, when present, must
    equal the path id. The change is refused (400) when it would leave a role the provider holds
    unserved (disabled, no chatModel, or no embedding model).

    **Scope:** `admin`.

    Args:
        provider_id (str):
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmProvider | Problem
    """

    return sync_detailed(
        provider_id=provider_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    provider_id: str,
    *,
    client: AuthenticatedClient,
    body: LlmProviderInput,
) -> Response[LlmProvider | Problem]:
    """Replace an LLM provider

     Omit `apiKey` to keep the stored key. Changing model or dimensions of the active embedding provider
    (or reassigning `embeddingProvider`) publishes an embedding-model change and starts a zero-downtime
    reindex (027). `baseUrl` passes OutboundUrlPolicy LAN_ALLOWED (035). A body `id`, when present, must
    equal the path id. The change is refused (400) when it would leave a role the provider holds
    unserved (disabled, no chatModel, or no embedding model).

    **Scope:** `admin`.

    Args:
        provider_id (str):
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmProvider | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    provider_id: str,
    *,
    client: AuthenticatedClient,
    body: LlmProviderInput,
) -> LlmProvider | Problem | None:
    """Replace an LLM provider

     Omit `apiKey` to keep the stored key. Changing model or dimensions of the active embedding provider
    (or reassigning `embeddingProvider`) publishes an embedding-model change and starts a zero-downtime
    reindex (027). `baseUrl` passes OutboundUrlPolicy LAN_ALLOWED (035). A body `id`, when present, must
    equal the path id. The change is refused (400) when it would leave a role the provider holds
    unserved (disabled, no chatModel, or no embedding model).

    **Scope:** `admin`.

    Args:
        provider_id (str):
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmProvider | Problem
    """

    return (
        await asyncio_detailed(
            provider_id=provider_id,
            client=client,
            body=body,
        )
    ).parsed
