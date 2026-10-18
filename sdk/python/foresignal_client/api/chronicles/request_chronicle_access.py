from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.access_request import AccessRequest
from ...models.access_request_input import AccessRequestInput
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    link_id: str,
    *,
    body: AccessRequestInput | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/chronicle-network/links/{link_id}/request-access".format(
            link_id=quote(str(link_id), safe=""),
        ),
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AccessRequest | Problem | None:
    if response.status_code == 201:
        response_201 = AccessRequest.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

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
) -> Response[AccessRequest | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    link_id: str,
    *,
    client: AuthenticatedClient,
    body: AccessRequestInput | Unset = UNSET,
) -> Response[AccessRequest | Problem]:
    """Request access to a related chronicle

     Sends the other chronicle's owner (locally, or through the peer channel) only the requester's
    handle, the message and an optional share offer. The link must be shown to the caller (opaque,
    within its daily cap); a link whose other chronicle the caller already sees is 409
    CHRONICLE_ALREADY_VISIBLE. Limits: 10 per principal per day (429 REQUEST_RATE_LIMITED); repeats to
    an owner who declined the same requester within 30 days are stored but suppressed, so that owner
    never sees them.

    **Scope:** `read`.

    Args:
        link_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AccessRequestInput | Unset): Request access to the other chronicle of a link

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AccessRequest | Problem]
    """

    kwargs = _get_kwargs(
        link_id=link_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    link_id: str,
    *,
    client: AuthenticatedClient,
    body: AccessRequestInput | Unset = UNSET,
) -> AccessRequest | Problem | None:
    """Request access to a related chronicle

     Sends the other chronicle's owner (locally, or through the peer channel) only the requester's
    handle, the message and an optional share offer. The link must be shown to the caller (opaque,
    within its daily cap); a link whose other chronicle the caller already sees is 409
    CHRONICLE_ALREADY_VISIBLE. Limits: 10 per principal per day (429 REQUEST_RATE_LIMITED); repeats to
    an owner who declined the same requester within 30 days are stored but suppressed, so that owner
    never sees them.

    **Scope:** `read`.

    Args:
        link_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AccessRequestInput | Unset): Request access to the other chronicle of a link

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AccessRequest | Problem
    """

    return sync_detailed(
        link_id=link_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    link_id: str,
    *,
    client: AuthenticatedClient,
    body: AccessRequestInput | Unset = UNSET,
) -> Response[AccessRequest | Problem]:
    """Request access to a related chronicle

     Sends the other chronicle's owner (locally, or through the peer channel) only the requester's
    handle, the message and an optional share offer. The link must be shown to the caller (opaque,
    within its daily cap); a link whose other chronicle the caller already sees is 409
    CHRONICLE_ALREADY_VISIBLE. Limits: 10 per principal per day (429 REQUEST_RATE_LIMITED); repeats to
    an owner who declined the same requester within 30 days are stored but suppressed, so that owner
    never sees them.

    **Scope:** `read`.

    Args:
        link_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AccessRequestInput | Unset): Request access to the other chronicle of a link

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AccessRequest | Problem]
    """

    kwargs = _get_kwargs(
        link_id=link_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    link_id: str,
    *,
    client: AuthenticatedClient,
    body: AccessRequestInput | Unset = UNSET,
) -> AccessRequest | Problem | None:
    """Request access to a related chronicle

     Sends the other chronicle's owner (locally, or through the peer channel) only the requester's
    handle, the message and an optional share offer. The link must be shown to the caller (opaque,
    within its daily cap); a link whose other chronicle the caller already sees is 409
    CHRONICLE_ALREADY_VISIBLE. Limits: 10 per principal per day (429 REQUEST_RATE_LIMITED); repeats to
    an owner who declined the same requester within 30 days are stored but suppressed, so that owner
    never sees them.

    **Scope:** `read`.

    Args:
        link_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AccessRequestInput | Unset): Request access to the other chronicle of a link

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AccessRequest | Problem
    """

    return (
        await asyncio_detailed(
            link_id=link_id,
            client=client,
            body=body,
        )
    ).parsed
