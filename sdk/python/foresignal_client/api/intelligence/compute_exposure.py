from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.exposure import Exposure
from ...models.exposure_request import ExposureRequest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ExposureRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/exposure",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Exposure | Problem | None:
    if response.status_code == 200:
        response_200 = Exposure.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Exposure | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ExposureRequest,
) -> Response[Exposure | Problem]:
    """Compute exposure of an area

     Facilities, places, watchlist entities, live traffic, optional population (GHSL pack) and
    infrastructure dependents inside a geometry, place or event area. Without a pack the population
    section is `not-installed`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        body (ExposureRequest): Exposure query: exactly one of geometry, placeId or eventId

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Exposure | Problem]
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
    body: ExposureRequest,
) -> Exposure | Problem | None:
    """Compute exposure of an area

     Facilities, places, watchlist entities, live traffic, optional population (GHSL pack) and
    infrastructure dependents inside a geometry, place or event area. Without a pack the population
    section is `not-installed`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        body (ExposureRequest): Exposure query: exactly one of geometry, placeId or eventId

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Exposure | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ExposureRequest,
) -> Response[Exposure | Problem]:
    """Compute exposure of an area

     Facilities, places, watchlist entities, live traffic, optional population (GHSL pack) and
    infrastructure dependents inside a geometry, place or event area. Without a pack the population
    section is `not-installed`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        body (ExposureRequest): Exposure query: exactly one of geometry, placeId or eventId

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Exposure | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ExposureRequest,
) -> Exposure | Problem | None:
    """Compute exposure of an area

     Facilities, places, watchlist entities, live traffic, optional population (GHSL pack) and
    infrastructure dependents inside a geometry, place or event area. Without a pack the population
    section is `not-installed`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        body (ExposureRequest): Exposure query: exactly one of geometry, placeId or eventId

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Exposure | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
