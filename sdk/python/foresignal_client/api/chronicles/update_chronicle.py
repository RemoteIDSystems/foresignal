from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle import Chronicle
from ...models.chronicle_patch import ChroniclePatch
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    chronicle_id: str,
    *,
    body: ChroniclePatch,
    if_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_match, Unset):
        headers["If-Match"] = if_match

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/chronicles/{chronicle_id}".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Chronicle | Problem | None:
    if response.status_code == 200:
        response_200 = Chronicle.from_dict(response.json())

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

    if response.status_code == 412:
        response_412 = Problem.from_dict(response.json())

        return response_412

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
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: ChroniclePatch,
    if_match: str | Unset = UNSET,
) -> Response[Chronicle | Problem]:
    """Update a chronicle

     JSON merge patch (owner or editor). A stricter TLP lowers discoverability automatically (134).

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        if_match (str | Unset):
        body (ChroniclePatch): JSON merge patch for a chronicle (owner or editor)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Chronicle | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        body=body,
        if_match=if_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: ChroniclePatch,
    if_match: str | Unset = UNSET,
) -> Chronicle | Problem | None:
    """Update a chronicle

     JSON merge patch (owner or editor). A stricter TLP lowers discoverability automatically (134).

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        if_match (str | Unset):
        body (ChroniclePatch): JSON merge patch for a chronicle (owner or editor)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Chronicle | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
        body=body,
        if_match=if_match,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: ChroniclePatch,
    if_match: str | Unset = UNSET,
) -> Response[Chronicle | Problem]:
    """Update a chronicle

     JSON merge patch (owner or editor). A stricter TLP lowers discoverability automatically (134).

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        if_match (str | Unset):
        body (ChroniclePatch): JSON merge patch for a chronicle (owner or editor)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Chronicle | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        body=body,
        if_match=if_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: ChroniclePatch,
    if_match: str | Unset = UNSET,
) -> Chronicle | Problem | None:
    """Update a chronicle

     JSON merge patch (owner or editor). A stricter TLP lowers discoverability automatically (134).

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        if_match (str | Unset):
        body (ChroniclePatch): JSON merge patch for a chronicle (owner or editor)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Chronicle | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
            body=body,
            if_match=if_match,
        )
    ).parsed
