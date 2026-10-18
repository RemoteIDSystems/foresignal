from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.media_provider import MediaProvider
from ...models.media_provider_input import MediaProviderInput
from ...models.problem import Problem
from ...models.update_media_provider_provider_id import UpdateMediaProviderProviderId
from ...types import Response


def _get_kwargs(
    provider_id: UpdateMediaProviderProviderId,
    *,
    body: MediaProviderInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/media/providers/{provider_id}".format(
            provider_id=quote(str(provider_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> MediaProvider | Problem | None:
    if response.status_code == 200:
        response_200 = MediaProvider.from_dict(response.json())

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
    provider_id: UpdateMediaProviderProviderId,
    *,
    client: AuthenticatedClient,
    body: MediaProviderInput,
) -> Response[MediaProvider | Problem]:
    """Update a rating provider

     Enables or disables a provider, records licence acknowledgement or redistribution permission, or
    stores the MBFC key. Enabling without acknowledgement → 409.

    **Scope:** `admin`.

    Args:
        provider_id (UpdateMediaProviderProviderId):
        body (MediaProviderInput): Update a rating provider (admin); enabling requires licence
            acknowledgement

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaProvider | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    provider_id: UpdateMediaProviderProviderId,
    *,
    client: AuthenticatedClient,
    body: MediaProviderInput,
) -> MediaProvider | Problem | None:
    """Update a rating provider

     Enables or disables a provider, records licence acknowledgement or redistribution permission, or
    stores the MBFC key. Enabling without acknowledgement → 409.

    **Scope:** `admin`.

    Args:
        provider_id (UpdateMediaProviderProviderId):
        body (MediaProviderInput): Update a rating provider (admin); enabling requires licence
            acknowledgement

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        MediaProvider | Problem
    """

    return sync_detailed(
        provider_id=provider_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    provider_id: UpdateMediaProviderProviderId,
    *,
    client: AuthenticatedClient,
    body: MediaProviderInput,
) -> Response[MediaProvider | Problem]:
    """Update a rating provider

     Enables or disables a provider, records licence acknowledgement or redistribution permission, or
    stores the MBFC key. Enabling without acknowledgement → 409.

    **Scope:** `admin`.

    Args:
        provider_id (UpdateMediaProviderProviderId):
        body (MediaProviderInput): Update a rating provider (admin); enabling requires licence
            acknowledgement

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[MediaProvider | Problem]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    provider_id: UpdateMediaProviderProviderId,
    *,
    client: AuthenticatedClient,
    body: MediaProviderInput,
) -> MediaProvider | Problem | None:
    """Update a rating provider

     Enables or disables a provider, records licence acknowledgement or redistribution permission, or
    stores the MBFC key. Enabling without acknowledgement → 409.

    **Scope:** `admin`.

    Args:
        provider_id (UpdateMediaProviderProviderId):
        body (MediaProviderInput): Update a rating provider (admin); enabling requires licence
            acknowledgement

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
            body=body,
        )
    ).parsed
