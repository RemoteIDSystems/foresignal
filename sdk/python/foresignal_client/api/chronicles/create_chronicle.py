from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle import Chronicle
from ...models.chronicle_input import ChronicleInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ChronicleInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/chronicles",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Chronicle | Problem | None:
    if response.status_code == 201:
        response_201 = Chronicle.from_dict(response.json())

        return response_201

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
) -> Response[Chronicle | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ChronicleInput,
) -> Response[Chronicle | Problem]:
    """Create a chronicle

     Creates a chronicle owned by the caller, with its 129 workspace. From a workspace
    (`fromWorkspaceId`), its layers, area and filters become chronicle layers. Capture starts only on
    activation (or with `activate=true`). Inherits the strictest TLP of linked inputs.

    **Scope:** `agent`.

    Args:
        body (ChronicleInput): Create a chronicle; it starts in status `proposed` (no capture)
            until activated

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Chronicle | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: ChronicleInput,
) -> Chronicle | Problem | None:
    """Create a chronicle

     Creates a chronicle owned by the caller, with its 129 workspace. From a workspace
    (`fromWorkspaceId`), its layers, area and filters become chronicle layers. Capture starts only on
    activation (or with `activate=true`). Inherits the strictest TLP of linked inputs.

    **Scope:** `agent`.

    Args:
        body (ChronicleInput): Create a chronicle; it starts in status `proposed` (no capture)
            until activated

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Chronicle | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ChronicleInput,
) -> Response[Chronicle | Problem]:
    """Create a chronicle

     Creates a chronicle owned by the caller, with its 129 workspace. From a workspace
    (`fromWorkspaceId`), its layers, area and filters become chronicle layers. Capture starts only on
    activation (or with `activate=true`). Inherits the strictest TLP of linked inputs.

    **Scope:** `agent`.

    Args:
        body (ChronicleInput): Create a chronicle; it starts in status `proposed` (no capture)
            until activated

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Chronicle | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ChronicleInput,
) -> Chronicle | Problem | None:
    """Create a chronicle

     Creates a chronicle owned by the caller, with its 129 workspace. From a workspace
    (`fromWorkspaceId`), its layers, area and filters become chronicle layers. Capture starts only on
    activation (or with `activate=true`). Inherits the strictest TLP of linked inputs.

    **Scope:** `agent`.

    Args:
        body (ChronicleInput): Create a chronicle; it starts in status `proposed` (no capture)
            until activated

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Chronicle | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
