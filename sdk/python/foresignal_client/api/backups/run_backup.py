from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.backup import Backup
from ...models.backup_run_request import BackupRunRequest
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    body: BackupRunRequest | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/backups/run",
    }

    if not isinstance(body, Unset):
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
    *,
    client: AuthenticatedClient,
    body: BackupRunRequest | Unset = UNSET,
) -> Response[Backup | Problem]:
    """Run a backup now

     Starts a DB (pg_dump/base) or config-export backup to the configured target (B4/180). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** returns the `pending` backup; a worker then runs `pg_dump --format=custom` of the
    application database (the password goes to it as `PGPASSWORD`, never on its command line) and/or
    writes the `exportSettings` bundle, stores each file on the target through the R2-016 archive tiers
    and verifies it there, marks the backup `complete`, and prunes complete backups of that type beyond
    `keepBackups`. A failure marks it `failed` with `error` and raises the operational alert
    `system.backup_failed`; when `pg_dump` is not on the server's PATH, a database backup fails with
    that reason and the server carries on. 409 BACKUP_NOT_CONFIGURED while backups are off, or an `s3`
    target lacks its bucket or secrets, or an `external` target its root; 409 ALREADY_RUNNING while
    another backup or restore runs.

    Args:
        body (BackupRunRequest | Unset): Run a backup now

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Backup | Problem]
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
    body: BackupRunRequest | Unset = UNSET,
) -> Backup | Problem | None:
    """Run a backup now

     Starts a DB (pg_dump/base) or config-export backup to the configured target (B4/180). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** returns the `pending` backup; a worker then runs `pg_dump --format=custom` of the
    application database (the password goes to it as `PGPASSWORD`, never on its command line) and/or
    writes the `exportSettings` bundle, stores each file on the target through the R2-016 archive tiers
    and verifies it there, marks the backup `complete`, and prunes complete backups of that type beyond
    `keepBackups`. A failure marks it `failed` with `error` and raises the operational alert
    `system.backup_failed`; when `pg_dump` is not on the server's PATH, a database backup fails with
    that reason and the server carries on. 409 BACKUP_NOT_CONFIGURED while backups are off, or an `s3`
    target lacks its bucket or secrets, or an `external` target its root; 409 ALREADY_RUNNING while
    another backup or restore runs.

    Args:
        body (BackupRunRequest | Unset): Run a backup now

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Backup | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: BackupRunRequest | Unset = UNSET,
) -> Response[Backup | Problem]:
    """Run a backup now

     Starts a DB (pg_dump/base) or config-export backup to the configured target (B4/180). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** returns the `pending` backup; a worker then runs `pg_dump --format=custom` of the
    application database (the password goes to it as `PGPASSWORD`, never on its command line) and/or
    writes the `exportSettings` bundle, stores each file on the target through the R2-016 archive tiers
    and verifies it there, marks the backup `complete`, and prunes complete backups of that type beyond
    `keepBackups`. A failure marks it `failed` with `error` and raises the operational alert
    `system.backup_failed`; when `pg_dump` is not on the server's PATH, a database backup fails with
    that reason and the server carries on. 409 BACKUP_NOT_CONFIGURED while backups are off, or an `s3`
    target lacks its bucket or secrets, or an `external` target its root; 409 ALREADY_RUNNING while
    another backup or restore runs.

    Args:
        body (BackupRunRequest | Unset): Run a backup now

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Backup | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: BackupRunRequest | Unset = UNSET,
) -> Backup | Problem | None:
    """Run a backup now

     Starts a DB (pg_dump/base) or config-export backup to the configured target (B4/180). Requires
    `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** returns the `pending` backup; a worker then runs `pg_dump --format=custom` of the
    application database (the password goes to it as `PGPASSWORD`, never on its command line) and/or
    writes the `exportSettings` bundle, stores each file on the target through the R2-016 archive tiers
    and verifies it there, marks the backup `complete`, and prunes complete backups of that type beyond
    `keepBackups`. A failure marks it `failed` with `error` and raises the operational alert
    `system.backup_failed`; when `pg_dump` is not on the server's PATH, a database backup fails with
    that reason and the server carries on. 409 BACKUP_NOT_CONFIGURED while backups are off, or an `s3`
    target lacks its bucket or secrets, or an `external` target its root; 409 ALREADY_RUNNING while
    another backup or restore runs.

    Args:
        body (BackupRunRequest | Unset): Run a backup now

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Backup | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
