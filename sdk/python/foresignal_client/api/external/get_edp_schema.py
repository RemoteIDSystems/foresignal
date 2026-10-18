from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_edp_schema_name import GetEdpSchemaName
from ...models.get_edp_schema_response_200 import GetEdpSchemaResponse200
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    name: GetEdpSchemaName,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/external/protocol/schemas/{name}".format(
            name=quote(str(name), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> GetEdpSchemaResponse200 | Problem | None:
    if response.status_code == 200:
        response_200 = GetEdpSchemaResponse200.from_dict(response.json())

        return response_200

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[GetEdpSchemaResponse200 | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    name: GetEdpSchemaName,
    *,
    client: AuthenticatedClient,
) -> Response[GetEdpSchemaResponse200 | Problem]:
    """Get an EDP JSON Schema

     A JSON Schema 2020-12 document exported from this specification's `Edp*` components.

    **Scope:** `open` (no token).

    Args:
        name (GetEdpSchemaName):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GetEdpSchemaResponse200 | Problem]
    """

    kwargs = _get_kwargs(
        name=name,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    name: GetEdpSchemaName,
    *,
    client: AuthenticatedClient,
) -> GetEdpSchemaResponse200 | Problem | None:
    """Get an EDP JSON Schema

     A JSON Schema 2020-12 document exported from this specification's `Edp*` components.

    **Scope:** `open` (no token).

    Args:
        name (GetEdpSchemaName):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GetEdpSchemaResponse200 | Problem
    """

    return sync_detailed(
        name=name,
        client=client,
    ).parsed


async def asyncio_detailed(
    name: GetEdpSchemaName,
    *,
    client: AuthenticatedClient,
) -> Response[GetEdpSchemaResponse200 | Problem]:
    """Get an EDP JSON Schema

     A JSON Schema 2020-12 document exported from this specification's `Edp*` components.

    **Scope:** `open` (no token).

    Args:
        name (GetEdpSchemaName):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GetEdpSchemaResponse200 | Problem]
    """

    kwargs = _get_kwargs(
        name=name,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    name: GetEdpSchemaName,
    *,
    client: AuthenticatedClient,
) -> GetEdpSchemaResponse200 | Problem | None:
    """Get an EDP JSON Schema

     A JSON Schema 2020-12 document exported from this specification's `Edp*` components.

    **Scope:** `open` (no token).

    Args:
        name (GetEdpSchemaName):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GetEdpSchemaResponse200 | Problem
    """

    return (
        await asyncio_detailed(
            name=name,
            client=client,
        )
    ).parsed
