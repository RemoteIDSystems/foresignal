from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.gps_logger_fix import GpsLoggerFix
from ...models.ingest_records import IngestRecords
from ...models.ingest_records_path_key_content_encoding import (
    IngestRecordsPathKeyContentEncoding,
)
from ...models.ingest_records_path_key_data_body import IngestRecordsPathKeyDataBody
from ...models.ingest_records_path_key_format import IngestRecordsPathKeyFormat
from ...models.osm_and_location import OsmAndLocation
from ...models.overland_batch import OverlandBatch
from ...models.own_tracks_message import OwnTracksMessage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyFormat,
    *,
    body: GpsLoggerFix
    | IngestRecords
    | OsmAndLocation
    | OverlandBatch
    | OwnTracksMessage
    | IngestRecordsPathKeyDataBody
    | Unset = UNSET,
    content_encoding: IngestRecordsPathKeyContentEncoding | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(content_encoding, Unset):
        headers["Content-Encoding"] = str(content_encoding)

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/ingest/{source_id}/k/{key}/{format_}".format(
            source_id=quote(str(source_id), safe=""),
            key=quote(str(key), safe=""),
            format_=quote(str(format_), safe=""),
        ),
    }

    if isinstance(
        body,
        GpsLoggerFix
        | IngestRecords
        | OsmAndLocation
        | OverlandBatch
        | OwnTracksMessage,
    ):
        if (
            isinstance(body, OsmAndLocation)
            or isinstance(body, GpsLoggerFix)
            or isinstance(body, OwnTracksMessage)
            or isinstance(body, OverlandBatch)
        ):
            _kwargs["json"] = body.to_dict()
        else:
            _kwargs["json"] = body.to_dict()

        headers["Content-Type"] = "application/json"
    if isinstance(body, IngestRecordsPathKeyDataBody):
        if not isinstance(body, Unset):
            _kwargs["data"] = body.to_dict()
        headers["Content-Type"] = "application/x-www-form-urlencoded"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyFormat,
    *,
    client: AuthenticatedClient,
    body: GpsLoggerFix
    | IngestRecords
    | OsmAndLocation
    | OverlandBatch
    | OwnTracksMessage
    | IngestRecordsPathKeyDataBody
    | Unset = UNSET,
    content_encoding: IngestRecordsPathKeyContentEncoding | Unset = UNSET,
) -> Response[Problem]:
    """Push records with the key in the path

     Path-key form of `ingestRecordsFormat` (query, form or JSON bodies) for tracker apps and device
    firmware that can set only a host and path (Ecowitt Customized upload, 059). Same limits, rejections
    and native replies. An `eik_` key in the path needs the source's `allowPathKey` (else 404); the
    sender must be in `ingestAllowCidrs` (default: private ranges; else 403 INGEST_SOURCE_DENIED).

    **Scope:** open (no token; the credential is in the request).

    Args:
        source_id (str):
        key (str):
        format_ (IngestRecordsPathKeyFormat):
        content_encoding (IngestRecordsPathKeyContentEncoding | Unset):
        body (GpsLoggerFix | IngestRecords | OsmAndLocation | OverlandBatch | OwnTracksMessage |
            Unset):
        body (IngestRecordsPathKeyDataBody | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        key=key,
        format_=format_,
        body=body,
        content_encoding=content_encoding,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyFormat,
    *,
    client: AuthenticatedClient,
    body: GpsLoggerFix
    | IngestRecords
    | OsmAndLocation
    | OverlandBatch
    | OwnTracksMessage
    | IngestRecordsPathKeyDataBody
    | Unset = UNSET,
    content_encoding: IngestRecordsPathKeyContentEncoding | Unset = UNSET,
) -> Problem | None:
    """Push records with the key in the path

     Path-key form of `ingestRecordsFormat` (query, form or JSON bodies) for tracker apps and device
    firmware that can set only a host and path (Ecowitt Customized upload, 059). Same limits, rejections
    and native replies. An `eik_` key in the path needs the source's `allowPathKey` (else 404); the
    sender must be in `ingestAllowCidrs` (default: private ranges; else 403 INGEST_SOURCE_DENIED).

    **Scope:** open (no token; the credential is in the request).

    Args:
        source_id (str):
        key (str):
        format_ (IngestRecordsPathKeyFormat):
        content_encoding (IngestRecordsPathKeyContentEncoding | Unset):
        body (GpsLoggerFix | IngestRecords | OsmAndLocation | OverlandBatch | OwnTracksMessage |
            Unset):
        body (IngestRecordsPathKeyDataBody | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        source_id=source_id,
        key=key,
        format_=format_,
        client=client,
        body=body,
        content_encoding=content_encoding,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyFormat,
    *,
    client: AuthenticatedClient,
    body: GpsLoggerFix
    | IngestRecords
    | OsmAndLocation
    | OverlandBatch
    | OwnTracksMessage
    | IngestRecordsPathKeyDataBody
    | Unset = UNSET,
    content_encoding: IngestRecordsPathKeyContentEncoding | Unset = UNSET,
) -> Response[Problem]:
    """Push records with the key in the path

     Path-key form of `ingestRecordsFormat` (query, form or JSON bodies) for tracker apps and device
    firmware that can set only a host and path (Ecowitt Customized upload, 059). Same limits, rejections
    and native replies. An `eik_` key in the path needs the source's `allowPathKey` (else 404); the
    sender must be in `ingestAllowCidrs` (default: private ranges; else 403 INGEST_SOURCE_DENIED).

    **Scope:** open (no token; the credential is in the request).

    Args:
        source_id (str):
        key (str):
        format_ (IngestRecordsPathKeyFormat):
        content_encoding (IngestRecordsPathKeyContentEncoding | Unset):
        body (GpsLoggerFix | IngestRecords | OsmAndLocation | OverlandBatch | OwnTracksMessage |
            Unset):
        body (IngestRecordsPathKeyDataBody | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        key=key,
        format_=format_,
        body=body,
        content_encoding=content_encoding,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    key: str,
    format_: IngestRecordsPathKeyFormat,
    *,
    client: AuthenticatedClient,
    body: GpsLoggerFix
    | IngestRecords
    | OsmAndLocation
    | OverlandBatch
    | OwnTracksMessage
    | IngestRecordsPathKeyDataBody
    | Unset = UNSET,
    content_encoding: IngestRecordsPathKeyContentEncoding | Unset = UNSET,
) -> Problem | None:
    """Push records with the key in the path

     Path-key form of `ingestRecordsFormat` (query, form or JSON bodies) for tracker apps and device
    firmware that can set only a host and path (Ecowitt Customized upload, 059). Same limits, rejections
    and native replies. An `eik_` key in the path needs the source's `allowPathKey` (else 404); the
    sender must be in `ingestAllowCidrs` (default: private ranges; else 403 INGEST_SOURCE_DENIED).

    **Scope:** open (no token; the credential is in the request).

    Args:
        source_id (str):
        key (str):
        format_ (IngestRecordsPathKeyFormat):
        content_encoding (IngestRecordsPathKeyContentEncoding | Unset):
        body (GpsLoggerFix | IngestRecords | OsmAndLocation | OverlandBatch | OwnTracksMessage |
            Unset):
        body (IngestRecordsPathKeyDataBody | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            key=key,
            format_=format_,
            client=client,
            body=body,
            content_encoding=content_encoding,
        )
    ).parsed
