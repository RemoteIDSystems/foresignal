from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.field_manifest import FieldManifest
from ...models.get_product_field_manifest_res import GetProductFieldManifestRes
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    product_id: str,
    frame_key: str,
    *,
    run: str | Unset = UNSET,
    res: GetProductFieldManifestRes | Unset = GetProductFieldManifestRes.VALUE_0,
    bbox: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
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
        "url": "/v1/products/{product_id}/fields/{frame_key}.json".format(
            product_id=quote(str(product_id), safe=""),
            frame_key=quote(str(frame_key), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | FieldManifest | Problem | None:
    if response.status_code == 200:
        response_200 = FieldManifest.from_dict(response.json())

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
) -> Response[Any | FieldManifest | Problem]:
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
    res: GetProductFieldManifestRes | Unset = GetProductFieldManifestRes.VALUE_0,
    bbox: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | FieldManifest | Problem]:
    """Get a wind field manifest

     GEV `schemaVersion: 1` manifest for the planar u/v Float32 field (`gfs` only in v1: a `kind=field`
    product with two bands and global coverage), with cycle, grid, component offsets, byte length,
    sha256 of the raw (uncompressed) data, attribution and a content-addressed `dataUrl`
    (`…/fields/{frameKey}.f32?run=<runIso>&res=<res>[&bbox=<snapped>]`; the run is always pinned).
    `stale` is true when the frame's run is not the newest complete run; a disabled product answers 503
    rather than an `unavailable` manifest. With `bbox`, `grid` describes the snapped subset (`lo1` in
    0..360, so `nx·dx` is less than 360). A concrete forecast time without `run` → 400 RUN_REQUIRED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        run (str | Unset):  Example: latest.
        res (GetProductFieldManifestRes | Unset):  Default: GetProductFieldManifestRes.VALUE_0.
        bbox (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | FieldManifest | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        run=run,
        res=res,
        bbox=bbox,
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
    res: GetProductFieldManifestRes | Unset = GetProductFieldManifestRes.VALUE_0,
    bbox: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | FieldManifest | Problem | None:
    """Get a wind field manifest

     GEV `schemaVersion: 1` manifest for the planar u/v Float32 field (`gfs` only in v1: a `kind=field`
    product with two bands and global coverage), with cycle, grid, component offsets, byte length,
    sha256 of the raw (uncompressed) data, attribution and a content-addressed `dataUrl`
    (`…/fields/{frameKey}.f32?run=<runIso>&res=<res>[&bbox=<snapped>]`; the run is always pinned).
    `stale` is true when the frame's run is not the newest complete run; a disabled product answers 503
    rather than an `unavailable` manifest. With `bbox`, `grid` describes the snapped subset (`lo1` in
    0..360, so `nx·dx` is less than 360). A concrete forecast time without `run` → 400 RUN_REQUIRED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        run (str | Unset):  Example: latest.
        res (GetProductFieldManifestRes | Unset):  Default: GetProductFieldManifestRes.VALUE_0.
        bbox (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | FieldManifest | Problem
    """

    return sync_detailed(
        product_id=product_id,
        frame_key=frame_key,
        client=client,
        run=run,
        res=res,
        bbox=bbox,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    frame_key: str,
    *,
    client: AuthenticatedClient,
    run: str | Unset = UNSET,
    res: GetProductFieldManifestRes | Unset = GetProductFieldManifestRes.VALUE_0,
    bbox: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | FieldManifest | Problem]:
    """Get a wind field manifest

     GEV `schemaVersion: 1` manifest for the planar u/v Float32 field (`gfs` only in v1: a `kind=field`
    product with two bands and global coverage), with cycle, grid, component offsets, byte length,
    sha256 of the raw (uncompressed) data, attribution and a content-addressed `dataUrl`
    (`…/fields/{frameKey}.f32?run=<runIso>&res=<res>[&bbox=<snapped>]`; the run is always pinned).
    `stale` is true when the frame's run is not the newest complete run; a disabled product answers 503
    rather than an `unavailable` manifest. With `bbox`, `grid` describes the snapped subset (`lo1` in
    0..360, so `nx·dx` is less than 360). A concrete forecast time without `run` → 400 RUN_REQUIRED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        run (str | Unset):  Example: latest.
        res (GetProductFieldManifestRes | Unset):  Default: GetProductFieldManifestRes.VALUE_0.
        bbox (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | FieldManifest | Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        run=run,
        res=res,
        bbox=bbox,
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
    res: GetProductFieldManifestRes | Unset = GetProductFieldManifestRes.VALUE_0,
    bbox: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | FieldManifest | Problem | None:
    """Get a wind field manifest

     GEV `schemaVersion: 1` manifest for the planar u/v Float32 field (`gfs` only in v1: a `kind=field`
    product with two bands and global coverage), with cycle, grid, component offsets, byte length,
    sha256 of the raw (uncompressed) data, attribution and a content-addressed `dataUrl`
    (`…/fields/{frameKey}.f32?run=<runIso>&res=<res>[&bbox=<snapped>]`; the run is always pinned).
    `stale` is true when the frame's run is not the newest complete run; a disabled product answers 503
    rather than an `unavailable` manifest. With `bbox`, `grid` describes the snapped subset (`lo1` in
    0..360, so `nx·dx` is less than 360). A concrete forecast time without `run` → 400 RUN_REQUIRED.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str):
        run (str | Unset):  Example: latest.
        res (GetProductFieldManifestRes | Unset):  Default: GetProductFieldManifestRes.VALUE_0.
        bbox (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | FieldManifest | Problem
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            frame_key=frame_key,
            client=client,
            run=run,
            res=res,
            bbox=bbox,
            if_none_match=if_none_match,
        )
    ).parsed
