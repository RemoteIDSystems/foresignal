from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analysis_cycle import AnalysisCycle
from ...models.analysis_cycle_input import AnalysisCycleInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: AnalysisCycleInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/analysis/cycles",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalysisCycle | Problem | None:
    if response.status_code == 201:
        response_201 = AnalysisCycle.from_dict(response.json())

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

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AnalysisCycle | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: AnalysisCycleInput,
) -> Response[AnalysisCycle | Problem]:
    """Create an analysis cycle

     Creates a cycle; scheduling follows the 029/102 pattern (persisted `nextRunAt`, missed runs run
    once).

    **Scope:** `agent`.

    Args:
        body (AnalysisCycleInput): Create or replace an analysis cycle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalysisCycle | Problem]
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
    body: AnalysisCycleInput,
) -> AnalysisCycle | Problem | None:
    """Create an analysis cycle

     Creates a cycle; scheduling follows the 029/102 pattern (persisted `nextRunAt`, missed runs run
    once).

    **Scope:** `agent`.

    Args:
        body (AnalysisCycleInput): Create or replace an analysis cycle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalysisCycle | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: AnalysisCycleInput,
) -> Response[AnalysisCycle | Problem]:
    """Create an analysis cycle

     Creates a cycle; scheduling follows the 029/102 pattern (persisted `nextRunAt`, missed runs run
    once).

    **Scope:** `agent`.

    Args:
        body (AnalysisCycleInput): Create or replace an analysis cycle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalysisCycle | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: AnalysisCycleInput,
) -> AnalysisCycle | Problem | None:
    """Create an analysis cycle

     Creates a cycle; scheduling follows the 029/102 pattern (persisted `nextRunAt`, missed runs run
    once).

    **Scope:** `agent`.

    Args:
        body (AnalysisCycleInput): Create or replace an analysis cycle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalysisCycle | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
