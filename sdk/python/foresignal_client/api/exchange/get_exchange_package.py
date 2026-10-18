from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.exchange_package import ExchangePackage
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    package_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/exchange/packages/{package_id}".format(
            package_id=quote(str(package_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ExchangePackage | Problem | None:
    if response.status_code == 200:
        response_200 = ExchangePackage.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ExchangePackage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    package_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExchangePackage | Problem]:
    """Get a sent package

     Build and delivery state of a package created here.

    **Scope:** `agent`.

    Args:
        package_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExchangePackage | Problem]
    """

    kwargs = _get_kwargs(
        package_id=package_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    package_id: str,
    *,
    client: AuthenticatedClient,
) -> ExchangePackage | Problem | None:
    """Get a sent package

     Build and delivery state of a package created here.

    **Scope:** `agent`.

    Args:
        package_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExchangePackage | Problem
    """

    return sync_detailed(
        package_id=package_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    package_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ExchangePackage | Problem]:
    """Get a sent package

     Build and delivery state of a package created here.

    **Scope:** `agent`.

    Args:
        package_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ExchangePackage | Problem]
    """

    kwargs = _get_kwargs(
        package_id=package_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    package_id: str,
    *,
    client: AuthenticatedClient,
) -> ExchangePackage | Problem | None:
    """Get a sent package

     Build and delivery state of a package created here.

    **Scope:** `agent`.

    Args:
        package_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ExchangePackage | Problem
    """

    return (
        await asyncio_detailed(
            package_id=package_id,
            client=client,
        )
    ).parsed
