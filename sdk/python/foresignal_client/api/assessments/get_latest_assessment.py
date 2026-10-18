from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.assessment import Assessment
from ...models.get_latest_assessment_cadence import GetLatestAssessmentCadence
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    cadence: GetLatestAssessmentCadence | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_cadence: str | Unset = UNSET
    if not isinstance(cadence, Unset):
        json_cadence = cadence.value

    params["cadence"] = json_cadence

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/assessments/latest",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Assessment | Problem | None:
    if response.status_code == 200:
        response_200 = Assessment.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

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
) -> Response[Assessment | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    cadence: GetLatestAssessmentCadence | Unset = UNSET,
) -> Response[Assessment | Problem]:
    """Get the latest assessment

     The newest assessment of a cadence (any cadence when omitted).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        cadence (GetLatestAssessmentCadence | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Assessment | Problem]
    """

    kwargs = _get_kwargs(
        cadence=cadence,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    cadence: GetLatestAssessmentCadence | Unset = UNSET,
) -> Assessment | Problem | None:
    """Get the latest assessment

     The newest assessment of a cadence (any cadence when omitted).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        cadence (GetLatestAssessmentCadence | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Assessment | Problem
    """

    return sync_detailed(
        client=client,
        cadence=cadence,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    cadence: GetLatestAssessmentCadence | Unset = UNSET,
) -> Response[Assessment | Problem]:
    """Get the latest assessment

     The newest assessment of a cadence (any cadence when omitted).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        cadence (GetLatestAssessmentCadence | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Assessment | Problem]
    """

    kwargs = _get_kwargs(
        cadence=cadence,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    cadence: GetLatestAssessmentCadence | Unset = UNSET,
) -> Assessment | Problem | None:
    """Get the latest assessment

     The newest assessment of a cadence (any cadence when omitted).

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        cadence (GetLatestAssessmentCadence | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Assessment | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            cadence=cadence,
        )
    ).parsed
