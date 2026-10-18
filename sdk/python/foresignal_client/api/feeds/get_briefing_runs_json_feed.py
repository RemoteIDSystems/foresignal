from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.json_feed import JsonFeed
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    briefing_id: UUID,
    *,
    limit: int | Unset = 50,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/briefings/{briefing_id}/runs/feed.json".format(
            briefing_id=quote(str(briefing_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | JsonFeed | Problem | None:
    if response.status_code == 200:
        response_200 = JsonFeed.from_dict(response.json())

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

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
) -> Response[Any | JsonFeed | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    limit: int | Unset = 50,
) -> Response[Any | JsonFeed | Problem]:
    """Briefing runs as a JSON Feed

     Runs of one briefing, newest first. Entry id `tag:foresignal,2026:{installId}/briefing-run/{id}`;
    content embeds the full run markdown, rendered and sanitised like alert entries. Conditional GET
    with ETag and `Last-Modified`; feed keys via `/v1/k/{key}/briefings/{briefingId}/runs/feed.atom`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        limit (int | Unset):  Default: 50.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | JsonFeed | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    limit: int | Unset = 50,
) -> Any | JsonFeed | Problem | None:
    """Briefing runs as a JSON Feed

     Runs of one briefing, newest first. Entry id `tag:foresignal,2026:{installId}/briefing-run/{id}`;
    content embeds the full run markdown, rendered and sanitised like alert entries. Conditional GET
    with ETag and `Last-Modified`; feed keys via `/v1/k/{key}/briefings/{briefingId}/runs/feed.atom`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        limit (int | Unset):  Default: 50.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | JsonFeed | Problem
    """

    return sync_detailed(
        briefing_id=briefing_id,
        client=client,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    limit: int | Unset = 50,
) -> Response[Any | JsonFeed | Problem]:
    """Briefing runs as a JSON Feed

     Runs of one briefing, newest first. Entry id `tag:foresignal,2026:{installId}/briefing-run/{id}`;
    content embeds the full run markdown, rendered and sanitised like alert entries. Conditional GET
    with ETag and `Last-Modified`; feed keys via `/v1/k/{key}/briefings/{briefingId}/runs/feed.atom`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        limit (int | Unset):  Default: 50.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | JsonFeed | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    briefing_id: UUID,
    *,
    client: AuthenticatedClient,
    limit: int | Unset = 50,
) -> Any | JsonFeed | Problem | None:
    """Briefing runs as a JSON Feed

     Runs of one briefing, newest first. Entry id `tag:foresignal,2026:{installId}/briefing-run/{id}`;
    content embeds the full run markdown, rendered and sanitised like alert entries. Conditional GET
    with ETag and `Last-Modified`; feed keys via `/v1/k/{key}/briefings/{briefingId}/runs/feed.atom`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        limit (int | Unset):  Default: 50.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | JsonFeed | Problem
    """

    return (
        await asyncio_detailed(
            briefing_id=briefing_id,
            client=client,
            limit=limit,
        )
    ).parsed
