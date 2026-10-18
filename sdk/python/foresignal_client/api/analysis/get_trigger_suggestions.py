import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.get_trigger_suggestions_status import GetTriggerSuggestionsStatus
from ...models.problem import Problem
from ...models.tuning_suggestion_list import TuningSuggestionList
from ...types import UNSET, Response, Unset


def _get_kwargs(
    rule_id: UUID,
    *,
    since: datetime.datetime | Unset = UNSET,
    simulate: bool | Unset = False,
    explain: bool | Unset = False,
    status: GetTriggerSuggestionsStatus | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["simulate"] = simulate

    params["explain"] = explain

    json_status: str | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = status.value

    params["status"] = json_status

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/triggers/{rule_id}/suggestions".format(
            rule_id=quote(str(rule_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TuningSuggestionList | None:
    if response.status_code == 200:
        response_200 = TuningSuggestionList.from_dict(response.json())

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
) -> Response[Problem | TuningSuggestionList]:
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
    simulate: bool | Unset = False,
    explain: bool | Unset = False,
    status: GetTriggerSuggestionsStatus | Unset = UNSET,
) -> Response[Problem | TuningSuggestionList]:
    """Tuning suggestions for a rule

     Deterministic suggestions with ready patches; never applied automatically. `simulate=true` needs
    agent scope and submits a backtest job for the expected effects.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):
        simulate (bool | Unset):  Default: False.
        explain (bool | Unset):  Default: False.
        status (GetTriggerSuggestionsStatus | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TuningSuggestionList]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        since=since,
        simulate=simulate,
        explain=explain,
        status=status,
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
    simulate: bool | Unset = False,
    explain: bool | Unset = False,
    status: GetTriggerSuggestionsStatus | Unset = UNSET,
) -> Problem | TuningSuggestionList | None:
    """Tuning suggestions for a rule

     Deterministic suggestions with ready patches; never applied automatically. `simulate=true` needs
    agent scope and submits a backtest job for the expected effects.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):
        simulate (bool | Unset):  Default: False.
        explain (bool | Unset):  Default: False.
        status (GetTriggerSuggestionsStatus | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TuningSuggestionList
    """

    return sync_detailed(
        rule_id=rule_id,
        client=client,
        since=since,
        simulate=simulate,
        explain=explain,
        status=status,
    ).parsed


async def asyncio_detailed(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    simulate: bool | Unset = False,
    explain: bool | Unset = False,
    status: GetTriggerSuggestionsStatus | Unset = UNSET,
) -> Response[Problem | TuningSuggestionList]:
    """Tuning suggestions for a rule

     Deterministic suggestions with ready patches; never applied automatically. `simulate=true` needs
    agent scope and submits a backtest job for the expected effects.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):
        simulate (bool | Unset):  Default: False.
        explain (bool | Unset):  Default: False.
        status (GetTriggerSuggestionsStatus | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TuningSuggestionList]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        since=since,
        simulate=simulate,
        explain=explain,
        status=status,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    simulate: bool | Unset = False,
    explain: bool | Unset = False,
    status: GetTriggerSuggestionsStatus | Unset = UNSET,
) -> Problem | TuningSuggestionList | None:
    """Tuning suggestions for a rule

     Deterministic suggestions with ready patches; never applied automatically. `simulate=true` needs
    agent scope and submits a backtest job for the expected effects.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):
        simulate (bool | Unset):  Default: False.
        explain (bool | Unset):  Default: False.
        status (GetTriggerSuggestionsStatus | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TuningSuggestionList
    """

    return (
        await asyncio_detailed(
            rule_id=rule_id,
            client=client,
            since=since,
            simulate=simulate,
            explain=explain,
            status=status,
        )
    ).parsed
