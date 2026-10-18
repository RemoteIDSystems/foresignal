from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.import_exchange_package_body import ImportExchangePackageBody
from ...models.problem import Problem
from ...models.received_package import ReceivedPackage
from ...types import Response


def _get_kwargs(
    *,
    body: ImportExchangePackageBody,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/exchange/import",
    }

    _kwargs["files"] = body.to_multipart()

    headers["Content-Type"] = "multipart/form-data; boundary=+++"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ReceivedPackage | None:
    if response.status_code == 201:
        response_201 = ReceivedPackage.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 413:
        response_413 = Problem.from_dict(response.json())

        return response_413

    if response.status_code == 422:
        response_422 = Problem.from_dict(response.json())

        return response_422

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ReceivedPackage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ImportExchangePackageBody,
) -> Response[Problem | ReceivedPackage]:
    """Import a package file

     Imports an uploaded package with the same verification as the peer inbox. `jwks` pins the sender's
    public keys when the sender is not a configured peer.

    **Scope:** `admin`.

    Args:
        body (ImportExchangePackageBody): Import form

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReceivedPackage]
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
    body: ImportExchangePackageBody,
) -> Problem | ReceivedPackage | None:
    """Import a package file

     Imports an uploaded package with the same verification as the peer inbox. `jwks` pins the sender's
    public keys when the sender is not a configured peer.

    **Scope:** `admin`.

    Args:
        body (ImportExchangePackageBody): Import form

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReceivedPackage
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ImportExchangePackageBody,
) -> Response[Problem | ReceivedPackage]:
    """Import a package file

     Imports an uploaded package with the same verification as the peer inbox. `jwks` pins the sender's
    public keys when the sender is not a configured peer.

    **Scope:** `admin`.

    Args:
        body (ImportExchangePackageBody): Import form

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReceivedPackage]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ImportExchangePackageBody,
) -> Problem | ReceivedPackage | None:
    """Import a package file

     Imports an uploaded package with the same verification as the peer inbox. `jwks` pins the sender's
    public keys when the sender is not a configured peer.

    **Scope:** `admin`.

    Args:
        body (ImportExchangePackageBody): Import form

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReceivedPackage
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
