from http import HTTPStatus
from io import BytesIO
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    view_id: str,
    z: int,
    x: int,
    y: int,
    *,
    ticket: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["ticket"] = ticket

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/views/{view_id}/tiles/{z}/{x}/{y}.mvt".format(
            view_id=quote(str(view_id), safe=""),
            z=quote(str(z), safe=""),
            x=quote(str(x), safe=""),
            y=quote(str(y), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> File | Problem | None:
    if response.status_code == 200:
        response_200 = File(payload=BytesIO(response.content))

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
) -> Response[File | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    view_id: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
) -> Response[File | Problem]:
    """Vector tile of a saved view

     Mapbox Vector Tile of the view's current records (016 properties); the cache key includes the view
    `version`. View data follows normal read rules unless the view references a place or watchlist, in
    which case a token is required (401).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        view_id (str):
        z (int):
        x (int):
        y (int):
        ticket (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        view_id=view_id,
        z=z,
        x=x,
        y=y,
        ticket=ticket,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    view_id: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
) -> File | Problem | None:
    """Vector tile of a saved view

     Mapbox Vector Tile of the view's current records (016 properties); the cache key includes the view
    `version`. View data follows normal read rules unless the view references a place or watchlist, in
    which case a token is required (401).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        view_id (str):
        z (int):
        x (int):
        y (int):
        ticket (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return sync_detailed(
        view_id=view_id,
        z=z,
        x=x,
        y=y,
        client=client,
        ticket=ticket,
    ).parsed


async def asyncio_detailed(
    view_id: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
) -> Response[File | Problem]:
    """Vector tile of a saved view

     Mapbox Vector Tile of the view's current records (016 properties); the cache key includes the view
    `version`. View data follows normal read rules unless the view references a place or watchlist, in
    which case a token is required (401).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        view_id (str):
        z (int):
        x (int):
        y (int):
        ticket (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        view_id=view_id,
        z=z,
        x=x,
        y=y,
        ticket=ticket,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    view_id: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    ticket: str | Unset = UNSET,
) -> File | Problem | None:
    """Vector tile of a saved view

     Mapbox Vector Tile of the view's current records (016 properties); the cache key includes the view
    `version`. View data follows normal read rules unless the view references a place or watchlist, in
    which case a token is required (401).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        view_id (str):
        z (int):
        x (int):
        y (int):
        ticket (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return (
        await asyncio_detailed(
            view_id=view_id,
            z=z,
            x=x,
            y=y,
            client=client,
            ticket=ticket,
        )
    ).parsed
