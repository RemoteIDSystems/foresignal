from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.intel_board import IntelBoard
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/intel/board",
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | IntelBoard | Problem | None:
    if response.status_code == 200:
        response_200 = IntelBoard.from_dict(response.json())

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

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
) -> Response[Any | IntelBoard | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | IntelBoard | Problem]:
    """Get the intel board

     Top situations, band changes and observed indicators in the last 24 h, forecasts due in 7 days, the
    latest assessment per cadence, 90-day calibration (ensemble next to the operator) and benchmark
    status, in ≤ 8 KB. Respects `private` and LOCAL_ONLY.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | IntelBoard | Problem]
    """

    kwargs = _get_kwargs(
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Any | IntelBoard | Problem | None:
    """Get the intel board

     Top situations, band changes and observed indicators in the last 24 h, forecasts due in 7 days, the
    latest assessment per cadence, 90-day calibration (ensemble next to the operator) and benchmark
    status, in ≤ 8 KB. Respects `private` and LOCAL_ONLY.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | IntelBoard | Problem
    """

    return sync_detailed(
        client=client,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | IntelBoard | Problem]:
    """Get the intel board

     Top situations, band changes and observed indicators in the last 24 h, forecasts due in 7 days, the
    latest assessment per cadence, 90-day calibration (ensemble next to the operator) and benchmark
    status, in ≤ 8 KB. Respects `private` and LOCAL_ONLY.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | IntelBoard | Problem]
    """

    kwargs = _get_kwargs(
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    if_none_match: str | Unset = UNSET,
) -> Any | IntelBoard | Problem | None:
    """Get the intel board

     Top situations, band changes and observed indicators in the last 24 h, forecasts due in 7 days, the
    latest assessment per cadence, 90-day calibration (ensemble next to the operator) and benchmark
    status, in ≤ 8 KB. Respects `private` and LOCAL_ONLY.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | IntelBoard | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            if_none_match=if_none_match,
        )
    ).parsed
