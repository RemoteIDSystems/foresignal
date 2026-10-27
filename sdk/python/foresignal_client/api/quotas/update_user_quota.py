from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.quota import Quota
from ...types import Response


def _get_kwargs(
    id: UUID,
    *,
    body: Quota,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/users/{id}/quota".format(
            id=quote(str(id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Quota | None:
    if response.status_code == 200:
        response_200 = Quota.from_dict(response.json())

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
) -> Response[Problem | Quota]:
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
    body: Quota,
) -> Response[Problem | Quota]:
    """Set a user's quota

     Replaces a user's own quota whole (R2-026): every member is a non-negative integer or null, and null
    (or absent) uses the instance default; an unknown member is 400. A quota with every member null
    removes the user's own limits. `0` allows nothing: no API requests, or no further alert rules,
    channels, views or stored bytes. `apiRateLimitPerMinute` limits the user's sessions and own API
    tokens together (429 RATE_LIMITED with Retry-After, `class: user`); when a user is refused in every
    minute for 5 minutes an operational alert `system.user_rate_limited` is raised, and
    `system.user_rate_limit_cleared` once a minute passes without a refusal. The count and storage
    limits apply when the user creates (409 QUOTA_EXCEEDED). Requires `users.manage`.

    **Scope:** `admin`.

    Args:
        id (UUID):
        body (Quota): Per-user limits so one user cannot exhaust the instance (D5/189, R2-026). In
            a user's quota a null member uses the instance default; unset there too it is unlimited.
            `0` allows nothing.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Quota]
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
    body: Quota,
) -> Problem | Quota | None:
    """Set a user's quota

     Replaces a user's own quota whole (R2-026): every member is a non-negative integer or null, and null
    (or absent) uses the instance default; an unknown member is 400. A quota with every member null
    removes the user's own limits. `0` allows nothing: no API requests, or no further alert rules,
    channels, views or stored bytes. `apiRateLimitPerMinute` limits the user's sessions and own API
    tokens together (429 RATE_LIMITED with Retry-After, `class: user`); when a user is refused in every
    minute for 5 minutes an operational alert `system.user_rate_limited` is raised, and
    `system.user_rate_limit_cleared` once a minute passes without a refusal. The count and storage
    limits apply when the user creates (409 QUOTA_EXCEEDED). Requires `users.manage`.

    **Scope:** `admin`.

    Args:
        id (UUID):
        body (Quota): Per-user limits so one user cannot exhaust the instance (D5/189, R2-026). In
            a user's quota a null member uses the instance default; unset there too it is unlimited.
            `0` allows nothing.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Quota
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
    body: Quota,
) -> Response[Problem | Quota]:
    """Set a user's quota

     Replaces a user's own quota whole (R2-026): every member is a non-negative integer or null, and null
    (or absent) uses the instance default; an unknown member is 400. A quota with every member null
    removes the user's own limits. `0` allows nothing: no API requests, or no further alert rules,
    channels, views or stored bytes. `apiRateLimitPerMinute` limits the user's sessions and own API
    tokens together (429 RATE_LIMITED with Retry-After, `class: user`); when a user is refused in every
    minute for 5 minutes an operational alert `system.user_rate_limited` is raised, and
    `system.user_rate_limit_cleared` once a minute passes without a refusal. The count and storage
    limits apply when the user creates (409 QUOTA_EXCEEDED). Requires `users.manage`.

    **Scope:** `admin`.

    Args:
        id (UUID):
        body (Quota): Per-user limits so one user cannot exhaust the instance (D5/189, R2-026). In
            a user's quota a null member uses the instance default; unset there too it is unlimited.
            `0` allows nothing.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Quota]
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
    body: Quota,
) -> Problem | Quota | None:
    """Set a user's quota

     Replaces a user's own quota whole (R2-026): every member is a non-negative integer or null, and null
    (or absent) uses the instance default; an unknown member is 400. A quota with every member null
    removes the user's own limits. `0` allows nothing: no API requests, or no further alert rules,
    channels, views or stored bytes. `apiRateLimitPerMinute` limits the user's sessions and own API
    tokens together (429 RATE_LIMITED with Retry-After, `class: user`); when a user is refused in every
    minute for 5 minutes an operational alert `system.user_rate_limited` is raised, and
    `system.user_rate_limit_cleared` once a minute passes without a refusal. The count and storage
    limits apply when the user creates (409 QUOTA_EXCEEDED). Requires `users.manage`.

    **Scope:** `admin`.

    Args:
        id (UUID):
        body (Quota): Per-user limits so one user cannot exhaust the instance (D5/189, R2-026). In
            a user's quota a null member uses the instance default; unset there too it is unlimited.
            `0` allows nothing.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Quota
    """

    return (
        await asyncio_detailed(
            id=id,
            client=client,
            body=body,
        )
    ).parsed
