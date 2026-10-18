from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.backtest_job import BacktestJob
from ...models.precursor_test_run_request import PrecursorTestRunRequest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    precursor_test_id: str,
    *,
    body: PrecursorTestRunRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/precursor-tests/{precursor_test_id}/test".format(
            precursor_test_id=quote(str(precursor_test_id), safe=""),
        ),
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
    precursor_test_id: str,
    *,
    client: AuthenticatedClient,
    body: PrecursorTestRunRequest,
) -> Response[BacktestJob | Problem]:
    """Run a precursor test

     Submits a `precursor` backtest job (PrecursorTester 1.0.0: Wilson interval, 200 circular shifts,
    lift, permutation p-value, reverse test).

    **Scope:** `agent` or `admin`.

    Args:
        precursor_test_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (PrecursorTestRunRequest): Range for a precursor test job: from/to or fromSeq/toSeq

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BacktestJob | Problem]
    """

    kwargs = _get_kwargs(
        precursor_test_id=precursor_test_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    precursor_test_id: str,
    *,
    client: AuthenticatedClient,
    body: PrecursorTestRunRequest,
) -> BacktestJob | Problem | None:
    """Run a precursor test

     Submits a `precursor` backtest job (PrecursorTester 1.0.0: Wilson interval, 200 circular shifts,
    lift, permutation p-value, reverse test).

    **Scope:** `agent` or `admin`.

    Args:
        precursor_test_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (PrecursorTestRunRequest): Range for a precursor test job: from/to or fromSeq/toSeq

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BacktestJob | Problem
    """

    return sync_detailed(
        precursor_test_id=precursor_test_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    precursor_test_id: str,
    *,
    client: AuthenticatedClient,
    body: PrecursorTestRunRequest,
) -> Response[BacktestJob | Problem]:
    """Run a precursor test

     Submits a `precursor` backtest job (PrecursorTester 1.0.0: Wilson interval, 200 circular shifts,
    lift, permutation p-value, reverse test).

    **Scope:** `agent` or `admin`.

    Args:
        precursor_test_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (PrecursorTestRunRequest): Range for a precursor test job: from/to or fromSeq/toSeq

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BacktestJob | Problem]
    """

    kwargs = _get_kwargs(
        precursor_test_id=precursor_test_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    precursor_test_id: str,
    *,
    client: AuthenticatedClient,
    body: PrecursorTestRunRequest,
) -> BacktestJob | Problem | None:
    """Run a precursor test

     Submits a `precursor` backtest job (PrecursorTester 1.0.0: Wilson interval, 200 circular shifts,
    lift, permutation p-value, reverse test).

    **Scope:** `agent` or `admin`.

    Args:
        precursor_test_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (PrecursorTestRunRequest): Range for a precursor test job: from/to or fromSeq/toSeq

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BacktestJob | Problem
    """

    return (
        await asyncio_detailed(
            precursor_test_id=precursor_test_id,
            client=client,
            body=body,
        )
    ).parsed
