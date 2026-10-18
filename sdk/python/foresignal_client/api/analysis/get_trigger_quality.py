import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.rule_quality import RuleQuality
from ...types import UNSET, Response, Unset


def _get_kwargs(
    rule_id: UUID,
    *,
    since: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/triggers/{rule_id}/quality".format(
            rule_id=quote(str(rule_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | RuleQuality | None:
    if response.status_code == 200:
        response_200 = RuleQuality.from_dict(response.json())

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
) -> Response[Problem | RuleQuality]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
) -> Response[Problem | RuleQuality]:
    """Alert quality of a rule

     Precision with a Wilson interval (null when fewer than 5 labelled), time to ack and resolve,
    flapping, duplicates and severity distribution.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RuleQuality]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        since=since,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
) -> Problem | RuleQuality | None:
    """Alert quality of a rule

     Precision with a Wilson interval (null when fewer than 5 labelled), time to ack and resolve,
    flapping, duplicates and severity distribution.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RuleQuality
    """

    return sync_detailed(
        rule_id=rule_id,
        client=client,
        since=since,
    ).parsed


async def asyncio_detailed(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
) -> Response[Problem | RuleQuality]:
    """Alert quality of a rule

     Precision with a Wilson interval (null when fewer than 5 labelled), time to ack and resolve,
    flapping, duplicates and severity distribution.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | RuleQuality]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        since=since,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
) -> Problem | RuleQuality | None:
    """Alert quality of a rule

     Precision with a Wilson interval (null when fewer than 5 labelled), time to ack and resolve,
    flapping, duplicates and severity distribution.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | RuleQuality
    """

    return (
        await asyncio_detailed(
            rule_id=rule_id,
            client=client,
            since=since,
        )
    ).parsed
