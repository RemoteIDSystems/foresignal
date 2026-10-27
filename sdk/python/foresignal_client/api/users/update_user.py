from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.user import User
from ...models.user_update import UserUpdate
from ...types import Response


def _get_kwargs(
    id: UUID,
    *,
    body: UserUpdate,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/users/{id}".format(
            id=quote(str(id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | User | None:
    if response.status_code == 200:
        response_200 = User.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | User]:
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
    body: UserUpdate,
) -> Response[Problem | User]:
    """Update a user

     Updates a user's profile fields and, with `dataRestriction`, what the user may read: a source and
    kind subset and an area (bbox, area, places), validated like a token's restriction (400
    VALIDATION_FAILED for an unknown source, kind or place or a member that is not a data member). Null
    lifts the restriction; absent leaves it. The change applies to the user's next request. Requires
    `users.manage`.

    **Scope:** `admin`.

    Args:
        id (UUID):
        body (UserUpdate): Update a user's profile fields (roles are set via setUserRoles; status
            via suspend/unsuspend)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | User]
    """

    kwargs = _get_kwargs(
        id=id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    id: UUID,
    *,
    client: AuthenticatedClient,
    body: UserUpdate,
) -> Problem | User | None:
    """Update a user

     Updates a user's profile fields and, with `dataRestriction`, what the user may read: a source and
    kind subset and an area (bbox, area, places), validated like a token's restriction (400
    VALIDATION_FAILED for an unknown source, kind or place or a member that is not a data member). Null
    lifts the restriction; absent leaves it. The change applies to the user's next request. Requires
    `users.manage`.

    **Scope:** `admin`.

    Args:
        id (UUID):
        body (UserUpdate): Update a user's profile fields (roles are set via setUserRoles; status
            via suspend/unsuspend)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | User
    """

    return sync_detailed(
        id=id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    id: UUID,
    *,
    client: AuthenticatedClient,
    body: UserUpdate,
) -> Response[Problem | User]:
    """Update a user

     Updates a user's profile fields and, with `dataRestriction`, what the user may read: a source and
    kind subset and an area (bbox, area, places), validated like a token's restriction (400
    VALIDATION_FAILED for an unknown source, kind or place or a member that is not a data member). Null
    lifts the restriction; absent leaves it. The change applies to the user's next request. Requires
    `users.manage`.

    **Scope:** `admin`.

    Args:
        id (UUID):
        body (UserUpdate): Update a user's profile fields (roles are set via setUserRoles; status
            via suspend/unsuspend)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | User]
    """

    kwargs = _get_kwargs(
        id=id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    id: UUID,
    *,
    client: AuthenticatedClient,
    body: UserUpdate,
) -> Problem | User | None:
    """Update a user

     Updates a user's profile fields and, with `dataRestriction`, what the user may read: a source and
    kind subset and an area (bbox, area, places), validated like a token's restriction (400
    VALIDATION_FAILED for an unknown source, kind or place or a member that is not a data member). Null
    lifts the restriction; absent leaves it. The change applies to the user's next request. Requires
    `users.manage`.

    **Scope:** `admin`.

    Args:
        id (UUID):
        body (UserUpdate): Update a user's profile fields (roles are set via setUserRoles; status
            via suspend/unsuspend)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | User
    """

    return (
        await asyncio_detailed(
            id=id,
            client=client,
            body=body,
        )
    ).parsed
