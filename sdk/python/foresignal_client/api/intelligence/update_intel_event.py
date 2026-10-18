from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.intel_event_patch import IntelEventPatch
from ...models.intelligence_event import IntelligenceEvent
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    event_id: str,
    *,
    body: IntelEventPatch,
    if_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_match, Unset):
        headers["If-Match"] = if_match

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/intel/events/{event_id}".format(
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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

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
    body: IntelEventPatch,
    if_match: str | Unset = UNSET,
) -> Response[IntelligenceEvent | Problem]:
    """Update an intelligence event

     Changes title, status (dismissed only by operators or agents), a logged severity override and notes.
    Optimistic locking: a stale `If-Match` version is 409 EVENT_VERSION_CONFLICT.

    **Scope:** `agent` or `admin`.

    Args:
        event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        if_match (str | Unset):
        body (IntelEventPatch): Changes to an intelligence event; severity overrides are logged

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelligenceEvent | Problem]
    """

    kwargs = _get_kwargs(
        event_id=event_id,
        body=body,
        if_match=if_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    event_id: str,
    *,
    client: AuthenticatedClient,
    body: IntelEventPatch,
    if_match: str | Unset = UNSET,
) -> IntelligenceEvent | Problem | None:
    """Update an intelligence event

     Changes title, status (dismissed only by operators or agents), a logged severity override and notes.
    Optimistic locking: a stale `If-Match` version is 409 EVENT_VERSION_CONFLICT.

    **Scope:** `agent` or `admin`.

    Args:
        event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        if_match (str | Unset):
        body (IntelEventPatch): Changes to an intelligence event; severity overrides are logged

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
        if_match=if_match,
    ).parsed


async def asyncio_detailed(
    event_id: str,
    *,
    client: AuthenticatedClient,
    body: IntelEventPatch,
    if_match: str | Unset = UNSET,
) -> Response[IntelligenceEvent | Problem]:
    """Update an intelligence event

     Changes title, status (dismissed only by operators or agents), a logged severity override and notes.
    Optimistic locking: a stale `If-Match` version is 409 EVENT_VERSION_CONFLICT.

    **Scope:** `agent` or `admin`.

    Args:
        event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        if_match (str | Unset):
        body (IntelEventPatch): Changes to an intelligence event; severity overrides are logged

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelligenceEvent | Problem]
    """

    kwargs = _get_kwargs(
        event_id=event_id,
        body=body,
        if_match=if_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    event_id: str,
    *,
    client: AuthenticatedClient,
    body: IntelEventPatch,
    if_match: str | Unset = UNSET,
) -> IntelligenceEvent | Problem | None:
    """Update an intelligence event

     Changes title, status (dismissed only by operators or agents), a logged severity override and notes.
    Optimistic locking: a stale `If-Match` version is 409 EVENT_VERSION_CONFLICT.

    **Scope:** `agent` or `admin`.

    Args:
        event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        if_match (str | Unset):
        body (IntelEventPatch): Changes to an intelligence event; severity overrides are logged

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
            if_match=if_match,
        )
    ).parsed
