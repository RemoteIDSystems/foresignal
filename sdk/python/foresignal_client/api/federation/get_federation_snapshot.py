from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.federation_snapshot_page import FederationSnapshotPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    at_seq: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["atSeq"] = at_seq

    params["cursor"] = cursor

    params["limit"] = limit

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params["bbox"] = bbox

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/federation/snapshot",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> FederationSnapshotPage | Problem | None:
    if response.status_code == 200:
        response_200 = FederationSnapshotPage.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 410:
        response_410 = Problem.from_dict(response.json())

        return response_410

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[FederationSnapshotPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    at_seq: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> Response[FederationSnapshotPage | Problem]:
    """Federation snapshot for resync

     Paged current heads (`record_head ⋈ record_live`) pinned to the watermark of the first page; pass
    `atSeq` and `cursor` for later pages, then continue the change feed from `atSeq`. Per record:
    FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT
    positions), private sources and exercise records (unless the token sets includeExercise) are
    excluded. Restriction columns of the token still apply.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        at_seq (int | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 1000.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationSnapshotPage | Problem]
    """

    kwargs = _get_kwargs(
        at_seq=at_seq,
        cursor=cursor,
        limit=limit,
        source=source,
        kind=kind,
        bbox=bbox,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    at_seq: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> FederationSnapshotPage | Problem | None:
    """Federation snapshot for resync

     Paged current heads (`record_head ⋈ record_live`) pinned to the watermark of the first page; pass
    `atSeq` and `cursor` for later pages, then continue the change feed from `atSeq`. Per record:
    FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT
    positions), private sources and exercise records (unless the token sets includeExercise) are
    excluded. Restriction columns of the token still apply.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        at_seq (int | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 1000.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationSnapshotPage | Problem
    """

    return sync_detailed(
        client=client,
        at_seq=at_seq,
        cursor=cursor,
        limit=limit,
        source=source,
        kind=kind,
        bbox=bbox,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    at_seq: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> Response[FederationSnapshotPage | Problem]:
    """Federation snapshot for resync

     Paged current heads (`record_head ⋈ record_live`) pinned to the watermark of the first page; pass
    `atSeq` and `cursor` for later pages, then continue the change feed from `atSeq`. Per record:
    FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT
    positions), private sources and exercise records (unless the token sets includeExercise) are
    excluded. Restriction columns of the token still apply.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        at_seq (int | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 1000.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FederationSnapshotPage | Problem]
    """

    kwargs = _get_kwargs(
        at_seq=at_seq,
        cursor=cursor,
        limit=limit,
        source=source,
        kind=kind,
        bbox=bbox,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    at_seq: int | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 1000,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    bbox: str | Unset = UNSET,
) -> FederationSnapshotPage | Problem | None:
    """Federation snapshot for resync

     Paged current heads (`record_head ⋈ record_live`) pinned to the watermark of the first page; pass
    `atSeq` and `cursor` for later pages, then continue the change feed from `atSeq`. Per record:
    FORBIDDEN and LOCAL_ONLY licences (opensky-states, acled, aisstream, local receivers, MLAT
    positions), private sources and exercise records (unless the token sets includeExercise) are
    excluded. Restriction columns of the token still apply.

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        at_seq (int | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 1000.
        source (list[str] | Unset):
        kind (list[str] | Unset):
        bbox (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FederationSnapshotPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            at_seq=at_seq,
            cursor=cursor,
            limit=limit,
            source=source,
            kind=kind,
            bbox=bbox,
        )
    ).parsed
