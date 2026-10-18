from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.allow_external_snapshot_response_200 import (
    AllowExternalSnapshotResponse200,
)
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
    snapshot_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/external/sources/{source_id}/snapshots/{snapshot_id}/allow".format(
            source_id=quote(str(source_id), safe=""),
            snapshot_id=quote(str(snapshot_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AllowExternalSnapshotResponse200 | Problem | None:
    if response.status_code == 200:
        response_200 = AllowExternalSnapshotResponse200.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AllowExternalSnapshotResponse200 | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    snapshot_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[AllowExternalSnapshotResponse200 | Problem]:
    """Release a held snapshot deletion once

     A snapshot whose absent share is above `maxAbsentRatio` is held and the source turns degraded
    (docs/External-Data-Protocol.md §3.2). This releases it once: the absence tombstones are computed
    against the scope's current entities now and ingested; the hold and the degraded status clear.

    **Scope:** `admin`.

    Args:
        source_id (str):
        snapshot_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AllowExternalSnapshotResponse200 | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        snapshot_id=snapshot_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    snapshot_id: str,
    *,
    client: AuthenticatedClient,
) -> AllowExternalSnapshotResponse200 | Problem | None:
    """Release a held snapshot deletion once

     A snapshot whose absent share is above `maxAbsentRatio` is held and the source turns degraded
    (docs/External-Data-Protocol.md §3.2). This releases it once: the absence tombstones are computed
    against the scope's current entities now and ingested; the hold and the degraded status clear.

    **Scope:** `admin`.

    Args:
        source_id (str):
        snapshot_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AllowExternalSnapshotResponse200 | Problem
    """

    return sync_detailed(
        source_id=source_id,
        snapshot_id=snapshot_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    snapshot_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[AllowExternalSnapshotResponse200 | Problem]:
    """Release a held snapshot deletion once

     A snapshot whose absent share is above `maxAbsentRatio` is held and the source turns degraded
    (docs/External-Data-Protocol.md §3.2). This releases it once: the absence tombstones are computed
    against the scope's current entities now and ingested; the hold and the degraded status clear.

    **Scope:** `admin`.

    Args:
        source_id (str):
        snapshot_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AllowExternalSnapshotResponse200 | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        snapshot_id=snapshot_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    snapshot_id: str,
    *,
    client: AuthenticatedClient,
) -> AllowExternalSnapshotResponse200 | Problem | None:
    """Release a held snapshot deletion once

     A snapshot whose absent share is above `maxAbsentRatio` is held and the source turns degraded
    (docs/External-Data-Protocol.md §3.2). This releases it once: the absence tombstones are computed
    against the scope's current entities now and ingested; the hold and the degraded status clear.

    **Scope:** `admin`.

    Args:
        source_id (str):
        snapshot_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AllowExternalSnapshotResponse200 | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            snapshot_id=snapshot_id,
            client=client,
        )
    ).parsed
