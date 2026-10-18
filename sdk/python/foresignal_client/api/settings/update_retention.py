from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.retention_settings import RetentionSettings
from ...types import Response


def _get_kwargs(
    *,
    body: RetentionSettings,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/retention",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RetentionSettings | None:
    if response.status_code == 200:
        response_200 = RetentionSettings.from_dict(response.json())

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
) -> Response[Problem | RetentionSettings]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: RetentionSettings,
) -> Response[Problem | RetentionSettings]:
    """Set retention settings

     Replaces retention settings (sources are written to source_settings in the same transaction). Every
    duration ≥ 1h; unknown sources and kinds are listed in one 400; a `sources` entry for a TRACK
    source, or a `kinds` entry only TRACK sources emit, → 400 RETENTION_TIER_FIXED. Takes effect on the
    next hourly run or via /run.

    **Scope:** `admin`.

    Args:
        body (RetentionSettings): Retention windows (Core-Data-Model §13). PUT replaces the whole
            object; every duration ≥ 1h. STANDARD windows resolve in order: kinds.{kind}, sources.{id}
            (a view over source_settings.retention), the descriptor's kindRetention, its
            defaultRetention, then default; TRACK always uses trackWindow. Members owned by later
            prompts (see x-foresignal-property-prompts) are accepted and not stored until those
            prompts. Example: {'default': '7d', 'alerts': '90d', 'usage': '30d', 'sources': {'usgs-
            earthquakes': '3650d'}}.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RetentionSettings]
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
    body: RetentionSettings,
) -> Problem | RetentionSettings | None:
    """Set retention settings

     Replaces retention settings (sources are written to source_settings in the same transaction). Every
    duration ≥ 1h; unknown sources and kinds are listed in one 400; a `sources` entry for a TRACK
    source, or a `kinds` entry only TRACK sources emit, → 400 RETENTION_TIER_FIXED. Takes effect on the
    next hourly run or via /run.

    **Scope:** `admin`.

    Args:
        body (RetentionSettings): Retention windows (Core-Data-Model §13). PUT replaces the whole
            object; every duration ≥ 1h. STANDARD windows resolve in order: kinds.{kind}, sources.{id}
            (a view over source_settings.retention), the descriptor's kindRetention, its
            defaultRetention, then default; TRACK always uses trackWindow. Members owned by later
            prompts (see x-foresignal-property-prompts) are accepted and not stored until those
            prompts. Example: {'default': '7d', 'alerts': '90d', 'usage': '30d', 'sources': {'usgs-
            earthquakes': '3650d'}}.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RetentionSettings
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: RetentionSettings,
) -> Response[Problem | RetentionSettings]:
    """Set retention settings

     Replaces retention settings (sources are written to source_settings in the same transaction). Every
    duration ≥ 1h; unknown sources and kinds are listed in one 400; a `sources` entry for a TRACK
    source, or a `kinds` entry only TRACK sources emit, → 400 RETENTION_TIER_FIXED. Takes effect on the
    next hourly run or via /run.

    **Scope:** `admin`.

    Args:
        body (RetentionSettings): Retention windows (Core-Data-Model §13). PUT replaces the whole
            object; every duration ≥ 1h. STANDARD windows resolve in order: kinds.{kind}, sources.{id}
            (a view over source_settings.retention), the descriptor's kindRetention, its
            defaultRetention, then default; TRACK always uses trackWindow. Members owned by later
            prompts (see x-foresignal-property-prompts) are accepted and not stored until those
            prompts. Example: {'default': '7d', 'alerts': '90d', 'usage': '30d', 'sources': {'usgs-
            earthquakes': '3650d'}}.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RetentionSettings]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: RetentionSettings,
) -> Problem | RetentionSettings | None:
    """Set retention settings

     Replaces retention settings (sources are written to source_settings in the same transaction). Every
    duration ≥ 1h; unknown sources and kinds are listed in one 400; a `sources` entry for a TRACK
    source, or a `kinds` entry only TRACK sources emit, → 400 RETENTION_TIER_FIXED. Takes effect on the
    next hourly run or via /run.

    **Scope:** `admin`.

    Args:
        body (RetentionSettings): Retention windows (Core-Data-Model §13). PUT replaces the whole
            object; every duration ≥ 1h. STANDARD windows resolve in order: kinds.{kind}, sources.{id}
            (a view over source_settings.retention), the descriptor's kindRetention, its
            defaultRetention, then default; TRACK always uses trackWindow. Members owned by later
            prompts (see x-foresignal-property-prompts) are accepted and not stored until those
            prompts. Example: {'default': '7d', 'alerts': '90d', 'usage': '30d', 'sources': {'usgs-
            earthquakes': '3650d'}}.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RetentionSettings
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
