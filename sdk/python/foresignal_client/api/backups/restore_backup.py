from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.backup import Backup
from ...models.backup_restore_request import BackupRestoreRequest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    id: str,
    *,
    body: BackupRestoreRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/backups/{id}/restore".format(
            id=quote(str(id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Backup | Problem | None:
    if response.status_code == 202:
        response_202 = Backup.from_dict(response.json())

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
) -> Response[Backup | Problem]:
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
    body: BackupRestoreRequest,
) -> Response[Backup | Problem]:
    """Restore from a backup

     Restores the instance from a backup; destructive, so `confirm` must be true (180). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** a `config` backup is imported at once through `importSettings` (atomic: a 400 listing
    every error applies nothing); the answer is the `restored` backup with its `importReport`. A
    `database` or `full` backup is restored in the background (the answer is the `restoring` backup)
    with `pg_restore --clean --if-exists --single-transaction` into the live database: it replaces every
    table, the backup history included, then brings the schema forward with the server's migrations and
    marks the backup `restored`. **Operational caveat:** the restore takes locks on every table, so
    ingest and API writes wait on it, and anything written after the backup was taken is lost; pause
    collection first and restart the server afterwards so every cache is rebuilt. While it runs, no
    retention or archive pass starts. A failed restore puts the backup's previous status back with
    `error` and raises `system.backup_failed`. 409 ALREADY_RUNNING while a backup, restore, retention or
    archive pass runs; 409 BACKUP_NOT_RESTORABLE for a failed backup, or a config bundle whose stored
    copy is missing or does not match its checksum (the reason is kept in `error`); 409
    BACKUP_NOT_CONFIGURED for an `s3` backup when the settings have no bucket or secrets.

    Args:
        id (str):
        body (BackupRestoreRequest): Restore from a backup (destructive; replaces current data)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Backup | Problem]
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
    body: BackupRestoreRequest,
) -> Backup | Problem | None:
    """Restore from a backup

     Restores the instance from a backup; destructive, so `confirm` must be true (180). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** a `config` backup is imported at once through `importSettings` (atomic: a 400 listing
    every error applies nothing); the answer is the `restored` backup with its `importReport`. A
    `database` or `full` backup is restored in the background (the answer is the `restoring` backup)
    with `pg_restore --clean --if-exists --single-transaction` into the live database: it replaces every
    table, the backup history included, then brings the schema forward with the server's migrations and
    marks the backup `restored`. **Operational caveat:** the restore takes locks on every table, so
    ingest and API writes wait on it, and anything written after the backup was taken is lost; pause
    collection first and restart the server afterwards so every cache is rebuilt. While it runs, no
    retention or archive pass starts. A failed restore puts the backup's previous status back with
    `error` and raises `system.backup_failed`. 409 ALREADY_RUNNING while a backup, restore, retention or
    archive pass runs; 409 BACKUP_NOT_RESTORABLE for a failed backup, or a config bundle whose stored
    copy is missing or does not match its checksum (the reason is kept in `error`); 409
    BACKUP_NOT_CONFIGURED for an `s3` backup when the settings have no bucket or secrets.

    Args:
        id (str):
        body (BackupRestoreRequest): Restore from a backup (destructive; replaces current data)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Backup | Problem
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
    body: BackupRestoreRequest,
) -> Response[Backup | Problem]:
    """Restore from a backup

     Restores the instance from a backup; destructive, so `confirm` must be true (180). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** a `config` backup is imported at once through `importSettings` (atomic: a 400 listing
    every error applies nothing); the answer is the `restored` backup with its `importReport`. A
    `database` or `full` backup is restored in the background (the answer is the `restoring` backup)
    with `pg_restore --clean --if-exists --single-transaction` into the live database: it replaces every
    table, the backup history included, then brings the schema forward with the server's migrations and
    marks the backup `restored`. **Operational caveat:** the restore takes locks on every table, so
    ingest and API writes wait on it, and anything written after the backup was taken is lost; pause
    collection first and restart the server afterwards so every cache is rebuilt. While it runs, no
    retention or archive pass starts. A failed restore puts the backup's previous status back with
    `error` and raises `system.backup_failed`. 409 ALREADY_RUNNING while a backup, restore, retention or
    archive pass runs; 409 BACKUP_NOT_RESTORABLE for a failed backup, or a config bundle whose stored
    copy is missing or does not match its checksum (the reason is kept in `error`); 409
    BACKUP_NOT_CONFIGURED for an `s3` backup when the settings have no bucket or secrets.

    Args:
        id (str):
        body (BackupRestoreRequest): Restore from a backup (destructive; replaces current data)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Backup | Problem]
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
    body: BackupRestoreRequest,
) -> Backup | Problem | None:
    """Restore from a backup

     Restores the instance from a backup; destructive, so `confirm` must be true (180). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** a `config` backup is imported at once through `importSettings` (atomic: a 400 listing
    every error applies nothing); the answer is the `restored` backup with its `importReport`. A
    `database` or `full` backup is restored in the background (the answer is the `restoring` backup)
    with `pg_restore --clean --if-exists --single-transaction` into the live database: it replaces every
    table, the backup history included, then brings the schema forward with the server's migrations and
    marks the backup `restored`. **Operational caveat:** the restore takes locks on every table, so
    ingest and API writes wait on it, and anything written after the backup was taken is lost; pause
    collection first and restart the server afterwards so every cache is rebuilt. While it runs, no
    retention or archive pass starts. A failed restore puts the backup's previous status back with
    `error` and raises `system.backup_failed`. 409 ALREADY_RUNNING while a backup, restore, retention or
    archive pass runs; 409 BACKUP_NOT_RESTORABLE for a failed backup, or a config bundle whose stored
    copy is missing or does not match its checksum (the reason is kept in `error`); 409
    BACKUP_NOT_CONFIGURED for an `s3` backup when the settings have no bucket or secrets.

    Args:
        id (str):
        body (BackupRestoreRequest): Restore from a backup (destructive; replaces current data)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Backup | Problem
    """

    return (
        await asyncio_detailed(
            id=id,
            client=client,
            body=body,
        )
    ).parsed
