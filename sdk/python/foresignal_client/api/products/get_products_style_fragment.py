from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_products_style_fragment_tms import GetProductsStyleFragmentTms
from ...models.problem import Problem
from ...models.style_fragment import StyleFragment
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    products: list[str],
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductsStyleFragmentTms
    | Unset = GetProductsStyleFragmentTms.WEBMERCATORQUAD,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_products = products

    params["products"] = json_products

    params["frameKey"] = frame_key

    params["style"] = style

    json_tms: str | Unset = UNSET
    if not isinstance(tms, Unset):
        json_tms = tms.value

    params["tms"] = json_tms

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/products/style-fragment.json",
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
    *,
    client: AuthenticatedClient,
    products: list[str],
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductsStyleFragmentTms
    | Unset = GetProductsStyleFragmentTms.WEBMERCATORQUAD,
) -> Response[Problem | StyleFragment]:
    """Style fragment for several products

     One fragment with a namespaced source and layer per product.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str]):
        frame_key (str | Unset):
        style (str | Unset):
        tms (GetProductsStyleFragmentTms | Unset):  Default:
            GetProductsStyleFragmentTms.WEBMERCATORQUAD.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StyleFragment]
    """

    kwargs = _get_kwargs(
        products=products,
        frame_key=frame_key,
        style=style,
        tms=tms,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    products: list[str],
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductsStyleFragmentTms
    | Unset = GetProductsStyleFragmentTms.WEBMERCATORQUAD,
) -> Problem | StyleFragment | None:
    """Style fragment for several products

     One fragment with a namespaced source and layer per product.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str]):
        frame_key (str | Unset):
        style (str | Unset):
        tms (GetProductsStyleFragmentTms | Unset):  Default:
            GetProductsStyleFragmentTms.WEBMERCATORQUAD.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StyleFragment
    """

    return sync_detailed(
        client=client,
        products=products,
        frame_key=frame_key,
        style=style,
        tms=tms,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    products: list[str],
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductsStyleFragmentTms
    | Unset = GetProductsStyleFragmentTms.WEBMERCATORQUAD,
) -> Response[Problem | StyleFragment]:
    """Style fragment for several products

     One fragment with a namespaced source and layer per product.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str]):
        frame_key (str | Unset):
        style (str | Unset):
        tms (GetProductsStyleFragmentTms | Unset):  Default:
            GetProductsStyleFragmentTms.WEBMERCATORQUAD.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | StyleFragment]
    """

    kwargs = _get_kwargs(
        products=products,
        frame_key=frame_key,
        style=style,
        tms=tms,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    products: list[str],
    frame_key: str | Unset = UNSET,
    style: str | Unset = UNSET,
    tms: GetProductsStyleFragmentTms
    | Unset = GetProductsStyleFragmentTms.WEBMERCATORQUAD,
) -> Problem | StyleFragment | None:
    """Style fragment for several products

     One fragment with a namespaced source and layer per product.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        products (list[str]):
        frame_key (str | Unset):
        style (str | Unset):
        tms (GetProductsStyleFragmentTms | Unset):  Default:
            GetProductsStyleFragmentTms.WEBMERCATORQUAD.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | StyleFragment
    """

    return (
        await asyncio_detailed(
            client=client,
            products=products,
            frame_key=frame_key,
            style=style,
            tms=tms,
        )
    ).parsed
