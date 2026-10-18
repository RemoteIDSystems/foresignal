from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_product_style_fragment_tms import GetProductStyleFragmentTms
from ...models.problem import Problem
from ...models.style_fragment import StyleFragment
from ...types import UNSET, Response, Unset


def _get_kwargs(
    product_id: str,
    *,
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductStyleFragmentTms
    | Unset = GetProductStyleFragmentTms.WEBMERCATORQUAD,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["frameKey"] = frame_key

    params["style"] = style

    json_tms: str | Unset = UNSET
    if not isinstance(tms, Unset):
        json_tms = tms.value

    params["tms"] = json_tms

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/{product_id}/style-fragment.json".format(
            product_id=quote(str(product_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | StyleFragment | None:
    if response.status_code == 200:
        response_200 = StyleFragment.from_dict(response.json())

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
) -> Response[Problem | StyleFragment]:
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
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductStyleFragmentTms
    | Unset = GetProductStyleFragmentTms.WEBMERCATORQUAD,
) -> Response[Problem | StyleFragment]:
    """MapLibre style fragment for a product

     `{version: 8, sources, layers}` with concrete content-addressed URLs, zooms, bounds, attribution and
    legend links. The server never fetches a basemap style.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str | Unset):
        style (str | Unset):
        tms (GetProductStyleFragmentTms | Unset):  Default:
            GetProductStyleFragmentTms.WEBMERCATORQUAD.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StyleFragment]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        style=style,
        tms=tms,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    product_id: str,
    *,
    client: AuthenticatedClient,
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductStyleFragmentTms
    | Unset = GetProductStyleFragmentTms.WEBMERCATORQUAD,
) -> Problem | StyleFragment | None:
    """MapLibre style fragment for a product

     `{version: 8, sources, layers}` with concrete content-addressed URLs, zooms, bounds, attribution and
    legend links. The server never fetches a basemap style.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str | Unset):
        style (str | Unset):
        tms (GetProductStyleFragmentTms | Unset):  Default:
            GetProductStyleFragmentTms.WEBMERCATORQUAD.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StyleFragment
    """

    return sync_detailed(
        product_id=product_id,
        client=client,
        frame_key=frame_key,
        style=style,
        tms=tms,
    ).parsed


async def asyncio_detailed(
    product_id: str,
    *,
    client: AuthenticatedClient,
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductStyleFragmentTms
    | Unset = GetProductStyleFragmentTms.WEBMERCATORQUAD,
) -> Response[Problem | StyleFragment]:
    """MapLibre style fragment for a product

     `{version: 8, sources, layers}` with concrete content-addressed URLs, zooms, bounds, attribution and
    legend links. The server never fetches a basemap style.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str | Unset):
        style (str | Unset):
        tms (GetProductStyleFragmentTms | Unset):  Default:
            GetProductStyleFragmentTms.WEBMERCATORQUAD.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StyleFragment]
    """

    kwargs = _get_kwargs(
        product_id=product_id,
        frame_key=frame_key,
        style=style,
        tms=tms,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    product_id: str,
    *,
    client: AuthenticatedClient,
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductStyleFragmentTms
    | Unset = GetProductStyleFragmentTms.WEBMERCATORQUAD,
) -> Problem | StyleFragment | None:
    """MapLibre style fragment for a product

     `{version: 8, sources, layers}` with concrete content-addressed URLs, zooms, bounds, attribution and
    legend links. The server never fetches a basemap style.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        product_id (str):
        frame_key (str | Unset):
        style (str | Unset):
        tms (GetProductStyleFragmentTms | Unset):  Default:
            GetProductStyleFragmentTms.WEBMERCATORQUAD.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StyleFragment
    """

    return (
        await asyncio_detailed(
            product_id=product_id,
            client=client,
            frame_key=frame_key,
            style=style,
            tms=tms,
        )
    ).parsed
