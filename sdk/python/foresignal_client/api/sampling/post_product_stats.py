from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.stats_response import StatsResponse
from ...types import Response


def _get_kwargs(
    product_id: str,
    *,
    body: Any,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/products/{product_id}/stats".format(
            product_id=quote(str(product_id), safe=""),
        ),
    }

    _kwargs["json"] = body

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | StatsResponse | None:
    if response.status_code == 200:
        response_200 = StatsResponse.from_dict(response.json())

        return response_200

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | StatsResponse]:
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
    body: Any,
) -> Response[Problem | StatsResponse]:
    """Compute area statistics

     min, max, mean, p95, coverage and the area above each threshold (spherical cell areas per grid type)
    over a geometry, place or bbox. Geometry ≤ 10,000 vertices and area ≤ 2,000,000 km² (400
    VALIDATION_FAILED beyond). Thresholds are given in the requested unit system. A cell belongs to the
    area when its center lies inside; `coveragePct` is the share of those cells holding a value. A non-
    ok status (unavailable, no-coverage, stale) carries no statistics (`min`…`p95` null, `thresholds`
    empty).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        body (Any): Area statistics request: exactly one of geometry, place or bbox

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsResponse]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Problem | StatsResponse | None:
    """Compute area statistics

     min, max, mean, p95, coverage and the area above each threshold (spherical cell areas per grid type)
    over a geometry, place or bbox. Geometry ≤ 10,000 vertices and area ≤ 2,000,000 km² (400
    VALIDATION_FAILED beyond). Thresholds are given in the requested unit system. A cell belongs to the
    area when its center lies inside; `coveragePct` is the share of those cells holding a value. A non-
    ok status (unavailable, no-coverage, stale) carries no statistics (`min`…`p95` null, `thresholds`
    empty).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        body (Any): Area statistics request: exactly one of geometry, place or bbox

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsResponse
    """

    return sync_detailed(
        product_id=product_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Response[Problem | StatsResponse]:
    """Compute area statistics

     min, max, mean, p95, coverage and the area above each threshold (spherical cell areas per grid type)
    over a geometry, place or bbox. Geometry ≤ 10,000 vertices and area ≤ 2,000,000 km² (400
    VALIDATION_FAILED beyond). Thresholds are given in the requested unit system. A cell belongs to the
    area when its center lies inside; `coveragePct` is the share of those cells holding a value. A non-
    ok status (unavailable, no-coverage, stale) carries no statistics (`min`…`p95` null, `thresholds`
    empty).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        body (Any): Area statistics request: exactly one of geometry, place or bbox

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StatsResponse]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    *,
    client: AuthenticatedClient,
    body: Any,
) -> Problem | StatsResponse | None:
    """Compute area statistics

     min, max, mean, p95, coverage and the area above each threshold (spherical cell areas per grid type)
    over a geometry, place or bbox. Geometry ≤ 10,000 vertices and area ≤ 2,000,000 km² (400
    VALIDATION_FAILED beyond). Thresholds are given in the requested unit system. A cell belongs to the
    area when its center lies inside; `coveragePct` is the share of those cells holding a value. A non-
    ok status (unavailable, no-coverage, stale) carries no statistics (`min`…`p95` null, `thresholds`
    empty).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        body (Any): Area statistics request: exactly one of geometry, place or bbox

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StatsResponse
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            client=client,
            body=body,
        )
    ).parsed
