from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ogc_tile_matrix_set import OgcTileMatrixSet
from ...models.ogc_tile_matrix_set_tile_matrix_set_id import (
    OgcTileMatrixSetTileMatrixSetId,
)
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    tile_matrix_set_id: OgcTileMatrixSetTileMatrixSetId,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ogc/tileMatrixSets/{tile_matrix_set_id}".format(
            tile_matrix_set_id=quote(str(tile_matrix_set_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> OgcTileMatrixSet | Problem | None:
    if response.status_code == 200:
        response_200 = OgcTileMatrixSet.from_dict(response.json())

        return response_200

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
) -> Response[OgcTileMatrixSet | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    tile_matrix_set_id: OgcTileMatrixSetTileMatrixSetId,
    *,
    client: AuthenticatedClient,
) -> Response[OgcTileMatrixSet | Problem]:
    """Tile matrix set definition

     OGC 17-083r4 JSON definition.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        tile_matrix_set_id (OgcTileMatrixSetTileMatrixSetId):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OgcTileMatrixSet | Problem]
    """

    kwargs = _get_kwargs(
        tile_matrix_set_id=tile_matrix_set_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    tile_matrix_set_id: OgcTileMatrixSetTileMatrixSetId,
    *,
    client: AuthenticatedClient,
) -> OgcTileMatrixSet | Problem | None:
    """Tile matrix set definition

     OGC 17-083r4 JSON definition.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        tile_matrix_set_id (OgcTileMatrixSetTileMatrixSetId):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OgcTileMatrixSet | Problem
    """

    return sync_detailed(
        tile_matrix_set_id=tile_matrix_set_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    tile_matrix_set_id: OgcTileMatrixSetTileMatrixSetId,
    *,
    client: AuthenticatedClient,
) -> Response[OgcTileMatrixSet | Problem]:
    """Tile matrix set definition

     OGC 17-083r4 JSON definition.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        tile_matrix_set_id (OgcTileMatrixSetTileMatrixSetId):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[OgcTileMatrixSet | Problem]
    """

    kwargs = _get_kwargs(
        tile_matrix_set_id=tile_matrix_set_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    tile_matrix_set_id: OgcTileMatrixSetTileMatrixSetId,
    *,
    client: AuthenticatedClient,
) -> OgcTileMatrixSet | Problem | None:
    """Tile matrix set definition

     OGC 17-083r4 JSON definition.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        tile_matrix_set_id (OgcTileMatrixSetTileMatrixSetId):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        OgcTileMatrixSet | Problem
    """

    return (
        await asyncio_detailed(
            tile_matrix_set_id=tile_matrix_set_id,
            client=client,
        )
    ).parsed
