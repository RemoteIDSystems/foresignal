from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    source_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/ground-stations/{source_id}/polarplot.jpeg".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem]:
    """Get the live pass polar plot

     The latest `/polarplot.jpeg` fetched from SatDump during an active autotrack pass, held in memory
    only and never fetched on client demand. `Cache-Control: private, max-age=5`. 404 NO_ACTIVE_PASS
    when no pass is active.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | None:
    """Get the live pass polar plot

     The latest `/polarplot.jpeg` fetched from SatDump during an active autotrack pass, held in memory
    only and never fetched on client demand. `Cache-Control: private, max-age=5`. 404 NO_ACTIVE_PASS
    when no pass is active.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem]:
    """Get the live pass polar plot

     The latest `/polarplot.jpeg` fetched from SatDump during an active autotrack pass, held in memory
    only and never fetched on client demand. `Cache-Control: private, max-age=5`. 404 NO_ACTIVE_PASS
    when no pass is active.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | None:
    """Get the live pass polar plot

     The latest `/polarplot.jpeg` fetched from SatDump during an active autotrack pass, held in memory
    only and never fetched on client demand. `Cache-Control: private, max-age=5`. 404 NO_ACTIVE_PASS
    when no pass is active.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        source_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
        )
    ).parsed
