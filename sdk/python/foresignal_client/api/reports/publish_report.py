from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.report_revision import ReportRevision
from ...types import Response


def _get_kwargs(
    report_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/reports/{report_id}/publish".format(
            report_id=quote(str(report_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | ReportRevision | None:
    if response.status_code == 201:
        response_201 = ReportRevision.from_dict(response.json())

        return response_201

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | ReportRevision]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    report_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ReportRevision]:
    """Publish a report revision

     Freezes an immutable revision: all refs pinned, maps and snapshots stored, renditions rendered
    (Markdown, HTML with the TLP banner on every page and the automated-analysis footer when any block
    is machine-generated, JSON bundle).

    **Scope:** `read`.

    Args:
        report_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReportRevision]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    report_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | ReportRevision | None:
    """Publish a report revision

     Freezes an immutable revision: all refs pinned, maps and snapshots stored, renditions rendered
    (Markdown, HTML with the TLP banner on every page and the automated-analysis footer when any block
    is machine-generated, JSON bundle).

    **Scope:** `read`.

    Args:
        report_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReportRevision
    """

    return sync_detailed(
        report_id=report_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    report_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | ReportRevision]:
    """Publish a report revision

     Freezes an immutable revision: all refs pinned, maps and snapshots stored, renditions rendered
    (Markdown, HTML with the TLP banner on every page and the automated-analysis footer when any block
    is machine-generated, JSON bundle).

    **Scope:** `read`.

    Args:
        report_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | ReportRevision]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    report_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | ReportRevision | None:
    """Publish a report revision

     Freezes an immutable revision: all refs pinned, maps and snapshots stored, renditions rendered
    (Markdown, HTML with the TLP banner on every page and the automated-analysis footer when any block
    is machine-generated, JSON bundle).

    **Scope:** `read`.

    Args:
        report_id (str):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | ReportRevision
    """

    return (
        await asyncio_detailed(
            report_id=report_id,
            client=client,
        )
    ).parsed
