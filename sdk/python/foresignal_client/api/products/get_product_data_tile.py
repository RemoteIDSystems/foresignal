from http import HTTPStatus
from io import BytesIO
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_data_tile_encoding import GetProductDataTileEncoding
from ...models.get_product_data_tile_tms import GetProductDataTileTms
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    product_id: str,
    frame_key: str,
    z: int,
    x: int,
    y: int,
    *,
    encoding: GetProductDataTileEncoding | Unset = GetProductDataTileEncoding.CUSTOM,
    tms: GetProductDataTileTms | Unset = GetProductDataTileTms.WEBMERCATORQUAD,
    run: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_encoding: str | Unset = UNSET
    if not isinstance(encoding, Unset):
        json_encoding = encoding.value

    params["encoding"] = json_encoding

    json_tms: str | Unset = UNSET
    if not isinstance(tms, Unset):
        json_tms = tms.value

    params["tms"] = json_tms

    params["run"] = run

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/data/{frame_key}/{z}/{x}/{y}.png".format(
            product_id=quote(str(product_id), safe=""),
            frame_key=quote(str(frame_key), safe=""),
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
) -> Response[File | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    product_id: str,
    frame_key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    encoding: GetProductDataTileEncoding | Unset = GetProductDataTileEncoding.CUSTOM,
    tms: GetProductDataTileTms | Unset = GetProductDataTileTms.WEBMERCATORQUAD,
    run: str | Unset = UNSET,
) -> Response[File | Problem]:
    """Data tile carrying physical values

     Terrain-RGB style: v = (R·65536 + G·256 + B) · scale + offset, no-data alpha 0, nearest resampling
    only. `terrarium` only for products whose range fits it.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        z (int):
        x (int):
        y (int):
        encoding (GetProductDataTileEncoding | Unset):  Default:
            GetProductDataTileEncoding.CUSTOM.
        tms (GetProductDataTileTms | Unset):  Default: GetProductDataTileTms.WEBMERCATORQUAD.
        run (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        z=z,
        x=x,
        y=y,
        encoding=encoding,
        tms=tms,
        run=run,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    frame_key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    encoding: GetProductDataTileEncoding | Unset = GetProductDataTileEncoding.CUSTOM,
    tms: GetProductDataTileTms | Unset = GetProductDataTileTms.WEBMERCATORQUAD,
    run: str | Unset = UNSET,
) -> File | Problem | None:
    """Data tile carrying physical values

     Terrain-RGB style: v = (R·65536 + G·256 + B) · scale + offset, no-data alpha 0, nearest resampling
    only. `terrarium` only for products whose range fits it.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        z (int):
        x (int):
        y (int):
        encoding (GetProductDataTileEncoding | Unset):  Default:
            GetProductDataTileEncoding.CUSTOM.
        tms (GetProductDataTileTms | Unset):  Default: GetProductDataTileTms.WEBMERCATORQUAD.
        run (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return sync_detailed(
        product_id=product_id,
        frame_key=frame_key,
        z=z,
        x=x,
        y=y,
        client=client,
        encoding=encoding,
        tms=tms,
        run=run,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    frame_key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    encoding: GetProductDataTileEncoding | Unset = GetProductDataTileEncoding.CUSTOM,
    tms: GetProductDataTileTms | Unset = GetProductDataTileTms.WEBMERCATORQUAD,
    run: str | Unset = UNSET,
) -> Response[File | Problem]:
    """Data tile carrying physical values

     Terrain-RGB style: v = (R·65536 + G·256 + B) · scale + offset, no-data alpha 0, nearest resampling
    only. `terrarium` only for products whose range fits it.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        z (int):
        x (int):
        y (int):
        encoding (GetProductDataTileEncoding | Unset):  Default:
            GetProductDataTileEncoding.CUSTOM.
        tms (GetProductDataTileTms | Unset):  Default: GetProductDataTileTms.WEBMERCATORQUAD.
        run (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        z=z,
        x=x,
        y=y,
        encoding=encoding,
        tms=tms,
        run=run,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    frame_key: str,
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    encoding: GetProductDataTileEncoding | Unset = GetProductDataTileEncoding.CUSTOM,
    tms: GetProductDataTileTms | Unset = GetProductDataTileTms.WEBMERCATORQUAD,
    run: str | Unset = UNSET,
) -> File | Problem | None:
    """Data tile carrying physical values

     Terrain-RGB style: v = (R·65536 + G·256 + B) · scale + offset, no-data alpha 0, nearest resampling
    only. `terrarium` only for products whose range fits it.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        z (int):
        x (int):
        y (int):
        encoding (GetProductDataTileEncoding | Unset):  Default:
            GetProductDataTileEncoding.CUSTOM.
        tms (GetProductDataTileTms | Unset):  Default: GetProductDataTileTms.WEBMERCATORQUAD.
        run (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            frame_key=frame_key,
            z=z,
            x=x,
            y=y,
            client=client,
            encoding=encoding,
            tms=tms,
            run=run,
        )
    ).parsed
