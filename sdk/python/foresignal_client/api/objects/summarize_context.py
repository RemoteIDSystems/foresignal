from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.context_summary import ContextSummary
from ...models.context_summary_request import ContextSummaryRequest
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: ContextSummaryRequest,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/context/summary",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ContextSummary | Problem | None:
    if response.status_code == 200:
        response_200 = ContextSummary.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ContextSummary | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: ContextSummaryRequest,
) -> Response[ContextSummary | Problem]:
    """Summarise a viewport and time window

     Layer counts (aggregated, never feature lists), a merged activity feed tagged by signal family,
    insights with issuer, base rate and knowledge cutoff, and top related objects, in ≤ 32 KB; cached
    per normalised context for 30 s.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        body (ContextSummaryRequest): A viewport, window and filters to summarise

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ContextSummary | Problem]
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
    body: ContextSummaryRequest,
) -> ContextSummary | Problem | None:
    """Summarise a viewport and time window

     Layer counts (aggregated, never feature lists), a merged activity feed tagged by signal family,
    insights with issuer, base rate and knowledge cutoff, and top related objects, in ≤ 32 KB; cached
    per normalised context for 30 s.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        body (ContextSummaryRequest): A viewport, window and filters to summarise

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ContextSummary | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: ContextSummaryRequest,
) -> Response[ContextSummary | Problem]:
    """Summarise a viewport and time window

     Layer counts (aggregated, never feature lists), a merged activity feed tagged by signal family,
    insights with issuer, base rate and knowledge cutoff, and top related objects, in ≤ 32 KB; cached
    per normalised context for 30 s.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        body (ContextSummaryRequest): A viewport, window and filters to summarise

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ContextSummary | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: ContextSummaryRequest,
) -> ContextSummary | Problem | None:
    """Summarise a viewport and time window

     Layer counts (aggregated, never feature lists), a merged activity feed tagged by signal family,
    insights with issuer, base rate and knowledge cutoff, and top related objects, in ≤ 32 KB; cached
    per normalised context for 30 s.

    **Scope:** `read` (open while `security.openReads` is on).

    **v2.0 (R2-013):** a signed-in user needs `data.read` (403 PERMISSION_DENIED otherwise, audited);
    what they read is clamped to their data restriction (`User.dataRestriction`) and source view. API
    tokens keep their scope rules.

    Args:
        body (ContextSummaryRequest): A viewport, window and filters to summarise

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ContextSummary | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
