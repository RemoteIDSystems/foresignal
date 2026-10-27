from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.place_weather_response import PlaceWeatherResponse
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    place_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/places/{place_id}/weather".format(
            place_id=quote(str(place_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> PlaceWeatherResponse | Problem | None:
    if response.status_code == 200:
        response_200 = PlaceWeatherResponse.from_dict(response.json())

        return response_200

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
) -> Response[PlaceWeatherResponse | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    place_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[PlaceWeatherResponse | Problem]:
    """Get weather at a place

     The `place-weather` record (wx-places head) plus cached forecast series (GFS/HRRR, NWS from 053).
    Sections that are stale, failing or without coverage keep their previous values with a non-ok
    status; unknown is never clear.

    **Scope:** `read` (always authenticated).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        place_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PlaceWeatherResponse | Problem]
    """

    kwargs = _get_kwargs(
        place_id=place_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    place_id: str,
    *,
    client: AuthenticatedClient,
) -> PlaceWeatherResponse | Problem | None:
    """Get weather at a place

     The `place-weather` record (wx-places head) plus cached forecast series (GFS/HRRR, NWS from 053).
    Sections that are stale, failing or without coverage keep their previous values with a non-ok
    status; unknown is never clear.

    **Scope:** `read` (always authenticated).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        place_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PlaceWeatherResponse | Problem
    """

    return sync_detailed(
        place_id=place_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    place_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[PlaceWeatherResponse | Problem]:
    """Get weather at a place

     The `place-weather` record (wx-places head) plus cached forecast series (GFS/HRRR, NWS from 053).
    Sections that are stale, failing or without coverage keep their previous values with a non-ok
    status; unknown is never clear.

    **Scope:** `read` (always authenticated).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        place_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[PlaceWeatherResponse | Problem]
    """

    kwargs = _get_kwargs(
        place_id=place_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    place_id: str,
    *,
    client: AuthenticatedClient,
) -> PlaceWeatherResponse | Problem | None:
    """Get weather at a place

     The `place-weather` record (wx-places head) plus cached forecast series (GFS/HRRR, NWS from 053).
    Sections that are stale, failing or without coverage keep their previous values with a non-ok
    status; unknown is never clear.

    **Scope:** `read` (always authenticated).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        place_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        PlaceWeatherResponse | Problem
    """

    return (
        await asyncio_detailed(
            place_id=place_id,
            client=client,
        )
    ).parsed
