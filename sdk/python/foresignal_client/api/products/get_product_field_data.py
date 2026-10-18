from http import HTTPStatus
from io import BytesIO
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_field_data_res import GetProductFieldDataRes
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    product_id: str,
    frame_key: str,
    *,
    run: str | Unset = UNSET,
    res: GetProductFieldDataRes | Unset = GetProductFieldDataRes.VALUE_0,
    bbox: str | Unset = UNSET,
    accept_encoding: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(accept_encoding, Unset):
        headers["Accept-Encoding"] = accept_encoding

    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    params["run"] = run

    json_res: str | Unset = UNSET
    if not isinstance(res, Unset):
        json_res = res.value

    params["res"] = json_res

    params["bbox"] = bbox

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/fields/{frame_key}.f32".format(
            product_id=quote(str(product_id), safe=""),
            frame_key=quote(str(frame_key), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
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

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

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
    product_id: str,
    frame_key: str,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    res: GetProductFieldDataRes | Unset = GetProductFieldDataRes.VALUE_0,
    bbox: str | Unset = UNSET,
    accept_encoding: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Download wind field data

     Planar little-endian Float32, u then v, rows north→south, longitudes 0..359 (1°: 360×181, 521,280
    bytes raw; 0.25°: 1440×721, 8,305,920 bytes raw), written once at ingest and served precompressed
    with `Content-Encoding: gzip`. 406 NOT_ACCEPTABLE only when `Accept-Encoding` refuses gzip
    (`gzip;q=0`, or only `identity`); no header accepts it. `bbox` is snapped outward to grid points,
    wraps across 0°, and is cut on the fly from the file of the requested resolution (cached in the tile
    cache). The ETag is derived from the raw data's sha256.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        run (str | Unset):  Example: latest.
        res (GetProductFieldDataRes | Unset):  Default: GetProductFieldDataRes.VALUE_0.
        bbox (str | Unset):
        accept_encoding (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        run=run,
        res=res,
        bbox=bbox,
        accept_encoding=accept_encoding,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    frame_key: str,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    res: GetProductFieldDataRes | Unset = GetProductFieldDataRes.VALUE_0,
    bbox: str | Unset = UNSET,
    accept_encoding: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Download wind field data

     Planar little-endian Float32, u then v, rows north→south, longitudes 0..359 (1°: 360×181, 521,280
    bytes raw; 0.25°: 1440×721, 8,305,920 bytes raw), written once at ingest and served precompressed
    with `Content-Encoding: gzip`. 406 NOT_ACCEPTABLE only when `Accept-Encoding` refuses gzip
    (`gzip;q=0`, or only `identity`); no header accepts it. `bbox` is snapped outward to grid points,
    wraps across 0°, and is cut on the fly from the file of the requested resolution (cached in the tile
    cache). The ETag is derived from the raw data's sha256.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        run (str | Unset):  Example: latest.
        res (GetProductFieldDataRes | Unset):  Default: GetProductFieldDataRes.VALUE_0.
        bbox (str | Unset):
        accept_encoding (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return sync_detailed(
        product_id=product_id,
        frame_key=frame_key,
        client=client,
        run=run,
        res=res,
        bbox=bbox,
        accept_encoding=accept_encoding,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    frame_key: str,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    res: GetProductFieldDataRes | Unset = GetProductFieldDataRes.VALUE_0,
    bbox: str | Unset = UNSET,
    accept_encoding: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Download wind field data

     Planar little-endian Float32, u then v, rows north→south, longitudes 0..359 (1°: 360×181, 521,280
    bytes raw; 0.25°: 1440×721, 8,305,920 bytes raw), written once at ingest and served precompressed
    with `Content-Encoding: gzip`. 406 NOT_ACCEPTABLE only when `Accept-Encoding` refuses gzip
    (`gzip;q=0`, or only `identity`); no header accepts it. `bbox` is snapped outward to grid points,
    wraps across 0°, and is cut on the fly from the file of the requested resolution (cached in the tile
    cache). The ETag is derived from the raw data's sha256.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        run (str | Unset):  Example: latest.
        res (GetProductFieldDataRes | Unset):  Default: GetProductFieldDataRes.VALUE_0.
        bbox (str | Unset):
        accept_encoding (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        run=run,
        res=res,
        bbox=bbox,
        accept_encoding=accept_encoding,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    frame_key: str,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    res: GetProductFieldDataRes | Unset = GetProductFieldDataRes.VALUE_0,
    bbox: str | Unset = UNSET,
    accept_encoding: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Download wind field data

     Planar little-endian Float32, u then v, rows north→south, longitudes 0..359 (1°: 360×181, 521,280
    bytes raw; 0.25°: 1440×721, 8,305,920 bytes raw), written once at ingest and served precompressed
    with `Content-Encoding: gzip`. 406 NOT_ACCEPTABLE only when `Accept-Encoding` refuses gzip
    (`gzip;q=0`, or only `identity`); no header accepts it. `bbox` is snapped outward to grid points,
    wraps across 0°, and is cut on the fly from the file of the requested resolution (cached in the tile
    cache). The ETag is derived from the raw data's sha256.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        run (str | Unset):  Example: latest.
        res (GetProductFieldDataRes | Unset):  Default: GetProductFieldDataRes.VALUE_0.
        bbox (str | Unset):
        accept_encoding (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            frame_key=frame_key,
            client=client,
            run=run,
            res=res,
            bbox=bbox,
            accept_encoding=accept_encoding,
            if_none_match=if_none_match,
        )
    ).parsed
