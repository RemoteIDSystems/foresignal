from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.llm_probe_result import LlmProbeResult
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
        "url": "/v1/llm/probe",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> LlmProbeResult | Problem | None:
    if response.status_code == 200:
        response_200 = LlmProbeResult.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[LlmProbeResult | Problem]:
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
) -> Response[LlmProbeResult | Problem]:
    """Test an unsaved LLM provider and list its models

     Dry run for the provider editor: takes an LlmProviderInput that is not saved and, in one round trip,
    lists the provider's models and runs the testLlmProvider probes (a tiny chat when `chatModel` is
    set, an embedding when an embedding model and dimensions are). Without a model the model listing is
    the connection test. Nothing is persisted: no provider, no usage, no health, no budget; the `apiKey`
    is used for these requests and discarded. Without `apiKey`, the stored key of the saved provider
    named by `id` is used when there is one, so a saved provider can be tested with edits that are not
    saved yet. The body is validated as createLlmProvider validates it (400, and 400 URL_NOT_ALLOWED for
    a base URL the outbound URL policy refuses); the requests go through the same outbound URL policy
    and pinning as a saved provider's. A provider failure is reported in the result, never as an error
    status.

    **Scope:** `admin`.

    Args:
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmProbeResult | Problem]
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
) -> LlmProbeResult | Problem | None:
    """Test an unsaved LLM provider and list its models

     Dry run for the provider editor: takes an LlmProviderInput that is not saved and, in one round trip,
    lists the provider's models and runs the testLlmProvider probes (a tiny chat when `chatModel` is
    set, an embedding when an embedding model and dimensions are). Without a model the model listing is
    the connection test. Nothing is persisted: no provider, no usage, no health, no budget; the `apiKey`
    is used for these requests and discarded. Without `apiKey`, the stored key of the saved provider
    named by `id` is used when there is one, so a saved provider can be tested with edits that are not
    saved yet. The body is validated as createLlmProvider validates it (400, and 400 URL_NOT_ALLOWED for
    a base URL the outbound URL policy refuses); the requests go through the same outbound URL policy
    and pinning as a saved provider's. A provider failure is reported in the result, never as an error
    status.

    **Scope:** `admin`.

    Args:
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmProbeResult | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: LlmProviderInput,
) -> Response[LlmProbeResult | Problem]:
    """Test an unsaved LLM provider and list its models

     Dry run for the provider editor: takes an LlmProviderInput that is not saved and, in one round trip,
    lists the provider's models and runs the testLlmProvider probes (a tiny chat when `chatModel` is
    set, an embedding when an embedding model and dimensions are). Without a model the model listing is
    the connection test. Nothing is persisted: no provider, no usage, no health, no budget; the `apiKey`
    is used for these requests and discarded. Without `apiKey`, the stored key of the saved provider
    named by `id` is used when there is one, so a saved provider can be tested with edits that are not
    saved yet. The body is validated as createLlmProvider validates it (400, and 400 URL_NOT_ALLOWED for
    a base URL the outbound URL policy refuses); the requests go through the same outbound URL policy
    and pinning as a saved provider's. A provider failure is reported in the result, never as an error
    status.

    **Scope:** `admin`.

    Args:
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[LlmProbeResult | Problem]
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
) -> LlmProbeResult | Problem | None:
    """Test an unsaved LLM provider and list its models

     Dry run for the provider editor: takes an LlmProviderInput that is not saved and, in one round trip,
    lists the provider's models and runs the testLlmProvider probes (a tiny chat when `chatModel` is
    set, an embedding when an embedding model and dimensions are). Without a model the model listing is
    the connection test. Nothing is persisted: no provider, no usage, no health, no budget; the `apiKey`
    is used for these requests and discarded. Without `apiKey`, the stored key of the saved provider
    named by `id` is used when there is one, so a saved provider can be tested with edits that are not
    saved yet. The body is validated as createLlmProvider validates it (400, and 400 URL_NOT_ALLOWED for
    a base URL the outbound URL policy refuses); the requests go through the same outbound URL policy
    and pinning as a saved provider's. A provider failure is reported in the result, never as an error
    status.

    **Scope:** `admin`.

    Args:
        body (LlmProviderInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        LlmProbeResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
