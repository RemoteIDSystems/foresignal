from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecast_question import ForecastQuestion
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    question_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/forecasts/questions/{question_id}".format(
            question_id=quote(str(question_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecastQuestion | Problem | None:
    if response.status_code == 200:
        response_200 = ForecastQuestion.from_dict(response.json())

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
) -> Response[ForecastQuestion | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    question_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ForecastQuestion | Problem]:
    """Get a forecast question

     Returns criteria and resolver, the latest forecast per forecaster (newest first), the ensemble with
    its ICD term, the resolution and, once resolved, the scores.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastQuestion | Problem]
    """

    kwargs = _get_kwargs(
        question_id=question_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    question_id: str,
    *,
    client: AuthenticatedClient,
) -> ForecastQuestion | Problem | None:
    """Get a forecast question

     Returns criteria and resolver, the latest forecast per forecaster (newest first), the ensemble with
    its ICD term, the resolution and, once resolved, the scores.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastQuestion | Problem
    """

    return sync_detailed(
        question_id=question_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    question_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ForecastQuestion | Problem]:
    """Get a forecast question

     Returns criteria and resolver, the latest forecast per forecaster (newest first), the ensemble with
    its ICD term, the resolution and, once resolved, the scores.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastQuestion | Problem]
    """

    kwargs = _get_kwargs(
        question_id=question_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    question_id: str,
    *,
    client: AuthenticatedClient,
) -> ForecastQuestion | Problem | None:
    """Get a forecast question

     Returns criteria and resolver, the latest forecast per forecaster (newest first), the ensemble with
    its ICD term, the resolution and, once resolved, the scores.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastQuestion | Problem
    """

    return (
        await asyncio_detailed(
            question_id=question_id,
            client=client,
        )
    ).parsed
