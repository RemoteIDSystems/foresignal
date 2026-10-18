from http import HTTPStatus
from io import BytesIO
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ogc_collection_tile_f import OgcCollectionTileF
from ...models.ogc_collection_tile_tile_matrix_set_id import (
    OgcCollectionTileTileMatrixSetId,
)
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    collection_id: str,
    tile_matrix_set_id: OgcCollectionTileTileMatrixSetId,
    tile_matrix: str,
    tile_row: int,
    tile_col: int,
    *,
    datetime_: str | Unset = UNSET,
    style: str | Unset = UNSET,
    f: OgcCollectionTileF | Unset = OgcCollectionTileF.PNG,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["datetime"] = datetime_

    params["style"] = style

    json_f: str | Unset = UNSET
    if not isinstance(f, Unset):
        json_f = f.value

    params["f"] = json_f

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ogc/collections/{collection_id}/map/tiles/{tile_matrix_set_id}/{tile_matrix}/{tile_row}/{tile_col}".format(
            collection_id=quote(str(collection_id), safe=""),
            tile_matrix_set_id=quote(str(tile_matrix_set_id), safe=""),
            tile_matrix=quote(str(tile_matrix), safe=""),
            tile_row=quote(str(tile_row), safe=""),
            tile_col=quote(str(tile_col), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | File | Problem | None:
    if response.status_code == 200:
        response_200 = File(payload=BytesIO(response.content))

        return response_200

    if response.status_code == 301:
        response_301 = cast(Any, None)
        return response_301

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 406:
        response_406 = Problem.from_dict(response.json())

        return response_406

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | File | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    collection_id: str,
    tile_matrix_set_id: OgcCollectionTileTileMatrixSetId,
    tile_matrix: str,
    tile_row: int,
    tile_col: int,
    *,
    client: AuthenticatedClient,
    datetime_: str | Unset = UNSET,
    style: str | Unset = UNSET,
    f: OgcCollectionTileF | Unset = OgcCollectionTileF.PNG,
) -> Response[Any | File | Problem]:
    """Map tile (OGC row/col order)

     Note the OGC `{tileMatrix}/{tileRow}/{tileCol}` order. `datetime` resolves like WMTS Time; a
    concrete time 301-redirects to the content-addressed tile.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        tile_matrix_set_id (OgcCollectionTileTileMatrixSetId):
        tile_matrix (str):
        tile_row (int):
        tile_col (int):
        datetime_ (str | Unset):
        style (str | Unset):
        f (OgcCollectionTileF | Unset):  Default: OgcCollectionTileF.PNG.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        tile_matrix_set_id=tile_matrix_set_id,
        tile_matrix=tile_matrix,
        tile_row=tile_row,
        tile_col=tile_col,
        datetime_=datetime_,
        style=style,
        f=f,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    collection_id: str,
    tile_matrix_set_id: OgcCollectionTileTileMatrixSetId,
    tile_matrix: str,
    tile_row: int,
    tile_col: int,
    *,
    client: AuthenticatedClient,
    datetime_: str | Unset = UNSET,
    style: str | Unset = UNSET,
    f: OgcCollectionTileF | Unset = OgcCollectionTileF.PNG,
) -> Any | File | Problem | None:
    """Map tile (OGC row/col order)

     Note the OGC `{tileMatrix}/{tileRow}/{tileCol}` order. `datetime` resolves like WMTS Time; a
    concrete time 301-redirects to the content-addressed tile.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        tile_matrix_set_id (OgcCollectionTileTileMatrixSetId):
        tile_matrix (str):
        tile_row (int):
        tile_col (int):
        datetime_ (str | Unset):
        style (str | Unset):
        f (OgcCollectionTileF | Unset):  Default: OgcCollectionTileF.PNG.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return sync_detailed(
        collection_id=collection_id,
        tile_matrix_set_id=tile_matrix_set_id,
        tile_matrix=tile_matrix,
        tile_row=tile_row,
        tile_col=tile_col,
        client=client,
        datetime_=datetime_,
        style=style,
        f=f,
    ).parsed


async def asyncio_detailed(
    collection_id: str,
    tile_matrix_set_id: OgcCollectionTileTileMatrixSetId,
    tile_matrix: str,
    tile_row: int,
    tile_col: int,
    *,
    client: AuthenticatedClient,
    datetime_: str | Unset = UNSET,
    style: str | Unset = UNSET,
    f: OgcCollectionTileF | Unset = OgcCollectionTileF.PNG,
) -> Response[Any | File | Problem]:
    """Map tile (OGC row/col order)

     Note the OGC `{tileMatrix}/{tileRow}/{tileCol}` order. `datetime` resolves like WMTS Time; a
    concrete time 301-redirects to the content-addressed tile.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        tile_matrix_set_id (OgcCollectionTileTileMatrixSetId):
        tile_matrix (str):
        tile_row (int):
        tile_col (int):
        datetime_ (str | Unset):
        style (str | Unset):
        f (OgcCollectionTileF | Unset):  Default: OgcCollectionTileF.PNG.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        tile_matrix_set_id=tile_matrix_set_id,
        tile_matrix=tile_matrix,
        tile_row=tile_row,
        tile_col=tile_col,
        datetime_=datetime_,
        style=style,
        f=f,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    collection_id: str,
    tile_matrix_set_id: OgcCollectionTileTileMatrixSetId,
    tile_matrix: str,
    tile_row: int,
    tile_col: int,
    *,
    client: AuthenticatedClient,
    datetime_: str | Unset = UNSET,
    style: str | Unset = UNSET,
    f: OgcCollectionTileF | Unset = OgcCollectionTileF.PNG,
) -> Any | File | Problem | None:
    """Map tile (OGC row/col order)

     Note the OGC `{tileMatrix}/{tileRow}/{tileCol}` order. `datetime` resolves like WMTS Time; a
    concrete time 301-redirects to the content-addressed tile.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        tile_matrix_set_id (OgcCollectionTileTileMatrixSetId):
        tile_matrix (str):
        tile_row (int):
        tile_col (int):
        datetime_ (str | Unset):
        style (str | Unset):
        f (OgcCollectionTileF | Unset):  Default: OgcCollectionTileF.PNG.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return (
        await asyncio_detailed(
            collection_id=collection_id,
            tile_matrix_set_id=tile_matrix_set_id,
            tile_matrix=tile_matrix,
            tile_row=tile_row,
            tile_col=tile_col,
            client=client,
            datetime_=datetime_,
            style=style,
            f=f,
        )
    ).parsed
