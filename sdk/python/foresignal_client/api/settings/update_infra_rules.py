from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.infra_rules import InfraRules
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: InfraRules,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/settings/infra-rules",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> InfraRules | Problem | None:
    if response.status_code == 200:
        response_200 = InfraRules.from_dict(response.json())

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
) -> Response[InfraRules | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: InfraRules,
) -> Response[InfraRules | Problem]:
    """Update infrastructure dependency rules

     Sets enabled flags and parameter overrides; takes effect on the next build.

    **Scope:** `admin`.

    Args:
        body (InfraRules): Dependency rules (versioned resource) with operator overrides stored in
            `app_setting['infra.rules']`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraRules | Problem]
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
    body: InfraRules,
) -> InfraRules | Problem | None:
    """Update infrastructure dependency rules

     Sets enabled flags and parameter overrides; takes effect on the next build.

    **Scope:** `admin`.

    Args:
        body (InfraRules): Dependency rules (versioned resource) with operator overrides stored in
            `app_setting['infra.rules']`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraRules | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: InfraRules,
) -> Response[InfraRules | Problem]:
    """Update infrastructure dependency rules

     Sets enabled flags and parameter overrides; takes effect on the next build.

    **Scope:** `admin`.

    Args:
        body (InfraRules): Dependency rules (versioned resource) with operator overrides stored in
            `app_setting['infra.rules']`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[InfraRules | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: InfraRules,
) -> InfraRules | Problem | None:
    """Update infrastructure dependency rules

     Sets enabled flags and parameter overrides; takes effect on the next build.

    **Scope:** `admin`.

    Args:
        body (InfraRules): Dependency rules (versioned resource) with operator overrides stored in
            `app_setting['infra.rules']`

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        InfraRules | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
