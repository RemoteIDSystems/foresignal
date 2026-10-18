from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.coverage_json import CoverageJson
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    collection_id: str,
    *,
    coords: str,
    datetime_: str | Unset = UNSET,
    parameter_name: list[str] | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["coords"] = coords

    params["datetime"] = datetime_

    json_parameter_name: list[str] | Unset = UNSET
    if not isinstance(parameter_name, Unset):
        json_parameter_name = parameter_name

    params["parameter-name"] = json_parameter_name

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ogc/collections/{collection_id}/area".format(
            collection_id=quote(str(collection_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CoverageJson | Problem | None:
    if response.status_code == 200:
        response_200 = CoverageJson.from_dict(response.json())

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

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[CoverageJson | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    coords: str,
    datetime_: str | Unset = UNSET,
    parameter_name: list[str] | Unset = UNSET,
) -> Response[CoverageJson | Problem]:
    """EDR area query

     Native-grid subset of the polygon's bounding box with units and the nodata mask (`null` values),
    values flat and row-major. A regular lat/lon grid answers a `Grid` domain; a projected grid
    (geostationary, Lambert, affine) answers a `MultiPoint` domain of its cell centers. More than
    100,000 native cells is 413 EDR_TOO_LARGE.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        coords (str):
        datetime_ (str | Unset):
        parameter_name (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CoverageJson | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        coords=coords,
        datetime_=datetime_,
        parameter_name=parameter_name,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    coords: str,
    datetime_: str | Unset = UNSET,
    parameter_name: list[str] | Unset = UNSET,
) -> CoverageJson | Problem | None:
    """EDR area query

     Native-grid subset of the polygon's bounding box with units and the nodata mask (`null` values),
    values flat and row-major. A regular lat/lon grid answers a `Grid` domain; a projected grid
    (geostationary, Lambert, affine) answers a `MultiPoint` domain of its cell centers. More than
    100,000 native cells is 413 EDR_TOO_LARGE.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        coords (str):
        datetime_ (str | Unset):
        parameter_name (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CoverageJson | Problem
    """

    return sync_detailed(
        collection_id=collection_id,
        client=client,
        coords=coords,
        datetime_=datetime_,
        parameter_name=parameter_name,
    ).parsed


async def asyncio_detailed(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    coords: str,
    datetime_: str | Unset = UNSET,
    parameter_name: list[str] | Unset = UNSET,
) -> Response[CoverageJson | Problem]:
    """EDR area query

     Native-grid subset of the polygon's bounding box with units and the nodata mask (`null` values),
    values flat and row-major. A regular lat/lon grid answers a `Grid` domain; a projected grid
    (geostationary, Lambert, affine) answers a `MultiPoint` domain of its cell centers. More than
    100,000 native cells is 413 EDR_TOO_LARGE.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        coords (str):
        datetime_ (str | Unset):
        parameter_name (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CoverageJson | Problem]
    """

    kwargs = _get_kwargs(
        collection_id=collection_id,
        coords=coords,
        datetime_=datetime_,
        parameter_name=parameter_name,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    collection_id: str,
    *,
    client: AuthenticatedClient,
    coords: str,
    datetime_: str | Unset = UNSET,
    parameter_name: list[str] | Unset = UNSET,
) -> CoverageJson | Problem | None:
    """EDR area query

     Native-grid subset of the polygon's bounding box with units and the nodata mask (`null` values),
    values flat and row-major. A regular lat/lon grid answers a `Grid` domain; a projected grid
    (geostationary, Lambert, affine) answers a `MultiPoint` domain of its cell centers. More than
    100,000 native cells is 413 EDR_TOO_LARGE.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        collection_id (str):
        coords (str):
        datetime_ (str | Unset):
        parameter_name (list[str] | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CoverageJson | Problem
    """

    return (
        await asyncio_detailed(
            collection_id=collection_id,
            client=client,
            coords=coords,
            datetime_=datetime_,
            parameter_name=parameter_name,
        )
    ).parsed
