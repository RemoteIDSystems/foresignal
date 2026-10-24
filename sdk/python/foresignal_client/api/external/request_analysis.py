from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analysis_request_result import AnalysisRequestResult
from ...models.analysis_request_submission import AnalysisRequestSubmission
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: AnalysisRequestSubmission,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/external/analysis/requests",
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
    body: AnalysisRequestSubmission,
) -> Response[AnalysisRequestResult | Problem]:
    """Request an on-demand forecast

     Asks a first-class external forecaster (a model like TimesFM) to forecast a ledger question now
    (155). Resolves the forecaster to its provider, which must declare `transport.analysisRequest`,
    persists a correlation and posts `{questionId, horizon, correlationId, callbackUrl}` to the
    provider. A provider that answers synchronously returns the forecast in its response and it is
    recorded at once (`recorded: true`); one that answers asynchronously posts the result later to
    `analysisCallback`.

    **Scope:** `agent`.

    Args:
        body (AnalysisRequestSubmission): Requests an on-demand forecast of a ledger question from
            an external forecaster (155).

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
    body: AnalysisRequestSubmission,
) -> AnalysisRequestResult | Problem | None:
    """Request an on-demand forecast

     Asks a first-class external forecaster (a model like TimesFM) to forecast a ledger question now
    (155). Resolves the forecaster to its provider, which must declare `transport.analysisRequest`,
    persists a correlation and posts `{questionId, horizon, correlationId, callbackUrl}` to the
    provider. A provider that answers synchronously returns the forecast in its response and it is
    recorded at once (`recorded: true`); one that answers asynchronously posts the result later to
    `analysisCallback`.

    **Scope:** `agent`.

    Args:
        body (AnalysisRequestSubmission): Requests an on-demand forecast of a ledger question from
            an external forecaster (155).

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
    body: AnalysisRequestSubmission,
) -> Response[AnalysisRequestResult | Problem]:
    """Request an on-demand forecast

     Asks a first-class external forecaster (a model like TimesFM) to forecast a ledger question now
    (155). Resolves the forecaster to its provider, which must declare `transport.analysisRequest`,
    persists a correlation and posts `{questionId, horizon, correlationId, callbackUrl}` to the
    provider. A provider that answers synchronously returns the forecast in its response and it is
    recorded at once (`recorded: true`); one that answers asynchronously posts the result later to
    `analysisCallback`.

    **Scope:** `agent`.

    Args:
        body (AnalysisRequestSubmission): Requests an on-demand forecast of a ledger question from
            an external forecaster (155).

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
    body: AnalysisRequestSubmission,
) -> AnalysisRequestResult | Problem | None:
    """Request an on-demand forecast

     Asks a first-class external forecaster (a model like TimesFM) to forecast a ledger question now
    (155). Resolves the forecaster to its provider, which must declare `transport.analysisRequest`,
    persists a correlation and posts `{questionId, horizon, correlationId, callbackUrl}` to the
    provider. A provider that answers synchronously returns the forecast in its response and it is
    recorded at once (`recorded: true`); one that answers asynchronously posts the result later to
    `analysisCallback`.

    **Scope:** `agent`.

    Args:
        body (AnalysisRequestSubmission): Requests an on-demand forecast of a ledger question from
            an external forecaster (155).

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
