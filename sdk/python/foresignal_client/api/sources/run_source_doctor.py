from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.doctor_options import DoctorOptions
from ...models.doctor_report import DoctorReport
from ...models.doctor_run_accepted import DoctorRunAccepted
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    *,
    body: DoctorOptions | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/sources/{source_id}/doctor".format(
            source_id=quote(str(source_id), safe=""),
        ),
    }

    if not isinstance(body, Unset):
        _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> DoctorReport | DoctorRunAccepted | Problem | None:
    if response.status_code == 200:
        response_200 = DoctorReport.from_dict(response.json())

        return response_200

    if response.status_code == 202:
        response_202 = DoctorRunAccepted.from_dict(response.json())

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
) -> Response[DoctorReport | DoctorRunAccepted | Problem]:
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
    body: DoctorOptions | Unset = UNSET,
) -> Response[DoctorReport | DoctorRunAccepted | Problem]:
    """Diagnose a source

     Probes the upstream (budgeted; skipped with `PROBE_SKIPPED_BUDGET` when out of budget or in
    backoff), diffs its structure against the recorded fixture, runs deterministic rules and, if
    enabled, asks the LLM for an explanation and a parser patch proposed as text (never applied).
    Synchronous up to 90 s, else 202 with `Location`. One run per source per 10 min (`DOCTOR_BUSY`).

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (DoctorOptions | Unset): Options for a doctor run

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[DoctorReport | DoctorRunAccepted | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: DoctorOptions | Unset = UNSET,
) -> DoctorReport | DoctorRunAccepted | Problem | None:
    """Diagnose a source

     Probes the upstream (budgeted; skipped with `PROBE_SKIPPED_BUDGET` when out of budget or in
    backoff), diffs its structure against the recorded fixture, runs deterministic rules and, if
    enabled, asks the LLM for an explanation and a parser patch proposed as text (never applied).
    Synchronous up to 90 s, else 202 with `Location`. One run per source per 10 min (`DOCTOR_BUSY`).

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (DoctorOptions | Unset): Options for a doctor run

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        DoctorReport | DoctorRunAccepted | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: DoctorOptions | Unset = UNSET,
) -> Response[DoctorReport | DoctorRunAccepted | Problem]:
    """Diagnose a source

     Probes the upstream (budgeted; skipped with `PROBE_SKIPPED_BUDGET` when out of budget or in
    backoff), diffs its structure against the recorded fixture, runs deterministic rules and, if
    enabled, asks the LLM for an explanation and a parser patch proposed as text (never applied).
    Synchronous up to 90 s, else 202 with `Location`. One run per source per 10 min (`DOCTOR_BUSY`).

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (DoctorOptions | Unset): Options for a doctor run

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[DoctorReport | DoctorRunAccepted | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    body: DoctorOptions | Unset = UNSET,
) -> DoctorReport | DoctorRunAccepted | Problem | None:
    """Diagnose a source

     Probes the upstream (budgeted; skipped with `PROBE_SKIPPED_BUDGET` when out of budget or in
    backoff), diffs its structure against the recorded fixture, runs deterministic rules and, if
    enabled, asks the LLM for an explanation and a parser patch proposed as text (never applied).
    Synchronous up to 90 s, else 202 with `Location`. One run per source per 10 min (`DOCTOR_BUSY`).

    **Scope:** `admin`.

    Args:
        source_id (str):
        body (DoctorOptions | Unset): Options for a doctor run

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        DoctorReport | DoctorRunAccepted | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            body=body,
        )
    ).parsed
