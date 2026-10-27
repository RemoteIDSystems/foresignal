from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.first_run_status import FirstRunStatus
from ...models.problem import Problem
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/system/first-run",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> FirstRunStatus | Problem | None:
    if response.status_code == 200:
        response_200 = FirstRunStatus.from_dict(response.json())

        return response_200

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[FirstRunStatus | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[FirstRunStatus | Problem]:
    """Check first-run status

     Reports whether the instance still needs first-run setup: `firstRun` while no user exists (C4/190,
    R2-027). Open, so the admin app can start the setup wizard before any account exists.
    `smtpConfigured` is true when account email is enabled with a host; `sourcesEnabled` counts the
    sources in the collection set; `apiVersion` is `info.version`.

    **Scope:** `open`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FirstRunStatus | Problem]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> FirstRunStatus | Problem | None:
    """Check first-run status

     Reports whether the instance still needs first-run setup: `firstRun` while no user exists (C4/190,
    R2-027). Open, so the admin app can start the setup wizard before any account exists.
    `smtpConfigured` is true when account email is enabled with a host; `sourcesEnabled` counts the
    sources in the collection set; `apiVersion` is `info.version`.

    **Scope:** `open`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FirstRunStatus | Problem
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[FirstRunStatus | Problem]:
    """Check first-run status

     Reports whether the instance still needs first-run setup: `firstRun` while no user exists (C4/190,
    R2-027). Open, so the admin app can start the setup wizard before any account exists.
    `smtpConfigured` is true when account email is enabled with a host; `sourcesEnabled` counts the
    sources in the collection set; `apiVersion` is `info.version`.

    **Scope:** `open`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FirstRunStatus | Problem]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> FirstRunStatus | Problem | None:
    """Check first-run status

     Reports whether the instance still needs first-run setup: `firstRun` while no user exists (C4/190,
    R2-027). Open, so the admin app can start the setup wizard before any account exists.
    `smtpConfigured` is true when account email is enabled with a host; `sourcesEnabled` counts the
    sources in the collection set; `apiVersion` is `info.version`.

    **Scope:** `open`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FirstRunStatus | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
