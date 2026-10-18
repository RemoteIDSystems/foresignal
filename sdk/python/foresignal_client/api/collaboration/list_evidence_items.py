from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.evidence_item_page import EvidenceItemPage
from ...models.list_evidence_items_source_reliability import (
    ListEvidenceItemsSourceReliability,
)
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    q: str | Unset = UNSET,
    source_reliability: ListEvidenceItemsSourceReliability | Unset = UNSET,
    tag: str | Unset = UNSET,
    investigation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["q"] = q

    json_source_reliability: str | Unset = UNSET
    if not isinstance(source_reliability, Unset):
        json_source_reliability = source_reliability.value

    params["sourceReliability"] = json_source_reliability

    params["tag"] = tag

    params["investigationId"] = investigation_id

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/evidence-items",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EvidenceItemPage | Problem | None:
    if response.status_code == 200:
        response_200 = EvidenceItemPage.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[EvidenceItemPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    source_reliability: ListEvidenceItemsSourceReliability | Unset = UNSET,
    tag: str | Unset = UNSET,
    investigation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[EvidenceItemPage | Problem]:
    """List evidence items

     Evidence items visible to the caller.

    **Scope:** `read`.

    Args:
        q (str | Unset):
        source_reliability (ListEvidenceItemsSourceReliability | Unset): Admiralty source
            reliability (A completely reliable … F cannot be judged)
        tag (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        investigation_id (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EvidenceItemPage | Problem]
    """

    kwargs = _get_kwargs(
        q=q,
        source_reliability=source_reliability,
        tag=tag,
        investigation_id=investigation_id,
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
    q: str | Unset = UNSET,
    source_reliability: ListEvidenceItemsSourceReliability | Unset = UNSET,
    tag: str | Unset = UNSET,
    investigation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> EvidenceItemPage | Problem | None:
    """List evidence items

     Evidence items visible to the caller.

    **Scope:** `read`.

    Args:
        q (str | Unset):
        source_reliability (ListEvidenceItemsSourceReliability | Unset): Admiralty source
            reliability (A completely reliable … F cannot be judged)
        tag (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        investigation_id (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EvidenceItemPage | Problem
    """

    return sync_detailed(
        client=client,
        q=q,
        source_reliability=source_reliability,
        tag=tag,
        investigation_id=investigation_id,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    source_reliability: ListEvidenceItemsSourceReliability | Unset = UNSET,
    tag: str | Unset = UNSET,
    investigation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[EvidenceItemPage | Problem]:
    """List evidence items

     Evidence items visible to the caller.

    **Scope:** `read`.

    Args:
        q (str | Unset):
        source_reliability (ListEvidenceItemsSourceReliability | Unset): Admiralty source
            reliability (A completely reliable … F cannot be judged)
        tag (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        investigation_id (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EvidenceItemPage | Problem]
    """

    kwargs = _get_kwargs(
        q=q,
        source_reliability=source_reliability,
        tag=tag,
        investigation_id=investigation_id,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    q: str | Unset = UNSET,
    source_reliability: ListEvidenceItemsSourceReliability | Unset = UNSET,
    tag: str | Unset = UNSET,
    investigation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> EvidenceItemPage | Problem | None:
    """List evidence items

     Evidence items visible to the caller.

    **Scope:** `read`.

    Args:
        q (str | Unset):
        source_reliability (ListEvidenceItemsSourceReliability | Unset): Admiralty source
            reliability (A completely reliable … F cannot be judged)
        tag (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        investigation_id (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EvidenceItemPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            q=q,
            source_reliability=source_reliability,
            tag=tag,
            investigation_id=investigation_id,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
