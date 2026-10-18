from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.archive_settings import ArchiveSettings
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ArchiveSettings,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/archive",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ArchiveSettings | Problem | None:
    if response.status_code == 200:
        response_200 = ArchiveSettings.from_dict(response.json())

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
) -> Response[ArchiveSettings | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ArchiveSettings,
) -> Response[ArchiveSettings | Problem]:
    """Update cold archive settings

     When enabled, ArchiveStep writes TRACK leaves and STANDARD chunks as hive-partitioned GeoParquet
    before retention deletes them, verifies the row count, then uploads to S3 when configured (SigV4;
    endpoint checked by the outbound URL policy, URL_NOT_ALLOWED). Omitted secrets keep their stored
    values.

    **Scope:** `admin`.

    Args:
        body (ArchiveSettings): Cold archive settings (settings key `archive`); secrets are write-
            only (111)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ArchiveSettings | Problem]
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
    body: ArchiveSettings,
) -> ArchiveSettings | Problem | None:
    """Update cold archive settings

     When enabled, ArchiveStep writes TRACK leaves and STANDARD chunks as hive-partitioned GeoParquet
    before retention deletes them, verifies the row count, then uploads to S3 when configured (SigV4;
    endpoint checked by the outbound URL policy, URL_NOT_ALLOWED). Omitted secrets keep their stored
    values.

    **Scope:** `admin`.

    Args:
        body (ArchiveSettings): Cold archive settings (settings key `archive`); secrets are write-
            only (111)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ArchiveSettings | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ArchiveSettings,
) -> Response[ArchiveSettings | Problem]:
    """Update cold archive settings

     When enabled, ArchiveStep writes TRACK leaves and STANDARD chunks as hive-partitioned GeoParquet
    before retention deletes them, verifies the row count, then uploads to S3 when configured (SigV4;
    endpoint checked by the outbound URL policy, URL_NOT_ALLOWED). Omitted secrets keep their stored
    values.

    **Scope:** `admin`.

    Args:
        body (ArchiveSettings): Cold archive settings (settings key `archive`); secrets are write-
            only (111)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ArchiveSettings | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ArchiveSettings,
) -> ArchiveSettings | Problem | None:
    """Update cold archive settings

     When enabled, ArchiveStep writes TRACK leaves and STANDARD chunks as hive-partitioned GeoParquet
    before retention deletes them, verifies the row count, then uploads to S3 when configured (SigV4;
    endpoint checked by the outbound URL policy, URL_NOT_ALLOWED). Omitted secrets keep their stored
    values.

    **Scope:** `admin`.

    Args:
        body (ArchiveSettings): Cold archive settings (settings key `archive`); secrets are write-
            only (111)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ArchiveSettings | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
