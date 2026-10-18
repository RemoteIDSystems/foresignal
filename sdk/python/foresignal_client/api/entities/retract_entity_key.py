from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.entity_key import EntityKey
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    entity_id: str,
    key_id: int,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/entities/{entity_id}/keys/{key_id}".format(
            entity_id=quote(str(entity_id), safe=""),
            key_id=quote(str(key_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EntityKey | Problem | None:
    if response.status_code == 200:
        response_200 = EntityKey.from_dict(response.json())

        return response_200

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
) -> Response[EntityKey | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    entity_id: str,
    key_id: int,
    *,
    client: AuthenticatedClient,
) -> Response[EntityKey | Problem]:
    """Retract an entity key

     Sets `validTo=now` and `retracted=true`; key rows are never deleted.

    **Scope:** `admin`.

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        key_id (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EntityKey | Problem]
    """

    kwargs = _get_kwargs(
        entity_id=entity_id,
        key_id=key_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    entity_id: str,
    key_id: int,
    *,
    client: AuthenticatedClient,
) -> EntityKey | Problem | None:
    """Retract an entity key

     Sets `validTo=now` and `retracted=true`; key rows are never deleted.

    **Scope:** `admin`.

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        key_id (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EntityKey | Problem
    """

    return sync_detailed(
        entity_id=entity_id,
        key_id=key_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    entity_id: str,
    key_id: int,
    *,
    client: AuthenticatedClient,
) -> Response[EntityKey | Problem]:
    """Retract an entity key

     Sets `validTo=now` and `retracted=true`; key rows are never deleted.

    **Scope:** `admin`.

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        key_id (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EntityKey | Problem]
    """

    kwargs = _get_kwargs(
        entity_id=entity_id,
        key_id=key_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    entity_id: str,
    key_id: int,
    *,
    client: AuthenticatedClient,
) -> EntityKey | Problem | None:
    """Retract an entity key

     Sets `validTo=now` and `retracted=true`; key rows are never deleted.

    **Scope:** `admin`.

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        key_id (int):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EntityKey | Problem
    """

    return (
        await asyncio_detailed(
            entity_id=entity_id,
            key_id=key_id,
            client=client,
        )
    ).parsed
