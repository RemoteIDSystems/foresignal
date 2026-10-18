from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.remote_raster_bundle import RemoteRasterBundle
from ...models.remote_raster_bundle_accepted import RemoteRasterBundleAccepted
from ...types import Response


def _get_kwargs(
    source_id: str,
    *,
    body: RemoteRasterBundle,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/ingest/{source_id}/rasters".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    _kwargs["files"] = body.to_multipart()

    headers["Content-Type"] = "multipart/form-data; boundary=+++"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RemoteRasterBundleAccepted | None:
    if response.status_code == 202:
        response_202 = RemoteRasterBundleAccepted.from_dict(response.json())

        return response_202

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if response.status_code == 415:
        response_415 = Problem.from_dict(response.json())

        return response_415

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | RemoteRasterBundleAccepted]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: RemoteRasterBundle,
) -> Response[Problem | RemoteRasterBundleAccepted]:
    """Push a pass bundle (remote station)

     A `meta` part `{passFolder, files[], product?}` plus file parts named `files` (`dataset.json`,
    `product.cbor`, PNG, JPEG, TIF), each ≤ 25 MB and ≤ 200 MB in total (413 `MEDIA_TOO_LARGE`). Names
    are single safe path segments (no separators, no `..`, no leading dot); every listed file needs a
    part and every part must be listed; images pass the ImageGuard header check (400). `dataset.json` is
    placed in the pass folder and every other file in the product directory: `product` when given, else
    the single product `dataset.json` lists, else the pass folder itself. The bundle is written
    atomically to `${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/incoming/{uploadId}/` (the inbox
    mount is read-only); the station's inbox loop processes it like a ready pass folder on its next scan
    (composites, thumbnails, provenance, polar GeoTIFF overlays, footprint or approximate swath) and the
    copy expires after 2 days. Only `satdump-{station}` sources answer; others are 404. `rasters` is a
    reserved segment that never routes to an IngestFormat. Each push costs one unit of the source's
    budget.

    **Scope:** `open` (the source's ingest key, or an admin token, is checked by the endpoint). The
    ingest key of a `satdump-{station}` source is its `ingestKey` credential.

    Args:
        source_id (str):
        body (RemoteRasterBundle): Multipart remote pass bundle processed like an inbox pass
            folder

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RemoteRasterBundleAccepted]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: RemoteRasterBundle,
) -> Problem | RemoteRasterBundleAccepted | None:
    """Push a pass bundle (remote station)

     A `meta` part `{passFolder, files[], product?}` plus file parts named `files` (`dataset.json`,
    `product.cbor`, PNG, JPEG, TIF), each ≤ 25 MB and ≤ 200 MB in total (413 `MEDIA_TOO_LARGE`). Names
    are single safe path segments (no separators, no `..`, no leading dot); every listed file needs a
    part and every part must be listed; images pass the ImageGuard header check (400). `dataset.json` is
    placed in the pass folder and every other file in the product directory: `product` when given, else
    the single product `dataset.json` lists, else the pass folder itself. The bundle is written
    atomically to `${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/incoming/{uploadId}/` (the inbox
    mount is read-only); the station's inbox loop processes it like a ready pass folder on its next scan
    (composites, thumbnails, provenance, polar GeoTIFF overlays, footprint or approximate swath) and the
    copy expires after 2 days. Only `satdump-{station}` sources answer; others are 404. `rasters` is a
    reserved segment that never routes to an IngestFormat. Each push costs one unit of the source's
    budget.

    **Scope:** `open` (the source's ingest key, or an admin token, is checked by the endpoint). The
    ingest key of a `satdump-{station}` source is its `ingestKey` credential.

    Args:
        source_id (str):
        body (RemoteRasterBundle): Multipart remote pass bundle processed like an inbox pass
            folder

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RemoteRasterBundleAccepted
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: RemoteRasterBundle,
) -> Response[Problem | RemoteRasterBundleAccepted]:
    """Push a pass bundle (remote station)

     A `meta` part `{passFolder, files[], product?}` plus file parts named `files` (`dataset.json`,
    `product.cbor`, PNG, JPEG, TIF), each ≤ 25 MB and ≤ 200 MB in total (413 `MEDIA_TOO_LARGE`). Names
    are single safe path segments (no separators, no `..`, no leading dot); every listed file needs a
    part and every part must be listed; images pass the ImageGuard header check (400). `dataset.json` is
    placed in the pass folder and every other file in the product directory: `product` when given, else
    the single product `dataset.json` lists, else the pass folder itself. The bundle is written
    atomically to `${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/incoming/{uploadId}/` (the inbox
    mount is read-only); the station's inbox loop processes it like a ready pass folder on its next scan
    (composites, thumbnails, provenance, polar GeoTIFF overlays, footprint or approximate swath) and the
    copy expires after 2 days. Only `satdump-{station}` sources answer; others are 404. `rasters` is a
    reserved segment that never routes to an IngestFormat. Each push costs one unit of the source's
    budget.

    **Scope:** `open` (the source's ingest key, or an admin token, is checked by the endpoint). The
    ingest key of a `satdump-{station}` source is its `ingestKey` credential.

    Args:
        source_id (str):
        body (RemoteRasterBundle): Multipart remote pass bundle processed like an inbox pass
            folder

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RemoteRasterBundleAccepted]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: RemoteRasterBundle,
) -> Problem | RemoteRasterBundleAccepted | None:
    """Push a pass bundle (remote station)

     A `meta` part `{passFolder, files[], product?}` plus file parts named `files` (`dataset.json`,
    `product.cbor`, PNG, JPEG, TIF), each ≤ 25 MB and ≤ 200 MB in total (413 `MEDIA_TOO_LARGE`). Names
    are single safe path segments (no separators, no `..`, no leading dot); every listed file needs a
    part and every part must be listed; images pass the ImageGuard header check (400). `dataset.json` is
    placed in the pass folder and every other file in the product directory: `product` when given, else
    the single product `dataset.json` lists, else the pass folder itself. The bundle is written
    atomically to `${FORESIGNAL_DATA_DIR}/rasters/satdump/{station}/incoming/{uploadId}/` (the inbox
    mount is read-only); the station's inbox loop processes it like a ready pass folder on its next scan
    (composites, thumbnails, provenance, polar GeoTIFF overlays, footprint or approximate swath) and the
    copy expires after 2 days. Only `satdump-{station}` sources answer; others are 404. `rasters` is a
    reserved segment that never routes to an IngestFormat. Each push costs one unit of the source's
    budget.

    **Scope:** `open` (the source's ingest key, or an admin token, is checked by the endpoint). The
    ingest key of a `satdump-{station}` source is its `ingestKey` credential.

    Args:
        source_id (str):
        body (RemoteRasterBundle): Multipart remote pass bundle processed like an inbox pass
            folder

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RemoteRasterBundleAccepted
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
