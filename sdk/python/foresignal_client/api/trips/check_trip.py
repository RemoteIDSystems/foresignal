from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.trip_check_request import TripCheckRequest
from ...models.trip_run import TripRun
from ...types import UNSET, Response, Unset


def _get_kwargs(
    trip_id: str,
    *,
    body: TripCheckRequest | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/trips/{trip_id}/check".format(
            trip_id=quote(str(trip_id), safe=""),
        ),
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TripRun | None:
    if response.status_code == 200:
        response_200 = TripRun.from_dict(response.json())

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
) -> Response[Problem | TripRun]:
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
    body: TripCheckRequest | Unset = UNSET,
) -> Response[Problem | TripRun]:
    """Run the pre-trip check now

     Finds hazards on and near the corridor (and forecast hazards whose validFrom falls in the trip
    window), ordered along the route, renders the trip briefing and delivers it, or returns it without
    delivering when `dryRun=true`.

    **Scope:** `agent`.

    Args:
        trip_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (TripCheckRequest | Unset): Run the pre-trip check now (114)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TripRun]
    """

    kwargs = _get_kwargs(
        trip_id=trip_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    trip_id: str,
    *,
    client: AuthenticatedClient,
    body: TripCheckRequest | Unset = UNSET,
) -> Problem | TripRun | None:
    """Run the pre-trip check now

     Finds hazards on and near the corridor (and forecast hazards whose validFrom falls in the trip
    window), ordered along the route, renders the trip briefing and delivers it, or returns it without
    delivering when `dryRun=true`.

    **Scope:** `agent`.

    Args:
        trip_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (TripCheckRequest | Unset): Run the pre-trip check now (114)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TripRun
    """

    return sync_detailed(
        trip_id=trip_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    trip_id: str,
    *,
    client: AuthenticatedClient,
    body: TripCheckRequest | Unset = UNSET,
) -> Response[Problem | TripRun]:
    """Run the pre-trip check now

     Finds hazards on and near the corridor (and forecast hazards whose validFrom falls in the trip
    window), ordered along the route, renders the trip briefing and delivers it, or returns it without
    delivering when `dryRun=true`.

    **Scope:** `agent`.

    Args:
        trip_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (TripCheckRequest | Unset): Run the pre-trip check now (114)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TripRun]
    """

    kwargs = _get_kwargs(
        trip_id=trip_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    trip_id: str,
    *,
    client: AuthenticatedClient,
    body: TripCheckRequest | Unset = UNSET,
) -> Problem | TripRun | None:
    """Run the pre-trip check now

     Finds hazards on and near the corridor (and forecast hazards whose validFrom falls in the trip
    window), ordered along the route, renders the trip briefing and delivers it, or returns it without
    delivering when `dryRun=true`.

    **Scope:** `agent`.

    Args:
        trip_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (TripCheckRequest | Unset): Run the pre-trip check now (114)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TripRun
    """

    return (
        await asyncio_detailed(
            trip_id=trip_id,
            client=client,
            body=body,
        )
    ).parsed
