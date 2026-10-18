from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.feature_collection import FeatureCollection
from ...models.ingest_outcome import IngestOutcome
from ...models.ingest_records import IngestRecords
from ...models.ingest_records_content_encoding import IngestRecordsContentEncoding
from ...models.ingest_records_data_body import IngestRecordsDataBody
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    *,
    body: FeatureCollection | IngestRecords | IngestRecordsDataBody | Unset = UNSET,
    content_encoding: IngestRecordsContentEncoding | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(content_encoding, Unset):
        headers["Content-Encoding"] = str(content_encoding)

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/ingest/{source_id}".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    if isinstance(body, FeatureCollection | IngestRecords):
        if isinstance(body, FeatureCollection):
            _kwargs["json"] = body.to_dict()
        else:
            _kwargs["json"] = body.to_dict()

        headers["Content-Type"] = "application/json"
    if isinstance(body, FeatureCollection):
        _kwargs["json"] = body.to_dict()

        headers["Content-Type"] = "application/geo+json"
    if isinstance(body, IngestRecordsDataBody):
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
    *,
    client: AuthenticatedClient,
    body: FeatureCollection | IngestRecords | IngestRecordsDataBody | Unset = UNSET,
    content_encoding: IngestRecordsContentEncoding | Unset = UNSET,
) -> Response[IngestOutcome | Problem]:
    """Push records into a source (inbound webhook)

     The `native` format, unless the source has a default format (059: a source created from a preset
    uses the preset's format here, so a form POST from a device lands in its mapping): a GeoJSON
    FeatureCollection (`id` → externalId; properties `kind` (optional with a single kind), `observedAt`,
    `eventTime`, `validUntil`, `tombstone`, `quality`; the rest → payload) or `{records:[...]}`. Max 5
    MB and 10,000 records. Credentials as for `ingestRecordsFormat` (ingest key header, Bearer, Basic,
    device key, admin token). Records enter the single writer as IngestRecords with the full v1.2
    pipeline; the response is the v1.2 `IngestOutcome`. Per-record rejections (e.g. MISSING_OBSERVED_AT
    under UPSTREAM) do not fail the request.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        content_encoding (IngestRecordsContentEncoding | Unset):
        body (FeatureCollection | IngestRecords):
        body (FeatureCollection):
        body (IngestRecordsDataBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IngestOutcome | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
        content_encoding=content_encoding,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: FeatureCollection | IngestRecords | IngestRecordsDataBody | Unset = UNSET,
    content_encoding: IngestRecordsContentEncoding | Unset = UNSET,
) -> IngestOutcome | Problem | None:
    """Push records into a source (inbound webhook)

     The `native` format, unless the source has a default format (059: a source created from a preset
    uses the preset's format here, so a form POST from a device lands in its mapping): a GeoJSON
    FeatureCollection (`id` → externalId; properties `kind` (optional with a single kind), `observedAt`,
    `eventTime`, `validUntil`, `tombstone`, `quality`; the rest → payload) or `{records:[...]}`. Max 5
    MB and 10,000 records. Credentials as for `ingestRecordsFormat` (ingest key header, Bearer, Basic,
    device key, admin token). Records enter the single writer as IngestRecords with the full v1.2
    pipeline; the response is the v1.2 `IngestOutcome`. Per-record rejections (e.g. MISSING_OBSERVED_AT
    under UPSTREAM) do not fail the request.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        content_encoding (IngestRecordsContentEncoding | Unset):
        body (FeatureCollection | IngestRecords):
        body (FeatureCollection):
        body (IngestRecordsDataBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IngestOutcome | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        body=body,
        content_encoding=content_encoding,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: FeatureCollection | IngestRecords | IngestRecordsDataBody | Unset = UNSET,
    content_encoding: IngestRecordsContentEncoding | Unset = UNSET,
) -> Response[IngestOutcome | Problem]:
    """Push records into a source (inbound webhook)

     The `native` format, unless the source has a default format (059: a source created from a preset
    uses the preset's format here, so a form POST from a device lands in its mapping): a GeoJSON
    FeatureCollection (`id` → externalId; properties `kind` (optional with a single kind), `observedAt`,
    `eventTime`, `validUntil`, `tombstone`, `quality`; the rest → payload) or `{records:[...]}`. Max 5
    MB and 10,000 records. Credentials as for `ingestRecordsFormat` (ingest key header, Bearer, Basic,
    device key, admin token). Records enter the single writer as IngestRecords with the full v1.2
    pipeline; the response is the v1.2 `IngestOutcome`. Per-record rejections (e.g. MISSING_OBSERVED_AT
    under UPSTREAM) do not fail the request.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        content_encoding (IngestRecordsContentEncoding | Unset):
        body (FeatureCollection | IngestRecords):
        body (FeatureCollection):
        body (IngestRecordsDataBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IngestOutcome | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
        content_encoding=content_encoding,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: FeatureCollection | IngestRecords | IngestRecordsDataBody | Unset = UNSET,
    content_encoding: IngestRecordsContentEncoding | Unset = UNSET,
) -> IngestOutcome | Problem | None:
    """Push records into a source (inbound webhook)

     The `native` format, unless the source has a default format (059: a source created from a preset
    uses the preset's format here, so a form POST from a device lands in its mapping): a GeoJSON
    FeatureCollection (`id` → externalId; properties `kind` (optional with a single kind), `observedAt`,
    `eventTime`, `validUntil`, `tombstone`, `quality`; the rest → payload) or `{records:[...]}`. Max 5
    MB and 10,000 records. Credentials as for `ingestRecordsFormat` (ingest key header, Bearer, Basic,
    device key, admin token). Records enter the single writer as IngestRecords with the full v1.2
    pipeline; the response is the v1.2 `IngestOutcome`. Per-record rejections (e.g. MISSING_OBSERVED_AT
    under UPSTREAM) do not fail the request.

    **Scope:** ingest key (or an `admin` token).

    Args:
        source_id (str):
        content_encoding (IngestRecordsContentEncoding | Unset):
        body (FeatureCollection | IngestRecords):
        body (FeatureCollection):
        body (IngestRecordsDataBody):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IngestOutcome | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
            content_encoding=content_encoding,
        )
    ).parsed
