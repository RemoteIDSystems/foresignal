from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.import_report import ImportReport
from ...models.problem import Problem
from ...models.settings_bundle import SettingsBundle
from ...types import Response


def _get_kwargs(
    *,
    body: SettingsBundle,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/import",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ImportReport | Problem | None:
    if response.status_code == 200:
        response_200 = ImportReport.from_dict(response.json())

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
) -> Response[ImportReport | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: SettingsBundle,
) -> Response[ImportReport | Problem]:
    """Import settings

     Applies a bundle in one transaction: known sources applied (an enable lacking required credentials
    is applied as disabled with a warning), unknown sources reported, triggers and channels upserted by
    id (channels without secrets keep theirs), retention, CORS, notifications and watchdog applied when
    present. Any validation error → 400 with every error and nothing applied.

    **Scope:** `admin`.

    Args:
        body (SettingsBundle):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ImportReport | Problem]
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
    body: SettingsBundle,
) -> ImportReport | Problem | None:
    """Import settings

     Applies a bundle in one transaction: known sources applied (an enable lacking required credentials
    is applied as disabled with a warning), unknown sources reported, triggers and channels upserted by
    id (channels without secrets keep theirs), retention, CORS, notifications and watchdog applied when
    present. Any validation error → 400 with every error and nothing applied.

    **Scope:** `admin`.

    Args:
        body (SettingsBundle):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ImportReport | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: SettingsBundle,
) -> Response[ImportReport | Problem]:
    """Import settings

     Applies a bundle in one transaction: known sources applied (an enable lacking required credentials
    is applied as disabled with a warning), unknown sources reported, triggers and channels upserted by
    id (channels without secrets keep theirs), retention, CORS, notifications and watchdog applied when
    present. Any validation error → 400 with every error and nothing applied.

    **Scope:** `admin`.

    Args:
        body (SettingsBundle):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ImportReport | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: SettingsBundle,
) -> ImportReport | Problem | None:
    """Import settings

     Applies a bundle in one transaction: known sources applied (an enable lacking required credentials
    is applied as disabled with a warning), unknown sources reported, triggers and channels upserted by
    id (channels without secrets keep theirs), retention, CORS, notifications and watchdog applied when
    present. Any validation error → 400 with every error and nothing applied.

    **Scope:** `admin`.

    Args:
        body (SettingsBundle):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ImportReport | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
