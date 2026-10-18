from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_infra_node_format import GetInfraNodeFormat
from ...models.infra_node import InfraNode
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    node_id: int,
    *,
    format_: GetInfraNodeFormat | Unset = GetInfraNodeFormat.JSON,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/infra/nodes/{node_id}".format(
            node_id=quote(str(node_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> InfraNode | Problem | None:
    if response.status_code == 200:
        response_200 = InfraNode.from_dict(response.json())

        return response_200

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
) -> Response[InfraNode | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    node_id: int,
    *,
    client: AuthenticatedClient,
    format_: GetInfraNodeFormat | Unset = GetInfraNodeFormat.JSON,
) -> Response[InfraNode | Problem]:
    """Get an infrastructure node

     Node with its in and out edges.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        node_id (int):
        format_ (GetInfraNodeFormat | Unset):  Default: GetInfraNodeFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraNode | Problem]
    """

    kwargs = _get_kwargs(
        node_id=node_id,
        format_=format_,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    node_id: int,
    *,
    client: AuthenticatedClient,
    format_: GetInfraNodeFormat | Unset = GetInfraNodeFormat.JSON,
) -> InfraNode | Problem | None:
    """Get an infrastructure node

     Node with its in and out edges.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        node_id (int):
        format_ (GetInfraNodeFormat | Unset):  Default: GetInfraNodeFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraNode | Problem
    """

    return sync_detailed(
        node_id=node_id,
        client=client,
        format_=format_,
    ).parsed


async def asyncio_detailed(
    node_id: int,
    *,
    client: AuthenticatedClient,
    format_: GetInfraNodeFormat | Unset = GetInfraNodeFormat.JSON,
) -> Response[InfraNode | Problem]:
    """Get an infrastructure node

     Node with its in and out edges.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        node_id (int):
        format_ (GetInfraNodeFormat | Unset):  Default: GetInfraNodeFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraNode | Problem]
    """

    kwargs = _get_kwargs(
        node_id=node_id,
        format_=format_,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    node_id: int,
    *,
    client: AuthenticatedClient,
    format_: GetInfraNodeFormat | Unset = GetInfraNodeFormat.JSON,
) -> InfraNode | Problem | None:
    """Get an infrastructure node

     Node with its in and out edges.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        node_id (int):
        format_ (GetInfraNodeFormat | Unset):  Default: GetInfraNodeFormat.JSON.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraNode | Problem
    """

    return (
        await asyncio_detailed(
            node_id=node_id,
            client=client,
            format_=format_,
        )
    ).parsed
