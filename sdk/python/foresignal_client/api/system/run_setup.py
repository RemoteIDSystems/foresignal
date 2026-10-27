from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.setup_request import SetupRequest
from ...models.setup_result import SetupResult
from ...types import Response


def _get_kwargs(
    *,
    body: SetupRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/setup",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SetupResult | None:
    if response.status_code == 200:
        response_200 = SetupResult.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | SetupResult]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: SetupRequest,
) -> Response[Problem | SetupResult]:
    """Run first-run setup

     Bootstraps a fresh instance (C4/190, R2-027). Only the bootstrap admin token printed at first start
    may call it (403 for any other token or a session) and only while no user exists (409 ALREADY_SETUP
    after). It creates the first admin, active at once with the built-in Admin role and the given
    password (checked against the password policy); moves what the v1 operator owned through the
    instance `owner` principal (their personal channels and workspaces, and any rule, view or LLM
    provider bound to it) to that admin, leaving every owner-less row (source, collector and server
    settings, global channels, shared LLM providers, existing rules and views) shared; optionally seeds
    SMTP (as `updateSmtpSettings`), enables `sources` in the collection set, and stores `thresholds` and
    `retention` (as their settings operations); and signs the admin in. All of it is one transaction: a
    body refused anywhere changes nothing. The session is gated to MFA enrollment until the admin
    enrolls (R2-024). API tokens are not touched.

    **Scope:** `admin`.

    Args:
        body (SetupRequest): First-run bootstrap: create the first admin and optionally seed SMTP,
            sources and thresholds (C4/190)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SetupResult]
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
    body: SetupRequest,
) -> Problem | SetupResult | None:
    """Run first-run setup

     Bootstraps a fresh instance (C4/190, R2-027). Only the bootstrap admin token printed at first start
    may call it (403 for any other token or a session) and only while no user exists (409 ALREADY_SETUP
    after). It creates the first admin, active at once with the built-in Admin role and the given
    password (checked against the password policy); moves what the v1 operator owned through the
    instance `owner` principal (their personal channels and workspaces, and any rule, view or LLM
    provider bound to it) to that admin, leaving every owner-less row (source, collector and server
    settings, global channels, shared LLM providers, existing rules and views) shared; optionally seeds
    SMTP (as `updateSmtpSettings`), enables `sources` in the collection set, and stores `thresholds` and
    `retention` (as their settings operations); and signs the admin in. All of it is one transaction: a
    body refused anywhere changes nothing. The session is gated to MFA enrollment until the admin
    enrolls (R2-024). API tokens are not touched.

    **Scope:** `admin`.

    Args:
        body (SetupRequest): First-run bootstrap: create the first admin and optionally seed SMTP,
            sources and thresholds (C4/190)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SetupResult
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: SetupRequest,
) -> Response[Problem | SetupResult]:
    """Run first-run setup

     Bootstraps a fresh instance (C4/190, R2-027). Only the bootstrap admin token printed at first start
    may call it (403 for any other token or a session) and only while no user exists (409 ALREADY_SETUP
    after). It creates the first admin, active at once with the built-in Admin role and the given
    password (checked against the password policy); moves what the v1 operator owned through the
    instance `owner` principal (their personal channels and workspaces, and any rule, view or LLM
    provider bound to it) to that admin, leaving every owner-less row (source, collector and server
    settings, global channels, shared LLM providers, existing rules and views) shared; optionally seeds
    SMTP (as `updateSmtpSettings`), enables `sources` in the collection set, and stores `thresholds` and
    `retention` (as their settings operations); and signs the admin in. All of it is one transaction: a
    body refused anywhere changes nothing. The session is gated to MFA enrollment until the admin
    enrolls (R2-024). API tokens are not touched.

    **Scope:** `admin`.

    Args:
        body (SetupRequest): First-run bootstrap: create the first admin and optionally seed SMTP,
            sources and thresholds (C4/190)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SetupResult]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: SetupRequest,
) -> Problem | SetupResult | None:
    """Run first-run setup

     Bootstraps a fresh instance (C4/190, R2-027). Only the bootstrap admin token printed at first start
    may call it (403 for any other token or a session) and only while no user exists (409 ALREADY_SETUP
    after). It creates the first admin, active at once with the built-in Admin role and the given
    password (checked against the password policy); moves what the v1 operator owned through the
    instance `owner` principal (their personal channels and workspaces, and any rule, view or LLM
    provider bound to it) to that admin, leaving every owner-less row (source, collector and server
    settings, global channels, shared LLM providers, existing rules and views) shared; optionally seeds
    SMTP (as `updateSmtpSettings`), enables `sources` in the collection set, and stores `thresholds` and
    `retention` (as their settings operations); and signs the admin in. All of it is one transaction: a
    body refused anywhere changes nothing. The session is gated to MFA enrollment until the admin
    enrolls (R2-024). API tokens are not touched.

    **Scope:** `admin`.

    Args:
        body (SetupRequest): First-run bootstrap: create the first admin and optionally seed SMTP,
            sources and thresholds (C4/190)

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SetupResult
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
