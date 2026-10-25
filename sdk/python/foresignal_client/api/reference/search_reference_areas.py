from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.reference_area import ReferenceArea
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    q: str,
    limit: int | Unset = 10,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["q"] = q

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/reference/areas",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | list[ReferenceArea] | None:
    if response.status_code == 200:
        response_200 = []
        _response_200 = response.json()
        for response_200_item_data in _response_200:
            response_200_item = ReferenceArea.from_dict(response_200_item_data)

            response_200.append(response_200_item)

        return response_200

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
) -> Response[Problem | list[ReferenceArea]]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    q: str,
    limit: int | Unset = 10,
) -> Response[Problem | list[ReferenceArea]]:
    """Find countries, states and places by name

     Countries, admin-1 regions (states, provinces) and populated places whose names match `q`, for
    scoping a rule to an area. An exact name ranks before a name that starts with the text, before one
    that contains it; then countries, admin-1 regions, and places by population. `q` may end with a
    comma and a qualifier, a country (code or name) or the admin-1 region a place lies in (code or
    name): `Paris, TX`, `Paris, France`.

    A country or admin-1 result is what a rule's `geofenceRef` names (`country` takes `code` of a
    country, `admin1` the `code` of an admin-1 region); a place is a point, which a rule uses as
    `near.point` with a radius. Countries come from the bundled table and are found with no reference
    data loaded; admin-1 regions need the loaded admin-1 boundaries and places the loaded gazetteer, and
    are simply absent without them. A place's admin-1 region is the one its point lies in.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str):
        limit (int | Unset):  Default: 10.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[ReferenceArea]]
    """

    kwargs = _get_kwargs(
        q=q,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    q: str,
    limit: int | Unset = 10,
) -> Problem | list[ReferenceArea] | None:
    """Find countries, states and places by name

     Countries, admin-1 regions (states, provinces) and populated places whose names match `q`, for
    scoping a rule to an area. An exact name ranks before a name that starts with the text, before one
    that contains it; then countries, admin-1 regions, and places by population. `q` may end with a
    comma and a qualifier, a country (code or name) or the admin-1 region a place lies in (code or
    name): `Paris, TX`, `Paris, France`.

    A country or admin-1 result is what a rule's `geofenceRef` names (`country` takes `code` of a
    country, `admin1` the `code` of an admin-1 region); a place is a point, which a rule uses as
    `near.point` with a radius. Countries come from the bundled table and are found with no reference
    data loaded; admin-1 regions need the loaded admin-1 boundaries and places the loaded gazetteer, and
    are simply absent without them. A place's admin-1 region is the one its point lies in.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str):
        limit (int | Unset):  Default: 10.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[ReferenceArea]
    """

    return sync_detailed(
        client=client,
        q=q,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    q: str,
    limit: int | Unset = 10,
) -> Response[Problem | list[ReferenceArea]]:
    """Find countries, states and places by name

     Countries, admin-1 regions (states, provinces) and populated places whose names match `q`, for
    scoping a rule to an area. An exact name ranks before a name that starts with the text, before one
    that contains it; then countries, admin-1 regions, and places by population. `q` may end with a
    comma and a qualifier, a country (code or name) or the admin-1 region a place lies in (code or
    name): `Paris, TX`, `Paris, France`.

    A country or admin-1 result is what a rule's `geofenceRef` names (`country` takes `code` of a
    country, `admin1` the `code` of an admin-1 region); a place is a point, which a rule uses as
    `near.point` with a radius. Countries come from the bundled table and are found with no reference
    data loaded; admin-1 regions need the loaded admin-1 boundaries and places the loaded gazetteer, and
    are simply absent without them. A place's admin-1 region is the one its point lies in.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str):
        limit (int | Unset):  Default: 10.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[ReferenceArea]]
    """

    kwargs = _get_kwargs(
        q=q,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    q: str,
    limit: int | Unset = 10,
) -> Problem | list[ReferenceArea] | None:
    """Find countries, states and places by name

     Countries, admin-1 regions (states, provinces) and populated places whose names match `q`, for
    scoping a rule to an area. An exact name ranks before a name that starts with the text, before one
    that contains it; then countries, admin-1 regions, and places by population. `q` may end with a
    comma and a qualifier, a country (code or name) or the admin-1 region a place lies in (code or
    name): `Paris, TX`, `Paris, France`.

    A country or admin-1 result is what a rule's `geofenceRef` names (`country` takes `code` of a
    country, `admin1` the `code` of an admin-1 region); a place is a point, which a rule uses as
    `near.point` with a radius. Countries come from the bundled table and are found with no reference
    data loaded; admin-1 regions need the loaded admin-1 boundaries and places the loaded gazetteer, and
    are simply absent without them. A place's admin-1 region is the one its point lies in.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        q (str):
        limit (int | Unset):  Default: 10.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[ReferenceArea]
    """

    return (
        await asyncio_detailed(
            client=client,
            q=q,
            limit=limit,
        )
    ).parsed
