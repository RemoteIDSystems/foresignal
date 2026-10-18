from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.cloud_event import CloudEvent
from ...models.feature_collection import FeatureCollection
from ...models.ingest_outcome import IngestOutcome
from ...models.ingest_records_format_content_encoding import (
    IngestRecordsFormatContentEncoding,
)
from ...models.ingest_records_format_data_body import IngestRecordsFormatDataBody
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    format_: str,
    *,
    body: FeatureCollection
    | CloudEvent
    | list[CloudEvent]
    | IngestRecordsFormatDataBody
    | Unset = UNSET,
    x_signature_256: str | Unset = UNSET,
    webhook_id: str | Unset = UNSET,
    webhook_timestamp: str | Unset = UNSET,
    webhook_signature: str | Unset = UNSET,
    x_foresignal_timestamp: str | Unset = UNSET,
    x_foresignal_signature: str | Unset = UNSET,
    ce_id: str | Unset = UNSET,
    ce_source: str | Unset = UNSET,
    ce_type: str | Unset = UNSET,
    ce_subject: str | Unset = UNSET,
    ce_time: str | Unset = UNSET,
    ce_specversion: str | Unset = UNSET,
    x_limit_u: str | Unset = UNSET,
    x_limit_d: str | Unset = UNSET,
    x_foresignal_external_id: str | Unset = UNSET,
    content_encoding: IngestRecordsFormatContentEncoding | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(x_signature_256, Unset):
        headers["X-Signature-256"] = x_signature_256

    if not isinstance(webhook_id, Unset):
        headers["webhook-id"] = webhook_id

    if not isinstance(webhook_timestamp, Unset):
        headers["webhook-timestamp"] = webhook_timestamp

    if not isinstance(webhook_signature, Unset):
        headers["webhook-signature"] = webhook_signature

    if not isinstance(x_foresignal_timestamp, Unset):
        headers["X-Foresignal-Timestamp"] = x_foresignal_timestamp

    if not isinstance(x_foresignal_signature, Unset):
        headers["X-Foresignal-Signature"] = x_foresignal_signature

    if not isinstance(ce_id, Unset):
        headers["ce-id"] = ce_id

    if not isinstance(ce_source, Unset):
        headers["ce-source"] = ce_source

    if not isinstance(ce_type, Unset):
        headers["ce-type"] = ce_type

    if not isinstance(ce_subject, Unset):
        headers["ce-subject"] = ce_subject

    if not isinstance(ce_time, Unset):
        headers["ce-time"] = ce_time

    if not isinstance(ce_specversion, Unset):
        headers["ce-specversion"] = ce_specversion

    if not isinstance(x_limit_u, Unset):
        headers["X-Limit-U"] = x_limit_u

    if not isinstance(x_limit_d, Unset):
        headers["X-Limit-D"] = x_limit_d

    if not isinstance(x_foresignal_external_id, Unset):
        headers["X-Foresignal-External-Id"] = x_foresignal_external_id

    if not isinstance(content_encoding, Unset):
        headers["Content-Encoding"] = str(content_encoding)

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/ingest/{source_id}/{format_}".format(
            source_id=quote(str(source_id), safe=""),
            format_=quote(str(format_), safe=""),
        ),
    }

    if isinstance(body, FeatureCollection):
        _kwargs["json"] = body.to_dict()

        headers["Content-Type"] = "application/geo+json"
    if isinstance(body, CloudEvent):
        _kwargs["json"] = body.to_dict()

        headers["Content-Type"] = "application/cloudevents+json"
    if isinstance(body, list[CloudEvent]):
        _kwargs["json"] = []
        for body_item_data in body:
            body_item = body_item_data.to_dict()
            _kwargs["json"].append(body_item)

        headers["Content-Type"] = "application/cloudevents-batch+json"
    if isinstance(body, IngestRecordsFormatDataBody):
        _kwargs["data"] = body.to_dict()
        headers["Content-Type"] = "application/x-www-form-urlencoded"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> IngestOutcome | Problem | None:
    if response.status_code == 200:
        response_200 = IngestOutcome.from_dict(response.json())

        return response_200

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

    if response.status_code == 415:
        response_415 = Problem.from_dict(response.json())

        return response_415

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
) -> Response[IngestOutcome | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    body: FeatureCollection
    | CloudEvent
    | list[CloudEvent]
    | IngestRecordsFormatDataBody
    | Unset = UNSET,
    x_signature_256: str | Unset = UNSET,
    webhook_id: str | Unset = UNSET,
    webhook_timestamp: str | Unset = UNSET,
    webhook_signature: str | Unset = UNSET,
    x_foresignal_timestamp: str | Unset = UNSET,
    x_foresignal_signature: str | Unset = UNSET,
    ce_id: str | Unset = UNSET,
    ce_source: str | Unset = UNSET,
    ce_type: str | Unset = UNSET,
    ce_subject: str | Unset = UNSET,
    ce_time: str | Unset = UNSET,
    ce_specversion: str | Unset = UNSET,
    x_limit_u: str | Unset = UNSET,
    x_limit_d: str | Unset = UNSET,
    x_foresignal_external_id: str | Unset = UNSET,
    content_encoding: IngestRecordsFormatContentEncoding | Unset = UNSET,
) -> Response[IngestOutcome | Problem]:
    """Push records in a named format

     Parses the body with the named `IngestFormat` and submits one batch to the single writer (same
    versioning, policies, live/touch semantics, change stream, triggers and retention as polled data).
    Credentials: `X-Foresignal-Ingest-Key`, `Authorization: Bearer eik_…`, Basic with the key as
    password, a device key (039, bound to one entity), or an `admin` token; a configured signature
    verifier must also pass unless `keyOptional`. 200 when the request is valid even with per-record
    rejections (MISSING_OBSERVED_AT, LOW_ACCURACY, …). OwnTracks, Overland, OsmAnd and GPSLogger get
    their native reply shapes; the device presets (059) reply as their firmware expects (ecowitt an
    empty 200, wu-pws `success`, gmc `OK.`). `Content-Encoding: gzip` is accepted (decompressed size
    capped at 5 MB → 413). The `same` format (086) reads one SAME header per line (multimon-ng `EAS:
    ZCZC-…` output or bare headers), ignores `NNNN`, collapses an identical header repeated within 10 s,
    and stores `same-alert` records on the named counties (the state's counties for `PSS000`) with
    `validUntil` = issue + purge. The `chirpstack-v4` format maps `?event=up`, moves the device on
    `?event=location`, acknowledges other events and refuses Protobuf (415). An `admin` token is the
    operator and is not asked for a sender signature.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        x_signature_256 (str | Unset):
        webhook_id (str | Unset):
        webhook_timestamp (str | Unset):
        webhook_signature (str | Unset):
        x_foresignal_timestamp (str | Unset):
        x_foresignal_signature (str | Unset):
        ce_id (str | Unset):
        ce_source (str | Unset):
        ce_type (str | Unset):
        ce_subject (str | Unset):
        ce_time (str | Unset):
        ce_specversion (str | Unset):
        x_limit_u (str | Unset):
        x_limit_d (str | Unset):
        x_foresignal_external_id (str | Unset):
        content_encoding (IngestRecordsFormatContentEncoding | Unset):
        body (FeatureCollection):
        body (CloudEvent): CloudEvents 1.0 structured-mode event accepted by the `cloudevents`
            format (024)
        body (list[CloudEvent]):
        body (IngestRecordsFormatDataBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IngestOutcome | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        format_=format_,
        body=body,
        x_signature_256=x_signature_256,
        webhook_id=webhook_id,
        webhook_timestamp=webhook_timestamp,
        webhook_signature=webhook_signature,
        x_foresignal_timestamp=x_foresignal_timestamp,
        x_foresignal_signature=x_foresignal_signature,
        ce_id=ce_id,
        ce_source=ce_source,
        ce_type=ce_type,
        ce_subject=ce_subject,
        ce_time=ce_time,
        ce_specversion=ce_specversion,
        x_limit_u=x_limit_u,
        x_limit_d=x_limit_d,
        x_foresignal_external_id=x_foresignal_external_id,
        content_encoding=content_encoding,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    body: FeatureCollection
    | CloudEvent
    | list[CloudEvent]
    | IngestRecordsFormatDataBody
    | Unset = UNSET,
    x_signature_256: str | Unset = UNSET,
    webhook_id: str | Unset = UNSET,
    webhook_timestamp: str | Unset = UNSET,
    webhook_signature: str | Unset = UNSET,
    x_foresignal_timestamp: str | Unset = UNSET,
    x_foresignal_signature: str | Unset = UNSET,
    ce_id: str | Unset = UNSET,
    ce_source: str | Unset = UNSET,
    ce_type: str | Unset = UNSET,
    ce_subject: str | Unset = UNSET,
    ce_time: str | Unset = UNSET,
    ce_specversion: str | Unset = UNSET,
    x_limit_u: str | Unset = UNSET,
    x_limit_d: str | Unset = UNSET,
    x_foresignal_external_id: str | Unset = UNSET,
    content_encoding: IngestRecordsFormatContentEncoding | Unset = UNSET,
) -> IngestOutcome | Problem | None:
    """Push records in a named format

     Parses the body with the named `IngestFormat` and submits one batch to the single writer (same
    versioning, policies, live/touch semantics, change stream, triggers and retention as polled data).
    Credentials: `X-Foresignal-Ingest-Key`, `Authorization: Bearer eik_…`, Basic with the key as
    password, a device key (039, bound to one entity), or an `admin` token; a configured signature
    verifier must also pass unless `keyOptional`. 200 when the request is valid even with per-record
    rejections (MISSING_OBSERVED_AT, LOW_ACCURACY, …). OwnTracks, Overland, OsmAnd and GPSLogger get
    their native reply shapes; the device presets (059) reply as their firmware expects (ecowitt an
    empty 200, wu-pws `success`, gmc `OK.`). `Content-Encoding: gzip` is accepted (decompressed size
    capped at 5 MB → 413). The `same` format (086) reads one SAME header per line (multimon-ng `EAS:
    ZCZC-…` output or bare headers), ignores `NNNN`, collapses an identical header repeated within 10 s,
    and stores `same-alert` records on the named counties (the state's counties for `PSS000`) with
    `validUntil` = issue + purge. The `chirpstack-v4` format maps `?event=up`, moves the device on
    `?event=location`, acknowledges other events and refuses Protobuf (415). An `admin` token is the
    operator and is not asked for a sender signature.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        x_signature_256 (str | Unset):
        webhook_id (str | Unset):
        webhook_timestamp (str | Unset):
        webhook_signature (str | Unset):
        x_foresignal_timestamp (str | Unset):
        x_foresignal_signature (str | Unset):
        ce_id (str | Unset):
        ce_source (str | Unset):
        ce_type (str | Unset):
        ce_subject (str | Unset):
        ce_time (str | Unset):
        ce_specversion (str | Unset):
        x_limit_u (str | Unset):
        x_limit_d (str | Unset):
        x_foresignal_external_id (str | Unset):
        content_encoding (IngestRecordsFormatContentEncoding | Unset):
        body (FeatureCollection):
        body (CloudEvent): CloudEvents 1.0 structured-mode event accepted by the `cloudevents`
            format (024)
        body (list[CloudEvent]):
        body (IngestRecordsFormatDataBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IngestOutcome | Problem
    """

    return sync_detailed(
        source_id=source_id,
        format_=format_,
        client=client,
        body=body,
        x_signature_256=x_signature_256,
        webhook_id=webhook_id,
        webhook_timestamp=webhook_timestamp,
        webhook_signature=webhook_signature,
        x_foresignal_timestamp=x_foresignal_timestamp,
        x_foresignal_signature=x_foresignal_signature,
        ce_id=ce_id,
        ce_source=ce_source,
        ce_type=ce_type,
        ce_subject=ce_subject,
        ce_time=ce_time,
        ce_specversion=ce_specversion,
        x_limit_u=x_limit_u,
        x_limit_d=x_limit_d,
        x_foresignal_external_id=x_foresignal_external_id,
        content_encoding=content_encoding,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    body: FeatureCollection
    | CloudEvent
    | list[CloudEvent]
    | IngestRecordsFormatDataBody
    | Unset = UNSET,
    x_signature_256: str | Unset = UNSET,
    webhook_id: str | Unset = UNSET,
    webhook_timestamp: str | Unset = UNSET,
    webhook_signature: str | Unset = UNSET,
    x_foresignal_timestamp: str | Unset = UNSET,
    x_foresignal_signature: str | Unset = UNSET,
    ce_id: str | Unset = UNSET,
    ce_source: str | Unset = UNSET,
    ce_type: str | Unset = UNSET,
    ce_subject: str | Unset = UNSET,
    ce_time: str | Unset = UNSET,
    ce_specversion: str | Unset = UNSET,
    x_limit_u: str | Unset = UNSET,
    x_limit_d: str | Unset = UNSET,
    x_foresignal_external_id: str | Unset = UNSET,
    content_encoding: IngestRecordsFormatContentEncoding | Unset = UNSET,
) -> Response[IngestOutcome | Problem]:
    """Push records in a named format

     Parses the body with the named `IngestFormat` and submits one batch to the single writer (same
    versioning, policies, live/touch semantics, change stream, triggers and retention as polled data).
    Credentials: `X-Foresignal-Ingest-Key`, `Authorization: Bearer eik_…`, Basic with the key as
    password, a device key (039, bound to one entity), or an `admin` token; a configured signature
    verifier must also pass unless `keyOptional`. 200 when the request is valid even with per-record
    rejections (MISSING_OBSERVED_AT, LOW_ACCURACY, …). OwnTracks, Overland, OsmAnd and GPSLogger get
    their native reply shapes; the device presets (059) reply as their firmware expects (ecowitt an
    empty 200, wu-pws `success`, gmc `OK.`). `Content-Encoding: gzip` is accepted (decompressed size
    capped at 5 MB → 413). The `same` format (086) reads one SAME header per line (multimon-ng `EAS:
    ZCZC-…` output or bare headers), ignores `NNNN`, collapses an identical header repeated within 10 s,
    and stores `same-alert` records on the named counties (the state's counties for `PSS000`) with
    `validUntil` = issue + purge. The `chirpstack-v4` format maps `?event=up`, moves the device on
    `?event=location`, acknowledges other events and refuses Protobuf (415). An `admin` token is the
    operator and is not asked for a sender signature.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        x_signature_256 (str | Unset):
        webhook_id (str | Unset):
        webhook_timestamp (str | Unset):
        webhook_signature (str | Unset):
        x_foresignal_timestamp (str | Unset):
        x_foresignal_signature (str | Unset):
        ce_id (str | Unset):
        ce_source (str | Unset):
        ce_type (str | Unset):
        ce_subject (str | Unset):
        ce_time (str | Unset):
        ce_specversion (str | Unset):
        x_limit_u (str | Unset):
        x_limit_d (str | Unset):
        x_foresignal_external_id (str | Unset):
        content_encoding (IngestRecordsFormatContentEncoding | Unset):
        body (FeatureCollection):
        body (CloudEvent): CloudEvents 1.0 structured-mode event accepted by the `cloudevents`
            format (024)
        body (list[CloudEvent]):
        body (IngestRecordsFormatDataBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IngestOutcome | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        format_=format_,
        body=body,
        x_signature_256=x_signature_256,
        webhook_id=webhook_id,
        webhook_timestamp=webhook_timestamp,
        webhook_signature=webhook_signature,
        x_foresignal_timestamp=x_foresignal_timestamp,
        x_foresignal_signature=x_foresignal_signature,
        ce_id=ce_id,
        ce_source=ce_source,
        ce_type=ce_type,
        ce_subject=ce_subject,
        ce_time=ce_time,
        ce_specversion=ce_specversion,
        x_limit_u=x_limit_u,
        x_limit_d=x_limit_d,
        x_foresignal_external_id=x_foresignal_external_id,
        content_encoding=content_encoding,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    format_: str,
    *,
    client: AuthenticatedClient,
    body: FeatureCollection
    | CloudEvent
    | list[CloudEvent]
    | IngestRecordsFormatDataBody
    | Unset = UNSET,
    x_signature_256: str | Unset = UNSET,
    webhook_id: str | Unset = UNSET,
    webhook_timestamp: str | Unset = UNSET,
    webhook_signature: str | Unset = UNSET,
    x_foresignal_timestamp: str | Unset = UNSET,
    x_foresignal_signature: str | Unset = UNSET,
    ce_id: str | Unset = UNSET,
    ce_source: str | Unset = UNSET,
    ce_type: str | Unset = UNSET,
    ce_subject: str | Unset = UNSET,
    ce_time: str | Unset = UNSET,
    ce_specversion: str | Unset = UNSET,
    x_limit_u: str | Unset = UNSET,
    x_limit_d: str | Unset = UNSET,
    x_foresignal_external_id: str | Unset = UNSET,
    content_encoding: IngestRecordsFormatContentEncoding | Unset = UNSET,
) -> IngestOutcome | Problem | None:
    """Push records in a named format

     Parses the body with the named `IngestFormat` and submits one batch to the single writer (same
    versioning, policies, live/touch semantics, change stream, triggers and retention as polled data).
    Credentials: `X-Foresignal-Ingest-Key`, `Authorization: Bearer eik_…`, Basic with the key as
    password, a device key (039, bound to one entity), or an `admin` token; a configured signature
    verifier must also pass unless `keyOptional`. 200 when the request is valid even with per-record
    rejections (MISSING_OBSERVED_AT, LOW_ACCURACY, …). OwnTracks, Overland, OsmAnd and GPSLogger get
    their native reply shapes; the device presets (059) reply as their firmware expects (ecowitt an
    empty 200, wu-pws `success`, gmc `OK.`). `Content-Encoding: gzip` is accepted (decompressed size
    capped at 5 MB → 413). The `same` format (086) reads one SAME header per line (multimon-ng `EAS:
    ZCZC-…` output or bare headers), ignores `NNNN`, collapses an identical header repeated within 10 s,
    and stores `same-alert` records on the named counties (the state's counties for `PSS000`) with
    `validUntil` = issue + purge. The `chirpstack-v4` format maps `?event=up`, moves the device on
    `?event=location`, acknowledges other events and refuses Protobuf (415). An `admin` token is the
    operator and is not asked for a sender signature.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        format_ (str):  Example: alertmanager.
        x_signature_256 (str | Unset):
        webhook_id (str | Unset):
        webhook_timestamp (str | Unset):
        webhook_signature (str | Unset):
        x_foresignal_timestamp (str | Unset):
        x_foresignal_signature (str | Unset):
        ce_id (str | Unset):
        ce_source (str | Unset):
        ce_type (str | Unset):
        ce_subject (str | Unset):
        ce_time (str | Unset):
        ce_specversion (str | Unset):
        x_limit_u (str | Unset):
        x_limit_d (str | Unset):
        x_foresignal_external_id (str | Unset):
        content_encoding (IngestRecordsFormatContentEncoding | Unset):
        body (FeatureCollection):
        body (CloudEvent): CloudEvents 1.0 structured-mode event accepted by the `cloudevents`
            format (024)
        body (list[CloudEvent]):
        body (IngestRecordsFormatDataBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IngestOutcome | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            format_=format_,
            client=client,
            body=body,
            x_signature_256=x_signature_256,
            webhook_id=webhook_id,
            webhook_timestamp=webhook_timestamp,
            webhook_signature=webhook_signature,
            x_foresignal_timestamp=x_foresignal_timestamp,
            x_foresignal_signature=x_foresignal_signature,
            ce_id=ce_id,
            ce_source=ce_source,
            ce_type=ce_type,
            ce_subject=ce_subject,
            ce_time=ce_time,
            ce_specversion=ce_specversion,
            x_limit_u=x_limit_u,
            x_limit_d=x_limit_d,
            x_foresignal_external_id=x_foresignal_external_id,
            content_encoding=content_encoding,
        )
    ).parsed
