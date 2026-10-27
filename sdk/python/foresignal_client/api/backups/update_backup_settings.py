from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.backup_settings import BackupSettings
from ...models.backup_settings_input import BackupSettingsInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: BackupSettingsInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/backup",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BackupSettings | Problem | None:
    if response.status_code == 200:
        response_200 = BackupSettings.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[BackupSettings | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: BackupSettingsInput,
) -> Response[BackupSettings | Problem]:
    """Update backup settings

     Replaces backup settings; an omitted secret keeps the stored one. Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** every problem is one 400: `enabled` and `target` are required, `schedule` must be a six-
    field cron expression, `external` needs an absolute `root`, `s3` needs `s3.endpoint` and
    `s3.bucket`, `keepBackups` is 1 to 10000 or null. The S3 endpoint must pass the outbound URL policy
    (400 URL_NOT_ALLOWED). Secrets are stored encrypted and never returned.

    Args:
        body (BackupSettingsInput): Backup settings with write-only S3 credentials. Replaces the
            settings: an omitted member takes its default, except `secrets`, where an omitted member
            keeps the stored value and an empty string or null clears it. Unknown members are a 400;
            the read-only `secretsSet` and `lastRunAt` of a GET may be sent back and are ignored.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BackupSettings | Problem]
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
    body: BackupSettingsInput,
) -> BackupSettings | Problem | None:
    """Update backup settings

     Replaces backup settings; an omitted secret keeps the stored one. Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** every problem is one 400: `enabled` and `target` are required, `schedule` must be a six-
    field cron expression, `external` needs an absolute `root`, `s3` needs `s3.endpoint` and
    `s3.bucket`, `keepBackups` is 1 to 10000 or null. The S3 endpoint must pass the outbound URL policy
    (400 URL_NOT_ALLOWED). Secrets are stored encrypted and never returned.

    Args:
        body (BackupSettingsInput): Backup settings with write-only S3 credentials. Replaces the
            settings: an omitted member takes its default, except `secrets`, where an omitted member
            keeps the stored value and an empty string or null clears it. Unknown members are a 400;
            the read-only `secretsSet` and `lastRunAt` of a GET may be sent back and are ignored.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BackupSettings | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: BackupSettingsInput,
) -> Response[BackupSettings | Problem]:
    """Update backup settings

     Replaces backup settings; an omitted secret keeps the stored one. Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** every problem is one 400: `enabled` and `target` are required, `schedule` must be a six-
    field cron expression, `external` needs an absolute `root`, `s3` needs `s3.endpoint` and
    `s3.bucket`, `keepBackups` is 1 to 10000 or null. The S3 endpoint must pass the outbound URL policy
    (400 URL_NOT_ALLOWED). Secrets are stored encrypted and never returned.

    Args:
        body (BackupSettingsInput): Backup settings with write-only S3 credentials. Replaces the
            settings: an omitted member takes its default, except `secrets`, where an omitted member
            keeps the stored value and an empty string or null clears it. Unknown members are a 400;
            the read-only `secretsSet` and `lastRunAt` of a GET may be sent back and are ignored.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BackupSettings | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: BackupSettingsInput,
) -> BackupSettings | Problem | None:
    """Update backup settings

     Replaces backup settings; an omitted secret keeps the stored one. Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-017:** every problem is one 400: `enabled` and `target` are required, `schedule` must be a six-
    field cron expression, `external` needs an absolute `root`, `s3` needs `s3.endpoint` and
    `s3.bucket`, `keepBackups` is 1 to 10000 or null. The S3 endpoint must pass the outbound URL policy
    (400 URL_NOT_ALLOWED). Secrets are stored encrypted and never returned.

    Args:
        body (BackupSettingsInput): Backup settings with write-only S3 credentials. Replaces the
            settings: an omitted member takes its default, except `secrets`, where an omitted member
            keeps the stored value and an empty string or null clears it. Unknown members are a 400;
            the read-only `secretsSet` and `lastRunAt` of a GET may be sent back and are ignored.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BackupSettings | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
