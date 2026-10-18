from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.federation_ingest_batch import FederationIngestBatch
from ...models.federation_ingest_content_encoding import FederationIngestContentEncoding
from ...models.federation_ingest_result import FederationIngestResult
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    peer_id: str,
    *,
    body: FederationIngestBatch,
    foresignal_signature: str,
    content_encoding: FederationIngestContentEncoding | Unset = UNSET,
    foresignal_key: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    headers["Foresignal-Signature"] = foresignal_signature

    if not isinstance(content_encoding, Unset):
        headers["Content-Encoding"] = str(content_encoding)

    if not isinstance(foresignal_key, Unset):
        headers["Foresignal-Key"] = foresignal_key

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/federation/ingest/{peer_id}".format(
            peer_id=quote(str(peer_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> FederationIngestResult | Problem | None:
    if response.status_code == 200:
        response_200 = FederationIngestResult.from_dict(response.json())

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

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[FederationIngestResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    peer_id: str,
    *,
    client: AuthenticatedClient,
    body: FederationIngestBatch,
    foresignal_signature: str,
    content_encoding: FederationIngestContentEncoding | Unset = UNSET,
    foresignal_key: str | Unset = UNSET,
) -> Response[FederationIngestResult | Problem]:
    """Receive a batch from an edge

     Hub endpoint for edge relays (peer row in mode RECEIVE): up to 1,000 version items with provenance,
    optionally gzip-encoded. The batch body must carry the edge's detached JWS in `Foresignal-
    Signature`, verified against the pinned key (`403 PEER_KEY_MISMATCH`). Idempotent on (origin_ref,
    origin_seq); the same loop, hop, dedup, licence and private checks as PULL apply. LOCAL_ONLY records
    are accepted only from `sameOperator` peers and stay LOCAL_ONLY.

    **Scope:** `peer` (peer ingest key `fsp_…` sent as a Bearer token).

    Args:
        peer_id (str):
        foresignal_signature (str):
        content_encoding (FederationIngestContentEncoding | Unset):
        foresignal_key (str | Unset):
        body (FederationIngestBatch): Edge-to-hub ingest batch (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationIngestResult | Problem]
    """

    kwargs = _get_kwargs(
        peer_id=peer_id,
        body=body,
        foresignal_signature=foresignal_signature,
        content_encoding=content_encoding,
        foresignal_key=foresignal_key,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    peer_id: str,
    *,
    client: AuthenticatedClient,
    body: FederationIngestBatch,
    foresignal_signature: str,
    content_encoding: FederationIngestContentEncoding | Unset = UNSET,
    foresignal_key: str | Unset = UNSET,
) -> FederationIngestResult | Problem | None:
    """Receive a batch from an edge

     Hub endpoint for edge relays (peer row in mode RECEIVE): up to 1,000 version items with provenance,
    optionally gzip-encoded. The batch body must carry the edge's detached JWS in `Foresignal-
    Signature`, verified against the pinned key (`403 PEER_KEY_MISMATCH`). Idempotent on (origin_ref,
    origin_seq); the same loop, hop, dedup, licence and private checks as PULL apply. LOCAL_ONLY records
    are accepted only from `sameOperator` peers and stay LOCAL_ONLY.

    **Scope:** `peer` (peer ingest key `fsp_…` sent as a Bearer token).

    Args:
        peer_id (str):
        foresignal_signature (str):
        content_encoding (FederationIngestContentEncoding | Unset):
        foresignal_key (str | Unset):
        body (FederationIngestBatch): Edge-to-hub ingest batch (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationIngestResult | Problem
    """

    return sync_detailed(
        peer_id=peer_id,
        client=client,
        body=body,
        foresignal_signature=foresignal_signature,
        content_encoding=content_encoding,
        foresignal_key=foresignal_key,
    ).parsed


async def asyncio_detailed(
    peer_id: str,
    *,
    client: AuthenticatedClient,
    body: FederationIngestBatch,
    foresignal_signature: str,
    content_encoding: FederationIngestContentEncoding | Unset = UNSET,
    foresignal_key: str | Unset = UNSET,
) -> Response[FederationIngestResult | Problem]:
    """Receive a batch from an edge

     Hub endpoint for edge relays (peer row in mode RECEIVE): up to 1,000 version items with provenance,
    optionally gzip-encoded. The batch body must carry the edge's detached JWS in `Foresignal-
    Signature`, verified against the pinned key (`403 PEER_KEY_MISMATCH`). Idempotent on (origin_ref,
    origin_seq); the same loop, hop, dedup, licence and private checks as PULL apply. LOCAL_ONLY records
    are accepted only from `sameOperator` peers and stay LOCAL_ONLY.

    **Scope:** `peer` (peer ingest key `fsp_…` sent as a Bearer token).

    Args:
        peer_id (str):
        foresignal_signature (str):
        content_encoding (FederationIngestContentEncoding | Unset):
        foresignal_key (str | Unset):
        body (FederationIngestBatch): Edge-to-hub ingest batch (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationIngestResult | Problem]
    """

    kwargs = _get_kwargs(
        peer_id=peer_id,
        body=body,
        foresignal_signature=foresignal_signature,
        content_encoding=content_encoding,
        foresignal_key=foresignal_key,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    peer_id: str,
    *,
    client: AuthenticatedClient,
    body: FederationIngestBatch,
    foresignal_signature: str,
    content_encoding: FederationIngestContentEncoding | Unset = UNSET,
    foresignal_key: str | Unset = UNSET,
) -> FederationIngestResult | Problem | None:
    """Receive a batch from an edge

     Hub endpoint for edge relays (peer row in mode RECEIVE): up to 1,000 version items with provenance,
    optionally gzip-encoded. The batch body must carry the edge's detached JWS in `Foresignal-
    Signature`, verified against the pinned key (`403 PEER_KEY_MISMATCH`). Idempotent on (origin_ref,
    origin_seq); the same loop, hop, dedup, licence and private checks as PULL apply. LOCAL_ONLY records
    are accepted only from `sameOperator` peers and stay LOCAL_ONLY.

    **Scope:** `peer` (peer ingest key `fsp_…` sent as a Bearer token).

    Args:
        peer_id (str):
        foresignal_signature (str):
        content_encoding (FederationIngestContentEncoding | Unset):
        foresignal_key (str | Unset):
        body (FederationIngestBatch): Edge-to-hub ingest batch (113)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationIngestResult | Problem
    """

    return (
        await asyncio_detailed(
            peer_id=peer_id,
            client=client,
            body=body,
            foresignal_signature=foresignal_signature,
            content_encoding=content_encoding,
            foresignal_key=foresignal_key,
        )
    ).parsed
