from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.intel_event_merge_input import IntelEventMergeInput
from ...models.intelligence_event import IntelligenceEvent
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    event_id: str,
    *,
    body: IntelEventMergeInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/intel/events/{event_id}/merge".format(
            event_id=quote(str(event_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> IntelligenceEvent | Problem | None:
    if response.status_code == 200:
        response_200 = IntelligenceEvent.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[IntelligenceEvent | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    event_id: str,
    *,
    client: AuthenticatedClient,
    body: IntelEventMergeInput,
) -> Response[IntelligenceEvent | Problem]:
    """Merge an event into another

     Moves members into `intoId` (the older id is kept by the engine for automatic merges), sets
    `mergedInto`, tombstones the merged record and logs it. Never merges across types.

    **Scope:** `agent` or `admin`.

    Args:
        event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (IntelEventMergeInput): Merge the path event into another open event of the same type

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelligenceEvent | Problem]
    """

    kwargs = _get_kwargs(
        event_id=event_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    event_id: str,
    *,
    client: AuthenticatedClient,
    body: IntelEventMergeInput,
) -> IntelligenceEvent | Problem | None:
    """Merge an event into another

     Moves members into `intoId` (the older id is kept by the engine for automatic merges), sets
    `mergedInto`, tombstones the merged record and logs it. Never merges across types.

    **Scope:** `agent` or `admin`.

    Args:
        event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (IntelEventMergeInput): Merge the path event into another open event of the same type

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IntelligenceEvent | Problem
    """

    return sync_detailed(
        event_id=event_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    event_id: str,
    *,
    client: AuthenticatedClient,
    body: IntelEventMergeInput,
) -> Response[IntelligenceEvent | Problem]:
    """Merge an event into another

     Moves members into `intoId` (the older id is kept by the engine for automatic merges), sets
    `mergedInto`, tombstones the merged record and logs it. Never merges across types.

    **Scope:** `agent` or `admin`.

    Args:
        event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (IntelEventMergeInput): Merge the path event into another open event of the same type

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelligenceEvent | Problem]
    """

    kwargs = _get_kwargs(
        event_id=event_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    event_id: str,
    *,
    client: AuthenticatedClient,
    body: IntelEventMergeInput,
) -> IntelligenceEvent | Problem | None:
    """Merge an event into another

     Moves members into `intoId` (the older id is kept by the engine for automatic merges), sets
    `mergedInto`, tombstones the merged record and logs it. Never merges across types.

    **Scope:** `agent` or `admin`.

    Args:
        event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (IntelEventMergeInput): Merge the path event into another open event of the same type

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IntelligenceEvent | Problem
    """

    return (
        await asyncio_detailed(
            event_id=event_id,
            client=client,
            body=body,
        )
    ).parsed
