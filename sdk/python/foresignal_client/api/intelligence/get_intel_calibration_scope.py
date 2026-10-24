from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_intel_calibration_scope_scope import GetIntelCalibrationScopeScope
from ...models.intel_calibration import IntelCalibration
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    scope: GetIntelCalibrationScopeScope,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/intel/calibration/{scope}".format(
            scope=quote(str(scope), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> IntelCalibration | Problem | None:
    if response.status_code == 200:
        response_200 = IntelCalibration.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[IntelCalibration | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    scope: GetIntelCalibrationScopeScope,
    *,
    client: AuthenticatedClient,
) -> Response[IntelCalibration | Problem]:
    """Calibration of one scope

     The calibration of one scope (151): `intel-event` or `precursor-active`. An unknown scope returns
    the empty, uncalibrated state.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        scope (GetIntelCalibrationScopeScope):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelCalibration | Problem]
    """

    kwargs = _get_kwargs(
        scope=scope,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    scope: GetIntelCalibrationScopeScope,
    *,
    client: AuthenticatedClient,
) -> IntelCalibration | Problem | None:
    """Calibration of one scope

     The calibration of one scope (151): `intel-event` or `precursor-active`. An unknown scope returns
    the empty, uncalibrated state.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        scope (GetIntelCalibrationScopeScope):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IntelCalibration | Problem
    """

    return sync_detailed(
        scope=scope,
        client=client,
    ).parsed


async def asyncio_detailed(
    scope: GetIntelCalibrationScopeScope,
    *,
    client: AuthenticatedClient,
) -> Response[IntelCalibration | Problem]:
    """Calibration of one scope

     The calibration of one scope (151): `intel-event` or `precursor-active`. An unknown scope returns
    the empty, uncalibrated state.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        scope (GetIntelCalibrationScopeScope):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[IntelCalibration | Problem]
    """

    kwargs = _get_kwargs(
        scope=scope,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    scope: GetIntelCalibrationScopeScope,
    *,
    client: AuthenticatedClient,
) -> IntelCalibration | Problem | None:
    """Calibration of one scope

     The calibration of one scope (151): `intel-event` or `precursor-active`. An unknown scope returns
    the empty, uncalibrated state.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        scope (GetIntelCalibrationScopeScope):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        IntelCalibration | Problem
    """

    return (
        await asyncio_detailed(
            scope=scope,
            client=client,
        )
    ).parsed
