import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.analyst_evaluation import AnalystEvaluation
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    profile_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["profileId"] = profile_id

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analyst/evaluation",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AnalystEvaluation | Problem | None:
    if response.status_code == 200:
        response_200 = AnalystEvaluation.from_dict(response.json())

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
) -> Response[AnalystEvaluation | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    profile_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> Response[AnalystEvaluation | Problem]:
    """Analyst evaluation aggregates

     Runs by status, noop share, tokens per day, proposal acceptance, dismissed notes, useful/wrong
    ratios and citations dropped per run.

    **Scope:** `agent` or `admin`.

    Args:
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystEvaluation | Problem]
    """

    kwargs = _get_kwargs(
        profile_id=profile_id,
        since=since,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    profile_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> AnalystEvaluation | Problem | None:
    """Analyst evaluation aggregates

     Runs by status, noop share, tokens per day, proposal acceptance, dismissed notes, useful/wrong
    ratios and citations dropped per run.

    **Scope:** `agent` or `admin`.

    Args:
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystEvaluation | Problem
    """

    return sync_detailed(
        client=client,
        profile_id=profile_id,
        since=since,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    profile_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> Response[AnalystEvaluation | Problem]:
    """Analyst evaluation aggregates

     Runs by status, noop share, tokens per day, proposal acceptance, dismissed notes, useful/wrong
    ratios and citations dropped per run.

    **Scope:** `agent` or `admin`.

    Args:
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AnalystEvaluation | Problem]
    """

    kwargs = _get_kwargs(
        profile_id=profile_id,
        since=since,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    profile_id: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
) -> AnalystEvaluation | Problem | None:
    """Analyst evaluation aggregates

     Runs by status, noop share, tokens per day, proposal acceptance, dismissed notes, useful/wrong
    ratios and citations dropped per run.

    **Scope:** `agent` or `admin`.

    Args:
        profile_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AnalystEvaluation | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            profile_id=profile_id,
            since=since,
        )
    ).parsed
