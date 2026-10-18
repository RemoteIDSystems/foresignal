from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.region_tags import RegionTags
from ...types import UNSET, Response


def _get_kwargs(
    *,
    lon: float,
    lat: float,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["lon"] = lon

    params["lat"] = lat

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/reference/reverse",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RegionTags | None:
    if response.status_code == 200:
        response_200 = RegionTags.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | RegionTags]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    lon: float,
    lat: float,
) -> Response[Problem | RegionTags]:
    """Reverse-geocode a point

     Country, admin1, US county, sea area and nearest place for a point; fields are null when reference
    data is missing (never fails).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        lon (float):
        lat (float):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RegionTags]
    """

    kwargs = _get_kwargs(
        lon=lon,
        lat=lat,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    lon: float,
    lat: float,
) -> Problem | RegionTags | None:
    """Reverse-geocode a point

     Country, admin1, US county, sea area and nearest place for a point; fields are null when reference
    data is missing (never fails).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        lon (float):
        lat (float):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RegionTags
    """

    return sync_detailed(
        client=client,
        lon=lon,
        lat=lat,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    lon: float,
    lat: float,
) -> Response[Problem | RegionTags]:
    """Reverse-geocode a point

     Country, admin1, US county, sea area and nearest place for a point; fields are null when reference
    data is missing (never fails).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        lon (float):
        lat (float):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RegionTags]
    """

    kwargs = _get_kwargs(
        lon=lon,
        lat=lat,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    lon: float,
    lat: float,
) -> Problem | RegionTags | None:
    """Reverse-geocode a point

     Country, admin1, US county, sea area and nearest place for a point; fields are null when reference
    data is missing (never fails).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        lon (float):
        lat (float):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RegionTags
    """

    return (
        await asyncio_detailed(
            client=client,
            lon=lon,
            lat=lat,
        )
    ).parsed
