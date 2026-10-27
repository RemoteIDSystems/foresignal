from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.archive import Archive
from ...models.archive_run_request import ArchiveRunRequest
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    body: ArchiveRunRequest | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/archives/run",
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Archive | Problem | None:
    if response.status_code == 202:
        response_202 = Archive.from_dict(response.json())

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
) -> Response[Archive | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ArchiveRunRequest | Unset = UNSET,
) -> Response[Archive | Problem]:
    """Run an archival pass

     Starts an archival pass now: select data older than the cutoff, export to the target with a verified
    checksum, mark archived, then purge from the hot store (B3/179). Returns the created segment.
    Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** the pass plans a retention run with the archived sources' STANDARD windows capped to now
    − cutoff (no other source's window moves), writes exactly the rows that plan removes (and the rows
    of TRACK partitions about to be dropped) as one gzip NDJSON file per source, offloads each to the
    tier and verifies it there (SHA-256 read back on the external disk; ETag and size on S3/Glacier),
    records the manifest (`complete`), and only then runs the planned retention pass, so the purge goes
    through the existing delete, drop and head-purge paths. A failed export or verification purges
    nothing, marks the segment `failed` and raises the operational alert `system.archive_failed`. The
    pass holds the retention job's single-run guard: while retention or another archive pass runs, the
    answer is 409 ALREADY_RUNNING. 409 ARCHIVE_NOT_CONFIGURED when the archive is off, or an s3/glacier
    target lacks its settings or secrets.

    Args:
        body (ArchiveRunRequest | Unset): Run an archival pass now

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Archive | Problem]
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
    body: ArchiveRunRequest | Unset = UNSET,
) -> Archive | Problem | None:
    """Run an archival pass

     Starts an archival pass now: select data older than the cutoff, export to the target with a verified
    checksum, mark archived, then purge from the hot store (B3/179). Returns the created segment.
    Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** the pass plans a retention run with the archived sources' STANDARD windows capped to now
    − cutoff (no other source's window moves), writes exactly the rows that plan removes (and the rows
    of TRACK partitions about to be dropped) as one gzip NDJSON file per source, offloads each to the
    tier and verifies it there (SHA-256 read back on the external disk; ETag and size on S3/Glacier),
    records the manifest (`complete`), and only then runs the planned retention pass, so the purge goes
    through the existing delete, drop and head-purge paths. A failed export or verification purges
    nothing, marks the segment `failed` and raises the operational alert `system.archive_failed`. The
    pass holds the retention job's single-run guard: while retention or another archive pass runs, the
    answer is 409 ALREADY_RUNNING. 409 ARCHIVE_NOT_CONFIGURED when the archive is off, or an s3/glacier
    target lacks its settings or secrets.

    Args:
        body (ArchiveRunRequest | Unset): Run an archival pass now

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Archive | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ArchiveRunRequest | Unset = UNSET,
) -> Response[Archive | Problem]:
    """Run an archival pass

     Starts an archival pass now: select data older than the cutoff, export to the target with a verified
    checksum, mark archived, then purge from the hot store (B3/179). Returns the created segment.
    Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** the pass plans a retention run with the archived sources' STANDARD windows capped to now
    − cutoff (no other source's window moves), writes exactly the rows that plan removes (and the rows
    of TRACK partitions about to be dropped) as one gzip NDJSON file per source, offloads each to the
    tier and verifies it there (SHA-256 read back on the external disk; ETag and size on S3/Glacier),
    records the manifest (`complete`), and only then runs the planned retention pass, so the purge goes
    through the existing delete, drop and head-purge paths. A failed export or verification purges
    nothing, marks the segment `failed` and raises the operational alert `system.archive_failed`. The
    pass holds the retention job's single-run guard: while retention or another archive pass runs, the
    answer is 409 ALREADY_RUNNING. 409 ARCHIVE_NOT_CONFIGURED when the archive is off, or an s3/glacier
    target lacks its settings or secrets.

    Args:
        body (ArchiveRunRequest | Unset): Run an archival pass now

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Archive | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ArchiveRunRequest | Unset = UNSET,
) -> Archive | Problem | None:
    """Run an archival pass

     Starts an archival pass now: select data older than the cutoff, export to the target with a verified
    checksum, mark archived, then purge from the hot store (B3/179). Returns the created segment.
    Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** the pass plans a retention run with the archived sources' STANDARD windows capped to now
    − cutoff (no other source's window moves), writes exactly the rows that plan removes (and the rows
    of TRACK partitions about to be dropped) as one gzip NDJSON file per source, offloads each to the
    tier and verifies it there (SHA-256 read back on the external disk; ETag and size on S3/Glacier),
    records the manifest (`complete`), and only then runs the planned retention pass, so the purge goes
    through the existing delete, drop and head-purge paths. A failed export or verification purges
    nothing, marks the segment `failed` and raises the operational alert `system.archive_failed`. The
    pass holds the retention job's single-run guard: while retention or another archive pass runs, the
    answer is 409 ALREADY_RUNNING. 409 ARCHIVE_NOT_CONFIGURED when the archive is off, or an s3/glacier
    target lacks its settings or secrets.

    Args:
        body (ArchiveRunRequest | Unset): Run an archival pass now

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Archive | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
