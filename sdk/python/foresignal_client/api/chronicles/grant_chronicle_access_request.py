from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.access_grant_input import AccessGrantInput
from ...models.access_request import AccessRequest
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    request_id: str,
    *,
    body: AccessGrantInput | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/chronicle-network/requests/{request_id}/grant".format(
            request_id=quote(str(request_id), safe=""),
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
    if response.status_code == 200:
        response_200 = AccessRequest.from_dict(response.json())

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
) -> Response[AccessRequest | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    request_id: str,
    *,
    client: AuthenticatedClient,
    body: AccessGrantInput | Unset = UNSET,
) -> Response[AccessRequest | Problem]:
    """Grant an access request

     Owner grants access: on this instance a 130 access-list grant (viewer or commenter, TLP checked)
    and, when the requester offered one, the requester's own chronicle shared back with the owner as
    viewer; for a remote requester a 132 signed package (a chronicle extract, or a chronicle-recap
    report drafted and published for the purpose) addressed to the requester's handle on its instance.
    The requester is told the status only.

    **Scope:** `agent`.

    Args:
        request_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AccessGrantInput | Unset): Grant an access request (owner)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AccessRequest | Problem]
    """

    kwargs = _get_kwargs(
        request_id=request_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    request_id: str,
    *,
    client: AuthenticatedClient,
    body: AccessGrantInput | Unset = UNSET,
) -> AccessRequest | Problem | None:
    """Grant an access request

     Owner grants access: on this instance a 130 access-list grant (viewer or commenter, TLP checked)
    and, when the requester offered one, the requester's own chronicle shared back with the owner as
    viewer; for a remote requester a 132 signed package (a chronicle extract, or a chronicle-recap
    report drafted and published for the purpose) addressed to the requester's handle on its instance.
    The requester is told the status only.

    **Scope:** `agent`.

    Args:
        request_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AccessGrantInput | Unset): Grant an access request (owner)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AccessRequest | Problem
    """

    return sync_detailed(
        request_id=request_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    request_id: str,
    *,
    client: AuthenticatedClient,
    body: AccessGrantInput | Unset = UNSET,
) -> Response[AccessRequest | Problem]:
    """Grant an access request

     Owner grants access: on this instance a 130 access-list grant (viewer or commenter, TLP checked)
    and, when the requester offered one, the requester's own chronicle shared back with the owner as
    viewer; for a remote requester a 132 signed package (a chronicle extract, or a chronicle-recap
    report drafted and published for the purpose) addressed to the requester's handle on its instance.
    The requester is told the status only.

    **Scope:** `agent`.

    Args:
        request_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AccessGrantInput | Unset): Grant an access request (owner)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AccessRequest | Problem]
    """

    kwargs = _get_kwargs(
        request_id=request_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    request_id: str,
    *,
    client: AuthenticatedClient,
    body: AccessGrantInput | Unset = UNSET,
) -> AccessRequest | Problem | None:
    """Grant an access request

     Owner grants access: on this instance a 130 access-list grant (viewer or commenter, TLP checked)
    and, when the requester offered one, the requester's own chronicle shared back with the owner as
    viewer; for a remote requester a 132 signed package (a chronicle extract, or a chronicle-recap
    report drafted and published for the purpose) addressed to the requester's handle on its instance.
    The requester is told the status only.

    **Scope:** `agent`.

    Args:
        request_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AccessGrantInput | Unset): Grant an access request (owner)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AccessRequest | Problem
    """

    return (
        await asyncio_detailed(
            request_id=request_id,
            client=client,
            body=body,
        )
    ).parsed
