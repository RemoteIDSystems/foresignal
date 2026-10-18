from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.tag import Tag
from ...models.tag_input import TagInput
from ...types import Response


def _get_kwargs(
    *,
    body: TagInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/tags",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Tag | None:
    if response.status_code == 201:
        response_201 = Tag.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | Tag]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: TagInput,
) -> Response[Problem | Tag]:
    """Create a tag

     Creates a personal or shared tag owned by the caller.

    **Scope:** `read`.

    Args:
        body (TagInput): Create a tag; personal tags are visible only to their owner, shared tags
            default to `instance` visibility

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Tag]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: TagInput,
) -> Problem | Tag | None:
    """Create a tag

     Creates a personal or shared tag owned by the caller.

    **Scope:** `read`.

    Args:
        body (TagInput): Create a tag; personal tags are visible only to their owner, shared tags
            default to `instance` visibility

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Tag
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: TagInput,
) -> Response[Problem | Tag]:
    """Create a tag

     Creates a personal or shared tag owned by the caller.

    **Scope:** `read`.

    Args:
        body (TagInput): Create a tag; personal tags are visible only to their owner, shared tags
            default to `instance` visibility

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Tag]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: TagInput,
) -> Problem | Tag | None:
    """Create a tag

     Creates a personal or shared tag owned by the caller.

    **Scope:** `read`.

    Args:
        body (TagInput): Create a tag; personal tags are visible only to their owner, shared tags
            default to `instance` visibility

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Tag
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
