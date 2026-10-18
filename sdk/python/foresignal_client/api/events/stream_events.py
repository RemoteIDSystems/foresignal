from http import HTTPStatus
from typing import Any
from uuid import UUID

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.situation_theme import SituationTheme
from ...models.stream_events_alerts_min_severity import StreamEventsAlertsMinSeverity
from ...models.stream_events_licence_policy_item import StreamEventsLicencePolicyItem
from ...models.stream_events_situations_status import StreamEventsSituationsStatus
from ...models.stream_events_workspace_client_kind import (
    StreamEventsWorkspaceClientKind,
)
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    topics: list[str] | Unset = UNSET,
    changes_source: list[str] | Unset = UNSET,
    changes_kind: list[str] | Unset = UNSET,
    changes_external_id: list[str] | Unset = UNSET,
    changes_bbox: str | Unset = UNSET,
    changes_near: str | Unset = UNSET,
    changes_radius_km: float | Unset = UNSET,
    changes_live: bool | Unset = True,
    changes_touches: bool | Unset = False,
    changes_payload: bool | Unset = False,
    alerts_min_severity: StreamEventsAlertsMinSeverity | Unset = UNSET,
    alerts_rule_id: list[UUID] | Unset = UNSET,
    alerts_source: list[str] | Unset = UNSET,
    alerts_kind: list[str] | Unset = UNSET,
    alerts_include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    changes_place: list[str] | Unset = UNSET,
    changes_view: str | Unset = UNSET,
    changes_in_watchlist: UUID | Unset = UNSET,
    alerts_include_exercise: bool | Unset = False,
    licence_policy: list[StreamEventsLicencePolicyItem] | Unset = UNSET,
    workspace_workspace_id: str | Unset = UNSET,
    workspace_client_id: str | Unset = UNSET,
    workspace_client_kind: StreamEventsWorkspaceClientKind | Unset = UNSET,
    chronicle_chronicle_id: list[str] | Unset = UNSET,
    last_event_id_query: str | Unset = UNSET,
    situations_status: StreamEventsSituationsStatus | Unset = UNSET,
    situations_theme: SituationTheme | Unset = UNSET,
    external_source_id: str | Unset = UNSET,
    last_event_id_header: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}
    if not isinstance(last_event_id_header, Unset):
        headers["Last-Event-ID"] = last_event_id_header

    if not isinstance(foresignal_licence_policy, Unset):
        headers["Foresignal-Licence-Policy"] = foresignal_licence_policy

    params: dict[str, Any] = {}

    json_topics: list[str] | Unset = UNSET
    if not isinstance(topics, Unset):
        json_topics = topics

    params["topics"] = json_topics

    json_changes_source: list[str] | Unset = UNSET
    if not isinstance(changes_source, Unset):
        json_changes_source = changes_source

    params["changes.source"] = json_changes_source

    json_changes_kind: list[str] | Unset = UNSET
    if not isinstance(changes_kind, Unset):
        json_changes_kind = changes_kind

    params["changes.kind"] = json_changes_kind

    json_changes_external_id: list[str] | Unset = UNSET
    if not isinstance(changes_external_id, Unset):
        json_changes_external_id = changes_external_id

    params["changes.externalId"] = json_changes_external_id

    params["changes.bbox"] = changes_bbox

    params["changes.near"] = changes_near

    params["changes.radiusKm"] = changes_radius_km

    params["changes.live"] = changes_live

    params["changes.touches"] = changes_touches

    params["changes.payload"] = changes_payload

    json_alerts_min_severity: str | Unset = UNSET
    if not isinstance(alerts_min_severity, Unset):
        json_alerts_min_severity = alerts_min_severity.value

    params["alerts.minSeverity"] = json_alerts_min_severity

    json_alerts_rule_id: list[str] | Unset = UNSET
    if not isinstance(alerts_rule_id, Unset):
        json_alerts_rule_id = []
        for alerts_rule_id_item_data in alerts_rule_id:
            alerts_rule_id_item = str(alerts_rule_id_item_data)
            json_alerts_rule_id.append(alerts_rule_id_item)

    params["alerts.ruleId"] = json_alerts_rule_id

    json_alerts_source: list[str] | Unset = UNSET
    if not isinstance(alerts_source, Unset):
        json_alerts_source = alerts_source

    params["alerts.source"] = json_alerts_source

    json_alerts_kind: list[str] | Unset = UNSET
    if not isinstance(alerts_kind, Unset):
        json_alerts_kind = alerts_kind

    params["alerts.kind"] = json_alerts_kind

    params["alerts.includeSuppressed"] = alerts_include_suppressed

    params["ticket"] = ticket

    json_changes_place: list[str] | Unset = UNSET
    if not isinstance(changes_place, Unset):
        json_changes_place = changes_place

    params["changes.place"] = json_changes_place

    params["changes.view"] = changes_view

    json_changes_in_watchlist: str | Unset = UNSET
    if not isinstance(changes_in_watchlist, Unset):
        json_changes_in_watchlist = str(changes_in_watchlist)
    params["changes.inWatchlist"] = json_changes_in_watchlist

    params["alerts.includeExercise"] = alerts_include_exercise

    json_licence_policy: list[str] | Unset = UNSET
    if not isinstance(licence_policy, Unset):
        json_licence_policy = []
        for licence_policy_item_data in licence_policy:
            licence_policy_item = licence_policy_item_data.value
            json_licence_policy.append(licence_policy_item)

    params["licencePolicy"] = json_licence_policy

    params["workspace.workspaceId"] = workspace_workspace_id

    params["workspace.clientId"] = workspace_client_id

    json_workspace_client_kind: str | Unset = UNSET
    if not isinstance(workspace_client_kind, Unset):
        json_workspace_client_kind = workspace_client_kind.value

    params["workspace.clientKind"] = json_workspace_client_kind

    json_chronicle_chronicle_id: list[str] | Unset = UNSET
    if not isinstance(chronicle_chronicle_id, Unset):
        json_chronicle_chronicle_id = chronicle_chronicle_id

    params["chronicle.chronicleId"] = json_chronicle_chronicle_id

    params["lastEventId"] = last_event_id_query

    json_situations_status: str | Unset = UNSET
    if not isinstance(situations_status, Unset):
        json_situations_status = situations_status.value

    params["situations.status"] = json_situations_status

    json_situations_theme: str | Unset = UNSET
    if not isinstance(situations_theme, Unset):
        json_situations_theme = situations_theme.value

    params["situations.theme"] = json_situations_theme

    params["external.sourceId"] = external_source_id

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/events/stream",
        "params": params,
    }

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | None:
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
) -> Response[Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    topics: list[str] | Unset = UNSET,
    changes_source: list[str] | Unset = UNSET,
    changes_kind: list[str] | Unset = UNSET,
    changes_external_id: list[str] | Unset = UNSET,
    changes_bbox: str | Unset = UNSET,
    changes_near: str | Unset = UNSET,
    changes_radius_km: float | Unset = UNSET,
    changes_live: bool | Unset = True,
    changes_touches: bool | Unset = False,
    changes_payload: bool | Unset = False,
    alerts_min_severity: StreamEventsAlertsMinSeverity | Unset = UNSET,
    alerts_rule_id: list[UUID] | Unset = UNSET,
    alerts_source: list[str] | Unset = UNSET,
    alerts_kind: list[str] | Unset = UNSET,
    alerts_include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    changes_place: list[str] | Unset = UNSET,
    changes_view: str | Unset = UNSET,
    changes_in_watchlist: UUID | Unset = UNSET,
    alerts_include_exercise: bool | Unset = False,
    licence_policy: list[StreamEventsLicencePolicyItem] | Unset = UNSET,
    workspace_workspace_id: str | Unset = UNSET,
    workspace_client_id: str | Unset = UNSET,
    workspace_client_kind: StreamEventsWorkspaceClientKind | Unset = UNSET,
    chronicle_chronicle_id: list[str] | Unset = UNSET,
    last_event_id_query: str | Unset = UNSET,
    situations_status: StreamEventsSituationsStatus | Unset = UNSET,
    situations_theme: SituationTheme | Unset = UNSET,
    external_source_id: str | Unset = UNSET,
    last_event_id_header: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Problem]:
    """Multiplexed, re-filterable event stream (SSE)

     One SSE connection for several topics (changes, alerts, notifications, system, and topics added
    later). The first event is `hello {streamId, topics, watermarkSeq, filterVersion}`; id-bearing
    events carry a composite `id:` holding the latest cursor of every subscribed topic, and `Last-Event-
    ID` resumes each topic. Swap the filter without reconnecting via PATCH
    `/v1/events/stream/{streamId}/filter` (debounce map pans 300–500 ms); live, touch and control events
    never carry an id.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topics (list[str] | Unset):
        changes_source (list[str] | Unset):
        changes_kind (list[str] | Unset):
        changes_external_id (list[str] | Unset):
        changes_bbox (str | Unset):
        changes_near (str | Unset):  Example: -96.80,32.78.
        changes_radius_km (float | Unset):
        changes_live (bool | Unset):  Default: True.
        changes_touches (bool | Unset):  Default: False.
        changes_payload (bool | Unset):  Default: False.
        alerts_min_severity (StreamEventsAlertsMinSeverity | Unset):
        alerts_rule_id (list[UUID] | Unset):
        alerts_source (list[str] | Unset):
        alerts_kind (list[str] | Unset):
        alerts_include_suppressed (bool | Unset):  Default: False.
        ticket (str | Unset):
        changes_place (list[str] | Unset):
        changes_view (str | Unset):
        changes_in_watchlist (UUID | Unset):
        alerts_include_exercise (bool | Unset):  Default: False.
        licence_policy (list[StreamEventsLicencePolicyItem] | Unset):
        workspace_workspace_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        workspace_client_id (str | Unset):
        workspace_client_kind (StreamEventsWorkspaceClientKind | Unset):
        chronicle_chronicle_id (list[str] | Unset):
        last_event_id_query (str | Unset):
        situations_status (StreamEventsSituationsStatus | Unset):
        situations_theme (SituationTheme | Unset): Situation theme (118 `ThemeTable`)
        external_source_id (str | Unset):
        last_event_id_header (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        topics=topics,
        changes_source=changes_source,
        changes_kind=changes_kind,
        changes_external_id=changes_external_id,
        changes_bbox=changes_bbox,
        changes_near=changes_near,
        changes_radius_km=changes_radius_km,
        changes_live=changes_live,
        changes_touches=changes_touches,
        changes_payload=changes_payload,
        alerts_min_severity=alerts_min_severity,
        alerts_rule_id=alerts_rule_id,
        alerts_source=alerts_source,
        alerts_kind=alerts_kind,
        alerts_include_suppressed=alerts_include_suppressed,
        ticket=ticket,
        changes_place=changes_place,
        changes_view=changes_view,
        changes_in_watchlist=changes_in_watchlist,
        alerts_include_exercise=alerts_include_exercise,
        licence_policy=licence_policy,
        workspace_workspace_id=workspace_workspace_id,
        workspace_client_id=workspace_client_id,
        workspace_client_kind=workspace_client_kind,
        chronicle_chronicle_id=chronicle_chronicle_id,
        last_event_id_query=last_event_id_query,
        situations_status=situations_status,
        situations_theme=situations_theme,
        external_source_id=external_source_id,
        last_event_id_header=last_event_id_header,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    topics: list[str] | Unset = UNSET,
    changes_source: list[str] | Unset = UNSET,
    changes_kind: list[str] | Unset = UNSET,
    changes_external_id: list[str] | Unset = UNSET,
    changes_bbox: str | Unset = UNSET,
    changes_near: str | Unset = UNSET,
    changes_radius_km: float | Unset = UNSET,
    changes_live: bool | Unset = True,
    changes_touches: bool | Unset = False,
    changes_payload: bool | Unset = False,
    alerts_min_severity: StreamEventsAlertsMinSeverity | Unset = UNSET,
    alerts_rule_id: list[UUID] | Unset = UNSET,
    alerts_source: list[str] | Unset = UNSET,
    alerts_kind: list[str] | Unset = UNSET,
    alerts_include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    changes_place: list[str] | Unset = UNSET,
    changes_view: str | Unset = UNSET,
    changes_in_watchlist: UUID | Unset = UNSET,
    alerts_include_exercise: bool | Unset = False,
    licence_policy: list[StreamEventsLicencePolicyItem] | Unset = UNSET,
    workspace_workspace_id: str | Unset = UNSET,
    workspace_client_id: str | Unset = UNSET,
    workspace_client_kind: StreamEventsWorkspaceClientKind | Unset = UNSET,
    chronicle_chronicle_id: list[str] | Unset = UNSET,
    last_event_id_query: str | Unset = UNSET,
    situations_status: StreamEventsSituationsStatus | Unset = UNSET,
    situations_theme: SituationTheme | Unset = UNSET,
    external_source_id: str | Unset = UNSET,
    last_event_id_header: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Problem | None:
    """Multiplexed, re-filterable event stream (SSE)

     One SSE connection for several topics (changes, alerts, notifications, system, and topics added
    later). The first event is `hello {streamId, topics, watermarkSeq, filterVersion}`; id-bearing
    events carry a composite `id:` holding the latest cursor of every subscribed topic, and `Last-Event-
    ID` resumes each topic. Swap the filter without reconnecting via PATCH
    `/v1/events/stream/{streamId}/filter` (debounce map pans 300–500 ms); live, touch and control events
    never carry an id.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topics (list[str] | Unset):
        changes_source (list[str] | Unset):
        changes_kind (list[str] | Unset):
        changes_external_id (list[str] | Unset):
        changes_bbox (str | Unset):
        changes_near (str | Unset):  Example: -96.80,32.78.
        changes_radius_km (float | Unset):
        changes_live (bool | Unset):  Default: True.
        changes_touches (bool | Unset):  Default: False.
        changes_payload (bool | Unset):  Default: False.
        alerts_min_severity (StreamEventsAlertsMinSeverity | Unset):
        alerts_rule_id (list[UUID] | Unset):
        alerts_source (list[str] | Unset):
        alerts_kind (list[str] | Unset):
        alerts_include_suppressed (bool | Unset):  Default: False.
        ticket (str | Unset):
        changes_place (list[str] | Unset):
        changes_view (str | Unset):
        changes_in_watchlist (UUID | Unset):
        alerts_include_exercise (bool | Unset):  Default: False.
        licence_policy (list[StreamEventsLicencePolicyItem] | Unset):
        workspace_workspace_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        workspace_client_id (str | Unset):
        workspace_client_kind (StreamEventsWorkspaceClientKind | Unset):
        chronicle_chronicle_id (list[str] | Unset):
        last_event_id_query (str | Unset):
        situations_status (StreamEventsSituationsStatus | Unset):
        situations_theme (SituationTheme | Unset): Situation theme (118 `ThemeTable`)
        external_source_id (str | Unset):
        last_event_id_header (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return sync_detailed(
        client=client,
        topics=topics,
        changes_source=changes_source,
        changes_kind=changes_kind,
        changes_external_id=changes_external_id,
        changes_bbox=changes_bbox,
        changes_near=changes_near,
        changes_radius_km=changes_radius_km,
        changes_live=changes_live,
        changes_touches=changes_touches,
        changes_payload=changes_payload,
        alerts_min_severity=alerts_min_severity,
        alerts_rule_id=alerts_rule_id,
        alerts_source=alerts_source,
        alerts_kind=alerts_kind,
        alerts_include_suppressed=alerts_include_suppressed,
        ticket=ticket,
        changes_place=changes_place,
        changes_view=changes_view,
        changes_in_watchlist=changes_in_watchlist,
        alerts_include_exercise=alerts_include_exercise,
        licence_policy=licence_policy,
        workspace_workspace_id=workspace_workspace_id,
        workspace_client_id=workspace_client_id,
        workspace_client_kind=workspace_client_kind,
        chronicle_chronicle_id=chronicle_chronicle_id,
        last_event_id_query=last_event_id_query,
        situations_status=situations_status,
        situations_theme=situations_theme,
        external_source_id=external_source_id,
        last_event_id_header=last_event_id_header,
        foresignal_licence_policy=foresignal_licence_policy,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    topics: list[str] | Unset = UNSET,
    changes_source: list[str] | Unset = UNSET,
    changes_kind: list[str] | Unset = UNSET,
    changes_external_id: list[str] | Unset = UNSET,
    changes_bbox: str | Unset = UNSET,
    changes_near: str | Unset = UNSET,
    changes_radius_km: float | Unset = UNSET,
    changes_live: bool | Unset = True,
    changes_touches: bool | Unset = False,
    changes_payload: bool | Unset = False,
    alerts_min_severity: StreamEventsAlertsMinSeverity | Unset = UNSET,
    alerts_rule_id: list[UUID] | Unset = UNSET,
    alerts_source: list[str] | Unset = UNSET,
    alerts_kind: list[str] | Unset = UNSET,
    alerts_include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    changes_place: list[str] | Unset = UNSET,
    changes_view: str | Unset = UNSET,
    changes_in_watchlist: UUID | Unset = UNSET,
    alerts_include_exercise: bool | Unset = False,
    licence_policy: list[StreamEventsLicencePolicyItem] | Unset = UNSET,
    workspace_workspace_id: str | Unset = UNSET,
    workspace_client_id: str | Unset = UNSET,
    workspace_client_kind: StreamEventsWorkspaceClientKind | Unset = UNSET,
    chronicle_chronicle_id: list[str] | Unset = UNSET,
    last_event_id_query: str | Unset = UNSET,
    situations_status: StreamEventsSituationsStatus | Unset = UNSET,
    situations_theme: SituationTheme | Unset = UNSET,
    external_source_id: str | Unset = UNSET,
    last_event_id_header: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Response[Problem]:
    """Multiplexed, re-filterable event stream (SSE)

     One SSE connection for several topics (changes, alerts, notifications, system, and topics added
    later). The first event is `hello {streamId, topics, watermarkSeq, filterVersion}`; id-bearing
    events carry a composite `id:` holding the latest cursor of every subscribed topic, and `Last-Event-
    ID` resumes each topic. Swap the filter without reconnecting via PATCH
    `/v1/events/stream/{streamId}/filter` (debounce map pans 300–500 ms); live, touch and control events
    never carry an id.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topics (list[str] | Unset):
        changes_source (list[str] | Unset):
        changes_kind (list[str] | Unset):
        changes_external_id (list[str] | Unset):
        changes_bbox (str | Unset):
        changes_near (str | Unset):  Example: -96.80,32.78.
        changes_radius_km (float | Unset):
        changes_live (bool | Unset):  Default: True.
        changes_touches (bool | Unset):  Default: False.
        changes_payload (bool | Unset):  Default: False.
        alerts_min_severity (StreamEventsAlertsMinSeverity | Unset):
        alerts_rule_id (list[UUID] | Unset):
        alerts_source (list[str] | Unset):
        alerts_kind (list[str] | Unset):
        alerts_include_suppressed (bool | Unset):  Default: False.
        ticket (str | Unset):
        changes_place (list[str] | Unset):
        changes_view (str | Unset):
        changes_in_watchlist (UUID | Unset):
        alerts_include_exercise (bool | Unset):  Default: False.
        licence_policy (list[StreamEventsLicencePolicyItem] | Unset):
        workspace_workspace_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        workspace_client_id (str | Unset):
        workspace_client_kind (StreamEventsWorkspaceClientKind | Unset):
        chronicle_chronicle_id (list[str] | Unset):
        last_event_id_query (str | Unset):
        situations_status (StreamEventsSituationsStatus | Unset):
        situations_theme (SituationTheme | Unset): Situation theme (118 `ThemeTable`)
        external_source_id (str | Unset):
        last_event_id_header (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem]
    """

    kwargs = _get_kwargs(
        topics=topics,
        changes_source=changes_source,
        changes_kind=changes_kind,
        changes_external_id=changes_external_id,
        changes_bbox=changes_bbox,
        changes_near=changes_near,
        changes_radius_km=changes_radius_km,
        changes_live=changes_live,
        changes_touches=changes_touches,
        changes_payload=changes_payload,
        alerts_min_severity=alerts_min_severity,
        alerts_rule_id=alerts_rule_id,
        alerts_source=alerts_source,
        alerts_kind=alerts_kind,
        alerts_include_suppressed=alerts_include_suppressed,
        ticket=ticket,
        changes_place=changes_place,
        changes_view=changes_view,
        changes_in_watchlist=changes_in_watchlist,
        alerts_include_exercise=alerts_include_exercise,
        licence_policy=licence_policy,
        workspace_workspace_id=workspace_workspace_id,
        workspace_client_id=workspace_client_id,
        workspace_client_kind=workspace_client_kind,
        chronicle_chronicle_id=chronicle_chronicle_id,
        last_event_id_query=last_event_id_query,
        situations_status=situations_status,
        situations_theme=situations_theme,
        external_source_id=external_source_id,
        last_event_id_header=last_event_id_header,
        foresignal_licence_policy=foresignal_licence_policy,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    topics: list[str] | Unset = UNSET,
    changes_source: list[str] | Unset = UNSET,
    changes_kind: list[str] | Unset = UNSET,
    changes_external_id: list[str] | Unset = UNSET,
    changes_bbox: str | Unset = UNSET,
    changes_near: str | Unset = UNSET,
    changes_radius_km: float | Unset = UNSET,
    changes_live: bool | Unset = True,
    changes_touches: bool | Unset = False,
    changes_payload: bool | Unset = False,
    alerts_min_severity: StreamEventsAlertsMinSeverity | Unset = UNSET,
    alerts_rule_id: list[UUID] | Unset = UNSET,
    alerts_source: list[str] | Unset = UNSET,
    alerts_kind: list[str] | Unset = UNSET,
    alerts_include_suppressed: bool | Unset = False,
    ticket: str | Unset = UNSET,
    changes_place: list[str] | Unset = UNSET,
    changes_view: str | Unset = UNSET,
    changes_in_watchlist: UUID | Unset = UNSET,
    alerts_include_exercise: bool | Unset = False,
    licence_policy: list[StreamEventsLicencePolicyItem] | Unset = UNSET,
    workspace_workspace_id: str | Unset = UNSET,
    workspace_client_id: str | Unset = UNSET,
    workspace_client_kind: StreamEventsWorkspaceClientKind | Unset = UNSET,
    chronicle_chronicle_id: list[str] | Unset = UNSET,
    last_event_id_query: str | Unset = UNSET,
    situations_status: StreamEventsSituationsStatus | Unset = UNSET,
    situations_theme: SituationTheme | Unset = UNSET,
    external_source_id: str | Unset = UNSET,
    last_event_id_header: str | Unset = UNSET,
    foresignal_licence_policy: str | Unset = UNSET,
) -> Problem | None:
    """Multiplexed, re-filterable event stream (SSE)

     One SSE connection for several topics (changes, alerts, notifications, system, and topics added
    later). The first event is `hello {streamId, topics, watermarkSeq, filterVersion}`; id-bearing
    events carry a composite `id:` holding the latest cursor of every subscribed topic, and `Last-Event-
    ID` resumes each topic. Swap the filter without reconnecting via PATCH
    `/v1/events/stream/{streamId}/filter` (debounce map pans 300–500 ms); live, touch and control events
    never carry an id.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        topics (list[str] | Unset):
        changes_source (list[str] | Unset):
        changes_kind (list[str] | Unset):
        changes_external_id (list[str] | Unset):
        changes_bbox (str | Unset):
        changes_near (str | Unset):  Example: -96.80,32.78.
        changes_radius_km (float | Unset):
        changes_live (bool | Unset):  Default: True.
        changes_touches (bool | Unset):  Default: False.
        changes_payload (bool | Unset):  Default: False.
        alerts_min_severity (StreamEventsAlertsMinSeverity | Unset):
        alerts_rule_id (list[UUID] | Unset):
        alerts_source (list[str] | Unset):
        alerts_kind (list[str] | Unset):
        alerts_include_suppressed (bool | Unset):  Default: False.
        ticket (str | Unset):
        changes_place (list[str] | Unset):
        changes_view (str | Unset):
        changes_in_watchlist (UUID | Unset):
        alerts_include_exercise (bool | Unset):  Default: False.
        licence_policy (list[StreamEventsLicencePolicyItem] | Unset):
        workspace_workspace_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        workspace_client_id (str | Unset):
        workspace_client_kind (StreamEventsWorkspaceClientKind | Unset):
        chronicle_chronicle_id (list[str] | Unset):
        last_event_id_query (str | Unset):
        situations_status (StreamEventsSituationsStatus | Unset):
        situations_theme (SituationTheme | Unset): Situation theme (118 `ThemeTable`)
        external_source_id (str | Unset):
        last_event_id_header (str | Unset):
        foresignal_licence_policy (str | Unset):  Example: no-nc, no-sa.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            topics=topics,
            changes_source=changes_source,
            changes_kind=changes_kind,
            changes_external_id=changes_external_id,
            changes_bbox=changes_bbox,
            changes_near=changes_near,
            changes_radius_km=changes_radius_km,
            changes_live=changes_live,
            changes_touches=changes_touches,
            changes_payload=changes_payload,
            alerts_min_severity=alerts_min_severity,
            alerts_rule_id=alerts_rule_id,
            alerts_source=alerts_source,
            alerts_kind=alerts_kind,
            alerts_include_suppressed=alerts_include_suppressed,
            ticket=ticket,
            changes_place=changes_place,
            changes_view=changes_view,
            changes_in_watchlist=changes_in_watchlist,
            alerts_include_exercise=alerts_include_exercise,
            licence_policy=licence_policy,
            workspace_workspace_id=workspace_workspace_id,
            workspace_client_id=workspace_client_id,
            workspace_client_kind=workspace_client_kind,
            chronicle_chronicle_id=chronicle_chronicle_id,
            last_event_id_query=last_event_id_query,
            situations_status=situations_status,
            situations_theme=situations_theme,
            external_source_id=external_source_id,
            last_event_id_header=last_event_id_header,
            foresignal_licence_policy=foresignal_licence_policy,
        )
    ).parsed
