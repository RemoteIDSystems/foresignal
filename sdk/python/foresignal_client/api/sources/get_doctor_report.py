from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.doctor_report import DoctorReport
from ...models.doctor_run_accepted import DoctorRunAccepted
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    report_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/doctor/reports/{report_id}".format(
            report_id=quote(str(report_id), safe=""),
        ),
    }

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
    report_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[DoctorReport | DoctorRunAccepted | Problem]:
    """Get a doctor report

     One doctor report; patches are always labeled "proposed, not applied". 202 with `DoctorRunAccepted`
    while the run is still in progress.

    **Scope:** `agent`.

    Args:
        report_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[DoctorReport | DoctorRunAccepted | Problem]
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
) -> DoctorReport | DoctorRunAccepted | Problem | None:
    """Get a doctor report

     One doctor report; patches are always labeled "proposed, not applied". 202 with `DoctorRunAccepted`
    while the run is still in progress.

    **Scope:** `agent`.

    Args:
        report_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        DoctorReport | DoctorRunAccepted | Problem
    """

    return sync_detailed(
        report_id=report_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    report_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[DoctorReport | DoctorRunAccepted | Problem]:
    """Get a doctor report

     One doctor report; patches are always labeled "proposed, not applied". 202 with `DoctorRunAccepted`
    while the run is still in progress.

    **Scope:** `agent`.

    Args:
        report_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[DoctorReport | DoctorRunAccepted | Problem]
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
) -> DoctorReport | DoctorRunAccepted | Problem | None:
    """Get a doctor report

     One doctor report; patches are always labeled "proposed, not applied". 202 with `DoctorRunAccepted`
    while the run is still in progress.

    **Scope:** `agent`.

    Args:
        report_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        DoctorReport | DoctorRunAccepted | Problem
    """

    return (
        await asyncio_detailed(
            report_id=report_id,
            client=client,
        )
    ).parsed
