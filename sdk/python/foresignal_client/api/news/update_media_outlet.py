from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.media_outlet import MediaOutlet
from ...models.media_outlet_patch import MediaOutletPatch
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    outlet_id: str,
    *,
    body: MediaOutletPatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/media/outlets/{outlet_id}".format(
            outlet_id=quote(str(outlet_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MediaOutlet | Problem | None:
    if response.status_code == 200:
        response_200 = MediaOutlet.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[MediaOutlet | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    outlet_id: str,
    *,
    client: AuthenticatedClient,
    body: MediaOutletPatch,
) -> Response[MediaOutlet | Problem]:
    """Update a media outlet

     JSON merge patch: type, owner, QID, domains, merging duplicate outlets. Ratings cannot be edited
    here.

    **Scope:** `admin`.

    Args:
        outlet_id (str):
        body (MediaOutletPatch): JSON merge patch for an outlet (admin)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaOutlet | Problem]
    """

    kwargs = _get_kwargs(
        outlet_id=outlet_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    outlet_id: str,
    *,
    client: AuthenticatedClient,
    body: MediaOutletPatch,
) -> MediaOutlet | Problem | None:
    """Update a media outlet

     JSON merge patch: type, owner, QID, domains, merging duplicate outlets. Ratings cannot be edited
    here.

    **Scope:** `admin`.

    Args:
        outlet_id (str):
        body (MediaOutletPatch): JSON merge patch for an outlet (admin)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaOutlet | Problem
    """

    return sync_detailed(
        outlet_id=outlet_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    outlet_id: str,
    *,
    client: AuthenticatedClient,
    body: MediaOutletPatch,
) -> Response[MediaOutlet | Problem]:
    """Update a media outlet

     JSON merge patch: type, owner, QID, domains, merging duplicate outlets. Ratings cannot be edited
    here.

    **Scope:** `admin`.

    Args:
        outlet_id (str):
        body (MediaOutletPatch): JSON merge patch for an outlet (admin)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaOutlet | Problem]
    """

    kwargs = _get_kwargs(
        outlet_id=outlet_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    outlet_id: str,
    *,
    client: AuthenticatedClient,
    body: MediaOutletPatch,
) -> MediaOutlet | Problem | None:
    """Update a media outlet

     JSON merge patch: type, owner, QID, domains, merging duplicate outlets. Ratings cannot be edited
    here.

    **Scope:** `admin`.

    Args:
        outlet_id (str):
        body (MediaOutletPatch): JSON merge patch for an outlet (admin)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaOutlet | Problem
    """

    return (
        await asyncio_detailed(
            outlet_id=outlet_id,
            client=client,
            body=body,
        )
    ).parsed
