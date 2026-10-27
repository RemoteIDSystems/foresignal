from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.collection_set import CollectionSet
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/sources/collection",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CollectionSet | Problem | None:
    if response.status_code == 200:
        response_200 = CollectionSet.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[CollectionSet | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[CollectionSet | Problem]:
    """Get the instance collection set

     Returns the instance collection set: every registered source with `inCollection` (whether the
    instance may collect it) and `enabledByUsers` (how many active users' views keep it). The collector
    pulls a source once when it is in this set and some active user's view keeps it, or, while no user
    is active, whenever it is in this set. Requires `collection.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CollectionSet | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> CollectionSet | Problem | None:
    """Get the instance collection set

     Returns the instance collection set: every registered source with `inCollection` (whether the
    instance may collect it) and `enabledByUsers` (how many active users' views keep it). The collector
    pulls a source once when it is in this set and some active user's view keeps it, or, while no user
    is active, whenever it is in this set. Requires `collection.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CollectionSet | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[CollectionSet | Problem]:
    """Get the instance collection set

     Returns the instance collection set: every registered source with `inCollection` (whether the
    instance may collect it) and `enabledByUsers` (how many active users' views keep it). The collector
    pulls a source once when it is in this set and some active user's view keeps it, or, while no user
    is active, whenever it is in this set. Requires `collection.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CollectionSet | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> CollectionSet | Problem | None:
    """Get the instance collection set

     Returns the instance collection set: every registered source with `inCollection` (whether the
    instance may collect it) and `enabledByUsers` (how many active users' views keep it). The collector
    pulls a source once when it is in this set and some active user's view keeps it, or, while no user
    is active, whenever it is in this set. Requires `collection.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CollectionSet | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
