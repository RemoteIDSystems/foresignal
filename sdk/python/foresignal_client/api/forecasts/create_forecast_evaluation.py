from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecast_evaluation import ForecastEvaluation
from ...models.forecast_evaluation_request import ForecastEvaluationRequest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ForecastEvaluationRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/forecasts/evaluations",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecastEvaluation | Problem | None:
    if response.status_code == 202:
        response_202 = ForecastEvaluation.from_dict(response.json())

        return response_202

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ForecastEvaluation | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ForecastEvaluationRequest,
) -> Response[ForecastEvaluation | Problem]:
    """Start a retrospective evaluation

     Runs a forecaster as of each resolved question's `opensAt` (readers forced to `asOf`) as a 103 job
    `forecast-evaluation` and scores it into a `retrospective` scorecard. LLM forecasters skip questions
    opened before the provider's `knowledgeCutoff` (`LEAKAGE_RISK`); without a cutoff they are refused
    with 409 `KNOWLEDGE_CUTOFF_REQUIRED`.

    **Scope:** `agent` or `admin`.

    Args:
        body (ForecastEvaluationRequest): Retrospective evaluation of one forecaster over past
            resolved questions

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastEvaluation | Problem]
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
    body: ForecastEvaluationRequest,
) -> ForecastEvaluation | Problem | None:
    """Start a retrospective evaluation

     Runs a forecaster as of each resolved question's `opensAt` (readers forced to `asOf`) as a 103 job
    `forecast-evaluation` and scores it into a `retrospective` scorecard. LLM forecasters skip questions
    opened before the provider's `knowledgeCutoff` (`LEAKAGE_RISK`); without a cutoff they are refused
    with 409 `KNOWLEDGE_CUTOFF_REQUIRED`.

    **Scope:** `agent` or `admin`.

    Args:
        body (ForecastEvaluationRequest): Retrospective evaluation of one forecaster over past
            resolved questions

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastEvaluation | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ForecastEvaluationRequest,
) -> Response[ForecastEvaluation | Problem]:
    """Start a retrospective evaluation

     Runs a forecaster as of each resolved question's `opensAt` (readers forced to `asOf`) as a 103 job
    `forecast-evaluation` and scores it into a `retrospective` scorecard. LLM forecasters skip questions
    opened before the provider's `knowledgeCutoff` (`LEAKAGE_RISK`); without a cutoff they are refused
    with 409 `KNOWLEDGE_CUTOFF_REQUIRED`.

    **Scope:** `agent` or `admin`.

    Args:
        body (ForecastEvaluationRequest): Retrospective evaluation of one forecaster over past
            resolved questions

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastEvaluation | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ForecastEvaluationRequest,
) -> ForecastEvaluation | Problem | None:
    """Start a retrospective evaluation

     Runs a forecaster as of each resolved question's `opensAt` (readers forced to `asOf`) as a 103 job
    `forecast-evaluation` and scores it into a `retrospective` scorecard. LLM forecasters skip questions
    opened before the provider's `knowledgeCutoff` (`LEAKAGE_RISK`); without a cutoff they are refused
    with 409 `KNOWLEDGE_CUTOFF_REQUIRED`.

    **Scope:** `agent` or `admin`.

    Args:
        body (ForecastEvaluationRequest): Retrospective evaluation of one forecaster over past
            resolved questions

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastEvaluation | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
