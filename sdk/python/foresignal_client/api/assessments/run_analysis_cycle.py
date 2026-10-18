from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.cycle_run import CycleRun
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    cycle_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/analysis/cycles/{cycle_id}/run".format(
            cycle_id=quote(str(cycle_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> CycleRun | Problem | None:
    if response.status_code == 202:
        response_202 = CycleRun.from_dict(response.json())

        return response_202

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[CycleRun | Problem]:
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
) -> Response[CycleRun | Problem]:
    """Run an analysis cycle now

     Builds the packet, runs the configured LLM roles within budget and always publishes an assessment
    (digest-only without an LLM). 409 `CYCLE_RUN_IN_PROGRESS`; 409 `LLM_NOT_CONFIGURED` only when roles
    require it.

    **Scope:** `agent`.

    Args:
        cycle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CycleRun | Problem]
    """

    kwargs = _get_kwargs(
        cycle_id=cycle_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    cycle_id: str,
    *,
    client: AuthenticatedClient,
) -> CycleRun | Problem | None:
    """Run an analysis cycle now

     Builds the packet, runs the configured LLM roles within budget and always publishes an assessment
    (digest-only without an LLM). 409 `CYCLE_RUN_IN_PROGRESS`; 409 `LLM_NOT_CONFIGURED` only when roles
    require it.

    **Scope:** `agent`.

    Args:
        cycle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CycleRun | Problem
    """

    return sync_detailed(
        cycle_id=cycle_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    cycle_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[CycleRun | Problem]:
    """Run an analysis cycle now

     Builds the packet, runs the configured LLM roles within budget and always publishes an assessment
    (digest-only without an LLM). 409 `CYCLE_RUN_IN_PROGRESS`; 409 `LLM_NOT_CONFIGURED` only when roles
    require it.

    **Scope:** `agent`.

    Args:
        cycle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[CycleRun | Problem]
    """

    kwargs = _get_kwargs(
        cycle_id=cycle_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    cycle_id: str,
    *,
    client: AuthenticatedClient,
) -> CycleRun | Problem | None:
    """Run an analysis cycle now

     Builds the packet, runs the configured LLM roles within budget and always publishes an assessment
    (digest-only without an LLM). 409 `CYCLE_RUN_IN_PROGRESS`; 409 `LLM_NOT_CONFIGURED` only when roles
    require it.

    **Scope:** `agent`.

    Args:
        cycle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        CycleRun | Problem
    """

    return (
        await asyncio_detailed(
            cycle_id=cycle_id,
            client=client,
        )
    ).parsed
