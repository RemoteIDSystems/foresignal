from http import HTTPStatus
from io import BytesIO
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.download_export_bundle import DownloadExportBundle
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    export_id: str,
    *,
    bundle: DownloadExportBundle | Unset = UNSET,
    file: str | Unset = UNSET,
    range_: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(range_, Unset):
        headers["Range"] = range_

    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    json_bundle: str | Unset = UNSET
    if not isinstance(bundle, Unset):
        json_bundle = bundle.value

    params["bundle"] = json_bundle

    params["file"] = file

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/exports/{export_id}/download".format(
            export_id=quote(str(export_id), safe=""),
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

    if response.status_code == 206:
        response_206 = File(payload=BytesIO(response.content))

        return response_206

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if response.status_code == 416:
        response_416 = Problem.from_dict(response.json())

        return response_416

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
    export_id: str,
    *,
    client: AuthenticatedClient,
    bundle: DownloadExportBundle | Unset = UNSET,
    file: str | Unset = UNSET,
    range_: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Download an export, snapshot or pack

     The single data file, a named file (`file`, e.g. one of a pack's MVT and PNG .pmtiles), or
    `bundle=zip` with the data, `manifest.json`, `manifest.json.jws`, `licence.json` and
    `ATTRIBUTION.txt`. Supports `Range` and `If-None-Match` (ETag = sha256). Errors: `410
    EXPORT_EXPIRED`; `409 EXPORT_NOT_READY` while the job is not DONE.

    **Scope:** `agent`.

    Args:
        export_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        bundle (DownloadExportBundle | Unset):
        file (str | Unset):
        range_ (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        export_id=export_id,
        bundle=bundle,
        file=file,
        range_=range_,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    export_id: str,
    *,
    client: AuthenticatedClient,
    bundle: DownloadExportBundle | Unset = UNSET,
    file: str | Unset = UNSET,
    range_: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Download an export, snapshot or pack

     The single data file, a named file (`file`, e.g. one of a pack's MVT and PNG .pmtiles), or
    `bundle=zip` with the data, `manifest.json`, `manifest.json.jws`, `licence.json` and
    `ATTRIBUTION.txt`. Supports `Range` and `If-None-Match` (ETag = sha256). Errors: `410
    EXPORT_EXPIRED`; `409 EXPORT_NOT_READY` while the job is not DONE.

    **Scope:** `agent`.

    Args:
        export_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        bundle (DownloadExportBundle | Unset):
        file (str | Unset):
        range_ (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return sync_detailed(
        export_id=export_id,
        client=client,
        bundle=bundle,
        file=file,
        range_=range_,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    export_id: str,
    *,
    client: AuthenticatedClient,
    bundle: DownloadExportBundle | Unset = UNSET,
    file: str | Unset = UNSET,
    range_: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | File | Problem]:
    """Download an export, snapshot or pack

     The single data file, a named file (`file`, e.g. one of a pack's MVT and PNG .pmtiles), or
    `bundle=zip` with the data, `manifest.json`, `manifest.json.jws`, `licence.json` and
    `ATTRIBUTION.txt`. Supports `Range` and `If-None-Match` (ETag = sha256). Errors: `410
    EXPORT_EXPIRED`; `409 EXPORT_NOT_READY` while the job is not DONE.

    **Scope:** `agent`.

    Args:
        export_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        bundle (DownloadExportBundle | Unset):
        file (str | Unset):
        range_ (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | File | Problem]
    """

    kwargs = _get_kwargs(
        export_id=export_id,
        bundle=bundle,
        file=file,
        range_=range_,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    export_id: str,
    *,
    client: AuthenticatedClient,
    bundle: DownloadExportBundle | Unset = UNSET,
    file: str | Unset = UNSET,
    range_: str | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | File | Problem | None:
    """Download an export, snapshot or pack

     The single data file, a named file (`file`, e.g. one of a pack's MVT and PNG .pmtiles), or
    `bundle=zip` with the data, `manifest.json`, `manifest.json.jws`, `licence.json` and
    `ATTRIBUTION.txt`. Supports `Range` and `If-None-Match` (ETag = sha256). Errors: `410
    EXPORT_EXPIRED`; `409 EXPORT_NOT_READY` while the job is not DONE.

    **Scope:** `agent`.

    Args:
        export_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        bundle (DownloadExportBundle | Unset):
        file (str | Unset):
        range_ (str | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | File | Problem
    """

    return (
        await asyncio_detailed(
            export_id=export_id,
            client=client,
            bundle=bundle,
            file=file,
            range_=range_,
            if_none_match=if_none_match,
        )
    ).parsed
