import datetime
from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.alert_page import AlertPage
from ...models.list_alerts_feedback_item import ListAlertsFeedbackItem
from ...models.list_alerts_include_item import ListAlertsIncludeItem
from ...models.list_alerts_min_severity import ListAlertsMinSeverity
from ...models.list_alerts_state_item import ListAlertsStateItem
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    rule_id: UUID | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    kind: list[str] | Unset = UNSET,
    state: list[ListAlertsStateItem] | Unset = UNSET,
    feedback: list[ListAlertsFeedbackItem] | Unset = UNSET,
    min_severity: ListAlertsMinSeverity | Unset = UNSET,
    tag: str | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    include: list[ListAlertsIncludeItem] | Unset = UNSET,
    include_exercise: bool | Unset = False,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_rule_id: str | Unset = UNSET
    if not isinstance(rule_id, Unset):
        json_rule_id = str(rule_id)
    params["ruleId"] = json_rule_id

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params["acknowledged"] = acknowledged

    params["cursor"] = cursor

    params["limit"] = limit

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    json_state: list[str] | Unset = UNSET
    if not isinstance(state, Unset):
        json_state = []
        for state_item_data in state:
            state_item = state_item_data.value
            json_state.append(state_item)

    params["state"] = json_state

    json_feedback: list[str] | Unset = UNSET
    if not isinstance(feedback, Unset):
        json_feedback = []
        for feedback_item_data in feedback:
            feedback_item = feedback_item_data.value
            json_feedback.append(feedback_item)

    params["feedback"] = json_feedback

    json_min_severity: str | Unset = UNSET
    if not isinstance(min_severity, Unset):
        json_min_severity = min_severity.value

    params["minSeverity"] = json_min_severity

    params["tag"] = tag

    params["includeSuppressed"] = include_suppressed

    json_country: list[str] | Unset = UNSET
    if not isinstance(country, Unset):
        json_country = country

    params["country"] = json_country

    json_admin1: list[str] | Unset = UNSET
    if not isinstance(admin1, Unset):
        json_admin1 = admin1

    params["admin1"] = json_admin1

    json_county: list[str] | Unset = UNSET
    if not isinstance(county, Unset):
        json_county = county

    params["county"] = json_county

    json_include: list[str] | Unset = UNSET
    if not isinstance(include, Unset):
        json_include = []
        for include_item_data in include:
            include_item = include_item_data.value
            json_include.append(include_item)

    params["include"] = json_include

    params["includeExercise"] = include_exercise

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/alerts",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> AlertPage | Problem | None:
    if response.status_code == 200:
        response_200 = AlertPage.from_dict(response.json())

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[AlertPage | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    rule_id: UUID | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    kind: list[str] | Unset = UNSET,
    state: list[ListAlertsStateItem] | Unset = UNSET,
    feedback: list[ListAlertsFeedbackItem] | Unset = UNSET,
    min_severity: ListAlertsMinSeverity | Unset = UNSET,
    tag: str | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    include: list[ListAlertsIncludeItem] | Unset = UNSET,
    include_exercise: bool | Unset = False,
) -> Response[AlertPage | Problem]:
    """List alerts newest first

     Alerts newest first, keyset on id. Suppressed alerts are hidden unless includeSuppressed. Anonymous
    callers (security.openReads) never see alerts of private sources.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID | Unset):
        source (list[str] | Unset):
        since (datetime.datetime | Unset):
        acknowledged (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        kind (list[str] | Unset):
        state (list[ListAlertsStateItem] | Unset):
        feedback (list[ListAlertsFeedbackItem] | Unset):
        min_severity (ListAlertsMinSeverity | Unset):
        tag (str | Unset):
        include_suppressed (bool | Unset):  Default: False.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        include (list[ListAlertsIncludeItem] | Unset):
        include_exercise (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AlertPage | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        source=source,
        since=since,
        acknowledged=acknowledged,
        cursor=cursor,
        limit=limit,
        kind=kind,
        state=state,
        feedback=feedback,
        min_severity=min_severity,
        tag=tag,
        include_suppressed=include_suppressed,
        country=country,
        admin1=admin1,
        county=county,
        include=include,
        include_exercise=include_exercise,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    rule_id: UUID | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    kind: list[str] | Unset = UNSET,
    state: list[ListAlertsStateItem] | Unset = UNSET,
    feedback: list[ListAlertsFeedbackItem] | Unset = UNSET,
    min_severity: ListAlertsMinSeverity | Unset = UNSET,
    tag: str | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    include: list[ListAlertsIncludeItem] | Unset = UNSET,
    include_exercise: bool | Unset = False,
) -> AlertPage | Problem | None:
    """List alerts newest first

     Alerts newest first, keyset on id. Suppressed alerts are hidden unless includeSuppressed. Anonymous
    callers (security.openReads) never see alerts of private sources.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID | Unset):
        source (list[str] | Unset):
        since (datetime.datetime | Unset):
        acknowledged (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        kind (list[str] | Unset):
        state (list[ListAlertsStateItem] | Unset):
        feedback (list[ListAlertsFeedbackItem] | Unset):
        min_severity (ListAlertsMinSeverity | Unset):
        tag (str | Unset):
        include_suppressed (bool | Unset):  Default: False.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        include (list[ListAlertsIncludeItem] | Unset):
        include_exercise (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AlertPage | Problem
    """

    return sync_detailed(
        client=client,
        rule_id=rule_id,
        source=source,
        since=since,
        acknowledged=acknowledged,
        cursor=cursor,
        limit=limit,
        kind=kind,
        state=state,
        feedback=feedback,
        min_severity=min_severity,
        tag=tag,
        include_suppressed=include_suppressed,
        country=country,
        admin1=admin1,
        county=county,
        include=include,
        include_exercise=include_exercise,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    rule_id: UUID | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    kind: list[str] | Unset = UNSET,
    state: list[ListAlertsStateItem] | Unset = UNSET,
    feedback: list[ListAlertsFeedbackItem] | Unset = UNSET,
    min_severity: ListAlertsMinSeverity | Unset = UNSET,
    tag: str | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    include: list[ListAlertsIncludeItem] | Unset = UNSET,
    include_exercise: bool | Unset = False,
) -> Response[AlertPage | Problem]:
    """List alerts newest first

     Alerts newest first, keyset on id. Suppressed alerts are hidden unless includeSuppressed. Anonymous
    callers (security.openReads) never see alerts of private sources.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID | Unset):
        source (list[str] | Unset):
        since (datetime.datetime | Unset):
        acknowledged (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        kind (list[str] | Unset):
        state (list[ListAlertsStateItem] | Unset):
        feedback (list[ListAlertsFeedbackItem] | Unset):
        min_severity (ListAlertsMinSeverity | Unset):
        tag (str | Unset):
        include_suppressed (bool | Unset):  Default: False.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        include (list[ListAlertsIncludeItem] | Unset):
        include_exercise (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[AlertPage | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        source=source,
        since=since,
        acknowledged=acknowledged,
        cursor=cursor,
        limit=limit,
        kind=kind,
        state=state,
        feedback=feedback,
        min_severity=min_severity,
        tag=tag,
        include_suppressed=include_suppressed,
        country=country,
        admin1=admin1,
        county=county,
        include=include,
        include_exercise=include_exercise,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    rule_id: UUID | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    since: datetime.datetime | Unset = UNSET,
    acknowledged: bool | Unset = UNSET,
    cursor: str | Unset = UNSET,
    limit: int | Unset = 500,
    kind: list[str] | Unset = UNSET,
    state: list[ListAlertsStateItem] | Unset = UNSET,
    feedback: list[ListAlertsFeedbackItem] | Unset = UNSET,
    min_severity: ListAlertsMinSeverity | Unset = UNSET,
    tag: str | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    country: list[str] | Unset = UNSET,
    admin1: list[str] | Unset = UNSET,
    county: list[str] | Unset = UNSET,
    include: list[ListAlertsIncludeItem] | Unset = UNSET,
    include_exercise: bool | Unset = False,
) -> AlertPage | Problem | None:
    """List alerts newest first

     Alerts newest first, keyset on id. Suppressed alerts are hidden unless includeSuppressed. Anonymous
    callers (security.openReads) never see alerts of private sources.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (UUID | Unset):
        source (list[str] | Unset):
        since (datetime.datetime | Unset):
        acknowledged (bool | Unset):
        cursor (str | Unset):
        limit (int | Unset):  Default: 500.
        kind (list[str] | Unset):
        state (list[ListAlertsStateItem] | Unset):
        feedback (list[ListAlertsFeedbackItem] | Unset):
        min_severity (ListAlertsMinSeverity | Unset):
        tag (str | Unset):
        include_suppressed (bool | Unset):  Default: False.
        country (list[str] | Unset):
        admin1 (list[str] | Unset):
        county (list[str] | Unset):
        include (list[ListAlertsIncludeItem] | Unset):
        include_exercise (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        AlertPage | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            rule_id=rule_id,
            source=source,
            since=since,
            acknowledged=acknowledged,
            cursor=cursor,
            limit=limit,
            kind=kind,
            state=state,
            feedback=feedback,
            min_severity=min_severity,
            tag=tag,
            include_suppressed=include_suppressed,
            country=country,
            admin1=admin1,
            county=county,
            include=include,
            include_exercise=include_exercise,
        )
    ).parsed
