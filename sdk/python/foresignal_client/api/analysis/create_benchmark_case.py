from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.benchmark_case import BenchmarkCase
from ...models.benchmark_case_input import BenchmarkCaseInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: BenchmarkCaseInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/benchmarks/cases",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BenchmarkCase | Problem | None:
    if response.status_code == 201:
        response_201 = BenchmarkCase.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[BenchmarkCase | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: BenchmarkCaseInput,
) -> Response[BenchmarkCase | Problem]:
    """Add a historical benchmark case

     Adds a known past episode with what the detectors should find; it runs in replay mode with `asOf`
    stepping from the window start.

    **Scope:** `agent`.

    Args:
        body (BenchmarkCaseInput): A new operator or agent historical case

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BenchmarkCase | Problem]
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
    body: BenchmarkCaseInput,
) -> BenchmarkCase | Problem | None:
    """Add a historical benchmark case

     Adds a known past episode with what the detectors should find; it runs in replay mode with `asOf`
    stepping from the window start.

    **Scope:** `agent`.

    Args:
        body (BenchmarkCaseInput): A new operator or agent historical case

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BenchmarkCase | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: BenchmarkCaseInput,
) -> Response[BenchmarkCase | Problem]:
    """Add a historical benchmark case

     Adds a known past episode with what the detectors should find; it runs in replay mode with `asOf`
    stepping from the window start.

    **Scope:** `agent`.

    Args:
        body (BenchmarkCaseInput): A new operator or agent historical case

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BenchmarkCase | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: BenchmarkCaseInput,
) -> BenchmarkCase | Problem | None:
    """Add a historical benchmark case

     Adds a known past episode with what the detectors should find; it runs in replay mode with `asOf`
    stepping from the window start.

    **Scope:** `agent`.

    Args:
        body (BenchmarkCaseInput): A new operator or agent historical case

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BenchmarkCase | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
