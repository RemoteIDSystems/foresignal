from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.propagation_graph import PropagationGraph
from ...types import Response


def _get_kwargs(
    narrative_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/narratives/{narrative_id}/propagation".format(
            narrative_id=quote(str(narrative_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | PropagationGraph | None:
    if response.status_code == 200:
        response_200 = PropagationGraph.from_dict(response.json())

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
) -> Response[Problem | PropagationGraph]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | PropagationGraph]:
    """Get a narrative's propagation graph

     Propagation across reposts, quotes, replies, links, text and media matches, platforms and news,
    summarised to ≤ 500 nodes, with earliest observed post per platform, first news pickup, descent
    share, cross-platform order and median repost delay.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PropagationGraph]
    """

    kwargs = _get_kwargs(
        narrative_id=narrative_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | PropagationGraph | None:
    """Get a narrative's propagation graph

     Propagation across reposts, quotes, replies, links, text and media matches, platforms and news,
    summarised to ≤ 500 nodes, with earliest observed post per platform, first news pickup, descent
    share, cross-platform order and median repost delay.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PropagationGraph
    """

    return sync_detailed(
        narrative_id=narrative_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | PropagationGraph]:
    """Get a narrative's propagation graph

     Propagation across reposts, quotes, replies, links, text and media matches, platforms and news,
    summarised to ≤ 500 nodes, with earliest observed post per platform, first news pickup, descent
    share, cross-platform order and median repost delay.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PropagationGraph]
    """

    kwargs = _get_kwargs(
        narrative_id=narrative_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    narrative_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | PropagationGraph | None:
    """Get a narrative's propagation graph

     Propagation across reposts, quotes, replies, links, text and media matches, platforms and news,
    summarised to ≤ 500 nodes, with earliest observed post per platform, first news pickup, descent
    share, cross-platform order and median repost delay.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PropagationGraph
    """

    return (
        await asyncio_detailed(
            narrative_id=narrative_id,
            client=client,
        )
    ).parsed
