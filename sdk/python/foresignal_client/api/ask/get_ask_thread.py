from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ask_thread import AskThread
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    thread_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ask/threads/{thread_id}".format(
            thread_id=quote(str(thread_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AskThread | Problem | None:
    if response.status_code == 200:
        response_200 = AskThread.from_dict(response.json())

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
) -> Response[AskThread | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    thread_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[AskThread | Problem]:
    """Get an Ask thread with messages

     Get an Ask thread with messages

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).
    A token sees its own threads; an admin token sees every thread; another token's thread is 404.

    Args:
        thread_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AskThread | Problem]
    """

    kwargs = _get_kwargs(
        thread_id=thread_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    thread_id: UUID,
    *,
    client: AuthenticatedClient,
) -> AskThread | Problem | None:
    """Get an Ask thread with messages

     Get an Ask thread with messages

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).
    A token sees its own threads; an admin token sees every thread; another token's thread is 404.

    Args:
        thread_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AskThread | Problem
    """

    return sync_detailed(
        thread_id=thread_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    thread_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[AskThread | Problem]:
    """Get an Ask thread with messages

     Get an Ask thread with messages

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).
    A token sees its own threads; an admin token sees every thread; another token's thread is 404.

    Args:
        thread_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AskThread | Problem]
    """

    kwargs = _get_kwargs(
        thread_id=thread_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    thread_id: UUID,
    *,
    client: AuthenticatedClient,
) -> AskThread | Problem | None:
    """Get an Ask thread with messages

     Get an Ask thread with messages

    **Scope:** `read` (always authenticated: a token is required even when `security.openReads` is on).
    A token sees its own threads; an admin token sees every thread; another token's thread is 404.

    Args:
        thread_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AskThread | Problem
    """

    return (
        await asyncio_detailed(
            thread_id=thread_id,
            client=client,
        )
    ).parsed
