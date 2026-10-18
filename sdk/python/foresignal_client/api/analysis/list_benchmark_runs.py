from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.benchmark_run_page import BenchmarkRunPage
from ...models.list_benchmark_runs_status import ListBenchmarkRunsStatus
from ...models.list_benchmark_runs_suite import ListBenchmarkRunsSuite
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    suite: ListBenchmarkRunsSuite | Unset = UNSET,
    status: ListBenchmarkRunsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_suite: str | Unset = UNSET
    if not isinstance(suite, Unset):
        json_suite = suite.value

    params["suite"] = json_suite

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/benchmarks/runs",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BenchmarkRunPage | Problem | None:
    if response.status_code == 200:
        response_200 = BenchmarkRunPage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[BenchmarkRunPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    suite: ListBenchmarkRunsSuite | Unset = UNSET,
    status: ListBenchmarkRunsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[BenchmarkRunPage | Problem]:
    """List benchmark runs

     Benchmark runs newest first, with per-component results.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        suite (ListBenchmarkRunsSuite | Unset):
        status (ListBenchmarkRunsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BenchmarkRunPage | Problem]
    """

    kwargs = _get_kwargs(
        suite=suite,
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    suite: ListBenchmarkRunsSuite | Unset = UNSET,
    status: ListBenchmarkRunsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> BenchmarkRunPage | Problem | None:
    """List benchmark runs

     Benchmark runs newest first, with per-component results.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        suite (ListBenchmarkRunsSuite | Unset):
        status (ListBenchmarkRunsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BenchmarkRunPage | Problem
    """

    return sync_detailed(
        client=client,
        suite=suite,
        status=status,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    suite: ListBenchmarkRunsSuite | Unset = UNSET,
    status: ListBenchmarkRunsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[BenchmarkRunPage | Problem]:
    """List benchmark runs

     Benchmark runs newest first, with per-component results.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        suite (ListBenchmarkRunsSuite | Unset):
        status (ListBenchmarkRunsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BenchmarkRunPage | Problem]
    """

    kwargs = _get_kwargs(
        suite=suite,
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    suite: ListBenchmarkRunsSuite | Unset = UNSET,
    status: ListBenchmarkRunsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> BenchmarkRunPage | Problem | None:
    """List benchmark runs

     Benchmark runs newest first, with per-component results.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        suite (ListBenchmarkRunsSuite | Unset):
        status (ListBenchmarkRunsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BenchmarkRunPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            suite=suite,
            status=status,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
