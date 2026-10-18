from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    product_id: str,
    *,
    style: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["style"] = style

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/mobac.xml".format(
            product_id=quote(str(product_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
) -> Response[Problem]:
    """ATAK MOBAC custom map source

     `<customMapSource>` with native zooms, tileType png, tileUpdate None and a `{$z}/{$x}/{$y}` template
    on the `latest` alias with the caller's key prefix. Meant to be fetched through a tile-key path.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        style=style,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
) -> Problem | None:
    """ATAK MOBAC custom map source

     `<customMapSource>` with native zooms, tileType png, tileUpdate None and a `{$z}/{$x}/{$y}` template
    on the `latest` alias with the caller's key prefix. Meant to be fetched through a tile-key path.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        product_id=product_id,
        client=client,
        style=style,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
) -> Response[Problem]:
    """ATAK MOBAC custom map source

     `<customMapSource>` with native zooms, tileType png, tileUpdate None and a `{$z}/{$x}/{$y}` template
    on the `latest` alias with the caller's key prefix. Meant to be fetched through a tile-key path.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        style=style,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    *,
    client: AuthenticatedClient,
    style: str | Unset = UNSET,
) -> Problem | None:
    """ATAK MOBAC custom map source

     `<customMapSource>` with native zooms, tileType png, tileUpdate None and a `{$z}/{$x}/{$y}` template
    on the `latest` alias with the caller's key prefix. Meant to be fetched through a tile-key path.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        style (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            client=client,
            style=style,
        )
    ).parsed
