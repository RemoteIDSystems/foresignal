from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.access_request_page import AccessRequestPage
from ...models.list_chronicle_access_requests_direction import (
    ListChronicleAccessRequestsDirection,
)
from ...models.list_chronicle_access_requests_status import (
    ListChronicleAccessRequestsStatus,
)
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    direction: ListChronicleAccessRequestsDirection | Unset = UNSET,
    status: ListChronicleAccessRequestsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_direction: str | Unset = UNSET
    if not isinstance(direction, Unset):
        json_direction = direction.value

    params["direction"] = json_direction

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/chronicle-network/requests",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AccessRequestPage | Problem | None:
    if response.status_code == 200:
        response_200 = AccessRequestPage.from_dict(response.json())

        return response_200

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
) -> Response[AccessRequestPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    direction: ListChronicleAccessRequestsDirection | Unset = UNSET,
    status: ListChronicleAccessRequestsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AccessRequestPage | Problem]:
    """List access requests

     Incoming and outgoing access requests of the caller.

    **Scope:** `read`.

    Args:
        direction (ListChronicleAccessRequestsDirection | Unset):
        status (ListChronicleAccessRequestsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AccessRequestPage | Problem]
    """

    kwargs = _get_kwargs(
        direction=direction,
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    direction: ListChronicleAccessRequestsDirection | Unset = UNSET,
    status: ListChronicleAccessRequestsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AccessRequestPage | Problem | None:
    """List access requests

     Incoming and outgoing access requests of the caller.

    **Scope:** `read`.

    Args:
        direction (ListChronicleAccessRequestsDirection | Unset):
        status (ListChronicleAccessRequestsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AccessRequestPage | Problem
    """

    return sync_detailed(
        client=client,
        direction=direction,
        status=status,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    direction: ListChronicleAccessRequestsDirection | Unset = UNSET,
    status: ListChronicleAccessRequestsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[AccessRequestPage | Problem]:
    """List access requests

     Incoming and outgoing access requests of the caller.

    **Scope:** `read`.

    Args:
        direction (ListChronicleAccessRequestsDirection | Unset):
        status (ListChronicleAccessRequestsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AccessRequestPage | Problem]
    """

    kwargs = _get_kwargs(
        direction=direction,
        status=status,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    direction: ListChronicleAccessRequestsDirection | Unset = UNSET,
    status: ListChronicleAccessRequestsStatus | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> AccessRequestPage | Problem | None:
    """List access requests

     Incoming and outgoing access requests of the caller.

    **Scope:** `read`.

    Args:
        direction (ListChronicleAccessRequestsDirection | Unset):
        status (ListChronicleAccessRequestsStatus | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AccessRequestPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            direction=direction,
            status=status,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
