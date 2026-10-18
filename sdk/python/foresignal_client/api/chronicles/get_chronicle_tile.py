import datetime
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
    chronicle_id: str,
    z: int,
    x: int,
    y: int,
    *,
    at: datetime.datetime | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_at: str | Unset = UNSET
    if not isinstance(at, Unset):
        json_at = at.isoformat()
    params["at"] = json_at

    json_layers: list[str] | Unset = UNSET
    if not isinstance(layers, Unset):
        json_layers = layers

    params["layers"] = json_layers

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicles/{chronicle_id}/tiles/{z}/{x}/{y}.mvt".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
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
    chronicle_id: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> Response[File | Problem]:
    """Vector tile of chronicle storage

     Mapbox Vector Tile at `at` over chronicle storage (016 machinery), so a globe replays without
    touching global history.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        z (int):
        x (int):
        y (int):
        at (datetime.datetime | Unset):
        layers (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        z=z,
        x=x,
        y=y,
        at=at,
        layers=layers,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> File | Problem | None:
    """Vector tile of chronicle storage

     Mapbox Vector Tile at `at` over chronicle storage (016 machinery), so a globe replays without
    touching global history.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        z (int):
        x (int):
        y (int):
        at (datetime.datetime | Unset):
        layers (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        z=z,
        x=x,
        y=y,
        client=client,
        at=at,
        layers=layers,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> Response[File | Problem]:
    """Vector tile of chronicle storage

     Mapbox Vector Tile at `at` over chronicle storage (016 machinery), so a globe replays without
    touching global history.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        z (int):
        x (int):
        y (int):
        at (datetime.datetime | Unset):
        layers (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        z=z,
        x=x,
        y=y,
        at=at,
        layers=layers,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime | Unset = UNSET,
    layers: list[str] | Unset = UNSET,
) -> File | Problem | None:
    """Vector tile of chronicle storage

     Mapbox Vector Tile at `at` over chronicle storage (016 machinery), so a globe replays without
    touching global history.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        z (int):
        x (int):
        y (int):
        at (datetime.datetime | Unset):
        layers (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            z=z,
            x=x,
            y=y,
            client=client,
            at=at,
            layers=layers,
        )
    ).parsed
