import datetime
from http import HTTPStatus
from typing import Any
from urllib.parse import quote
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.trigger_test_result import TriggerTestResult
from ...types import UNSET, Response, Unset


def _get_kwargs(
    rule_id: UUID,
    *,
    since: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 100,
    until: datetime.datetime | Unset = UNSET,
    from_seq: int | Unset = UNSET,
    to_seq: int | Unset = UNSET,
    simulate: bool | Unset = False,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["limit"] = limit

    json_until: str | Unset = UNSET
    if not isinstance(until, Unset):
        json_until = until.isoformat()
    params["until"] = json_until

    params["fromSeq"] = from_seq

    params["toSeq"] = to_seq

    params["simulate"] = simulate

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/triggers/{rule_id}/test".format(
            rule_id=quote(str(rule_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | TriggerTestResult | None:
    if response.status_code == 200:
        response_200 = TriggerTestResult.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | TriggerTestResult]:
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
    limit: int | Unset = 100,
    until: datetime.datetime | Unset = UNSET,
    from_seq: int | Unset = UNSET,
    to_seq: int | Unset = UNSET,
    simulate: bool | Unset = False,
) -> Response[Problem | TriggerTestResult]:
    """Dry-run a rule against stored data

     Backtest over stored versions by seq range (`fromSeq`/`toSeq`) or time (`since`/`until`, translated
    via seq_clock; default last 24 h), in 50,000-seq chunks. Without `simulate` returns raw matches
    ignoring cooldown, snooze and the storm cap; with it runs the full Java pass and reports wouldFire /
    wouldSuppress. Creates nothing; `truncatedByRetention` when the range starts below the retained
    floor.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):
        limit (int | Unset):  Default: 100.
        until (datetime.datetime | Unset):
        from_seq (int | Unset):
        to_seq (int | Unset):
        simulate (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TriggerTestResult]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        since=since,
        limit=limit,
        until=until,
        from_seq=from_seq,
        to_seq=to_seq,
        simulate=simulate,
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
    limit: int | Unset = 100,
    until: datetime.datetime | Unset = UNSET,
    from_seq: int | Unset = UNSET,
    to_seq: int | Unset = UNSET,
    simulate: bool | Unset = False,
) -> Problem | TriggerTestResult | None:
    """Dry-run a rule against stored data

     Backtest over stored versions by seq range (`fromSeq`/`toSeq`) or time (`since`/`until`, translated
    via seq_clock; default last 24 h), in 50,000-seq chunks. Without `simulate` returns raw matches
    ignoring cooldown, snooze and the storm cap; with it runs the full Java pass and reports wouldFire /
    wouldSuppress. Creates nothing; `truncatedByRetention` when the range starts below the retained
    floor.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):
        limit (int | Unset):  Default: 100.
        until (datetime.datetime | Unset):
        from_seq (int | Unset):
        to_seq (int | Unset):
        simulate (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TriggerTestResult
    """

    return sync_detailed(
        rule_id=rule_id,
        client=client,
        since=since,
        limit=limit,
        until=until,
        from_seq=from_seq,
        to_seq=to_seq,
        simulate=simulate,
    ).parsed


async def asyncio_detailed(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 100,
    until: datetime.datetime | Unset = UNSET,
    from_seq: int | Unset = UNSET,
    to_seq: int | Unset = UNSET,
    simulate: bool | Unset = False,
) -> Response[Problem | TriggerTestResult]:
    """Dry-run a rule against stored data

     Backtest over stored versions by seq range (`fromSeq`/`toSeq`) or time (`since`/`until`, translated
    via seq_clock; default last 24 h), in 50,000-seq chunks. Without `simulate` returns raw matches
    ignoring cooldown, snooze and the storm cap; with it runs the full Java pass and reports wouldFire /
    wouldSuppress. Creates nothing; `truncatedByRetention` when the range starts below the retained
    floor.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):
        limit (int | Unset):  Default: 100.
        until (datetime.datetime | Unset):
        from_seq (int | Unset):
        to_seq (int | Unset):
        simulate (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | TriggerTestResult]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        since=since,
        limit=limit,
        until=until,
        from_seq=from_seq,
        to_seq=to_seq,
        simulate=simulate,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    rule_id: UUID,
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
    limit: int | Unset = 100,
    until: datetime.datetime | Unset = UNSET,
    from_seq: int | Unset = UNSET,
    to_seq: int | Unset = UNSET,
    simulate: bool | Unset = False,
) -> Problem | TriggerTestResult | None:
    """Dry-run a rule against stored data

     Backtest over stored versions by seq range (`fromSeq`/`toSeq`) or time (`since`/`until`, translated
    via seq_clock; default last 24 h), in 50,000-seq chunks. Without `simulate` returns raw matches
    ignoring cooldown, snooze and the storm cap; with it runs the full Java pass and reports wouldFire /
    wouldSuppress. Creates nothing; `truncatedByRetention` when the range starts below the retained
    floor.

    **Scope:** `agent`.

    Args:
        rule_id (UUID):
        since (datetime.datetime | Unset):
        limit (int | Unset):  Default: 100.
        until (datetime.datetime | Unset):
        from_seq (int | Unset):
        to_seq (int | Unset):
        simulate (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | TriggerTestResult
    """

    return (
        await asyncio_detailed(
            rule_id=rule_id,
            client=client,
            since=since,
            limit=limit,
            until=until,
            from_seq=from_seq,
            to_seq=to_seq,
            simulate=simulate,
        )
    ).parsed
