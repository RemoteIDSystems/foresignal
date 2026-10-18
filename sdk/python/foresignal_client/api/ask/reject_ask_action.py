from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.ask_action import AskAction
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    action_id: UUID,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/ask/actions/{action_id}/reject".format(
            action_id=quote(str(action_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AskAction | Problem | None:
    if response.status_code == 200:
        response_200 = AskAction.from_dict(response.json())

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
) -> Response[AskAction | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    action_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[AskAction | Problem]:
    """Reject a proposed action

     Discards a pending proposed action; it is never executed. Another non-admin token's action → 404;
    not pending → 409 ACTION_NOT_PENDING.

    **Scope:** `agent` or `admin`.

    Args:
        action_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AskAction | Problem]
    """

    kwargs = _get_kwargs(
        action_id=action_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    action_id: UUID,
    *,
    client: AuthenticatedClient,
) -> AskAction | Problem | None:
    """Reject a proposed action

     Discards a pending proposed action; it is never executed. Another non-admin token's action → 404;
    not pending → 409 ACTION_NOT_PENDING.

    **Scope:** `agent` or `admin`.

    Args:
        action_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AskAction | Problem
    """

    return sync_detailed(
        action_id=action_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    action_id: UUID,
    *,
    client: AuthenticatedClient,
) -> Response[AskAction | Problem]:
    """Reject a proposed action

     Discards a pending proposed action; it is never executed. Another non-admin token's action → 404;
    not pending → 409 ACTION_NOT_PENDING.

    **Scope:** `agent` or `admin`.

    Args:
        action_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AskAction | Problem]
    """

    kwargs = _get_kwargs(
        action_id=action_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    action_id: UUID,
    *,
    client: AuthenticatedClient,
) -> AskAction | Problem | None:
    """Reject a proposed action

     Discards a pending proposed action; it is never executed. Another non-admin token's action → 404;
    not pending → 409 ACTION_NOT_PENDING.

    **Scope:** `agent` or `admin`.

    Args:
        action_id (UUID):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AskAction | Problem
    """

    return (
        await asyncio_detailed(
            action_id=action_id,
            client=client,
        )
    ).parsed
