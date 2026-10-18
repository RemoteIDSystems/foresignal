import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_state import ChronicleState
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    chronicle_id: str,
    *,
    at: datetime.datetime,
    layer: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_at = at.isoformat()
    params["at"] = json_at

    params["layer"] = layer

    params["bbox"] = bbox

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicles/{chronicle_id}/state".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChronicleState | Problem | None:
    if response.status_code == 200:
        response_200 = ChronicleState.from_dict(response.json())

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
) -> Response[ChronicleState | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime,
    layer: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ChronicleState | Problem]:
    """Replay chronicle state at a time

     State of every layer at `at` from chronicle storage only (no look-ahead), paged by layer.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        at (datetime.datetime):
        layer (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        bbox (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleState | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        at=at,
        layer=layer,
        bbox=bbox,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime,
    layer: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ChronicleState | Problem | None:
    """Replay chronicle state at a time

     State of every layer at `at` from chronicle storage only (no look-ahead), paged by layer.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        at (datetime.datetime):
        layer (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        bbox (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleState | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
        at=at,
        layer=layer,
        bbox=bbox,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime,
    layer: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ChronicleState | Problem]:
    """Replay chronicle state at a time

     State of every layer at `at` from chronicle storage only (no look-ahead), paged by layer.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        at (datetime.datetime):
        layer (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        bbox (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleState | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        at=at,
        layer=layer,
        bbox=bbox,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    at: datetime.datetime,
    layer: str | Unset = UNSET,
    bbox: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ChronicleState | Problem | None:
    """Replay chronicle state at a time

     State of every layer at `at` from chronicle storage only (no look-ahead), paged by layer.

    **Scope:** `read`.

    Args:
        chronicle_id (str):
        at (datetime.datetime):
        layer (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        bbox (str | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleState | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
            at=at,
            layer=layer,
            bbox=bbox,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
