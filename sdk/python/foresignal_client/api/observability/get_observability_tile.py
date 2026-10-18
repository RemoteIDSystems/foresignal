import datetime
from http import HTTPStatus
from io import BytesIO
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, File, Response, Unset


def _get_kwargs(
    z: int,
    x: int,
    y: int,
    *,
    family: str,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["family"] = family

    json_at: str | Unset = UNSET
    if not isinstance(at, Unset):
        json_at = at.isoformat()
    params["at"] = json_at

    params["band"] = band

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/observability/tiles/{z}/{x}/{y}.mvt".format(
            z=quote(str(z), safe=""),
            x=quote(str(x), safe=""),
            y=quote(str(y), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> File | Problem | None:
    if response.status_code == 200:
        response_200 = File(payload=BytesIO(response.content))

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

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
) -> Response[File | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    family: str,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
) -> Response[File | Problem]:
    """Coverage layer vector tile

     H3 cells with `p`, `lo`, `hi` and methods for a family at a time: how well instrumented an area is.
    Uses the 016 tile machinery.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        z (int):
        x (int):
        y (int):
        family (str):
        at (datetime.datetime | Unset):
        band (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        z=z,
        x=x,
        y=y,
        family=family,
        at=at,
        band=band,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    family: str,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
) -> File | Problem | None:
    """Coverage layer vector tile

     H3 cells with `p`, `lo`, `hi` and methods for a family at a time: how well instrumented an area is.
    Uses the 016 tile machinery.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        z (int):
        x (int):
        y (int):
        family (str):
        at (datetime.datetime | Unset):
        band (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return sync_detailed(
        z=z,
        x=x,
        y=y,
        client=client,
        family=family,
        at=at,
        band=band,
    ).parsed


async def asyncio_detailed(
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    family: str,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
) -> Response[File | Problem]:
    """Coverage layer vector tile

     H3 cells with `p`, `lo`, `hi` and methods for a family at a time: how well instrumented an area is.
    Uses the 016 tile machinery.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        z (int):
        x (int):
        y (int):
        family (str):
        at (datetime.datetime | Unset):
        band (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[File | Problem]
    """

    kwargs = _get_kwargs(
        z=z,
        x=x,
        y=y,
        family=family,
        at=at,
        band=band,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    z: int,
    x: int,
    y: int,
    *,
    client: AuthenticatedClient,
    family: str,
    at: datetime.datetime | Unset = UNSET,
    band: str | Unset = UNSET,
) -> File | Problem | None:
    """Coverage layer vector tile

     H3 cells with `p`, `lo`, `hi` and methods for a family at a time: how well instrumented an area is.
    Uses the 016 tile machinery.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        z (int):
        x (int):
        y (int):
        family (str):
        at (datetime.datetime | Unset):
        band (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        File | Problem
    """

    return (
        await asyncio_detailed(
            z=z,
            x=x,
            y=y,
            client=client,
            family=family,
            at=at,
            band=band,
        )
    ).parsed
