from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_map_style_theme import GetMapStyleTheme
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    layers: str | Unset = UNSET,
    theme: GetMapStyleTheme | Unset = GetMapStyleTheme.LIGHT,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["layers"] = layers

    json_theme: str | Unset = UNSET
    if not isinstance(theme, Unset):
        json_theme = theme.value

    params["theme"] = json_theme

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/map/style.json",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    layers: str | Unset = UNSET,
    theme: GetMapStyleTheme | Unset = GetMapStyleTheme.LIGHT,
) -> Response[Any | Problem]:
    """MapLibre overlay style for record layers

     Version-8 MapLibre style generated from each kind's `kindDisplay` hints: one vector source per
    requested layer (016 per-layer TileJSON, prefixed `/v1/k/{key}/` when the request came through a
    tile key), symbol/circle/line/fill layers per kind, hex-aggregation sources below `aggBelowZoom`,
    and `sprite` pointing at the content-addressed sprite. Overlay only: no `glyphs`, no text layers, no
    basemap. Private sources are omitted unless the caller passes the private gate. Strong ETag, `max-
    age=300`; `If-None-Match` → 304. Generated URLs use `publicBaseUrl`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layers (str | Unset):  Example: aircraft,earthquakes.
        theme (GetMapStyleTheme | Unset):  Default: GetMapStyleTheme.LIGHT.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        layers=layers,
        theme=theme,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    layers: str | Unset = UNSET,
    theme: GetMapStyleTheme | Unset = GetMapStyleTheme.LIGHT,
) -> Any | Problem | None:
    """MapLibre overlay style for record layers

     Version-8 MapLibre style generated from each kind's `kindDisplay` hints: one vector source per
    requested layer (016 per-layer TileJSON, prefixed `/v1/k/{key}/` when the request came through a
    tile key), symbol/circle/line/fill layers per kind, hex-aggregation sources below `aggBelowZoom`,
    and `sprite` pointing at the content-addressed sprite. Overlay only: no `glyphs`, no text layers, no
    basemap. Private sources are omitted unless the caller passes the private gate. Strong ETag, `max-
    age=300`; `If-None-Match` → 304. Generated URLs use `publicBaseUrl`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layers (str | Unset):  Example: aircraft,earthquakes.
        theme (GetMapStyleTheme | Unset):  Default: GetMapStyleTheme.LIGHT.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        client=client,
        layers=layers,
        theme=theme,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    layers: str | Unset = UNSET,
    theme: GetMapStyleTheme | Unset = GetMapStyleTheme.LIGHT,
) -> Response[Any | Problem]:
    """MapLibre overlay style for record layers

     Version-8 MapLibre style generated from each kind's `kindDisplay` hints: one vector source per
    requested layer (016 per-layer TileJSON, prefixed `/v1/k/{key}/` when the request came through a
    tile key), symbol/circle/line/fill layers per kind, hex-aggregation sources below `aggBelowZoom`,
    and `sprite` pointing at the content-addressed sprite. Overlay only: no `glyphs`, no text layers, no
    basemap. Private sources are omitted unless the caller passes the private gate. Strong ETag, `max-
    age=300`; `If-None-Match` → 304. Generated URLs use `publicBaseUrl`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layers (str | Unset):  Example: aircraft,earthquakes.
        theme (GetMapStyleTheme | Unset):  Default: GetMapStyleTheme.LIGHT.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        layers=layers,
        theme=theme,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    layers: str | Unset = UNSET,
    theme: GetMapStyleTheme | Unset = GetMapStyleTheme.LIGHT,
) -> Any | Problem | None:
    """MapLibre overlay style for record layers

     Version-8 MapLibre style generated from each kind's `kindDisplay` hints: one vector source per
    requested layer (016 per-layer TileJSON, prefixed `/v1/k/{key}/` when the request came through a
    tile key), symbol/circle/line/fill layers per kind, hex-aggregation sources below `aggBelowZoom`,
    and `sprite` pointing at the content-addressed sprite. Overlay only: no `glyphs`, no text layers, no
    basemap. Private sources are omitted unless the caller passes the private gate. Strong ETag, `max-
    age=300`; `If-None-Match` → 304. Generated URLs use `publicBaseUrl`.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        layers (str | Unset):  Example: aircraft,earthquakes.
        theme (GetMapStyleTheme | Unset):  Default: GetMapStyleTheme.LIGHT.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            layers=layers,
            theme=theme,
        )
    ).parsed
