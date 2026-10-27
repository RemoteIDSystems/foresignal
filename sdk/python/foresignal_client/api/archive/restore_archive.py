from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.archive import Archive
from ...models.archive_restore_request import ArchiveRestoreRequest
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    id: str,
    *,
    body: ArchiveRestoreRequest | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/archives/{id}/restore".format(
            id=quote(str(id), safe=""),
        ),
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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

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
    id: str,
    *,
    client: AuthenticatedClient,
    body: ArchiveRestoreRequest | Unset = UNSET,
) -> Response[Archive | Problem]:
    """Restore an archive

     Pulls an archive segment back from cold storage into the hot store (B3/179). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** `confirm: true` is required (400 otherwise). Each file is pulled from its tier and its
    SHA-256 checked, then its rows are loaded into the restore area `record_version_restored` (keyed by
    the archive id), not back into record_version, whose partitions for those seqs are gone and whose
    retention would delete them again. A Glacier segment stays `restoring` while S3 thaws its objects
    (typically hours); the runner asks again each minute. A restore may be large and slow. 409
    ALREADY_RESTORED, ALREADY_RUNNING (the segment or another restore is in progress),
    ARCHIVE_NOT_RESTORABLE (a failed segment) or ARCHIVE_NOT_CONFIGURED (s3/glacier settings missing).

    Args:
        id (str):
        body (ArchiveRestoreRequest | Unset): Restore an archive back into the hot store

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Archive | Problem]
    """

    kwargs = _get_kwargs(
        id=id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    id: str,
    *,
    client: AuthenticatedClient,
    body: ArchiveRestoreRequest | Unset = UNSET,
) -> Archive | Problem | None:
    """Restore an archive

     Pulls an archive segment back from cold storage into the hot store (B3/179). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** `confirm: true` is required (400 otherwise). Each file is pulled from its tier and its
    SHA-256 checked, then its rows are loaded into the restore area `record_version_restored` (keyed by
    the archive id), not back into record_version, whose partitions for those seqs are gone and whose
    retention would delete them again. A Glacier segment stays `restoring` while S3 thaws its objects
    (typically hours); the runner asks again each minute. A restore may be large and slow. 409
    ALREADY_RESTORED, ALREADY_RUNNING (the segment or another restore is in progress),
    ARCHIVE_NOT_RESTORABLE (a failed segment) or ARCHIVE_NOT_CONFIGURED (s3/glacier settings missing).

    Args:
        id (str):
        body (ArchiveRestoreRequest | Unset): Restore an archive back into the hot store

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Archive | Problem
    """

    return sync_detailed(
        id=id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    id: str,
    *,
    client: AuthenticatedClient,
    body: ArchiveRestoreRequest | Unset = UNSET,
) -> Response[Archive | Problem]:
    """Restore an archive

     Pulls an archive segment back from cold storage into the hot store (B3/179). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** `confirm: true` is required (400 otherwise). Each file is pulled from its tier and its
    SHA-256 checked, then its rows are loaded into the restore area `record_version_restored` (keyed by
    the archive id), not back into record_version, whose partitions for those seqs are gone and whose
    retention would delete them again. A Glacier segment stays `restoring` while S3 thaws its objects
    (typically hours); the runner asks again each minute. A restore may be large and slow. 409
    ALREADY_RESTORED, ALREADY_RUNNING (the segment or another restore is in progress),
    ARCHIVE_NOT_RESTORABLE (a failed segment) or ARCHIVE_NOT_CONFIGURED (s3/glacier settings missing).

    Args:
        id (str):
        body (ArchiveRestoreRequest | Unset): Restore an archive back into the hot store

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Archive | Problem]
    """

    kwargs = _get_kwargs(
        id=id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    id: str,
    *,
    client: AuthenticatedClient,
    body: ArchiveRestoreRequest | Unset = UNSET,
) -> Archive | Problem | None:
    """Restore an archive

     Pulls an archive segment back from cold storage into the hot store (B3/179). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** `confirm: true` is required (400 otherwise). Each file is pulled from its tier and its
    SHA-256 checked, then its rows are loaded into the restore area `record_version_restored` (keyed by
    the archive id), not back into record_version, whose partitions for those seqs are gone and whose
    retention would delete them again. A Glacier segment stays `restoring` while S3 thaws its objects
    (typically hours); the runner asks again each minute. A restore may be large and slow. 409
    ALREADY_RESTORED, ALREADY_RUNNING (the segment or another restore is in progress),
    ARCHIVE_NOT_RESTORABLE (a failed segment) or ARCHIVE_NOT_CONFIGURED (s3/glacier settings missing).

    Args:
        id (str):
        body (ArchiveRestoreRequest | Unset): Restore an archive back into the hot store

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Archive | Problem
    """

    return (
        await asyncio_detailed(
            id=id,
            client=client,
            body=body,
        )
    ).parsed
