from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.archive_page import ArchivePage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    status: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["status"] = status

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/archives",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ArchivePage | Problem | None:
    if response.status_code == 200:
        response_200 = ArchivePage.from_dict(response.json())

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
) -> Response[ArchivePage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    status: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ArchivePage | Problem]:
    """List archive segments

     Lists cold-storage archive segments with sizes and status, newest first (B3/179). This is the
    archival-tier inventory (`archive_manifest`); the compliance cold-archive catalog of legal holds is
    getArchiveCatalog. Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** an unknown `status` or a cursor this endpoint did not issue is 400 VALIDATION_FAILED.

    Args:
        status (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ArchivePage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    status: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ArchivePage | Problem | None:
    """List archive segments

     Lists cold-storage archive segments with sizes and status, newest first (B3/179). This is the
    archival-tier inventory (`archive_manifest`); the compliance cold-archive catalog of legal holds is
    getArchiveCatalog. Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** an unknown `status` or a cursor this endpoint did not issue is 400 VALIDATION_FAILED.

    Args:
        status (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ArchivePage | Problem
    """

    return sync_detailed(
        client=client,
        status=status,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    status: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ArchivePage | Problem]:
    """List archive segments

     Lists cold-storage archive segments with sizes and status, newest first (B3/179). This is the
    archival-tier inventory (`archive_manifest`); the compliance cold-archive catalog of legal holds is
    getArchiveCatalog. Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** an unknown `status` or a cursor this endpoint did not issue is 400 VALIDATION_FAILED.

    Args:
        status (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ArchivePage | Problem]
    """

    kwargs = _get_kwargs(
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    status: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ArchivePage | Problem | None:
    """List archive segments

     Lists cold-storage archive segments with sizes and status, newest first (B3/179). This is the
    archival-tier inventory (`archive_manifest`); the compliance cold-archive catalog of legal holds is
    getArchiveCatalog. Requires `storage.manage`.

    **Scope:** `admin`.

    **R2-016:** an unknown `status` or a cursor this endpoint did not issue is 400 VALIDATION_FAILED.

    Args:
        status (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ArchivePage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            status=status,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
