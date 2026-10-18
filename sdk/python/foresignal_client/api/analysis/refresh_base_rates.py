from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.backtest_job import BacktestJob
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/base-rates/refresh",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BacktestJob | Problem | None:
    if response.status_code == 202:
        response_202 = BacktestJob.from_dict(response.json())

        return response_202

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

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
) -> Response[BacktestJob | Problem]:
    """Refresh automatic base rates

     Recomputes automatic rates now (every family × region with ≥ 52 weeks of coverage, windows P7D and
    P28D), as the monthly cycle does, and recomputes region profiles.

    **Scope:** `agent` or `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BacktestJob | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> BacktestJob | Problem | None:
    """Refresh automatic base rates

     Recomputes automatic rates now (every family × region with ≥ 52 weeks of coverage, windows P7D and
    P28D), as the monthly cycle does, and recomputes region profiles.

    **Scope:** `agent` or `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BacktestJob | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[BacktestJob | Problem]:
    """Refresh automatic base rates

     Recomputes automatic rates now (every family × region with ≥ 52 weeks of coverage, windows P7D and
    P28D), as the monthly cycle does, and recomputes region profiles.

    **Scope:** `agent` or `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BacktestJob | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> BacktestJob | Problem | None:
    """Refresh automatic base rates

     Recomputes automatic rates now (every family × region with ≥ 52 weeks of coverage, windows P7D and
    P28D), as the monthly cycle does, and recomputes region profiles.

    **Scope:** `agent` or `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BacktestJob | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
