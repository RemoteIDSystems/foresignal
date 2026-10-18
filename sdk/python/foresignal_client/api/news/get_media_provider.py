from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_media_provider_provider_id import GetMediaProviderProviderId
from ...models.media_provider import MediaProvider
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    provider_id: GetMediaProviderProviderId,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/media/providers/{provider_id}".format(
            provider_id=quote(str(provider_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MediaProvider | Problem | None:
    if response.status_code == 200:
        response_200 = MediaProvider.from_dict(response.json())

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
) -> Response[MediaProvider | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    provider_id: GetMediaProviderProviderId,
    *,
    client: AuthenticatedClient,
) -> Response[MediaProvider | Problem]:
    """Get a rating provider

     One rating provider.

    **Scope:** `admin`.

    Args:
        provider_id (GetMediaProviderProviderId):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaProvider | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    provider_id: GetMediaProviderProviderId,
    *,
    client: AuthenticatedClient,
) -> MediaProvider | Problem | None:
    """Get a rating provider

     One rating provider.

    **Scope:** `admin`.

    Args:
        provider_id (GetMediaProviderProviderId):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaProvider | Problem
    """

    return sync_detailed(
        provider_id=provider_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    provider_id: GetMediaProviderProviderId,
    *,
    client: AuthenticatedClient,
) -> Response[MediaProvider | Problem]:
    """Get a rating provider

     One rating provider.

    **Scope:** `admin`.

    Args:
        provider_id (GetMediaProviderProviderId):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaProvider | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    provider_id: GetMediaProviderProviderId,
    *,
    client: AuthenticatedClient,
) -> MediaProvider | Problem | None:
    """Get a rating provider

     One rating provider.

    **Scope:** `admin`.

    Args:
        provider_id (GetMediaProviderProviderId):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaProvider | Problem
    """

    return (
        await asyncio_detailed(
            provider_id=provider_id,
            client=client,
        )
    ).parsed
