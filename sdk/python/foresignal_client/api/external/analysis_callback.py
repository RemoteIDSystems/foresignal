from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analysis_callback import AnalysisCallback
from ...models.analysis_request_result import AnalysisRequestResult
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: AnalysisCallback,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/external/analysis/callback",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalysisRequestResult | Problem | None:
    if response.status_code == 200:
        response_200 = AnalysisRequestResult.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AnalysisRequestResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: AnalysisCallback,
) -> Response[AnalysisRequestResult | Problem]:
    """Ingest a correlated analysis result

     The callback a provider posts an asynchronous on-demand forecast to (155). Resolves the
    `correlationId` to its question and external forecaster and records the forecast through the usual
    binding path; decile output is mapped to the ledger's required quantiles. An unknown, already-
    answered or unusable result is accepted and ignored (`recorded: false`).

    **Scope:** open — the unguessable `correlationId` in the body is the credential (the provider
    receives it in the request and echoes it back), the same pattern as `register`.

    Args:
        body (AnalysisCallback): A provider's asynchronous answer to an on-demand analysis request
            (155).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalysisRequestResult | Problem]
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
    body: AnalysisCallback,
) -> AnalysisRequestResult | Problem | None:
    """Ingest a correlated analysis result

     The callback a provider posts an asynchronous on-demand forecast to (155). Resolves the
    `correlationId` to its question and external forecaster and records the forecast through the usual
    binding path; decile output is mapped to the ledger's required quantiles. An unknown, already-
    answered or unusable result is accepted and ignored (`recorded: false`).

    **Scope:** open — the unguessable `correlationId` in the body is the credential (the provider
    receives it in the request and echoes it back), the same pattern as `register`.

    Args:
        body (AnalysisCallback): A provider's asynchronous answer to an on-demand analysis request
            (155).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalysisRequestResult | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: AnalysisCallback,
) -> Response[AnalysisRequestResult | Problem]:
    """Ingest a correlated analysis result

     The callback a provider posts an asynchronous on-demand forecast to (155). Resolves the
    `correlationId` to its question and external forecaster and records the forecast through the usual
    binding path; decile output is mapped to the ledger's required quantiles. An unknown, already-
    answered or unusable result is accepted and ignored (`recorded: false`).

    **Scope:** open — the unguessable `correlationId` in the body is the credential (the provider
    receives it in the request and echoes it back), the same pattern as `register`.

    Args:
        body (AnalysisCallback): A provider's asynchronous answer to an on-demand analysis request
            (155).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalysisRequestResult | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: AnalysisCallback,
) -> AnalysisRequestResult | Problem | None:
    """Ingest a correlated analysis result

     The callback a provider posts an asynchronous on-demand forecast to (155). Resolves the
    `correlationId` to its question and external forecaster and records the forecast through the usual
    binding path; decile output is mapped to the ledger's required quantiles. An unknown, already-
    answered or unusable result is accepted and ignored (`recorded: false`).

    **Scope:** open — the unguessable `correlationId` in the body is the credential (the provider
    receives it in the request and echoes it back), the same pattern as `register`.

    Args:
        body (AnalysisCallback): A provider's asynchronous answer to an on-demand analysis request
            (155).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalysisRequestResult | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
