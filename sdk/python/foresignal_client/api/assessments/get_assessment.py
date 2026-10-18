from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.assessment import Assessment
from ...models.get_assessment_format import GetAssessmentFormat
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    assessment_id: str,
    *,
    format_: GetAssessmentFormat | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_format_: str | Unset = UNSET
    if not isinstance(format_, Unset):
        json_format_ = format_.value

    params["format"] = json_format_

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/assessments/{assessment_id}".format(
            assessment_id=quote(str(assessment_id), safe=""),
        ),
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
    assessment_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetAssessmentFormat | Unset = UNSET,
) -> Response[Assessment | Problem]:
    """Get an assessment

     JSON by default; `format=md` or `format=html` (or the matching `Accept`) returns the 029 rendering.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        format_ (GetAssessmentFormat | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Assessment | Problem]
    """

    kwargs = _get_kwargs(
        assessment_id=assessment_id,
        format_=format_,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    assessment_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetAssessmentFormat | Unset = UNSET,
) -> Assessment | Problem | None:
    """Get an assessment

     JSON by default; `format=md` or `format=html` (or the matching `Accept`) returns the 029 rendering.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        format_ (GetAssessmentFormat | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Assessment | Problem
    """

    return sync_detailed(
        assessment_id=assessment_id,
        client=client,
        format_=format_,
    ).parsed


async def asyncio_detailed(
    assessment_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetAssessmentFormat | Unset = UNSET,
) -> Response[Assessment | Problem]:
    """Get an assessment

     JSON by default; `format=md` or `format=html` (or the matching `Accept`) returns the 029 rendering.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        format_ (GetAssessmentFormat | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Assessment | Problem]
    """

    kwargs = _get_kwargs(
        assessment_id=assessment_id,
        format_=format_,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    assessment_id: str,
    *,
    client: AuthenticatedClient,
    format_: GetAssessmentFormat | Unset = UNSET,
) -> Assessment | Problem | None:
    """Get an assessment

     JSON by default; `format=md` or `format=html` (or the matching `Accept`) returns the 029 rendering.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        format_ (GetAssessmentFormat | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Assessment | Problem
    """

    return (
        await asyncio_detailed(
            assessment_id=assessment_id,
            client=client,
            format_=format_,
        )
    ).parsed
