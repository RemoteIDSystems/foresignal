from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.trip import Trip
from ...types import Response


def _get_kwargs(
    trip_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/trips/{trip_id}".format(
            trip_id=quote(str(trip_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Trip | None:
    if response.status_code == 200:
        response_200 = Trip.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | Trip]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    trip_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | Trip]:
    """Get a trip

     A trip with its corridor and next occurrence.

    **Scope:** `agent`.

    Args:
        trip_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Trip]
    """

    kwargs = _get_kwargs(
        trip_id=trip_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    trip_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | Trip | None:
    """Get a trip

     A trip with its corridor and next occurrence.

    **Scope:** `agent`.

    Args:
        trip_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Trip
    """

    return sync_detailed(
        trip_id=trip_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    trip_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | Trip]:
    """Get a trip

     A trip with its corridor and next occurrence.

    **Scope:** `agent`.

    Args:
        trip_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Trip]
    """

    kwargs = _get_kwargs(
        trip_id=trip_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    trip_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | Trip | None:
    """Get a trip

     A trip with its corridor and next occurrence.

    **Scope:** `agent`.

    Args:
        trip_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Trip
    """

    return (
        await asyncio_detailed(
            trip_id=trip_id,
            client=client,
        )
    ).parsed
