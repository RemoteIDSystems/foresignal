from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.principal import Principal
from ...models.principal_patch import PrincipalPatch
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    principal_id: str,
    *,
    body: PrincipalPatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/principals/{principal_id}".format(
            principal_id=quote(str(principal_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Principal | Problem | None:
    if response.status_code == 200:
        response_200 = Principal.from_dict(response.json())

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
) -> Response[Principal | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    principal_id: str,
    *,
    client: AuthenticatedClient,
    body: PrincipalPatch,
) -> Response[Principal | Problem]:
    """Update a principal

     JSON merge patch. Disabling a principal stops its tokens. Audited.

    **Scope:** `admin`.

    Args:
        principal_id (str):
        body (PrincipalPatch): JSON merge patch for a principal (admin); the handle is immutable

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Principal | Problem]
    """

    kwargs = _get_kwargs(
        principal_id=principal_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    principal_id: str,
    *,
    client: AuthenticatedClient,
    body: PrincipalPatch,
) -> Principal | Problem | None:
    """Update a principal

     JSON merge patch. Disabling a principal stops its tokens. Audited.

    **Scope:** `admin`.

    Args:
        principal_id (str):
        body (PrincipalPatch): JSON merge patch for a principal (admin); the handle is immutable

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Principal | Problem
    """

    return sync_detailed(
        principal_id=principal_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    principal_id: str,
    *,
    client: AuthenticatedClient,
    body: PrincipalPatch,
) -> Response[Principal | Problem]:
    """Update a principal

     JSON merge patch. Disabling a principal stops its tokens. Audited.

    **Scope:** `admin`.

    Args:
        principal_id (str):
        body (PrincipalPatch): JSON merge patch for a principal (admin); the handle is immutable

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Principal | Problem]
    """

    kwargs = _get_kwargs(
        principal_id=principal_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    principal_id: str,
    *,
    client: AuthenticatedClient,
    body: PrincipalPatch,
) -> Principal | Problem | None:
    """Update a principal

     JSON merge patch. Disabling a principal stops its tokens. Audited.

    **Scope:** `admin`.

    Args:
        principal_id (str):
        body (PrincipalPatch): JSON merge patch for a principal (admin); the handle is immutable

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Principal | Problem
    """

    return (
        await asyncio_detailed(
            principal_id=principal_id,
            client=client,
            body=body,
        )
    ).parsed
