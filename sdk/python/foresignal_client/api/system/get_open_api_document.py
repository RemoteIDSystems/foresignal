from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_open_api_document_response_200 import GetOpenApiDocumentResponse200
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/openapi.json",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> GetOpenApiDocumentResponse200 | Problem | None:
    if response.status_code == 200:
        response_200 = GetOpenApiDocumentResponse200.from_dict(response.json())

        return response_200

    if 400 <= response.status_code <= 499:
        response_4xx = Problem.from_dict(response.json())

        return response_4xx

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[GetOpenApiDocumentResponse200 | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient | Client,
) -> Response[GetOpenApiDocumentResponse200 | Problem]:
    """This OpenAPI document

     Serves docs/OpenAPI.json byte-identical (copied to the classpath at build time). Swagger UI at /docs
    points here; the code-generated spec used by the contract test is internal.

    **Scope:** `open` (no token).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GetOpenApiDocumentResponse200 | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient | Client,
) -> GetOpenApiDocumentResponse200 | Problem | None:
    """This OpenAPI document

     Serves docs/OpenAPI.json byte-identical (copied to the classpath at build time). Swagger UI at /docs
    points here; the code-generated spec used by the contract test is internal.

    **Scope:** `open` (no token).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GetOpenApiDocumentResponse200 | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient | Client,
) -> Response[GetOpenApiDocumentResponse200 | Problem]:
    """This OpenAPI document

     Serves docs/OpenAPI.json byte-identical (copied to the classpath at build time). Swagger UI at /docs
    points here; the code-generated spec used by the contract test is internal.

    **Scope:** `open` (no token).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[GetOpenApiDocumentResponse200 | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient | Client,
) -> GetOpenApiDocumentResponse200 | Problem | None:
    """This OpenAPI document

     Serves docs/OpenAPI.json byte-identical (copied to the classpath at build time). Swagger UI at /docs
    points here; the code-generated spec used by the contract test is internal.

    **Scope:** `open` (no token).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        GetOpenApiDocumentResponse200 | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
