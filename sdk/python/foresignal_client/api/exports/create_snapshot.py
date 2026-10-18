from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.snapshot import Snapshot
from ...models.snapshot_input import SnapshotInput
from ...types import Response


def _get_kwargs(
    *,
    body: SnapshotInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/snapshots",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Snapshot | None:
    if response.status_code == 202:
        response_202 = Snapshot.from_dict(response.json())

        return response_202

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | Snapshot]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: SnapshotInput,
) -> Response[Problem | Snapshot]:
    """Create an atSeq snapshot

     Rebuilds state at seq S (default the current watermarkSeq, or `asOf` translated with S(T)) from
    record_version per tier, writing GeoParquet plus a signed manifest. Below a source's floorSeq rows
    come from held copies, else `400 SNAPSHOT_BEYOND_RETENTION`. Validity is judged at the transaction
    time of S (its ingested_at, or the requested asOf); a source whose state at S lies before its
    retention horizon is read from the copies of holds covering it. Private sources need
    `includePrivate=true`; FORBIDDEN and LOCAL_ONLY sources need `personalUse=true`, as for exports.
    Snapshots are export jobs of kind SNAPSHOT (list, poll and download through `/v1/exports`) and never
    expire unless `expiresAt` is set.

    **Scope:** `admin`.

    Args:
        body (SnapshotInput): Create an atSeq snapshot: state at seq S (default the current
            watermarkSeq, or asOf translated with S(T)) (111)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Snapshot]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: SnapshotInput,
) -> Problem | Snapshot | None:
    """Create an atSeq snapshot

     Rebuilds state at seq S (default the current watermarkSeq, or `asOf` translated with S(T)) from
    record_version per tier, writing GeoParquet plus a signed manifest. Below a source's floorSeq rows
    come from held copies, else `400 SNAPSHOT_BEYOND_RETENTION`. Validity is judged at the transaction
    time of S (its ingested_at, or the requested asOf); a source whose state at S lies before its
    retention horizon is read from the copies of holds covering it. Private sources need
    `includePrivate=true`; FORBIDDEN and LOCAL_ONLY sources need `personalUse=true`, as for exports.
    Snapshots are export jobs of kind SNAPSHOT (list, poll and download through `/v1/exports`) and never
    expire unless `expiresAt` is set.

    **Scope:** `admin`.

    Args:
        body (SnapshotInput): Create an atSeq snapshot: state at seq S (default the current
            watermarkSeq, or asOf translated with S(T)) (111)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Snapshot
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: SnapshotInput,
) -> Response[Problem | Snapshot]:
    """Create an atSeq snapshot

     Rebuilds state at seq S (default the current watermarkSeq, or `asOf` translated with S(T)) from
    record_version per tier, writing GeoParquet plus a signed manifest. Below a source's floorSeq rows
    come from held copies, else `400 SNAPSHOT_BEYOND_RETENTION`. Validity is judged at the transaction
    time of S (its ingested_at, or the requested asOf); a source whose state at S lies before its
    retention horizon is read from the copies of holds covering it. Private sources need
    `includePrivate=true`; FORBIDDEN and LOCAL_ONLY sources need `personalUse=true`, as for exports.
    Snapshots are export jobs of kind SNAPSHOT (list, poll and download through `/v1/exports`) and never
    expire unless `expiresAt` is set.

    **Scope:** `admin`.

    Args:
        body (SnapshotInput): Create an atSeq snapshot: state at seq S (default the current
            watermarkSeq, or asOf translated with S(T)) (111)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Snapshot]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: SnapshotInput,
) -> Problem | Snapshot | None:
    """Create an atSeq snapshot

     Rebuilds state at seq S (default the current watermarkSeq, or `asOf` translated with S(T)) from
    record_version per tier, writing GeoParquet plus a signed manifest. Below a source's floorSeq rows
    come from held copies, else `400 SNAPSHOT_BEYOND_RETENTION`. Validity is judged at the transaction
    time of S (its ingested_at, or the requested asOf); a source whose state at S lies before its
    retention horizon is read from the copies of holds covering it. Private sources need
    `includePrivate=true`; FORBIDDEN and LOCAL_ONLY sources need `personalUse=true`, as for exports.
    Snapshots are export jobs of kind SNAPSHOT (list, poll and download through `/v1/exports`) and never
    expire unless `expiresAt` is set.

    **Scope:** `admin`.

    Args:
        body (SnapshotInput): Create an atSeq snapshot: state at seq S (default the current
            watermarkSeq, or asOf translated with S(T)) (111)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Snapshot
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
