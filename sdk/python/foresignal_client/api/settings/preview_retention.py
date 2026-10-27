from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.retention_preview import RetentionPreview
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/settings/retention/preview",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RetentionPreview | None:
    if response.status_code == 200:
        response_200 = RetentionPreview.from_dict(response.json())

        return response_200

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
) -> Response[Problem | RetentionPreview]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | RetentionPreview]:
    """Preview what retention would prune

     Dry run of the current retention policy: what the next run would prune per subsystem (records per
    kind, TRACK leaves, frames, media, stopped chronicles), with a size estimate and nothing deleted
    (B2/178). It uses the settings the next run applies, including a window the database-size cap has
    stepped down to; with `capHistory` off it shows today's per-subsystem windows. Frames pinned by a
    chronicle are not counted; quota eviction is not previewed. Requires `storage.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RetentionPreview]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | RetentionPreview | None:
    """Preview what retention would prune

     Dry run of the current retention policy: what the next run would prune per subsystem (records per
    kind, TRACK leaves, frames, media, stopped chronicles), with a size estimate and nothing deleted
    (B2/178). It uses the settings the next run applies, including a window the database-size cap has
    stepped down to; with `capHistory` off it shows today's per-subsystem windows. Frames pinned by a
    chronicle are not counted; quota eviction is not previewed. Requires `storage.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RetentionPreview
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | RetentionPreview]:
    """Preview what retention would prune

     Dry run of the current retention policy: what the next run would prune per subsystem (records per
    kind, TRACK leaves, frames, media, stopped chronicles), with a size estimate and nothing deleted
    (B2/178). It uses the settings the next run applies, including a window the database-size cap has
    stepped down to; with `capHistory` off it shows today's per-subsystem windows. Frames pinned by a
    chronicle are not counted; quota eviction is not previewed. Requires `storage.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RetentionPreview]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | RetentionPreview | None:
    """Preview what retention would prune

     Dry run of the current retention policy: what the next run would prune per subsystem (records per
    kind, TRACK leaves, frames, media, stopped chronicles), with a size estimate and nothing deleted
    (B2/178). It uses the settings the next run applies, including a window the database-size cap has
    stepped down to; with `capHistory` off it shows today's per-subsystem windows. Frames pinned by a
    chronicle are not counted; quota eviction is not previewed. Requires `storage.manage`.

    **Scope:** `admin`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RetentionPreview
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
