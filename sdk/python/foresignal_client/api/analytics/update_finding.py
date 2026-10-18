from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analytic_finding import AnalyticFinding
from ...models.finding_patch import FindingPatch
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    finding_id: str,
    *,
    body: FindingPatch,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "patch",
        "url": "/v1/analytics/findings/{finding_id}".format(
            finding_id=quote(str(finding_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/merge-patch+json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalyticFinding | Problem | None:
    if response.status_code == 200:
        response_200 = AnalyticFinding.from_dict(response.json())

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
) -> Response[AnalyticFinding | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    finding_id: str,
    *,
    client: AuthenticatedClient,
    body: FindingPatch,
) -> Response[AnalyticFinding | Problem]:
    """Dismiss an analytic finding

     Dismisses a finding with a reason (JSON merge patch); its `foresignal-analytics` record is
    tombstoned and `finding.dismissed` is emitted.

    **Scope:** `agent` or `admin`.

    Args:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (FindingPatch): JSON merge patch for a finding: dismiss with a reason

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalyticFinding | Problem]
    """

    kwargs = _get_kwargs(
        finding_id=finding_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    finding_id: str,
    *,
    client: AuthenticatedClient,
    body: FindingPatch,
) -> AnalyticFinding | Problem | None:
    """Dismiss an analytic finding

     Dismisses a finding with a reason (JSON merge patch); its `foresignal-analytics` record is
    tombstoned and `finding.dismissed` is emitted.

    **Scope:** `agent` or `admin`.

    Args:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (FindingPatch): JSON merge patch for a finding: dismiss with a reason

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalyticFinding | Problem
    """

    return sync_detailed(
        finding_id=finding_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    finding_id: str,
    *,
    client: AuthenticatedClient,
    body: FindingPatch,
) -> Response[AnalyticFinding | Problem]:
    """Dismiss an analytic finding

     Dismisses a finding with a reason (JSON merge patch); its `foresignal-analytics` record is
    tombstoned and `finding.dismissed` is emitted.

    **Scope:** `agent` or `admin`.

    Args:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (FindingPatch): JSON merge patch for a finding: dismiss with a reason

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalyticFinding | Problem]
    """

    kwargs = _get_kwargs(
        finding_id=finding_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    finding_id: str,
    *,
    client: AuthenticatedClient,
    body: FindingPatch,
) -> AnalyticFinding | Problem | None:
    """Dismiss an analytic finding

     Dismisses a finding with a reason (JSON merge patch); its `foresignal-analytics` record is
    tombstoned and `finding.dismissed` is emitted.

    **Scope:** `agent` or `admin`.

    Args:
        finding_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (FindingPatch): JSON merge patch for a finding: dismiss with a reason

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalyticFinding | Problem
    """

    return (
        await asyncio_detailed(
            finding_id=finding_id,
            client=client,
            body=body,
        )
    ).parsed
