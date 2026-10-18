import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.situation_changes import SituationChanges
from ...types import UNSET, Response


def _get_kwargs(
    situation_id: str,
    *,
    since: datetime.datetime,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_since = since.isoformat()
    params["since"] = json_since

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/situations/{situation_id}/changes".format(
            situation_id=quote(str(situation_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | SituationChanges | None:
    if response.status_code == 200:
        response_200 = SituationChanges.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

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
) -> Response[Problem | SituationChanges]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime,
) -> Response[Problem | SituationChanges]:
    """What changed since a time

     Diff between the daily snapshot of the UTC day of `since` and now: status, severity, new members by
    family, area change and regions added. Answered from snapshots, without an LLM.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SituationChanges]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        since=since,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime,
) -> Problem | SituationChanges | None:
    """What changed since a time

     Diff between the daily snapshot of the UTC day of `since` and now: status, severity, new members by
    family, area change and regions added. Answered from snapshots, without an LLM.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SituationChanges
    """

    return sync_detailed(
        situation_id=situation_id,
        client=client,
        since=since,
    ).parsed


async def asyncio_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime,
) -> Response[Problem | SituationChanges]:
    """What changed since a time

     Diff between the daily snapshot of the UTC day of `since` and now: status, severity, new members by
    family, area change and regions added. Answered from snapshots, without an LLM.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | SituationChanges]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
        since=since,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    situation_id: str,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime,
) -> Problem | SituationChanges | None:
    """What changed since a time

     Diff between the daily snapshot of the UTC day of `since` and now: status, severity, new members by
    family, area change and regions added. Answered from snapshots, without an LLM.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        since (datetime.datetime):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | SituationChanges
    """

    return (
        await asyncio_detailed(
            situation_id=situation_id,
            client=client,
            since=since,
        )
    ).parsed
