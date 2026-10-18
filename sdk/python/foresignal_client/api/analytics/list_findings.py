import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.finding_page import FindingPage
from ...models.finding_severity import FindingSeverity
from ...models.list_findings_status_item import ListFindingsStatusItem
from ...models.list_findings_type_item import ListFindingsTypeItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    type_: list[ListFindingsTypeItem] | Unset = UNSET,
    severity: list[FindingSeverity] | Unset = UNSET,
    status: list[ListFindingsStatusItem] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    ref: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_type_: list[str] | Unset = UNSET
    if not isinstance(type_, Unset):
        json_type_ = []
        for type_item_data in type_:
            type_item = type_item_data.value
            json_type_.append(type_item)

    params["type"] = json_type_

    json_severity: list[str] | Unset = UNSET
    if not isinstance(severity, Unset):
        json_severity = []
        for severity_item_data in severity:
            severity_item = severity_item_data.value
            json_severity.append(severity_item)

    params["severity"] = json_severity

    json_status: list[str] | Unset = UNSET
    if not isinstance(status, Unset):
        json_status = []
        for status_item_data in status:
            status_item = status_item_data.value
            json_status.append(status_item)

    params["status"] = json_status

    json_region: list[str] | Unset = UNSET
    if not isinstance(region, Unset):
        json_region = region

    params["region"] = json_region

    params["ref"] = ref

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["situationId"] = situation_id

    params["cursor"] = cursor

    params["limit"] = limit

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/analytics/findings",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> FindingPage | Problem | None:
    if response.status_code == 200:
        response_200 = FindingPage.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[FindingPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    type_: list[ListFindingsTypeItem] | Unset = UNSET,
    severity: list[FindingSeverity] | Unset = UNSET,
    status: list[ListFindingsStatusItem] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    ref: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[FindingPage | Problem]:
    """List analytic findings

     Lists findings newest first. Every finding carries its base rate and caveats; private findings are
    returned only to callers allowed private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListFindingsTypeItem] | Unset):
        severity (list[FindingSeverity] | Unset):
        status (list[ListFindingsStatusItem] | Unset):
        region (list[str] | Unset):
        ref (str | Unset): Series reference (Intelligence-Design §4.3):
            `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-
            chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the
            cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}`
            (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily
            news coverage in evidence groups by lean bucket left, center, right, unrated or state); or
            `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social
            mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or
            engagement, every language summed unless a language bucket is given;
            `social:all/{platform}/denominator[/{language}]` is the platform-language denominator;
            complete and paged collection only) Example: series:cftc-cot/067651.disagg.mm_net.
        since (datetime.datetime | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FindingPage | Problem]
    """

    kwargs = _get_kwargs(
        type_=type_,
        severity=severity,
        status=status,
        region=region,
        ref=ref,
        since=since,
        situation_id=situation_id,
        cursor=cursor,
        limit=limit,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    type_: list[ListFindingsTypeItem] | Unset = UNSET,
    severity: list[FindingSeverity] | Unset = UNSET,
    status: list[ListFindingsStatusItem] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    ref: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> FindingPage | Problem | None:
    """List analytic findings

     Lists findings newest first. Every finding carries its base rate and caveats; private findings are
    returned only to callers allowed private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListFindingsTypeItem] | Unset):
        severity (list[FindingSeverity] | Unset):
        status (list[ListFindingsStatusItem] | Unset):
        region (list[str] | Unset):
        ref (str | Unset): Series reference (Intelligence-Design §4.3):
            `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-
            chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the
            cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}`
            (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily
            news coverage in evidence groups by lean bucket left, center, right, unrated or state); or
            `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social
            mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or
            engagement, every language summed unless a language bucket is given;
            `social:all/{platform}/denominator[/{language}]` is the platform-language denominator;
            complete and paged collection only) Example: series:cftc-cot/067651.disagg.mm_net.
        since (datetime.datetime | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FindingPage | Problem
    """

    return sync_detailed(
        client=client,
        type_=type_,
        severity=severity,
        status=status,
        region=region,
        ref=ref,
        since=since,
        situation_id=situation_id,
        cursor=cursor,
        limit=limit,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    type_: list[ListFindingsTypeItem] | Unset = UNSET,
    severity: list[FindingSeverity] | Unset = UNSET,
    status: list[ListFindingsStatusItem] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    ref: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> Response[FindingPage | Problem]:
    """List analytic findings

     Lists findings newest first. Every finding carries its base rate and caveats; private findings are
    returned only to callers allowed private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListFindingsTypeItem] | Unset):
        severity (list[FindingSeverity] | Unset):
        status (list[ListFindingsStatusItem] | Unset):
        region (list[str] | Unset):
        ref (str | Unset): Series reference (Intelligence-Design §4.3):
            `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-
            chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the
            cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}`
            (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily
            news coverage in evidence groups by lean bucket left, center, right, unrated or state); or
            `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social
            mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or
            engagement, every language summed unless a language bucket is given;
            `social:all/{platform}/denominator[/{language}]` is the platform-language denominator;
            complete and paged collection only) Example: series:cftc-cot/067651.disagg.mm_net.
        since (datetime.datetime | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FindingPage | Problem]
    """

    kwargs = _get_kwargs(
        type_=type_,
        severity=severity,
        status=status,
        region=region,
        ref=ref,
        since=since,
        situation_id=situation_id,
        cursor=cursor,
        limit=limit,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    type_: list[ListFindingsTypeItem] | Unset = UNSET,
    severity: list[FindingSeverity] | Unset = UNSET,
    status: list[ListFindingsStatusItem] | Unset = UNSET,
    region: list[str] | Unset = UNSET,
    ref: str | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    situation_id: str | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 100,
) -> FindingPage | Problem | None:
    """List analytic findings

     Lists findings newest first. Every finding carries its base rate and caveats; private findings are
    returned only to callers allowed private egress.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        type_ (list[ListFindingsTypeItem] | Unset):
        severity (list[FindingSeverity] | Unset):
        status (list[ListFindingsStatusItem] | Unset):
        region (list[str] | Unset):
        ref (str | Unset): Series reference (Intelligence-Design §4.3):
            `series:{sourceId}/{seriesId}` (093/117 series; `series:imf-portwatch-
            chokepoints/{portId}` for PortWatch daily transits),
            `rollup:{hour|day}/{sourceId|*}/{kind}/{region|cell:h3}` (activity rollups; `*` is the
            cross-source event count with canonical events counted once) `rule:{ruleId}/{groupKey}`
            (100 anomaly rule samples) or `rollup:news/{topicId|situationId}/{bucket}` (133: daily
            news coverage in evidence groups by lean bucket left, center, right, unrated or state); or
            `social:{topic|place|entity}:{id}/{platform}/{metric}[/{language}]` (135: hourly social
            mention volume of a scope on one network, metric posts, uniqueAuthors, reposts or
            engagement, every language summed unless a language bucket is given;
            `social:all/{platform}/denominator[/{language}]` is the platform-language denominator;
            complete and paged collection only) Example: series:cftc-cot/067651.disagg.mm_net.
        since (datetime.datetime | Unset):
        situation_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cursor (str | Unset):
        limit (int | Unset):  Default: 100.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FindingPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            type_=type_,
            severity=severity,
            status=status,
            region=region,
            ref=ref,
            since=since,
            situation_id=situation_id,
            cursor=cursor,
            limit=limit,
        )
    ).parsed
