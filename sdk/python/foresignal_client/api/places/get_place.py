from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.place import Place
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    place_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/places/{place_id}".format(
            place_id=quote(str(place_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Place | Problem | None:
    if response.status_code == 200:
        response_200 = Place.from_dict(response.json())

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
) -> Response[Place | Problem]:
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
) -> Response[Place | Problem]:
    """Get a place

     404 UNKNOWN_PLACE. Places are sensitive: reads need a token even when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        place_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Place | Problem]
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
) -> Place | Problem | None:
    """Get a place

     404 UNKNOWN_PLACE. Places are sensitive: reads need a token even when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        place_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Place | Problem
    """

    return sync_detailed(
        place_id=place_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    place_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Place | Problem]:
    """Get a place

     404 UNKNOWN_PLACE. Places are sensitive: reads need a token even when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        place_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Place | Problem]
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
) -> Place | Problem | None:
    """Get a place

     404 UNKNOWN_PLACE. Places are sensitive: reads need a token even when `openReads` is on.

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).

    Args:
        place_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Place | Problem
    """

    return (
        await asyncio_detailed(
            place_id=place_id,
            client=client,
        )
    ).parsed
