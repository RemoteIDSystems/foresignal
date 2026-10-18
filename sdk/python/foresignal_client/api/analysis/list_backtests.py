from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.backtest_job_page import BacktestJobPage
from ...models.list_backtests_status import ListBacktestsStatus
from ...models.list_backtests_type import ListBacktestsType
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: ListBacktestsStatus | Unset = UNSET,
    type_: ListBacktestsType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    json_type_: str | Unset = UNSET
    if not isinstance(type_, Unset):
        json_type_ = type_.value

    params["type"] = json_type_

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/backtests",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BacktestJobPage | Problem | None:
    if response.status_code == 200:
        response_200 = BacktestJobPage.from_dict(response.json())

        return response_200

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
) -> Response[BacktestJobPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: ListBacktestsStatus | Unset = UNSET,
    type_: ListBacktestsType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[BacktestJobPage | Problem]:
    """List backtest jobs

     Finished jobs are kept for 30 days.

    **Scope:** `agent` or `admin`.

    Args:
        status (ListBacktestsStatus | Unset):
        type_ (ListBacktestsType | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BacktestJobPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        type_=type_,
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
    status: ListBacktestsStatus | Unset = UNSET,
    type_: ListBacktestsType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> BacktestJobPage | Problem | None:
    """List backtest jobs

     Finished jobs are kept for 30 days.

    **Scope:** `agent` or `admin`.

    Args:
        status (ListBacktestsStatus | Unset):
        type_ (ListBacktestsType | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BacktestJobPage | Problem
    """

    return sync_detailed(
        client=client,
        status=status,
        type_=type_,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: ListBacktestsStatus | Unset = UNSET,
    type_: ListBacktestsType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[BacktestJobPage | Problem]:
    """List backtest jobs

     Finished jobs are kept for 30 days.

    **Scope:** `agent` or `admin`.

    Args:
        status (ListBacktestsStatus | Unset):
        type_ (ListBacktestsType | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BacktestJobPage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        type_=type_,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: ListBacktestsStatus | Unset = UNSET,
    type_: ListBacktestsType | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> BacktestJobPage | Problem | None:
    """List backtest jobs

     Finished jobs are kept for 30 days.

    **Scope:** `agent` or `admin`.

    Args:
        status (ListBacktestsStatus | Unset):
        type_ (ListBacktestsType | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BacktestJobPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            type_=type_,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
