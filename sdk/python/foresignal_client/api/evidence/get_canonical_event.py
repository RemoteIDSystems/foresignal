from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.canonical_event import CanonicalEvent
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    canonical_event_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/evidence/events/{canonical_event_id}".format(
            canonical_event_id=quote(str(canonical_event_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CanonicalEvent | Problem | None:
    if response.status_code == 200:
        response_200 = CanonicalEvent.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[CanonicalEvent | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    canonical_event_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[CanonicalEvent | Problem]:
    """Get a canonical event with evidence

     The event with its observations (originators, link confidence and reasons), evidence groups and
    quality flags. Errors: `404 UNKNOWN_CANONICAL_EVENT`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        canonical_event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CanonicalEvent | Problem]
    """

    kwargs = _get_kwargs(
        canonical_event_id=canonical_event_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    canonical_event_id: str,
    *,
    client: AuthenticatedClient,
) -> CanonicalEvent | Problem | None:
    """Get a canonical event with evidence

     The event with its observations (originators, link confidence and reasons), evidence groups and
    quality flags. Errors: `404 UNKNOWN_CANONICAL_EVENT`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        canonical_event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CanonicalEvent | Problem
    """

    return sync_detailed(
        canonical_event_id=canonical_event_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    canonical_event_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[CanonicalEvent | Problem]:
    """Get a canonical event with evidence

     The event with its observations (originators, link confidence and reasons), evidence groups and
    quality flags. Errors: `404 UNKNOWN_CANONICAL_EVENT`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        canonical_event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CanonicalEvent | Problem]
    """

    kwargs = _get_kwargs(
        canonical_event_id=canonical_event_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    canonical_event_id: str,
    *,
    client: AuthenticatedClient,
) -> CanonicalEvent | Problem | None:
    """Get a canonical event with evidence

     The event with its observations (originators, link confidence and reasons), evidence groups and
    quality flags. Errors: `404 UNKNOWN_CANONICAL_EVENT`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        canonical_event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CanonicalEvent | Problem
    """

    return (
        await asyncio_detailed(
            canonical_event_id=canonical_event_id,
            client=client,
        )
    ).parsed
