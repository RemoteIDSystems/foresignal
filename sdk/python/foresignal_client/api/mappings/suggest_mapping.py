from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.mapping_suggest_request import MappingSuggestRequest
from ...models.mapping_suggest_result import MappingSuggestResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: MappingSuggestRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/mappings/suggest",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MappingSuggestResult | Problem | None:
    if response.status_code == 200:
        response_200 = MappingSuggestResult.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 422:
        response_422 = Problem.from_dict(response.json())

        return response_422

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[MappingSuggestResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: MappingSuggestRequest,
) -> Response[MappingSuggestResult | Problem]:
    """LLM-suggested draft RecordMapping

     Asks the chat provider (purpose `mapping-suggest`, 026 budget) for a draft mapping. The sample is
    untrusted data inside delimiters; the output is validated with `MappingCompiler` with up to 2 repair
    rounds, then previewed over the sample. The draft is never stored or applied: saving it is a
    separate `PUT /v1/custom-sources/{id}`. A remote provider requires `confirmEgress: true` (409
    `EGRESS_CONFIRMATION_REQUIRED`); samples from a private source are refused for remote providers
    (403); no provider → 409 `LLM_NOT_CONFIGURED`.

    **Scope:** `admin`.

    Args:
        body (MappingSuggestRequest): Request for an LLM-drafted mapping (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MappingSuggestResult | Problem]
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
    body: MappingSuggestRequest,
) -> MappingSuggestResult | Problem | None:
    """LLM-suggested draft RecordMapping

     Asks the chat provider (purpose `mapping-suggest`, 026 budget) for a draft mapping. The sample is
    untrusted data inside delimiters; the output is validated with `MappingCompiler` with up to 2 repair
    rounds, then previewed over the sample. The draft is never stored or applied: saving it is a
    separate `PUT /v1/custom-sources/{id}`. A remote provider requires `confirmEgress: true` (409
    `EGRESS_CONFIRMATION_REQUIRED`); samples from a private source are refused for remote providers
    (403); no provider → 409 `LLM_NOT_CONFIGURED`.

    **Scope:** `admin`.

    Args:
        body (MappingSuggestRequest): Request for an LLM-drafted mapping (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MappingSuggestResult | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: MappingSuggestRequest,
) -> Response[MappingSuggestResult | Problem]:
    """LLM-suggested draft RecordMapping

     Asks the chat provider (purpose `mapping-suggest`, 026 budget) for a draft mapping. The sample is
    untrusted data inside delimiters; the output is validated with `MappingCompiler` with up to 2 repair
    rounds, then previewed over the sample. The draft is never stored or applied: saving it is a
    separate `PUT /v1/custom-sources/{id}`. A remote provider requires `confirmEgress: true` (409
    `EGRESS_CONFIRMATION_REQUIRED`); samples from a private source are refused for remote providers
    (403); no provider → 409 `LLM_NOT_CONFIGURED`.

    **Scope:** `admin`.

    Args:
        body (MappingSuggestRequest): Request for an LLM-drafted mapping (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MappingSuggestResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: MappingSuggestRequest,
) -> MappingSuggestResult | Problem | None:
    """LLM-suggested draft RecordMapping

     Asks the chat provider (purpose `mapping-suggest`, 026 budget) for a draft mapping. The sample is
    untrusted data inside delimiters; the output is validated with `MappingCompiler` with up to 2 repair
    rounds, then previewed over the sample. The draft is never stored or applied: saving it is a
    separate `PUT /v1/custom-sources/{id}`. A remote provider requires `confirmEgress: true` (409
    `EGRESS_CONFIRMATION_REQUIRED`); samples from a private source are refused for remote providers
    (403); no provider → 409 `LLM_NOT_CONFIGURED`.

    **Scope:** `admin`.

    Args:
        body (MappingSuggestRequest): Request for an LLM-drafted mapping (059)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MappingSuggestResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
