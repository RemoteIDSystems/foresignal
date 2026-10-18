from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.raster_frame import RasterFrame
from ...types import UNSET, Response, Unset


def _get_kwargs(
    product_id: str,
    *,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    params["run"] = run

    params["includePartial"] = include_partial

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/frames/latest".format(
            product_id=quote(str(product_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | RasterFrame | None:
    if response.status_code == 200:
        response_200 = RasterFrame.from_dict(response.json())

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem | RasterFrame]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | RasterFrame]:
    """Get the latest frame

     The newest complete observed frame, or for forecasts the newest complete run's step nearest now.
    Short max-age (`min(cadence/2, 60s)`) with an ETag; `public` only when `openReads` covers the path.
    404 UNKNOWN_FRAME when no frame exists yet.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        run (str | Unset):  Example: latest.
        include_partial (bool | Unset):  Default: False.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | RasterFrame]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        run=run,
        include_partial=include_partial,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | RasterFrame | None:
    """Get the latest frame

     The newest complete observed frame, or for forecasts the newest complete run's step nearest now.
    Short max-age (`min(cadence/2, 60s)`) with an ETag; `public` only when `openReads` covers the path.
    404 UNKNOWN_FRAME when no frame exists yet.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        run (str | Unset):  Example: latest.
        include_partial (bool | Unset):  Default: False.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | RasterFrame
    """

    return sync_detailed(
        product_id=product_id,
        client=client,
        run=run,
        include_partial=include_partial,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | Problem | RasterFrame]:
    """Get the latest frame

     The newest complete observed frame, or for forecasts the newest complete run's step nearest now.
    Short max-age (`min(cadence/2, 60s)`) with an ETag; `public` only when `openReads` covers the path.
    404 UNKNOWN_FRAME when no frame exists yet.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        run (str | Unset):  Example: latest.
        include_partial (bool | Unset):  Default: False.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem | RasterFrame]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        run=run,
        include_partial=include_partial,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    include_partial: bool | Unset = False,
    if_none_match: str | Unset = UNSET,
) -> Any | Problem | RasterFrame | None:
    """Get the latest frame

     The newest complete observed frame, or for forecasts the newest complete run's step nearest now.
    Short max-age (`min(cadence/2, 60s)`) with an ETag; `public` only when `openReads` covers the path.
    404 UNKNOWN_FRAME when no frame exists yet.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        run (str | Unset):  Example: latest.
        include_partial (bool | Unset):  Default: False.
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem | RasterFrame
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            client=client,
            run=run,
            include_partial=include_partial,
            if_none_match=if_none_match,
        )
    ).parsed
