from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.user_delete_result import UserDeleteResult
from ...types import UNSET, Response, Unset


def _get_kwargs(
    id: UUID,
    *,
    reassign: bool | Unset = True,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["reassign"] = reassign

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/users/{id}".format(
            id=quote(str(id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | UserDeleteResult | None:
    if response.status_code == 200:
        response_200 = UserDeleteResult.from_dict(response.json())

        return response_200

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | UserDeleteResult]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    id: UUID,
    *,
    client: AuthenticatedClient,
    reassign: bool | Unset = True,
) -> Response[Problem | UserDeleteResult]:
    """Delete a user

     Deletes a user. By default the user's owned config (triggers, channels, personal LLM providers,
    saved views) is reassigned to an admin rather than deleted; pass `reassign=false` to delete it
    instead (168). The last remaining admin cannot be deleted (409).

    **Scope:** `admin`.

    Args:
        id (UUID):
        reassign (bool | Unset):  Default: True.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserDeleteResult]
    """

    kwargs = _get_kwargs(
        id=id,
        reassign=reassign,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    id: UUID,
    *,
    client: AuthenticatedClient,
    reassign: bool | Unset = True,
) -> Problem | UserDeleteResult | None:
    """Delete a user

     Deletes a user. By default the user's owned config (triggers, channels, personal LLM providers,
    saved views) is reassigned to an admin rather than deleted; pass `reassign=false` to delete it
    instead (168). The last remaining admin cannot be deleted (409).

    **Scope:** `admin`.

    Args:
        id (UUID):
        reassign (bool | Unset):  Default: True.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserDeleteResult
    """

    return sync_detailed(
        id=id,
        client=client,
        reassign=reassign,
    ).parsed


async def asyncio_detailed(
    id: UUID,
    *,
    client: AuthenticatedClient,
    reassign: bool | Unset = True,
) -> Response[Problem | UserDeleteResult]:
    """Delete a user

     Deletes a user. By default the user's owned config (triggers, channels, personal LLM providers,
    saved views) is reassigned to an admin rather than deleted; pass `reassign=false` to delete it
    instead (168). The last remaining admin cannot be deleted (409).

    **Scope:** `admin`.

    Args:
        id (UUID):
        reassign (bool | Unset):  Default: True.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | UserDeleteResult]
    """

    kwargs = _get_kwargs(
        id=id,
        reassign=reassign,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    id: UUID,
    *,
    client: AuthenticatedClient,
    reassign: bool | Unset = True,
) -> Problem | UserDeleteResult | None:
    """Delete a user

     Deletes a user. By default the user's owned config (triggers, channels, personal LLM providers,
    saved views) is reassigned to an admin rather than deleted; pass `reassign=false` to delete it
    instead (168). The last remaining admin cannot be deleted (409).

    **Scope:** `admin`.

    Args:
        id (UUID):
        reassign (bool | Unset):  Default: True.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | UserDeleteResult
    """

    return (
        await asyncio_detailed(
            id=id,
            client=client,
            reassign=reassign,
        )
    ).parsed
