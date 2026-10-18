from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.remote_media_accepted import RemoteMediaAccepted
from ...models.remote_media_upload import RemoteMediaUpload
from ...types import Response


def _get_kwargs(
    source_id: str,
    *,
    body: RemoteMediaUpload,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/ingest/{source_id}/media".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    _kwargs["files"] = body.to_multipart()

    headers["Content-Type"] = "multipart/form-data; boundary=+++"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RemoteMediaAccepted | None:
    if response.status_code == 202:
        response_202 = RemoteMediaAccepted.from_dict(response.json())

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
) -> Response[Problem | RemoteMediaAccepted]:
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
    body: RemoteMediaUpload,
) -> Response[Problem | RemoteMediaAccepted]:
    """Push a decoded image (remote station)

     A single image (`file`, PNG, JPEG or TIFF, ≤ 25 MB) plus `meta`. The running station passes it
    through ImageGuard before decoding, stores it with 256/1024 px thumbnails and attaches it to the
    matching pass: `meta.passId` when the station knows that pass, else the 049 join rule (same
    satellite, timestamp inside [first lock − 5 min, LOS + 5 min]), else a pass minted from the image;
    the pass gets a new `decoded` version. Only sources whose descriptor accepts remote media (the
    `satdump-{station}` family) answer; any other source, or a station whose stream is not running, is
    404. `media` is a reserved segment that never routes to an IngestFormat. Each push costs one unit of
    the source's budget (429 `RATE_LIMITED` with `Retry-After` when spent). Syncing SatDump's output
    into the inbox (rsync or Syncthing) remains the recommended remote path.

    **Scope:** `open` (the source's ingest key, or an admin token, is checked by the endpoint). A
    `satdump-{station}` source has no minted ingest key: the operator stores the key a remote station
    presents as the source's `ingestKey` credential (`PUT /v1/sources/{id}/credentials`).

    Args:
        source_id (str):
        body (RemoteMediaUpload): Multipart remote media upload (remote Pi, design §4.7 b)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RemoteMediaAccepted]
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
    body: RemoteMediaUpload,
) -> Problem | RemoteMediaAccepted | None:
    """Push a decoded image (remote station)

     A single image (`file`, PNG, JPEG or TIFF, ≤ 25 MB) plus `meta`. The running station passes it
    through ImageGuard before decoding, stores it with 256/1024 px thumbnails and attaches it to the
    matching pass: `meta.passId` when the station knows that pass, else the 049 join rule (same
    satellite, timestamp inside [first lock − 5 min, LOS + 5 min]), else a pass minted from the image;
    the pass gets a new `decoded` version. Only sources whose descriptor accepts remote media (the
    `satdump-{station}` family) answer; any other source, or a station whose stream is not running, is
    404. `media` is a reserved segment that never routes to an IngestFormat. Each push costs one unit of
    the source's budget (429 `RATE_LIMITED` with `Retry-After` when spent). Syncing SatDump's output
    into the inbox (rsync or Syncthing) remains the recommended remote path.

    **Scope:** `open` (the source's ingest key, or an admin token, is checked by the endpoint). A
    `satdump-{station}` source has no minted ingest key: the operator stores the key a remote station
    presents as the source's `ingestKey` credential (`PUT /v1/sources/{id}/credentials`).

    Args:
        source_id (str):
        body (RemoteMediaUpload): Multipart remote media upload (remote Pi, design §4.7 b)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RemoteMediaAccepted
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
    body: RemoteMediaUpload,
) -> Response[Problem | RemoteMediaAccepted]:
    """Push a decoded image (remote station)

     A single image (`file`, PNG, JPEG or TIFF, ≤ 25 MB) plus `meta`. The running station passes it
    through ImageGuard before decoding, stores it with 256/1024 px thumbnails and attaches it to the
    matching pass: `meta.passId` when the station knows that pass, else the 049 join rule (same
    satellite, timestamp inside [first lock − 5 min, LOS + 5 min]), else a pass minted from the image;
    the pass gets a new `decoded` version. Only sources whose descriptor accepts remote media (the
    `satdump-{station}` family) answer; any other source, or a station whose stream is not running, is
    404. `media` is a reserved segment that never routes to an IngestFormat. Each push costs one unit of
    the source's budget (429 `RATE_LIMITED` with `Retry-After` when spent). Syncing SatDump's output
    into the inbox (rsync or Syncthing) remains the recommended remote path.

    **Scope:** `open` (the source's ingest key, or an admin token, is checked by the endpoint). A
    `satdump-{station}` source has no minted ingest key: the operator stores the key a remote station
    presents as the source's `ingestKey` credential (`PUT /v1/sources/{id}/credentials`).

    Args:
        source_id (str):
        body (RemoteMediaUpload): Multipart remote media upload (remote Pi, design §4.7 b)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RemoteMediaAccepted]
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
    body: RemoteMediaUpload,
) -> Problem | RemoteMediaAccepted | None:
    """Push a decoded image (remote station)

     A single image (`file`, PNG, JPEG or TIFF, ≤ 25 MB) plus `meta`. The running station passes it
    through ImageGuard before decoding, stores it with 256/1024 px thumbnails and attaches it to the
    matching pass: `meta.passId` when the station knows that pass, else the 049 join rule (same
    satellite, timestamp inside [first lock − 5 min, LOS + 5 min]), else a pass minted from the image;
    the pass gets a new `decoded` version. Only sources whose descriptor accepts remote media (the
    `satdump-{station}` family) answer; any other source, or a station whose stream is not running, is
    404. `media` is a reserved segment that never routes to an IngestFormat. Each push costs one unit of
    the source's budget (429 `RATE_LIMITED` with `Retry-After` when spent). Syncing SatDump's output
    into the inbox (rsync or Syncthing) remains the recommended remote path.

    **Scope:** `open` (the source's ingest key, or an admin token, is checked by the endpoint). A
    `satdump-{station}` source has no minted ingest key: the operator stores the key a remote station
    presents as the source's `ingestKey` credential (`PUT /v1/sources/{id}/credentials`).

    Args:
        source_id (str):
        body (RemoteMediaUpload): Multipart remote media upload (remote Pi, design §4.7 b)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RemoteMediaAccepted
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
