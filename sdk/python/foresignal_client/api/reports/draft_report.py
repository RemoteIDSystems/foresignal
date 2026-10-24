from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.report_draft_request import ReportDraftRequest
from ...types import Response


def _get_kwargs(
    *,
    body: ReportDraftRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/reports/draft",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ReportDraftRequest,
) -> Response[Problem]:
    """Draft a report from a source

     Builds a deterministic draft (templates `situation-brief`, `investigation-summary`, `chronicle-
    recap`, `weekly-assessment`, `custom`) from a selection, tag, investigation, chronicle, situation or
    workspace and stores it as a `draft`. `narrative=true` adds a labeled LLM narrative block via the
    028 tool loop with verified citations; it needs agent scope and LLM budget (026 budgets;
    `LLM_NOT_CONFIGURED`).

    **Scope:** `read`.

    Args:
        body (ReportDraftRequest): Build a deterministic draft report from a source

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
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
    body: ReportDraftRequest,
) -> Problem | None:
    """Draft a report from a source

     Builds a deterministic draft (templates `situation-brief`, `investigation-summary`, `chronicle-
    recap`, `weekly-assessment`, `custom`) from a selection, tag, investigation, chronicle, situation or
    workspace and stores it as a `draft`. `narrative=true` adds a labeled LLM narrative block via the
    028 tool loop with verified citations; it needs agent scope and LLM budget (026 budgets;
    `LLM_NOT_CONFIGURED`).

    **Scope:** `read`.

    Args:
        body (ReportDraftRequest): Build a deterministic draft report from a source

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ReportDraftRequest,
) -> Response[Problem]:
    """Draft a report from a source

     Builds a deterministic draft (templates `situation-brief`, `investigation-summary`, `chronicle-
    recap`, `weekly-assessment`, `custom`) from a selection, tag, investigation, chronicle, situation or
    workspace and stores it as a `draft`. `narrative=true` adds a labeled LLM narrative block via the
    028 tool loop with verified citations; it needs agent scope and LLM budget (026 budgets;
    `LLM_NOT_CONFIGURED`).

    **Scope:** `read`.

    Args:
        body (ReportDraftRequest): Build a deterministic draft report from a source

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ReportDraftRequest,
) -> Problem | None:
    """Draft a report from a source

     Builds a deterministic draft (templates `situation-brief`, `investigation-summary`, `chronicle-
    recap`, `weekly-assessment`, `custom`) from a selection, tag, investigation, chronicle, situation or
    workspace and stores it as a `draft`. `narrative=true` adds a labeled LLM narrative block via the
    028 tool loop with verified citations; it needs agent scope and LLM budget (026 budgets;
    `LLM_NOT_CONFIGURED`).

    **Scope:** `read`.

    Args:
        body (ReportDraftRequest): Build a deterministic draft report from a source

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
