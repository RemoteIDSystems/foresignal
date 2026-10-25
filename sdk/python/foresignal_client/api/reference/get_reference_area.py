from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.reference_area_outline import ReferenceAreaOutline
from ...types import Response


def _get_kwargs(
    code: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/reference/areas/{code}".format(
            code=quote(str(code), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ReferenceAreaOutline | None:
    if response.status_code == 200:
        response_200 = ReferenceAreaOutline.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ReferenceAreaOutline]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    code: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ReferenceAreaOutline]:
    """Outline of a country or admin-1 region

     The names, bounding box and outline of a country (ISO 3166-1 alpha-2 code) or admin-1 region
    (`ref_admin1` code such as `US-TX`), to draw the area a rule's `geofenceRef` names. The outline is
    simplified to about 0.01° (about 1 km), for display only: a rule always tests the full boundary. A
    country without loaded boundaries is drawn from the bundled simplified polygons.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        code (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReferenceAreaOutline]
    """

    kwargs = _get_kwargs(
        code=code,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    code: str,
    *,
    client: AuthenticatedClient,
) -> Problem | ReferenceAreaOutline | None:
    """Outline of a country or admin-1 region

     The names, bounding box and outline of a country (ISO 3166-1 alpha-2 code) or admin-1 region
    (`ref_admin1` code such as `US-TX`), to draw the area a rule's `geofenceRef` names. The outline is
    simplified to about 0.01° (about 1 km), for display only: a rule always tests the full boundary. A
    country without loaded boundaries is drawn from the bundled simplified polygons.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        code (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReferenceAreaOutline
    """

    return sync_detailed(
        code=code,
        client=client,
    ).parsed


async def asyncio_detailed(
    code: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ReferenceAreaOutline]:
    """Outline of a country or admin-1 region

     The names, bounding box and outline of a country (ISO 3166-1 alpha-2 code) or admin-1 region
    (`ref_admin1` code such as `US-TX`), to draw the area a rule's `geofenceRef` names. The outline is
    simplified to about 0.01° (about 1 km), for display only: a rule always tests the full boundary. A
    country without loaded boundaries is drawn from the bundled simplified polygons.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        code (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReferenceAreaOutline]
    """

    kwargs = _get_kwargs(
        code=code,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    code: str,
    *,
    client: AuthenticatedClient,
) -> Problem | ReferenceAreaOutline | None:
    """Outline of a country or admin-1 region

     The names, bounding box and outline of a country (ISO 3166-1 alpha-2 code) or admin-1 region
    (`ref_admin1` code such as `US-TX`), to draw the area a rule's `geofenceRef` names. The outline is
    simplified to about 0.01° (about 1 km), for display only: a rule always tests the full boundary. A
    country without loaded boundaries is drawn from the bundled simplified polygons.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        code (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReferenceAreaOutline
    """

    return (
        await asyncio_detailed(
            code=code,
            client=client,
        )
    ).parsed
