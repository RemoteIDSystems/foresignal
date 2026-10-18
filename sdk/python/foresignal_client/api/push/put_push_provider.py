from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.push_provider import PushProvider
from ...models.push_provider_input import PushProviderInput
from ...types import Response


def _get_kwargs(
    provider_id: str,
    *,
    body: PushProviderInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/push/providers/{provider_id}".format(
            provider_id=quote(str(provider_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | PushProvider | None:
    if response.status_code == 200:
        response_200 = PushProvider.from_dict(response.json())

        return response_200

    if response.status_code == 201:
        response_201 = PushProvider.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | PushProvider]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    provider_id: str,
    *,
    client: AuthenticatedClient,
    body: PushProviderInput,
) -> Response[Problem | PushProvider]:
    """Create or replace a push provider

     Full replace; omitted `secrets` keep the stored ones (a new provider, or a change of `type`, needs
    them). Validates that the APNs `.p8` parses as an EC P-256 PKCS#8 key and the FCM service-account
    JSON has an RSA `private_key`; the JSON's `project_id`, `client_email` and `token_uri` fill config
    members left out. Only the PEM key is stored, encrypted. Output-only members of a `PushProvider`
    (`id`, `secretFields`, `lastTestAt`, `lastError`, `updatedAt`) are accepted and ignored, so a listed
    provider can be sent back.

    **Scope:** `admin`.

    Args:
        provider_id (str):
        body (PushProviderInput): Create or replace a native push provider (032); omitted
            `secrets` keep the stored ones

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushProvider]
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
    provider_id: str,
    *,
    client: AuthenticatedClient,
    body: PushProviderInput,
) -> Problem | PushProvider | None:
    """Create or replace a push provider

     Full replace; omitted `secrets` keep the stored ones (a new provider, or a change of `type`, needs
    them). Validates that the APNs `.p8` parses as an EC P-256 PKCS#8 key and the FCM service-account
    JSON has an RSA `private_key`; the JSON's `project_id`, `client_email` and `token_uri` fill config
    members left out. Only the PEM key is stored, encrypted. Output-only members of a `PushProvider`
    (`id`, `secretFields`, `lastTestAt`, `lastError`, `updatedAt`) are accepted and ignored, so a listed
    provider can be sent back.

    **Scope:** `admin`.

    Args:
        provider_id (str):
        body (PushProviderInput): Create or replace a native push provider (032); omitted
            `secrets` keep the stored ones

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushProvider
    """

    return sync_detailed(
        provider_id=provider_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    provider_id: str,
    *,
    client: AuthenticatedClient,
    body: PushProviderInput,
) -> Response[Problem | PushProvider]:
    """Create or replace a push provider

     Full replace; omitted `secrets` keep the stored ones (a new provider, or a change of `type`, needs
    them). Validates that the APNs `.p8` parses as an EC P-256 PKCS#8 key and the FCM service-account
    JSON has an RSA `private_key`; the JSON's `project_id`, `client_email` and `token_uri` fill config
    members left out. Only the PEM key is stored, encrypted. Output-only members of a `PushProvider`
    (`id`, `secretFields`, `lastTestAt`, `lastError`, `updatedAt`) are accepted and ignored, so a listed
    provider can be sent back.

    **Scope:** `admin`.

    Args:
        provider_id (str):
        body (PushProviderInput): Create or replace a native push provider (032); omitted
            `secrets` keep the stored ones

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | PushProvider]
    """

    kwargs = _get_kwargs(
        provider_id=provider_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    provider_id: str,
    *,
    client: AuthenticatedClient,
    body: PushProviderInput,
) -> Problem | PushProvider | None:
    """Create or replace a push provider

     Full replace; omitted `secrets` keep the stored ones (a new provider, or a change of `type`, needs
    them). Validates that the APNs `.p8` parses as an EC P-256 PKCS#8 key and the FCM service-account
    JSON has an RSA `private_key`; the JSON's `project_id`, `client_email` and `token_uri` fill config
    members left out. Only the PEM key is stored, encrypted. Output-only members of a `PushProvider`
    (`id`, `secretFields`, `lastTestAt`, `lastError`, `updatedAt`) are accepted and ignored, so a listed
    provider can be sent back.

    **Scope:** `admin`.

    Args:
        provider_id (str):
        body (PushProviderInput): Create or replace a native push provider (032); omitted
            `secrets` keep the stored ones

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | PushProvider
    """

    return (
        await asyncio_detailed(
            provider_id=provider_id,
            client=client,
            body=body,
        )
    ).parsed
