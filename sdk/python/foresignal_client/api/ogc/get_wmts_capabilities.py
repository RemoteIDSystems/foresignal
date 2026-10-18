from http import HTTPStatus
from typing import Any, cast

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/wmts/1.0.0/WMTSCapabilities.xml",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

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
) -> Response[Any | Problem]:
    """WMTS capabilities

     One layer per product × style with both tile matrix sets, `Time` (and `RefTime` for forecasts)
    dimensions and RESTful ResourceURL templates. Through `/v1/k/{key}/wmts/...` every URL embeds the
    key prefix. Each layer carries `TileMatrixSetLimits` for its native zoom range and bounds. ETag,
    `private, max-age=60` with `Vary: Authorization` (the layer list depends on the caller).

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Any | Problem | None:
    """WMTS capabilities

     One layer per product × style with both tile matrix sets, `Time` (and `RefTime` for forecasts)
    dimensions and RESTful ResourceURL templates. Through `/v1/k/{key}/wmts/...` every URL embeds the
    key prefix. Each layer carries `TileMatrixSetLimits` for its native zoom range and bounds. ETag,
    `private, max-age=60` with `Vary: Authorization` (the layer list depends on the caller).

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Any | Problem]:
    """WMTS capabilities

     One layer per product × style with both tile matrix sets, `Time` (and `RefTime` for forecasts)
    dimensions and RESTful ResourceURL templates. Through `/v1/k/{key}/wmts/...` every URL embeds the
    key prefix. Each layer carries `TileMatrixSetLimits` for its native zoom range and bounds. ETag,
    `private, max-age=60` with `Vary: Authorization` (the layer list depends on the caller).

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Any | Problem | None:
    """WMTS capabilities

     One layer per product × style with both tile matrix sets, `Time` (and `RefTime` for forecasts)
    dimensions and RESTful ResourceURL templates. Through `/v1/k/{key}/wmts/...` every URL embeds the
    key prefix. Each layer carries `TileMatrixSetLimits` for its native zoom range and bounds. ETag,
    `private, max-age=60` with `Vary: Authorization` (the layer list depends on the caller).

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
