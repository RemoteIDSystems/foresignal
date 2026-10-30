from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.translation_settings import TranslationSettings
from ...models.translation_settings_input import TranslationSettingsInput
from ...types import Response


def _get_kwargs(
    *,
    body: TranslationSettingsInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/translation",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TranslationSettings | None:
    if response.status_code == 200:
        response_200 = TranslationSettings.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | TranslationSettings]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: TranslationSettingsInput,
) -> Response[Problem | TranslationSettings]:
    """Set news translation settings

     Replaces the settings. Takes effect for news ingested afterwards; an already-translated article is
    not translated again unless its source text changes.

    **Scope:** `admin`.

    Args:
        body (TranslationSettingsInput): News translation settings (settings key `translation`,
            NP-002). Replaces the settings: an omitted member takes its default; the read-only `spend`
            of a GET may be sent back and is ignored.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TranslationSettings]
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
    body: TranslationSettingsInput,
) -> Problem | TranslationSettings | None:
    """Set news translation settings

     Replaces the settings. Takes effect for news ingested afterwards; an already-translated article is
    not translated again unless its source text changes.

    **Scope:** `admin`.

    Args:
        body (TranslationSettingsInput): News translation settings (settings key `translation`,
            NP-002). Replaces the settings: an omitted member takes its default; the read-only `spend`
            of a GET may be sent back and is ignored.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TranslationSettings
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: TranslationSettingsInput,
) -> Response[Problem | TranslationSettings]:
    """Set news translation settings

     Replaces the settings. Takes effect for news ingested afterwards; an already-translated article is
    not translated again unless its source text changes.

    **Scope:** `admin`.

    Args:
        body (TranslationSettingsInput): News translation settings (settings key `translation`,
            NP-002). Replaces the settings: an omitted member takes its default; the read-only `spend`
            of a GET may be sent back and is ignored.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TranslationSettings]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: TranslationSettingsInput,
) -> Problem | TranslationSettings | None:
    """Set news translation settings

     Replaces the settings. Takes effect for news ingested afterwards; an already-translated article is
    not translated again unless its source text changes.

    **Scope:** `admin`.

    Args:
        body (TranslationSettingsInput): News translation settings (settings key `translation`,
            NP-002). Replaces the settings: an omitted member takes its default; the read-only `spend`
            of a GET may be sent back and is ignored.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TranslationSettings
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
