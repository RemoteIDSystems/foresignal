from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.alert import Alert
from ...models.alert_event import AlertEvent
from ...models.heartbeat_event import HeartbeatEvent
from ...models.problem import Problem
from ...models.reset_event import ResetEvent
from ...models.stream_alerts_min_severity import StreamAlertsMinSeverity
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    rule_id: list[UUID] | Unset = UNSET,
    min_severity: StreamAlertsMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    include_exercise: bool | Unset = False,
    last_event_id: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(last_event_id, Unset):
        headers["Last-Event-ID"] = last_event_id

    params: dict[str, Any] = {}

    json_rule_id: list[str] | Unset = UNSET
    if not isinstance(rule_id, Unset):
        json_rule_id = []
        for rule_id_item_data in rule_id:
            rule_id_item = str(rule_id_item_data)
            json_rule_id.append(rule_id_item)

    params["ruleId"] = json_rule_id

    json_min_severity: str | Unset = UNSET
    if not isinstance(min_severity, Unset):
        json_min_severity = min_severity.value

    params["minSeverity"] = json_min_severity

    json_source: list[str] | Unset = UNSET
    if not isinstance(source, Unset):
        json_source = source

    params["source"] = json_source

    json_kind: list[str] | Unset = UNSET
    if not isinstance(kind, Unset):
        json_kind = kind

    params["kind"] = json_kind

    params["includeSuppressed"] = include_suppressed

    params["ticket"] = ticket

    params["includeExercise"] = include_exercise

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/alerts/stream",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem | None:
    if response.status_code == 200:

        def _parse_response_200(
            data: object,
        ) -> Alert | AlertEvent | HeartbeatEvent | ResetEvent:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_0 = AlertEvent.from_dict(data)

                return response_200_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_1 = ResetEvent.from_dict(data)

                return response_200_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                response_200_type_2 = HeartbeatEvent.from_dict(data)

                return response_200_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            response_200_type_3 = Alert.from_dict(data)

            return response_200_type_3

        response_200 = _parse_response_200(response.text)

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

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if response.status_code == 503:
        response_503 = Problem.from_dict(response.json())

        return response_503

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    rule_id: list[UUID] | Unset = UNSET,
    min_severity: StreamAlertsMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    include_exercise: bool | Unset = False,
    last_event_id: str | Unset = UNSET,
) -> Response[Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem]:
    """Live alert stream (SSE)

     Alert lifecycle as SSE: `alert` (fired), `alert.updated` and `alert.resolved`, data AlertEvent,
    `id:` = alert rev. Resume replays rows with rev > Last-Event-ID in their current state; clients
    upsert by alert id. A malformed Last-Event-ID is 400 INVALID_CURSOR. Anonymous callers never see
    alerts of private sources. `alert.enriched` (030, data Alert) is an update of the same alert with
    its enrichment summary; clients treat it as an upsert by alert id. A replay sends each alert once in
    its current state, summary included.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (list[UUID] | Unset):
        min_severity (StreamAlertsMinSeverity | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        include_suppressed (bool | Unset):  Default: False.
        ticket (str | Unset):
        include_exercise (bool | Unset):  Default: False.
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        min_severity=min_severity,
        source=source,
        kind=kind,
        include_suppressed=include_suppressed,
        ticket=ticket,
        include_exercise=include_exercise,
        last_event_id=last_event_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    rule_id: list[UUID] | Unset = UNSET,
    min_severity: StreamAlertsMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    include_exercise: bool | Unset = False,
    last_event_id: str | Unset = UNSET,
) -> Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem | None:
    """Live alert stream (SSE)

     Alert lifecycle as SSE: `alert` (fired), `alert.updated` and `alert.resolved`, data AlertEvent,
    `id:` = alert rev. Resume replays rows with rev > Last-Event-ID in their current state; clients
    upsert by alert id. A malformed Last-Event-ID is 400 INVALID_CURSOR. Anonymous callers never see
    alerts of private sources. `alert.enriched` (030, data Alert) is an update of the same alert with
    its enrichment summary; clients treat it as an upsert by alert id. A replay sends each alert once in
    its current state, summary included.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (list[UUID] | Unset):
        min_severity (StreamAlertsMinSeverity | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        include_suppressed (bool | Unset):  Default: False.
        ticket (str | Unset):
        include_exercise (bool | Unset):  Default: False.
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem
    """

    return sync_detailed(
        client=client,
        rule_id=rule_id,
        min_severity=min_severity,
        source=source,
        kind=kind,
        include_suppressed=include_suppressed,
        ticket=ticket,
        include_exercise=include_exercise,
        last_event_id=last_event_id,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    rule_id: list[UUID] | Unset = UNSET,
    min_severity: StreamAlertsMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    include_exercise: bool | Unset = False,
    last_event_id: str | Unset = UNSET,
) -> Response[Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem]:
    """Live alert stream (SSE)

     Alert lifecycle as SSE: `alert` (fired), `alert.updated` and `alert.resolved`, data AlertEvent,
    `id:` = alert rev. Resume replays rows with rev > Last-Event-ID in their current state; clients
    upsert by alert id. A malformed Last-Event-ID is 400 INVALID_CURSOR. Anonymous callers never see
    alerts of private sources. `alert.enriched` (030, data Alert) is an update of the same alert with
    its enrichment summary; clients treat it as an upsert by alert id. A replay sends each alert once in
    its current state, summary included.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (list[UUID] | Unset):
        min_severity (StreamAlertsMinSeverity | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        include_suppressed (bool | Unset):  Default: False.
        ticket (str | Unset):
        include_exercise (bool | Unset):  Default: False.
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem]
    """

    kwargs = _get_kwargs(
        rule_id=rule_id,
        min_severity=min_severity,
        source=source,
        kind=kind,
        include_suppressed=include_suppressed,
        ticket=ticket,
        include_exercise=include_exercise,
        last_event_id=last_event_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    rule_id: list[UUID] | Unset = UNSET,
    min_severity: StreamAlertsMinSeverity | Unset = UNSET,
    source: list[str] | Unset = UNSET,
    kind: list[str] | Unset = UNSET,
    include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    include_exercise: bool | Unset = False,
    last_event_id: str | Unset = UNSET,
) -> Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem | None:
    """Live alert stream (SSE)

     Alert lifecycle as SSE: `alert` (fired), `alert.updated` and `alert.resolved`, data AlertEvent,
    `id:` = alert rev. Resume replays rows with rev > Last-Event-ID in their current state; clients
    upsert by alert id. A malformed Last-Event-ID is 400 INVALID_CURSOR. Anonymous callers never see
    alerts of private sources. `alert.enriched` (030, data Alert) is an update of the same alert with
    its enrichment summary; clients treat it as an upsert by alert id. A replay sends each alert once in
    its current state, summary included.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        rule_id (list[UUID] | Unset):
        min_severity (StreamAlertsMinSeverity | Unset):
        source (list[str] | Unset):
        kind (list[str] | Unset):
        include_suppressed (bool | Unset):  Default: False.
        ticket (str | Unset):
        include_exercise (bool | Unset):  Default: False.
        last_event_id (str | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Alert | AlertEvent | HeartbeatEvent | ResetEvent | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            rule_id=rule_id,
            min_severity=min_severity,
            source=source,
            kind=kind,
            include_suppressed=include_suppressed,
            ticket=ticket,
            include_exercise=include_exercise,
            last_event_id=last_event_id,
        )
    ).parsed
