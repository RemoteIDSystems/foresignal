from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecast import Forecast
from ...models.forecast_input import ForecastInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    question_id: str,
    *,
    body: ForecastInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/forecasts/questions/{question_id}/forecasts".format(
            question_id=quote(str(question_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Forecast | Problem | None:
    if response.status_code == 201:
        response_201 = Forecast.from_dict(response.json())

        return response_201

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
) -> Response[Forecast | Problem]:
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
    body: ForecastInput,
) -> Response[Forecast | Problem]:
    """Submit a forecast

     Records an immutable forecast; `madeAt` and `asOfSeq` are set by the server and earlier forecasts of
    the same forecaster are superseded, never edited. Read scope submits as `operator:{handle}` (the
    token's principal); agent scope may name another registered `operator` or `external` forecaster. A
    token is required even while `security.openReads` is on.

    **Scope:** `read` (always authenticated: the forecast is recorded under the caller's principal).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ForecastInput): Submit a forecast; exactly one of probability, distribution or
            quantiles matching the question type

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Forecast | Problem]
    """

    kwargs = _get_kwargs(
        question_id=question_id,
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    question_id: str,
    *,
    client: AuthenticatedClient,
    body: ForecastInput,
) -> Forecast | Problem | None:
    """Submit a forecast

     Records an immutable forecast; `madeAt` and `asOfSeq` are set by the server and earlier forecasts of
    the same forecaster are superseded, never edited. Read scope submits as `operator:{handle}` (the
    token's principal); agent scope may name another registered `operator` or `external` forecaster. A
    token is required even while `security.openReads` is on.

    **Scope:** `read` (always authenticated: the forecast is recorded under the caller's principal).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ForecastInput): Submit a forecast; exactly one of probability, distribution or
            quantiles matching the question type

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Forecast | Problem
    """

    return sync_detailed(
        question_id=question_id,
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    question_id: str,
    *,
    client: AuthenticatedClient,
    body: ForecastInput,
) -> Response[Forecast | Problem]:
    """Submit a forecast

     Records an immutable forecast; `madeAt` and `asOfSeq` are set by the server and earlier forecasts of
    the same forecaster are superseded, never edited. Read scope submits as `operator:{handle}` (the
    token's principal); agent scope may name another registered `operator` or `external` forecaster. A
    token is required even while `security.openReads` is on.

    **Scope:** `read` (always authenticated: the forecast is recorded under the caller's principal).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ForecastInput): Submit a forecast; exactly one of probability, distribution or
            quantiles matching the question type

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Forecast | Problem]
    """

    kwargs = _get_kwargs(
        question_id=question_id,
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    question_id: str,
    *,
    client: AuthenticatedClient,
    body: ForecastInput,
) -> Forecast | Problem | None:
    """Submit a forecast

     Records an immutable forecast; `madeAt` and `asOfSeq` are set by the server and earlier forecasts of
    the same forecaster are superseded, never edited. Read scope submits as `operator:{handle}` (the
    token's principal); agent scope may name another registered `operator` or `external` forecaster. A
    token is required even while `security.openReads` is on.

    **Scope:** `read` (always authenticated: the forecast is recorded under the caller's principal).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ForecastInput): Submit a forecast; exactly one of probability, distribution or
            quantiles matching the question type

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Forecast | Problem
    """

    return (
        await asyncio_detailed(
            question_id=question_id,
            client=client,
            body=body,
        )
    ).parsed
