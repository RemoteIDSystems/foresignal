from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.backtest_job import BacktestJob
from ...models.event_study_request import EventStudyRequest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: EventStudyRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/analytics/event-studies",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BacktestJob | Problem | None:
    if response.status_code == 202:
        response_202 = BacktestJob.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[BacktestJob | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: EventStudyRequest,
) -> Response[BacktestJob | Problem]:
    """Run a batch event study

     Submits a 103 job of type `event-study` (asOf replay, no look-ahead); the job's `result` is an
    `EventStudyResult`, read with `GET /v1/backtests/{id}`. At most one queued or running job per token
    (409 `BACKTEST_IN_PROGRESS`).

    **Scope:** `agent` or `admin`.

    Args:
        body (EventStudyRequest): Batch event study: events from a selector against one target
            series (103 job `event-study`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BacktestJob | Problem]
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
    body: EventStudyRequest,
) -> BacktestJob | Problem | None:
    """Run a batch event study

     Submits a 103 job of type `event-study` (asOf replay, no look-ahead); the job's `result` is an
    `EventStudyResult`, read with `GET /v1/backtests/{id}`. At most one queued or running job per token
    (409 `BACKTEST_IN_PROGRESS`).

    **Scope:** `agent` or `admin`.

    Args:
        body (EventStudyRequest): Batch event study: events from a selector against one target
            series (103 job `event-study`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BacktestJob | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: EventStudyRequest,
) -> Response[BacktestJob | Problem]:
    """Run a batch event study

     Submits a 103 job of type `event-study` (asOf replay, no look-ahead); the job's `result` is an
    `EventStudyResult`, read with `GET /v1/backtests/{id}`. At most one queued or running job per token
    (409 `BACKTEST_IN_PROGRESS`).

    **Scope:** `agent` or `admin`.

    Args:
        body (EventStudyRequest): Batch event study: events from a selector against one target
            series (103 job `event-study`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BacktestJob | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: EventStudyRequest,
) -> BacktestJob | Problem | None:
    """Run a batch event study

     Submits a 103 job of type `event-study` (asOf replay, no look-ahead); the job's `result` is an
    `EventStudyResult`, read with `GET /v1/backtests/{id}`. At most one queued or running job per token
    (409 `BACKTEST_IN_PROGRESS`).

    **Scope:** `agent` or `admin`.

    Args:
        body (EventStudyRequest): Batch event study: events from a selector against one target
            series (103 job `event-study`)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BacktestJob | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
