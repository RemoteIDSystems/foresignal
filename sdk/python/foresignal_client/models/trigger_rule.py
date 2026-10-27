from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.trigger_rule_anchor_status_type_1 import TriggerRuleAnchorStatusType1
from ..models.trigger_rule_anchor_status_type_2_type_1 import (
    TriggerRuleAnchorStatusType2Type1,
)
from ..models.trigger_rule_anchor_status_type_3_type_1 import (
    TriggerRuleAnchorStatusType3Type1,
)
from ..models.trigger_rule_geofence_origin import TriggerRuleGeofenceOrigin
from ..models.trigger_rule_input_cooldown_scope import TriggerRuleInputCooldownScope
from ..models.trigger_rule_input_mode import TriggerRuleInputMode
from ..models.trigger_rule_input_severity import TriggerRuleInputSeverity
from ..models.trigger_rule_input_type import TriggerRuleInputType
from ..models.trigger_rule_input_weather_context import TriggerRuleInputWeatherContext
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.absence_window import AbsenceWindow
    from ..models.active_window import ActiveWindow
    from ..models.anomaly_window import AnomalyWindow
    from ..models.approach_condition import ApproachCondition
    from ..models.auto_resolve import AutoResolve
    from ..models.condition import Condition
    from ..models.count_window import CountWindow
    from ..models.geofence_ref import GeofenceRef
    from ..models.geometry import Geometry
    from ..models.near_type_1 import NearType1
    from ..models.near_type_2 import NearType2
    from ..models.trigger_rule_feedback_counts import TriggerRuleFeedbackCounts
    from ..models.window_status import WindowStatus


T = TypeVar("T", bound="TriggerRule")


