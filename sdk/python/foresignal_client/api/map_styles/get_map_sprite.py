from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.map_sprite_index import MapSpriteIndex
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    sprite_file: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/map/sprites/{sprite_file}".format(
            sprite_file=quote(str(sprite_file), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MapSpriteIndex | Problem | None:
    if response.status_code == 200:
        response_200 = MapSpriteIndex.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[MapSpriteIndex | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    sprite_file: str,
    *,
    client: AuthenticatedClient,
) -> Response[MapSpriteIndex | Problem]:
    """Get the SDF sprite sheet or index

     Content-addressed sprite generated from Maki and Temaki (CC0) with `spreet --sdf`:
    `foresignal-{sha8}.json|png` at 1x and `foresignal-{sha8}@2x.json|png`. Responses are `max-
    age=31536000, immutable` (`public` or `private` per the openReads cache rule). An unknown `sha8`
    returns 404 with a `Link` header naming the current sprite.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        sprite_file (str):  Example: foresignal-3fa94c1b@2x.png.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MapSpriteIndex | Problem]
    """

    kwargs = _get_kwargs(
        sprite_file=sprite_file,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    sprite_file: str,
    *,
    client: AuthenticatedClient,
) -> MapSpriteIndex | Problem | None:
    """Get the SDF sprite sheet or index

     Content-addressed sprite generated from Maki and Temaki (CC0) with `spreet --sdf`:
    `foresignal-{sha8}.json|png` at 1x and `foresignal-{sha8}@2x.json|png`. Responses are `max-
    age=31536000, immutable` (`public` or `private` per the openReads cache rule). An unknown `sha8`
    returns 404 with a `Link` header naming the current sprite.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        sprite_file (str):  Example: foresignal-3fa94c1b@2x.png.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MapSpriteIndex | Problem
    """

    return sync_detailed(
        sprite_file=sprite_file,
        client=client,
    ).parsed


async def asyncio_detailed(
    sprite_file: str,
    *,
    client: AuthenticatedClient,
) -> Response[MapSpriteIndex | Problem]:
    """Get the SDF sprite sheet or index

     Content-addressed sprite generated from Maki and Temaki (CC0) with `spreet --sdf`:
    `foresignal-{sha8}.json|png` at 1x and `foresignal-{sha8}@2x.json|png`. Responses are `max-
    age=31536000, immutable` (`public` or `private` per the openReads cache rule). An unknown `sha8`
    returns 404 with a `Link` header naming the current sprite.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        sprite_file (str):  Example: foresignal-3fa94c1b@2x.png.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MapSpriteIndex | Problem]
    """

    kwargs = _get_kwargs(
        sprite_file=sprite_file,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    sprite_file: str,
    *,
    client: AuthenticatedClient,
) -> MapSpriteIndex | Problem | None:
    """Get the SDF sprite sheet or index

     Content-addressed sprite generated from Maki and Temaki (CC0) with `spreet --sdf`:
    `foresignal-{sha8}.json|png` at 1x and `foresignal-{sha8}@2x.json|png`. Responses are `max-
    age=31536000, immutable` (`public` or `private` per the openReads cache rule). An unknown `sha8`
    returns 404 with a `Link` header naming the current sprite.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        sprite_file (str):  Example: foresignal-3fa94c1b@2x.png.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MapSpriteIndex | Problem
    """

    return (
        await asyncio_detailed(
            sprite_file=sprite_file,
            client=client,
        )
    ).parsed
