from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecast_evaluation import ForecastEvaluation
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    evaluation_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/forecasts/evaluations/{evaluation_id}".format(
            evaluation_id=quote(str(evaluation_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecastEvaluation | Problem | None:
    if response.status_code == 200:
        response_200 = ForecastEvaluation.from_dict(response.json())

        return response_200

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
) -> Response[ForecastEvaluation | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    evaluation_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ForecastEvaluation | Problem]:
    """Get a retrospective evaluation

     Returns an evaluation's progress, skipped counts and, when done, its retrospective scorecard.

    **Scope:** `agent` or `admin`.

    Args:
        evaluation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastEvaluation | Problem]
    """

    kwargs = _get_kwargs(
        evaluation_id=evaluation_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    evaluation_id: str,
    *,
    client: AuthenticatedClient,
) -> ForecastEvaluation | Problem | None:
    """Get a retrospective evaluation

     Returns an evaluation's progress, skipped counts and, when done, its retrospective scorecard.

    **Scope:** `agent` or `admin`.

    Args:
        evaluation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastEvaluation | Problem
    """

    return sync_detailed(
        evaluation_id=evaluation_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    evaluation_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[ForecastEvaluation | Problem]:
    """Get a retrospective evaluation

     Returns an evaluation's progress, skipped counts and, when done, its retrospective scorecard.

    **Scope:** `agent` or `admin`.

    Args:
        evaluation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastEvaluation | Problem]
    """

    kwargs = _get_kwargs(
        evaluation_id=evaluation_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    evaluation_id: str,
    *,
    client: AuthenticatedClient,
) -> ForecastEvaluation | Problem | None:
    """Get a retrospective evaluation

     Returns an evaluation's progress, skipped counts and, when done, its retrospective scorecard.

    **Scope:** `agent` or `admin`.

    Args:
        evaluation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastEvaluation | Problem
    """

    return (
        await asyncio_detailed(
            evaluation_id=evaluation_id,
            client=client,
        )
    ).parsed