@_attrs_define
class TriggerRule:
    """
    Attributes:
        name (str):
        id (UUID):
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
        enabled (bool | Unset):  Default: True.
        source (None | str | Unset): Deprecated alias normalised into `sources` (both given → 400)
        kind (None | str | Unset): Deprecated alias normalised into `kinds` (both given → 400)
        conditions (list[ApproachCondition | Condition] | Unset): Payload conditions and `approach` conditions, ANDed
        geofence (Geometry | None | Unset): Polygon/MultiPolygon, or LineString/MultiLineString with bufferMeters
            (buffered server-side as ST_Buffer(line::geography, bufferMeters)); ≤ 10,000 vertices; fixed, split at the
            antimeridian and stored as MultiPolygon. Mutually exclusive with near. When `geofenceRoute` (a line) is sent
            together with bufferMeters, the route is the input and `geofence` is ignored, so a rule read back can be sent
            back unchanged.
        mode (TriggerRuleInputMode | Unset): match: every matching non-HEARTBEAT version. enter: matches and the
            previous version did not (or was a tombstone, or is new). exit: the previous version matched and this one does
            not or is a tombstone. Edge modes need a geofence, near or a condition. Default: TriggerRuleInputMode.MATCH.
        only_new (bool | Unset):  Default: False.
        cooldown (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 7d.
        message_template (str | Unset): Placeholders {{name}}, {{source}}, {{externalId}}, {{kind}}, {{title}},
            {{severity}}, {{observedAt}}, {{eventTime}}, {{time}}, {{validFrom}}, {{payload.<path>}}; region placeholders
            (040), empty when unknown: {{near.name}}, {{near.km}}, {{near.mi}}, {{near.bearing}} (16-point compass),
            {{near.text}} ("12 km NNE of Austin, TX"; miles with {{near.text|imperial}} or a channel's
            config.units=imperial), {{country}} (ISO2), {{country.name}}, {{admin1}}, {{admin1.name}}, {{county}} (the
            county name, else its FIPS5) and {{sea}} Default: '{{name}}: {{source}}/{{externalId}}'. Example:
            M{{payload.mag}} — {{payload.place}}.
        severity (TriggerRuleInputSeverity | Unset):  Default: TriggerRuleInputSeverity.WARNING.
        enrich (bool | Unset): Add an LLM context summary to each alert (030). Accepted without a chat provider: the
            flag is stored and enrichment is skipped until one is assigned and llm.enrichmentEnabled is on Default: False.
        enrich_radius_km (float | Unset): Enrichment radius (030) Default: 100.0.
        enrich_window (str | Unset): Context window around the alert for enrichment (030): ISO-8601 duration or
            shorthand, 10m to 7d Default: '24h'. Example: 24h.
        tags (list[str] | Unset): Copied to alerts
        sources (list[str] | Unset): Source ids (≤ 50)
        kinds (list[str] | Unset): Kinds (≤ 50)
        buffer_meters (float | None | Unset): Required when geofence is a LineString/MultiLineString: corridor half-
            width (1–50,000 m)
        hysteresis_meters (float | Unset): Edge band for enter/exit geofence and near rules (0–5,000 m, default 0): an
            enter edge needs the position inside the fence shrunk by the band, an exit edge outside the fence grown by it,
            and edges per (rule, entity) alternate, so jitter along a boundary fires nothing. 400
            GEOFENCE_TOO_SMALL_FOR_HYSTERESIS when the shrunk fence is empty.
        near (Any | NearType1 | NearType2 | None | Unset): Places, point or moving anchor (036); materialised into
            geofence
        reappear_as_enter (bool | Unset): Fire enter on REAPPEAR (default true with a geofence, else false)
        cooldown_scope (TriggerRuleInputCooldownScope | Unset):  Default: TriggerRuleInputCooldownScope.RECORD.
        auto_resolve (AutoResolve | bool | None | Unset): true = all defaults on, false/null = off; not allowed on exit
            rules
        max_alerts_per_hour (int | None | Unset): Storm cap per rule (null → triggers.defaultMaxAlertsPerHour, default
            120; 0 = unlimited)
        auto_disable_on_storm (bool | Unset):  Default: False.
        active_from (datetime.datetime | None | Unset):
        active_until (datetime.datetime | None | Unset): Rule is disabled automatically after this time
        active_window (ActiveWindow | None | Unset):
        forecast_horizon (None | str | Unset): Fire on versions whose validFrom ≤ evaluation time + horizon (400
            FEATURE_UNAVAILABLE without validFrom). ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 6h.
        escalation_policy_id (None | Unset | UUID): Escalation policy (033), validated on save
        geofence_ref (GeofenceRef | None | Unset): Region preset instead of a polygon (040); 409 REFERENCE_DATA_MISSING
            when reference data is absent
        weather_context (TriggerRuleInputWeatherContext | Unset): Compute `alert.context.weather` for this rule's
            geolocated alerts (global setting `weather.alertContext`) Default: TriggerRuleInputWeatherContext.AUTO.
        type_ (TriggerRuleInputType | Unset): Rule type; existing rules are `record`. `onlyNew`, `mode` and
            `reappearAsEnter` are rejected for windowed types (400 WINDOW_RULE_INVALID). Default:
            TriggerRuleInputType.RECORD.
        window (AbsenceWindow | AnomalyWindow | CountWindow | None | Unset): Window spec for count, absence and anomaly
            rules; the rule `type` selects the variant
        last_fired_at (datetime.datetime | None | Unset):
        alert_count (int | Unset):
        last_error (None | str | Unset):
        last_error_at (datetime.datetime | None | Unset):
        open_alert_count (int | Unset):
        suppressed_count (int | Unset): Alerts suppressed by the storm cap
        last_storm_at (datetime.datetime | None | Unset):
        feedback_counts (TriggerRuleFeedbackCounts | Unset): Alert feedback counts
        snoozed_until (datetime.datetime | None | Unset): Read-only; set through snoozeTrigger
        geofence_route (Geometry | None | Unset): Original line of a buffered route geofence (display only); `geofence`
            holds the materialised MultiPolygon
        muted_until (datetime.datetime | None | Unset): Set through POST /v1/triggers/{ruleId}/mute (033)
        geofence_origin (TriggerRuleGeofenceOrigin | Unset): 036
        anchor_status (None | TriggerRuleAnchorStatusType1 | TriggerRuleAnchorStatusType2Type1 |
            TriggerRuleAnchorStatusType3Type1 | Unset): Moving-anchor state (036); stale suspends the rule (ANCHOR_STALE)
        window_status (None | Unset | WindowStatus): Windowed rules only
        owner_principal_id (None | str | Unset): The principal of the user who owns the rule (R2-008); null for a shared
            rule managed by an admin. Ignored on input.
    """

    name: str
    id: UUID
    created_at: datetime.datetime
    updated_at: datetime.datetime
    enabled: bool | Unset = True
    source: None | str | Unset = UNSET
    kind: None | str | Unset = UNSET
    conditions: list[ApproachCondition | Condition] | Unset = UNSET
    geofence: Geometry | None | Unset = UNSET
    mode: TriggerRuleInputMode | Unset = TriggerRuleInputMode.MATCH
    only_new: bool | Unset = False
    cooldown: str | Unset = UNSET
    message_template: str | Unset = "{{name}}: {{source}}/{{externalId}}"
    severity: TriggerRuleInputSeverity | Unset = TriggerRuleInputSeverity.WARNING
    enrich: bool | Unset = False
    enrich_radius_km: float | Unset = 100.0
    enrich_window: str | Unset = "24h"
    tags: list[str] | Unset = UNSET
    sources: list[str] | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    buffer_meters: float | None | Unset = UNSET
    hysteresis_meters: float | Unset = UNSET
    near: Any | NearType1 | NearType2 | None | Unset = UNSET
    reappear_as_enter: bool | Unset = UNSET
    cooldown_scope: TriggerRuleInputCooldownScope | Unset = (
        TriggerRuleInputCooldownScope.RECORD
    )
    auto_resolve: AutoResolve | bool | None | Unset = UNSET
    max_alerts_per_hour: int | None | Unset = UNSET
    auto_disable_on_storm: bool | Unset = False
    active_from: datetime.datetime | None | Unset = UNSET
    active_until: datetime.datetime | None | Unset = UNSET
    active_window: ActiveWindow | None | Unset = UNSET
    forecast_horizon: None | str | Unset = UNSET
    escalation_policy_id: None | Unset | UUID = UNSET
    geofence_ref: GeofenceRef | None | Unset = UNSET
    weather_context: TriggerRuleInputWeatherContext | Unset = (
        TriggerRuleInputWeatherContext.AUTO
    )
    type_: TriggerRuleInputType | Unset = TriggerRuleInputType.RECORD
    window: AbsenceWindow | AnomalyWindow | CountWindow | None | Unset = UNSET
    last_fired_at: datetime.datetime | None | Unset = UNSET
    alert_count: int | Unset = UNSET
    last_error: None | str | Unset = UNSET
    last_error_at: datetime.datetime | None | Unset = UNSET
    open_alert_count: int | Unset = UNSET
    suppressed_count: int | Unset = UNSET
    last_storm_at: datetime.datetime | None | Unset = UNSET
    feedback_counts: TriggerRuleFeedbackCounts | Unset = UNSET
    snoozed_until: datetime.datetime | None | Unset = UNSET
    geofence_route: Geometry | None | Unset = UNSET
    muted_until: datetime.datetime | None | Unset = UNSET
    geofence_origin: TriggerRuleGeofenceOrigin | Unset = UNSET
    anchor_status: (
        None
        | TriggerRuleAnchorStatusType1
        | TriggerRuleAnchorStatusType2Type1
        | TriggerRuleAnchorStatusType3Type1
        | Unset
    ) = UNSET
    window_status: None | Unset | WindowStatus = UNSET
    owner_principal_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.absence_window import AbsenceWindow
        from ..models.active_window import ActiveWindow
        from ..models.anomaly_window import AnomalyWindow
        from ..models.auto_resolve import AutoResolve
        from ..models.condition import Condition
        from ..models.count_window import CountWindow
        from ..models.geofence_ref import GeofenceRef
        from ..models.geometry import Geometry
        from ..models.near_type_1 import NearType1
        from ..models.near_type_2 import NearType2
        from ..models.window_status import WindowStatus

        name = self.name

        id = str(self.id)

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

        enabled = self.enabled

        source: None | str | Unset
        if isinstance(self.source, Unset):
            source = UNSET
        else:
            source = self.source

        kind: None | str | Unset
        if isinstance(self.kind, Unset):
            kind = UNSET
        else:
            kind = self.kind

        conditions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.conditions, Unset):
            conditions = []
            for conditions_item_data in self.conditions:
                conditions_item: dict[str, Any]
                if isinstance(conditions_item_data, Condition):
                    conditions_item = conditions_item_data.to_dict()
                else:
                    conditions_item = conditions_item_data.to_dict()

                conditions.append(conditions_item)

        geofence: dict[str, Any] | None | Unset
        if isinstance(self.geofence, Unset):
            geofence = UNSET
        elif isinstance(self.geofence, Geometry):
            geofence = self.geofence.to_dict()
        else:
            geofence = self.geofence

        mode: str | Unset = UNSET
        if not isinstance(self.mode, Unset):
            mode = self.mode.value

        only_new = self.only_new

        cooldown = self.cooldown

        message_template = self.message_template

        severity: str | Unset = UNSET
        if not isinstance(self.severity, Unset):
            severity = self.severity.value

        enrich = self.enrich

        enrich_radius_km = self.enrich_radius_km

        enrich_window = self.enrich_window

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        buffer_meters: float | None | Unset
        if isinstance(self.buffer_meters, Unset):
            buffer_meters = UNSET
        else:
            buffer_meters = self.buffer_meters

        hysteresis_meters = self.hysteresis_meters

        near: Any | dict[str, Any] | None | Unset
        if isinstance(self.near, Unset):
            near = UNSET
        elif isinstance(self.near, NearType1) or isinstance(self.near, NearType2):
            near = self.near.to_dict()
        else:
            near = self.near

        reappear_as_enter = self.reappear_as_enter

        cooldown_scope: str | Unset = UNSET
        if not isinstance(self.cooldown_scope, Unset):
            cooldown_scope = self.cooldown_scope.value

        auto_resolve: bool | dict[str, Any] | None | Unset
        if isinstance(self.auto_resolve, Unset):
            auto_resolve = UNSET
        elif isinstance(self.auto_resolve, AutoResolve):
            auto_resolve = self.auto_resolve.to_dict()
        else:
            auto_resolve = self.auto_resolve

        max_alerts_per_hour: int | None | Unset
        if isinstance(self.max_alerts_per_hour, Unset):
            max_alerts_per_hour = UNSET
        else:
            max_alerts_per_hour = self.max_alerts_per_hour

        auto_disable_on_storm = self.auto_disable_on_storm

        active_from: None | str | Unset
        if isinstance(self.active_from, Unset):
            active_from = UNSET
        elif isinstance(self.active_from, datetime.datetime):
            active_from = self.active_from.isoformat()
        else:
            active_from = self.active_from

        active_until: None | str | Unset
        if isinstance(self.active_until, Unset):
            active_until = UNSET
        elif isinstance(self.active_until, datetime.datetime):
            active_until = self.active_until.isoformat()
        else:
            active_until = self.active_until

        active_window: dict[str, Any] | None | Unset
        if isinstance(self.active_window, Unset):
            active_window = UNSET
        elif isinstance(self.active_window, ActiveWindow):
            active_window = self.active_window.to_dict()
        else:
            active_window = self.active_window

        forecast_horizon: None | str | Unset
        if isinstance(self.forecast_horizon, Unset):
            forecast_horizon = UNSET
        else:
            forecast_horizon = self.forecast_horizon

        escalation_policy_id: None | str | Unset
        if isinstance(self.escalation_policy_id, Unset):
            escalation_policy_id = UNSET
        elif isinstance(self.escalation_policy_id, UUID):
            escalation_policy_id = str(self.escalation_policy_id)
        else:
            escalation_policy_id = self.escalation_policy_id

        geofence_ref: dict[str, Any] | None | Unset
        if isinstance(self.geofence_ref, Unset):
            geofence_ref = UNSET
        elif isinstance(self.geofence_ref, GeofenceRef):
            geofence_ref = self.geofence_ref.to_dict()
        else:
            geofence_ref = self.geofence_ref

        weather_context: str | Unset = UNSET
        if not isinstance(self.weather_context, Unset):
            weather_context = self.weather_context.value

        type_: str | Unset = UNSET
        if not isinstance(self.type_, Unset):
            type_ = self.type_.value

        window: dict[str, Any] | None | Unset
        if isinstance(self.window, Unset):
            window = UNSET
        elif (
            isinstance(self.window, CountWindow)
            or isinstance(self.window, AbsenceWindow)
            or isinstance(self.window, AnomalyWindow)
        ):
            window = self.window.to_dict()
        else:
            window = self.window

        last_fired_at: None | str | Unset
        if isinstance(self.last_fired_at, Unset):
            last_fired_at = UNSET
        elif isinstance(self.last_fired_at, datetime.datetime):
            last_fired_at = self.last_fired_at.isoformat()
        else:
            last_fired_at = self.last_fired_at

        alert_count = self.alert_count

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        last_error_at: None | str | Unset
        if isinstance(self.last_error_at, Unset):
            last_error_at = UNSET
        elif isinstance(self.last_error_at, datetime.datetime):
            last_error_at = self.last_error_at.isoformat()
        else:
            last_error_at = self.last_error_at

        open_alert_count = self.open_alert_count

        suppressed_count = self.suppressed_count

        last_storm_at: None | str | Unset
        if isinstance(self.last_storm_at, Unset):
            last_storm_at = UNSET
        elif isinstance(self.last_storm_at, datetime.datetime):
            last_storm_at = self.last_storm_at.isoformat()
        else:
            last_storm_at = self.last_storm_at

        feedback_counts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.feedback_counts, Unset):
            feedback_counts = self.feedback_counts.to_dict()

        snoozed_until: None | str | Unset
        if isinstance(self.snoozed_until, Unset):
            snoozed_until = UNSET
        elif isinstance(self.snoozed_until, datetime.datetime):
            snoozed_until = self.snoozed_until.isoformat()
        else:
            snoozed_until = self.snoozed_until

        geofence_route: dict[str, Any] | None | Unset
        if isinstance(self.geofence_route, Unset):
            geofence_route = UNSET
        elif isinstance(self.geofence_route, Geometry):
            geofence_route = self.geofence_route.to_dict()
        else:
            geofence_route = self.geofence_route

        muted_until: None | str | Unset
        if isinstance(self.muted_until, Unset):
            muted_until = UNSET
        elif isinstance(self.muted_until, datetime.datetime):
            muted_until = self.muted_until.isoformat()
        else:
            muted_until = self.muted_until

        geofence_origin: str | Unset = UNSET
        if not isinstance(self.geofence_origin, Unset):
            geofence_origin = self.geofence_origin.value

        anchor_status: None | str | Unset
        if isinstance(self.anchor_status, Unset):
            anchor_status = UNSET
        elif (
            isinstance(self.anchor_status, TriggerRuleAnchorStatusType1)
            or isinstance(self.anchor_status, TriggerRuleAnchorStatusType2Type1)
            or isinstance(self.anchor_status, TriggerRuleAnchorStatusType3Type1)
        ):
            anchor_status = self.anchor_status.value
        else:
            anchor_status = self.anchor_status

        window_status: dict[str, Any] | None | Unset
        if isinstance(self.window_status, Unset):
            window_status = UNSET
        elif isinstance(self.window_status, WindowStatus):
            window_status = self.window_status.to_dict()
        else:
            window_status = self.window_status

        owner_principal_id: None | str | Unset
        if isinstance(self.owner_principal_id, Unset):
            owner_principal_id = UNSET
        else:
            owner_principal_id = self.owner_principal_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "id": id,
                "createdAt": created_at,
                "updatedAt": updated_at,
            }
        )
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if source is not UNSET:
            field_dict["source"] = source
        if kind is not UNSET:
            field_dict["kind"] = kind
        if conditions is not UNSET:
            field_dict["conditions"] = conditions
        if geofence is not UNSET:
            field_dict["geofence"] = geofence
        if mode is not UNSET:
            field_dict["mode"] = mode
        if only_new is not UNSET:
            field_dict["onlyNew"] = only_new
        if cooldown is not UNSET:
            field_dict["cooldown"] = cooldown
        if message_template is not UNSET:
            field_dict["messageTemplate"] = message_template
        if severity is not UNSET:
            field_dict["severity"] = severity
        if enrich is not UNSET:
            field_dict["enrich"] = enrich
        if enrich_radius_km is not UNSET:
            field_dict["enrichRadiusKm"] = enrich_radius_km
        if enrich_window is not UNSET:
            field_dict["enrichWindow"] = enrich_window
        if tags is not UNSET:
            field_dict["tags"] = tags
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if buffer_meters is not UNSET:
            field_dict["bufferMeters"] = buffer_meters
        if hysteresis_meters is not UNSET:
            field_dict["hysteresisMeters"] = hysteresis_meters
        if near is not UNSET:
            field_dict["near"] = near
        if reappear_as_enter is not UNSET:
            field_dict["reappearAsEnter"] = reappear_as_enter
        if cooldown_scope is not UNSET:
            field_dict["cooldownScope"] = cooldown_scope
        if auto_resolve is not UNSET:
            field_dict["autoResolve"] = auto_resolve
        if max_alerts_per_hour is not UNSET:
            field_dict["maxAlertsPerHour"] = max_alerts_per_hour
        if auto_disable_on_storm is not UNSET:
            field_dict["autoDisableOnStorm"] = auto_disable_on_storm
        if active_from is not UNSET:
            field_dict["activeFrom"] = active_from
        if active_until is not UNSET:
            field_dict["activeUntil"] = active_until
        if active_window is not UNSET:
            field_dict["activeWindow"] = active_window
        if forecast_horizon is not UNSET:
            field_dict["forecastHorizon"] = forecast_horizon
        if escalation_policy_id is not UNSET:
            field_dict["escalationPolicyId"] = escalation_policy_id
        if geofence_ref is not UNSET:
            field_dict["geofenceRef"] = geofence_ref
        if weather_context is not UNSET:
            field_dict["weatherContext"] = weather_context
        if type_ is not UNSET:
            field_dict["type"] = type_
        if window is not UNSET:
            field_dict["window"] = window
        if last_fired_at is not UNSET:
            field_dict["lastFiredAt"] = last_fired_at
        if alert_count is not UNSET:
            field_dict["alertCount"] = alert_count
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if last_error_at is not UNSET:
            field_dict["lastErrorAt"] = last_error_at
        if open_alert_count is not UNSET:
            field_dict["openAlertCount"] = open_alert_count
        if suppressed_count is not UNSET:
            field_dict["suppressedCount"] = suppressed_count
        if last_storm_at is not UNSET:
            field_dict["lastStormAt"] = last_storm_at
        if feedback_counts is not UNSET:
            field_dict["feedbackCounts"] = feedback_counts
        if snoozed_until is not UNSET:
            field_dict["snoozedUntil"] = snoozed_until
        if geofence_route is not UNSET:
            field_dict["geofenceRoute"] = geofence_route
        if muted_until is not UNSET:
            field_dict["mutedUntil"] = muted_until
        if geofence_origin is not UNSET:
            field_dict["geofenceOrigin"] = geofence_origin
        if anchor_status is not UNSET:
            field_dict["anchorStatus"] = anchor_status
        if window_status is not UNSET:
            field_dict["windowStatus"] = window_status
        if owner_principal_id is not UNSET:
            field_dict["ownerPrincipalId"] = owner_principal_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.absence_window import AbsenceWindow
        from ..models.active_window import ActiveWindow
        from ..models.anomaly_window import AnomalyWindow
        from ..models.approach_condition import ApproachCondition
        from ..models.auto_resolve import AutoResolve
        from ..models.condition import Condition
        from ..models.count_window import CountWindow
        from ..models.geofence_ref import GeofenceRef
        from ..models.geometry import Geometry
        from ..models.near_type_1 import NearType1
        from ..models.near_type_2 import NearType2
        from ..models.trigger_rule_feedback_counts import TriggerRuleFeedbackCounts
        from ..models.window_status import WindowStatus

        d = dict(src_dict)
        name = d.pop("name")

        id = UUID(d.pop("id"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        enabled = d.pop("enabled", UNSET)

        def _parse_source(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        source = _parse_source(d.pop("source", UNSET))

        def _parse_kind(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kind = _parse_kind(d.pop("kind", UNSET))

        _conditions = d.pop("conditions", UNSET)
        conditions: list[ApproachCondition | Condition] | Unset = UNSET
        if _conditions is not UNSET:
            conditions = []
            for conditions_item_data in _conditions:

                def _parse_conditions_item(
                    data: object,
                ) -> ApproachCondition | Condition:
                    try:
                        if not isinstance(data, dict):
                            raise TypeError()
                        conditions_item_type_0 = Condition.from_dict(data)

                        return conditions_item_type_0
                    except (TypeError, ValueError, AttributeError, KeyError):
                        pass
                    if not isinstance(data, dict):
                        raise TypeError()
                    conditions_item_type_1 = ApproachCondition.from_dict(data)

                    return conditions_item_type_1

                conditions_item = _parse_conditions_item(conditions_item_data)

                conditions.append(conditions_item)

        def _parse_geofence(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geofence_type_0 = Geometry.from_dict(data)

                return geofence_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geofence = _parse_geofence(d.pop("geofence", UNSET))

        _mode = d.pop("mode", UNSET)
        mode: TriggerRuleInputMode | Unset
        if isinstance(_mode, Unset):
            mode = UNSET
        else:
            mode = TriggerRuleInputMode(_mode)

        only_new = d.pop("onlyNew", UNSET)

        cooldown = d.pop("cooldown", UNSET)

        message_template = d.pop("messageTemplate", UNSET)

        _severity = d.pop("severity", UNSET)
        severity: TriggerRuleInputSeverity | Unset
        if isinstance(_severity, Unset):
            severity = UNSET
        else:
            severity = TriggerRuleInputSeverity(_severity)

        enrich = d.pop("enrich", UNSET)

        enrich_radius_km = d.pop("enrichRadiusKm", UNSET)

        enrich_window = d.pop("enrichWindow", UNSET)

        tags = cast(list[str], d.pop("tags", UNSET))

        sources = cast(list[str], d.pop("sources", UNSET))

        kinds = cast(list[str], d.pop("kinds", UNSET))

        def _parse_buffer_meters(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        buffer_meters = _parse_buffer_meters(d.pop("bufferMeters", UNSET))

        hysteresis_meters = d.pop("hysteresisMeters", UNSET)

        def _parse_near(data: object) -> Any | NearType1 | NearType2 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_near_type_1 = NearType1.from_dict(data)

                return componentsschemas_near_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                componentsschemas_near_type_2 = NearType2.from_dict(data)

                return componentsschemas_near_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Any | NearType1 | NearType2 | None | Unset, data)

        near = _parse_near(d.pop("near", UNSET))

        reappear_as_enter = d.pop("reappearAsEnter", UNSET)

        _cooldown_scope = d.pop("cooldownScope", UNSET)
        cooldown_scope: TriggerRuleInputCooldownScope | Unset
        if isinstance(_cooldown_scope, Unset):
            cooldown_scope = UNSET
        else:
            cooldown_scope = TriggerRuleInputCooldownScope(_cooldown_scope)

        def _parse_auto_resolve(data: object) -> AutoResolve | bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                auto_resolve_type_0 = AutoResolve.from_dict(data)

                return auto_resolve_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AutoResolve | bool | None | Unset, data)

        auto_resolve = _parse_auto_resolve(d.pop("autoResolve", UNSET))

        def _parse_max_alerts_per_hour(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        max_alerts_per_hour = _parse_max_alerts_per_hour(
            d.pop("maxAlertsPerHour", UNSET)
        )

        auto_disable_on_storm = d.pop("autoDisableOnStorm", UNSET)

        def _parse_active_from(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                active_from_type_0 = datetime.datetime.fromisoformat(data)

                return active_from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        active_from = _parse_active_from(d.pop("activeFrom", UNSET))

        def _parse_active_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                active_until_type_0 = datetime.datetime.fromisoformat(data)

                return active_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        active_until = _parse_active_until(d.pop("activeUntil", UNSET))

        def _parse_active_window(data: object) -> ActiveWindow | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                active_window_type_0 = ActiveWindow.from_dict(data)

                return active_window_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ActiveWindow | None | Unset, data)

        active_window = _parse_active_window(d.pop("activeWindow", UNSET))

        def _parse_forecast_horizon(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        forecast_horizon = _parse_forecast_horizon(d.pop("forecastHorizon", UNSET))

        def _parse_escalation_policy_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                escalation_policy_id_type_0 = UUID(data)

                return escalation_policy_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        escalation_policy_id = _parse_escalation_policy_id(
            d.pop("escalationPolicyId", UNSET)
        )

        def _parse_geofence_ref(data: object) -> GeofenceRef | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geofence_ref_type_0 = GeofenceRef.from_dict(data)

                return geofence_ref_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(GeofenceRef | None | Unset, data)

        geofence_ref = _parse_geofence_ref(d.pop("geofenceRef", UNSET))

        _weather_context = d.pop("weatherContext", UNSET)
        weather_context: TriggerRuleInputWeatherContext | Unset
        if isinstance(_weather_context, Unset):
            weather_context = UNSET
        else:
            weather_context = TriggerRuleInputWeatherContext(_weather_context)

        _type_ = d.pop("type", UNSET)
        type_: TriggerRuleInputType | Unset
        if isinstance(_type_, Unset):
            type_ = UNSET
        else:
            type_ = TriggerRuleInputType(_type_)

        def _parse_window(
            data: object,
        ) -> AbsenceWindow | AnomalyWindow | CountWindow | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                window_type_0 = CountWindow.from_dict(data)

                return window_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                window_type_1 = AbsenceWindow.from_dict(data)

                return window_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                window_type_2 = AnomalyWindow.from_dict(data)

                return window_type_2
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AbsenceWindow | AnomalyWindow | CountWindow | None | Unset, data
            )

        window = _parse_window(d.pop("window", UNSET))

        def _parse_last_fired_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_fired_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_fired_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_fired_at = _parse_last_fired_at(d.pop("lastFiredAt", UNSET))

        alert_count = d.pop("alertCount", UNSET)

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        def _parse_last_error_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_error_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_error_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_error_at = _parse_last_error_at(d.pop("lastErrorAt", UNSET))

        open_alert_count = d.pop("openAlertCount", UNSET)

        suppressed_count = d.pop("suppressedCount", UNSET)

        def _parse_last_storm_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_storm_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_storm_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_storm_at = _parse_last_storm_at(d.pop("lastStormAt", UNSET))

        _feedback_counts = d.pop("feedbackCounts", UNSET)
        feedback_counts: TriggerRuleFeedbackCounts | Unset
        if isinstance(_feedback_counts, Unset):
            feedback_counts = UNSET
        else:
            feedback_counts = TriggerRuleFeedbackCounts.from_dict(_feedback_counts)

        def _parse_snoozed_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                snoozed_until_type_0 = datetime.datetime.fromisoformat(data)

                return snoozed_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        snoozed_until = _parse_snoozed_until(d.pop("snoozedUntil", UNSET))

        def _parse_geofence_route(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geofence_route_type_0 = Geometry.from_dict(data)

                return geofence_route_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geofence_route = _parse_geofence_route(d.pop("geofenceRoute", UNSET))

        def _parse_muted_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                muted_until_type_0 = datetime.datetime.fromisoformat(data)

                return muted_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        muted_until = _parse_muted_until(d.pop("mutedUntil", UNSET))

        _geofence_origin = d.pop("geofenceOrigin", UNSET)
        geofence_origin: TriggerRuleGeofenceOrigin | Unset
        if isinstance(_geofence_origin, Unset):
            geofence_origin = UNSET
        else:
            geofence_origin = TriggerRuleGeofenceOrigin(_geofence_origin)

        def _parse_anchor_status(
            data: object,
        ) -> (
            None
            | TriggerRuleAnchorStatusType1
            | TriggerRuleAnchorStatusType2Type1
            | TriggerRuleAnchorStatusType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                anchor_status_type_1 = TriggerRuleAnchorStatusType1(data)

                return anchor_status_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                anchor_status_type_2_type_1 = TriggerRuleAnchorStatusType2Type1(data)

                return anchor_status_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                anchor_status_type_3_type_1 = TriggerRuleAnchorStatusType3Type1(data)

                return anchor_status_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | TriggerRuleAnchorStatusType1
                | TriggerRuleAnchorStatusType2Type1
                | TriggerRuleAnchorStatusType3Type1
                | Unset,
                data,
            )

        anchor_status = _parse_anchor_status(d.pop("anchorStatus", UNSET))

        def _parse_window_status(data: object) -> None | Unset | WindowStatus:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                window_status_type_0 = WindowStatus.from_dict(data)

                return window_status_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | WindowStatus, data)

        window_status = _parse_window_status(d.pop("windowStatus", UNSET))

        def _parse_owner_principal_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        owner_principal_id = _parse_owner_principal_id(d.pop("ownerPrincipalId", UNSET))

        trigger_rule = cls(
            name=name,
            id=id,
            created_at=created_at,
            updated_at=updated_at,
            enabled=enabled,
            source=source,
            kind=kind,
            conditions=conditions,
            geofence=geofence,
            mode=mode,
            only_new=only_new,
            cooldown=cooldown,
            message_template=message_template,
            severity=severity,
            enrich=enrich,
            enrich_radius_km=enrich_radius_km,
            enrich_window=enrich_window,
            tags=tags,
            sources=sources,
            kinds=kinds,
            buffer_meters=buffer_meters,
            hysteresis_meters=hysteresis_meters,
            near=near,
            reappear_as_enter=reappear_as_enter,
            cooldown_scope=cooldown_scope,
            auto_resolve=auto_resolve,
            max_alerts_per_hour=max_alerts_per_hour,
            auto_disable_on_storm=auto_disable_on_storm,
            active_from=active_from,
            active_until=active_until,
            active_window=active_window,
            forecast_horizon=forecast_horizon,
            escalation_policy_id=escalation_policy_id,
            geofence_ref=geofence_ref,
            weather_context=weather_context,
            type_=type_,
            window=window,
            last_fired_at=last_fired_at,
            alert_count=alert_count,
            last_error=last_error,
            last_error_at=last_error_at,
            open_alert_count=open_alert_count,
            suppressed_count=suppressed_count,
            last_storm_at=last_storm_at,
            feedback_counts=feedback_counts,
            snoozed_until=snoozed_until,
            geofence_route=geofence_route,
            muted_until=muted_until,
            geofence_origin=geofence_origin,
            anchor_status=anchor_status,
            window_status=window_status,
            owner_principal_id=owner_principal_id,
        )

        trigger_rule.additional_properties = d
        return trigger_rule

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
