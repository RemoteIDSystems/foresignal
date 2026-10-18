from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.chronicle_layer import ChronicleLayer
from ...models.chronicle_layer_input import ChronicleLayerInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    chronicle_id: str,
    *,
    body: ChronicleLayerInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/chronicles/{chronicle_id}/layers".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ChronicleLayer | Problem | None:
    if response.status_code == 201:
        response_201 = ChronicleLayer.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ChronicleLayer | Problem]:
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
    body: ChronicleLayerInput,
) -> Response[ChronicleLayer | Problem]:
    """Add a chronicle layer

     Adds a layer (owner or editor); capture for it starts from the edit's seq onward.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        body (ChronicleLayerInput): Add a layer to a chronicle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleLayer | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: ChronicleLayerInput,
) -> ChronicleLayer | Problem | None:
    """Add a chronicle layer

     Adds a layer (owner or editor); capture for it starts from the edit's seq onward.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        body (ChronicleLayerInput): Add a layer to a chronicle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleLayer | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: ChronicleLayerInput,
) -> Response[ChronicleLayer | Problem]:
    """Add a chronicle layer

     Adds a layer (owner or editor); capture for it starts from the edit's seq onward.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        body (ChronicleLayerInput): Add a layer to a chronicle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ChronicleLayer | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    *,
    client: AuthenticatedClient,
    body: ChronicleLayerInput,
) -> ChronicleLayer | Problem | None:
    """Add a chronicle layer

     Adds a layer (owner or editor); capture for it starts from the edit's seq onward.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        body (ChronicleLayerInput): Add a layer to a chronicle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ChronicleLayer | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            client=client,
            body=body,
        )
    ).parsed
