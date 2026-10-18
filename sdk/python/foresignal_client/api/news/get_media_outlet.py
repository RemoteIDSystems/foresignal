from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.media_outlet import MediaOutlet
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    outlet_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/media/outlets/{outlet_id}".format(
            outlet_id=quote(str(outlet_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MediaOutlet | Problem | None:
    if response.status_code == 200:
        response_200 = MediaOutlet.from_dict(response.json())

        return response_200

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
) -> Response[MediaOutlet | Problem]:
    """Get a media outlet

     Ratings per provider with licence and date, consensus and ownership.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        outlet_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaOutlet | Problem]
    """

    kwargs = _get_kwargs(
        outlet_id=outlet_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    outlet_id: str,
    *,
    client: AuthenticatedClient,
) -> MediaOutlet | Problem | None:
    """Get a media outlet

     Ratings per provider with licence and date, consensus and ownership.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        outlet_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaOutlet | Problem
    """

    return sync_detailed(
        outlet_id=outlet_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    outlet_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[MediaOutlet | Problem]:
    """Get a media outlet

     Ratings per provider with licence and date, consensus and ownership.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        outlet_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaOutlet | Problem]
    """

    kwargs = _get_kwargs(
        outlet_id=outlet_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    outlet_id: str,
    *,
    client: AuthenticatedClient,
) -> MediaOutlet | Problem | None:
    """Get a media outlet

     Ratings per provider with licence and date, consensus and ownership.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        outlet_id (str):

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
        )
    ).parsed
