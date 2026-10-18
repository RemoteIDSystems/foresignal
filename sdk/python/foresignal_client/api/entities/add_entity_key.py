from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.entity_key import EntityKey
from ...models.entity_key_input import EntityKeyInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    entity_id: str,
    *,
    body: EntityKeyInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/entities/{entity_id}/keys".format(
            entity_id=quote(str(entity_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> EntityKey | Problem | None:
    if response.status_code == 201:
        response_201 = EntityKey.from_dict(response.json())

        return response_201

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
) -> Response[EntityKey | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    entity_id: str,
    *,
    client: AuthenticatedClient,
    body: EntityKeyInput,
) -> Response[EntityKey | Problem]:
    """Assert a key on an entity

     Operator key assertion with confidence and validity (origin OPERATOR). A key open on another entity
    is 409 ENTITY_KEY_CONFLICT unless `force` is true.

    **Scope:** `admin`.

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (EntityKeyInput): Operator assertion of a key on an entity

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EntityKey | Problem]
    """

    kwargs = _get_kwargs(
        entity_id=entity_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    entity_id: str,
    *,
    client: AuthenticatedClient,
    body: EntityKeyInput,
) -> EntityKey | Problem | None:
    """Assert a key on an entity

     Operator key assertion with confidence and validity (origin OPERATOR). A key open on another entity
    is 409 ENTITY_KEY_CONFLICT unless `force` is true.

    **Scope:** `admin`.

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (EntityKeyInput): Operator assertion of a key on an entity

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EntityKey | Problem
    """

    return sync_detailed(
        entity_id=entity_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    entity_id: str,
    *,
    client: AuthenticatedClient,
    body: EntityKeyInput,
) -> Response[EntityKey | Problem]:
    """Assert a key on an entity

     Operator key assertion with confidence and validity (origin OPERATOR). A key open on another entity
    is 409 ENTITY_KEY_CONFLICT unless `force` is true.

    **Scope:** `admin`.

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (EntityKeyInput): Operator assertion of a key on an entity

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[EntityKey | Problem]
    """

    kwargs = _get_kwargs(
        entity_id=entity_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    entity_id: str,
    *,
    client: AuthenticatedClient,
    body: EntityKeyInput,
) -> EntityKey | Problem | None:
    """Assert a key on an entity

     Operator key assertion with confidence and validity (origin OPERATOR). A key open on another entity
    is 409 ENTITY_KEY_CONFLICT unless `force` is true.

    **Scope:** `admin`.

    Args:
        entity_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (EntityKeyInput): Operator assertion of a key on an entity

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        EntityKey | Problem
    """

    return (
        await asyncio_detailed(
            entity_id=entity_id,
            client=client,
            body=body,
        )
    ).parsed
