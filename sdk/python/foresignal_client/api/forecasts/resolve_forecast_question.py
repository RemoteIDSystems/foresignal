from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecast_question import ForecastQuestion
from ...models.forecast_resolution_input import ForecastResolutionInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    question_id: str,
    *,
    body: ForecastResolutionInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/forecasts/questions/{question_id}/resolve".format(
            question_id=quote(str(question_id), safe=""),
        ),
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecastQuestion | Problem | None:
    if response.status_code == 200:
        response_200 = ForecastQuestion.from_dict(response.json())

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

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

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
    body: ForecastResolutionInput,
) -> Response[ForecastQuestion | Problem]:
    """Resolve a question manually

     Resolves a manual question (or confirms a `hypothesis-outcome` one) with pinned evidence; scores are
    computed and `question.resolved` is emitted. Questions with machine resolvers resolve automatically
    and return 409 `QUESTION_CLOSED` here (annul them instead).

    **Scope:** `agent` or `admin`.

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ForecastResolutionInput): Manual resolution with evidence

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastQuestion | Problem]
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
    body: ForecastResolutionInput,
) -> ForecastQuestion | Problem | None:
    """Resolve a question manually

     Resolves a manual question (or confirms a `hypothesis-outcome` one) with pinned evidence; scores are
    computed and `question.resolved` is emitted. Questions with machine resolvers resolve automatically
    and return 409 `QUESTION_CLOSED` here (annul them instead).

    **Scope:** `agent` or `admin`.

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ForecastResolutionInput): Manual resolution with evidence

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastQuestion | Problem
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
    body: ForecastResolutionInput,
) -> Response[ForecastQuestion | Problem]:
    """Resolve a question manually

     Resolves a manual question (or confirms a `hypothesis-outcome` one) with pinned evidence; scores are
    computed and `question.resolved` is emitted. Questions with machine resolvers resolve automatically
    and return 409 `QUESTION_CLOSED` here (annul them instead).

    **Scope:** `agent` or `admin`.

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ForecastResolutionInput): Manual resolution with evidence

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastQuestion | Problem]
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
    body: ForecastResolutionInput,
) -> ForecastQuestion | Problem | None:
    """Resolve a question manually

     Resolves a manual question (or confirms a `hypothesis-outcome` one) with pinned evidence; scores are
    computed and `question.resolved` is emitted. Questions with machine resolvers resolve automatically
    and return 409 `QUESTION_CLOSED` here (annul them instead).

    **Scope:** `agent` or `admin`.

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        body (ForecastResolutionInput): Manual resolution with evidence

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
            body=body,
        )
    ).parsed
