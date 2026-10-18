import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.entity import Entity
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    key: str,
    at: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["key"] = key

    json_at: str | Unset = UNSET
    if not isinstance(at, Unset):
        json_at = at.isoformat()
    params["at"] = json_at

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/entities/resolve",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Entity | Problem | None:
    if response.status_code == 200:
        response_200 = Entity.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

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
) -> Response[Entity | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    key: str,
    at: datetime.datetime | Unset = UNSET,
) -> Response[Entity | Problem]:
    """Resolve a key to its entity

     Returns the entity holding the key at `at` (default now).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        key (str):  Example: icao24:ae1234.
        at (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Entity | Problem]
    """

    kwargs = _get_kwargs(
        key=key,
        at=at,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    key: str,
    at: datetime.datetime | Unset = UNSET,
) -> Entity | Problem | None:
    """Resolve a key to its entity

     Returns the entity holding the key at `at` (default now).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        key (str):  Example: icao24:ae1234.
        at (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Entity | Problem
    """

    return sync_detailed(
        client=client,
        key=key,
        at=at,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    key: str,
    at: datetime.datetime | Unset = UNSET,
) -> Response[Entity | Problem]:
    """Resolve a key to its entity

     Returns the entity holding the key at `at` (default now).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        key (str):  Example: icao24:ae1234.
        at (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Entity | Problem]
    """

    kwargs = _get_kwargs(
        key=key,
        at=at,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    key: str,
    at: datetime.datetime | Unset = UNSET,
) -> Entity | Problem | None:
    """Resolve a key to its entity

     Returns the entity holding the key at `at` (default now).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        key (str):  Example: icao24:ae1234.
        at (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Entity | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            key=key,
            at=at,
        )
    ).parsed
