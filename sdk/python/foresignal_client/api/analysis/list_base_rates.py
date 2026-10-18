from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.base_rate_page import BaseRatePage
from ...models.list_base_rates_window import ListBaseRatesWindow
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    window: ListBaseRatesWindow | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_family: list[str] | Unset = UNSET
    if not isinstance(family, Unset):
        json_family = family

    params["family"] = json_family

    json_region: list[str] | Unset = UNSET
    if not isinstance(region, Unset):
        json_region = region

    params["region"] = json_region

    json_window: str | Unset = UNSET
    if not isinstance(window, Unset):
        json_window = window.value

    params["window"] = json_window

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/base-rates",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BaseRatePage | Problem | None:
    if response.status_code == 200:
        response_200 = BaseRatePage.from_dict(response.json())

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
) -> Response[BaseRatePage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    window: ListBaseRatesWindow | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[BaseRatePage | Problem]:
    """List base rates

     Lists stored base rates (automatic and custom), each with reference class, period, n, interval,
    sources and caveats.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (list[str] | Unset):
        region (list[str] | Unset):
        window (ListBaseRatesWindow | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BaseRatePage | Problem]
    """

    kwargs = _get_kwargs(
        family=family,
        region=region,
        window=window,
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
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    window: ListBaseRatesWindow | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> BaseRatePage | Problem | None:
    """List base rates

     Lists stored base rates (automatic and custom), each with reference class, period, n, interval,
    sources and caveats.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (list[str] | Unset):
        region (list[str] | Unset):
        window (ListBaseRatesWindow | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BaseRatePage | Problem
    """

    return sync_detailed(
        client=client,
        family=family,
        region=region,
        window=window,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    window: ListBaseRatesWindow | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[BaseRatePage | Problem]:
    """List base rates

     Lists stored base rates (automatic and custom), each with reference class, period, n, interval,
    sources and caveats.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (list[str] | Unset):
        region (list[str] | Unset):
        window (ListBaseRatesWindow | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BaseRatePage | Problem]
    """

    kwargs = _get_kwargs(
        family=family,
        region=region,
        window=window,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    family: list[str] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    window: ListBaseRatesWindow | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> BaseRatePage | Problem | None:
    """List base rates

     Lists stored base rates (automatic and custom), each with reference class, period, n, interval,
    sources and caveats.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        family (list[str] | Unset):
        region (list[str] | Unset):
        window (ListBaseRatesWindow | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BaseRatePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            family=family,
            region=region,
            window=window,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
