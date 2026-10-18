from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.report_patch import ReportPatch
from ...types import UNSET, Response, Unset


def _get_kwargs(
    report_id: str,
    *,
    body: ReportPatch,
    if_match: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(if_match, Unset):
        headers["If-Match"] = if_match

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/reports/{report_id}".format(
            report_id=quote(str(report_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

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
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportPatch,
    if_match: str | Unset = UNSET,
) -> Response[Problem]:
    """Update a report draft

     JSON merge patch (editors). Optimistic lock on the draft `version` (body `version` or `If-Match`); a
    stale version → 409 `REPORT_VERSION_CONFLICT`. Published revisions never change.

    **Scope:** `read`.

    Args:
        report_id (str):
        if_match (str | Unset):
        body (ReportPatch): JSON merge patch for a report draft (editors); `version` is the
            optimistic lock

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
        body=body,
        if_match=if_match,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportPatch,
    if_match: str | Unset = UNSET,
) -> Problem | None:
    """Update a report draft

     JSON merge patch (editors). Optimistic lock on the draft `version` (body `version` or `If-Match`); a
    stale version → 409 `REPORT_VERSION_CONFLICT`. Published revisions never change.

    **Scope:** `read`.

    Args:
        report_id (str):
        if_match (str | Unset):
        body (ReportPatch): JSON merge patch for a report draft (editors); `version` is the
            optimistic lock

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        report_id=report_id,
        client=client,
        body=body,
        if_match=if_match,
    ).parsed


async def asyncio_detailed(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportPatch,
    if_match: str | Unset = UNSET,
) -> Response[Problem]:
    """Update a report draft

     JSON merge patch (editors). Optimistic lock on the draft `version` (body `version` or `If-Match`); a
    stale version → 409 `REPORT_VERSION_CONFLICT`. Published revisions never change.

    **Scope:** `read`.

    Args:
        report_id (str):
        if_match (str | Unset):
        body (ReportPatch): JSON merge patch for a report draft (editors); `version` is the
            optimistic lock

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        report_id=report_id,
        body=body,
        if_match=if_match,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    report_id: str,
    *,
    client: AuthenticatedClient,
    body: ReportPatch,
    if_match: str | Unset = UNSET,
) -> Problem | None:
    """Update a report draft

     JSON merge patch (editors). Optimistic lock on the draft `version` (body `version` or `If-Match`); a
    stale version → 409 `REPORT_VERSION_CONFLICT`. Published revisions never change.

    **Scope:** `read`.

    Args:
        report_id (str):
        if_match (str | Unset):
        body (ReportPatch): JSON merge patch for a report draft (editors); `version` is the
            optimistic lock

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            report_id=report_id,
            client=client,
            body=body,
            if_match=if_match,
        )
    ).parsed
