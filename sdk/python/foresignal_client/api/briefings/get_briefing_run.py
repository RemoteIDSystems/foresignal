from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.briefing_run import BriefingRun
from ...models.get_briefing_run_format import GetBriefingRunFormat
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    briefing_id: UUID,
    run_id: UUID,
    *,
    format_: GetBriefingRunFormat | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_none_match, Unset):
        headers["If-None-Match"] = if_none_match

    params: dict[str, Any] = {}

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/briefings/{briefing_id}/runs/{run_id}".format(
            briefing_id=quote(str(briefing_id), safe=""),
            run_id=quote(str(run_id), safe=""),
        ),
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | BriefingRun | Problem | None:
    if response.status_code == 200:
        response_200 = BriefingRun.from_dict(response.json())

        return response_200

    if response.status_code == 304:
        response_304 = cast(Any, None)
        return response_304

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Any | BriefingRun | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    briefing_id: UUID,
    run_id: UUID,
    *,
    client: AuthenticatedClient,
    format_: GetBriefingRunFormat | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | BriefingRun | Problem]:
    """Get a briefing run

     JSON by default; sanitized HTML (allowlisted headings, lists, tables, links with rel="noopener
    nofollow", code, emphasis; light/dark stylesheet; strict CSP) with `Accept: text/html` or
    `?format=html`. ETag per run.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        run_id (UUID):
        format_ (GetBriefingRunFormat | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | BriefingRun | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
        run_id=run_id,
        format_=format_,
        if_none_match=if_none_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    briefing_id: UUID,
    run_id: UUID,
    *,
    client: AuthenticatedClient,
    format_: GetBriefingRunFormat | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | BriefingRun | Problem | None:
    """Get a briefing run

     JSON by default; sanitized HTML (allowlisted headings, lists, tables, links with rel="noopener
    nofollow", code, emphasis; light/dark stylesheet; strict CSP) with `Accept: text/html` or
    `?format=html`. ETag per run.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        run_id (UUID):
        format_ (GetBriefingRunFormat | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | BriefingRun | Problem
    """

    return sync_detailed(
        briefing_id=briefing_id,
        run_id=run_id,
        client=client,
        format_=format_,
        if_none_match=if_none_match,
    ).parsed


async def asyncio_detailed(
    briefing_id: UUID,
    run_id: UUID,
    *,
    client: AuthenticatedClient,
    format_: GetBriefingRunFormat | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Response[Any | BriefingRun | Problem]:
    """Get a briefing run

     JSON by default; sanitized HTML (allowlisted headings, lists, tables, links with rel="noopener
    nofollow", code, emphasis; light/dark stylesheet; strict CSP) with `Accept: text/html` or
    `?format=html`. ETag per run.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        run_id (UUID):
        format_ (GetBriefingRunFormat | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | BriefingRun | Problem]
    """

    kwargs = _get_kwargs(
        briefing_id=briefing_id,
        run_id=run_id,
        format_=format_,
        if_none_match=if_none_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    briefing_id: UUID,
    run_id: UUID,
    *,
    client: AuthenticatedClient,
    format_: GetBriefingRunFormat | Unset = UNSET,
    if_none_match: str | Unset = UNSET,
) -> Any | BriefingRun | Problem | None:
    """Get a briefing run

     JSON by default; sanitized HTML (allowlisted headings, lists, tables, links with rel="noopener
    nofollow", code, emphasis; light/dark stylesheet; strict CSP) with `Accept: text/html` or
    `?format=html`. ETag per run.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        briefing_id (UUID):
        run_id (UUID):
        format_ (GetBriefingRunFormat | Unset):
        if_none_match (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | BriefingRun | Problem
    """

    return (
        await asyncio_detailed(
            briefing_id=briefing_id,
            run_id=run_id,
            client=client,
            format_=format_,
            if_none_match=if_none_match,
        )
    ).parsed
