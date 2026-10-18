from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.places_aoi import PlacesAoi
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/places/aoi",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PlacesAoi | Problem | None:
    if response.status_code == 200:
        response_200 = PlacesAoi.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[PlacesAoi | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[PlacesAoi | Problem]:
    """Get the places area of interest

     Union of place areas (each buffered to at least max(radiusKm, weather.aoiBufferKm)), its envelope
    and version; the default AOI when there are no places. Places are sensitive: reads need a token even
    when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PlacesAoi | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> PlacesAoi | Problem | None:
    """Get the places area of interest

     Union of place areas (each buffered to at least max(radiusKm, weather.aoiBufferKm)), its envelope
    and version; the default AOI when there are no places. Places are sensitive: reads need a token even
    when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PlacesAoi | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[PlacesAoi | Problem]:
    """Get the places area of interest

     Union of place areas (each buffered to at least max(radiusKm, weather.aoiBufferKm)), its envelope
    and version; the default AOI when there are no places. Places are sensitive: reads need a token even
    when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PlacesAoi | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> PlacesAoi | Problem | None:
    """Get the places area of interest

     Union of place areas (each buffered to at least max(radiusKm, weather.aoiBufferKm)), its envelope
    and version; the default AOI when there are no places. Places are sensitive: reads need a token even
    when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PlacesAoi | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
