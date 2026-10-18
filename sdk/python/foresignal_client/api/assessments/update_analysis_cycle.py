from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analysis_cycle import AnalysisCycle
from ...models.analysis_cycle_input import AnalysisCycleInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    cycle_id: str,
    *,
    body: AnalysisCycleInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "put",
        "url": "/v1/analysis/cycles/{cycle_id}".format(
            cycle_id=quote(str(cycle_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalysisCycle | Problem | None:
    if response.status_code == 200:
        response_200 = AnalysisCycle.from_dict(response.json())

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
    cycle_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalysisCycleInput,
) -> Response[AnalysisCycle | Problem]:
    """Replace an analysis cycle

     Replaces the cycle definition and recomputes `nextRunAt`.

    **Scope:** `agent`.

    Args:
        cycle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalysisCycleInput): Create or replace an analysis cycle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalysisCycle | Problem]
    """

    kwargs = _get_kwargs(
        cycle_id=cycle_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    cycle_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalysisCycleInput,
) -> AnalysisCycle | Problem | None:
    """Replace an analysis cycle

     Replaces the cycle definition and recomputes `nextRunAt`.

    **Scope:** `agent`.

    Args:
        cycle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalysisCycleInput): Create or replace an analysis cycle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalysisCycle | Problem
    """

    return sync_detailed(
        cycle_id=cycle_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    cycle_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalysisCycleInput,
) -> Response[AnalysisCycle | Problem]:
    """Replace an analysis cycle

     Replaces the cycle definition and recomputes `nextRunAt`.

    **Scope:** `agent`.

    Args:
        cycle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalysisCycleInput): Create or replace an analysis cycle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalysisCycle | Problem]
    """

    kwargs = _get_kwargs(
        cycle_id=cycle_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    cycle_id: str,
    *,
    client: AuthenticatedClient,
    body: AnalysisCycleInput,
) -> AnalysisCycle | Problem | None:
    """Replace an analysis cycle

     Replaces the cycle definition and recomputes `nextRunAt`.

    **Scope:** `agent`.

    Args:
        cycle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (AnalysisCycleInput): Create or replace an analysis cycle

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalysisCycle | Problem
    """

    return (
        await asyncio_detailed(
            cycle_id=cycle_id,
            client=client,
            body=body,
        )
    ).parsed
