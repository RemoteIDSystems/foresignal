from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.forecast_page import ForecastPage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    question_id: str,
    *,
    forecaster: list[str] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_forecaster: list[str] | Unset = UNSET
    if not isinstance(forecaster, Unset):
        json_forecaster = forecaster

    params["forecaster"] = json_forecaster

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/forecasts/questions/{question_id}/forecasts".format(
            question_id=quote(str(question_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ForecastPage | Problem | None:
    if response.status_code == 200:
        response_200 = ForecastPage.from_dict(response.json())

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
) -> Response[ForecastPage | Problem]:
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
    forecaster: list[str] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ForecastPage | Problem]:
    """List forecasts on a question

     Lists the question's forecasts newest first, including superseded ones.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        forecaster (list[str] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastPage | Problem]
    """

    kwargs = _get_kwargs(
        question_id=question_id,
        forecaster=forecaster,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    question_id: str,
    *,
    client: AuthenticatedClient,
    forecaster: list[str] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ForecastPage | Problem | None:
    """List forecasts on a question

     Lists the question's forecasts newest first, including superseded ones.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        forecaster (list[str] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastPage | Problem
    """

    return sync_detailed(
        question_id=question_id,
        client=client,
        forecaster=forecaster,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    question_id: str,
    *,
    client: AuthenticatedClient,
    forecaster: list[str] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[ForecastPage | Problem]:
    """List forecasts on a question

     Lists the question's forecasts newest first, including superseded ones.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        forecaster (list[str] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ForecastPage | Problem]
    """

    kwargs = _get_kwargs(
        question_id=question_id,
        forecaster=forecaster,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    question_id: str,
    *,
    client: AuthenticatedClient,
    forecaster: list[str] | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> ForecastPage | Problem | None:
    """List forecasts on a question

     Lists the question's forecasts newest first, including superseded ones.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        question_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        forecaster (list[str] | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ForecastPage | Problem
    """

    return (
        await asyncio_detailed(
            question_id=question_id,
            client=client,
            forecaster=forecaster,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
