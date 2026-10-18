from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.event_selector_min_severity_type_1 import EventSelectorMinSeverityType1
from ..models.event_selector_min_severity_type_2_type_1 import (
    EventSelectorMinSeverityType2Type1,
)
from ..models.event_selector_min_severity_type_3_type_1 import (
    EventSelectorMinSeverityType3Type1,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.event_selector_change_point import EventSelectorChangePoint
    from ..models.event_selector_rule import EventSelectorRule
    from ..models.event_selector_situation_activated import (
        EventSelectorSituationActivated,
    )
    from ..models.event_selector_unexplained_move import EventSelectorUnexplainedMove


T = TypeVar("T", bound="EventSelector")


@_attrs_define
class EventSelector:
    """Selects event occurrences: one of 103's selector forms (`rule`, `alertRuleId`, `intelEventType`, `anomalyRuleId` +
    `classification`) or a 118-121 addition (`alertRuleTag`, `situationActivated`, `changePoint`, `unexplainedMove`,
    `referenceFamily`, `timestamps`); exactly one form per selector, optionally narrowed by `regionCodes` and
    `minSeverity`.

        Attributes:
            rule (EventSelectorRule | Unset): Inline record filter in 017 trigger-condition syntax
            alert_rule_id (str | Unset): Alerts fired by this 017 rule
            alert_rule_tag (str | Unset): Alerts fired by rules carrying this tag (120 event families)
            intel_event_type (str | Unset): 101 intelligence event type
            anomaly_rule_id (str | Unset): 100 anomaly rule id; use with `classification`
            classification (str | Unset): 100 anomaly classification, e.g. `activity-up`
            situation_activated (EventSelectorSituationActivated | Unset): Situations turning active (118), optionally by
                theme
            change_point (EventSelectorChangePoint | Unset): Change-point findings (119) on series of one family
            unexplained_move (EventSelectorUnexplainedMove | Unset): `unexplained-move` findings (119) on series of one
                family
            reference_family (str | Unset): 121 reference-history family, e.g. `conflict:battles` or `seismic:m5`
            timestamps (list[datetime.datetime] | Unset): Explicit event times
            min_severity (EventSelectorMinSeverityType1 | EventSelectorMinSeverityType2Type1 |
                EventSelectorMinSeverityType3Type1 | None | Unset): Minimum severity of the selected events (alert/intel
                severities or finding severities)
            region_codes (list[str] | Unset): Only occurrences tagged with one of these regions
            label (None | str | Unset): Display label (generated when absent)
    """

    rule: EventSelectorRule | Unset = UNSET
    alert_rule_id: str | Unset = UNSET
    alert_rule_tag: str | Unset = UNSET
    intel_event_type: str | Unset = UNSET
    anomaly_rule_id: str | Unset = UNSET
    classification: str | Unset = UNSET
    situation_activated: EventSelectorSituationActivated | Unset = UNSET
    change_point: EventSelectorChangePoint | Unset = UNSET
    unexplained_move: EventSelectorUnexplainedMove | Unset = UNSET
    reference_family: str | Unset = UNSET
    timestamps: list[datetime.datetime] | Unset = UNSET
    min_severity: (
        EventSelectorMinSeverityType1
        | EventSelectorMinSeverityType2Type1
        | EventSelectorMinSeverityType3Type1
        | None
        | Unset
    ) = UNSET
    region_codes: list[str] | Unset = UNSET
    label: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule: dict[str, Any] | Unset = UNSET
        if not isinstance(self.rule, Unset):
            rule = self.rule.to_dict()

        alert_rule_id = self.alert_rule_id

        alert_rule_tag = self.alert_rule_tag

        intel_event_type = self.intel_event_type

        anomaly_rule_id = self.anomaly_rule_id

        classification = self.classification

        situation_activated: dict[str, Any] | Unset = UNSET
        if not isinstance(self.situation_activated, Unset):
            situation_activated = self.situation_activated.to_dict()

        change_point: dict[str, Any] | Unset = UNSET
        if not isinstance(self.change_point, Unset):
            change_point = self.change_point.to_dict()

        unexplained_move: dict[str, Any] | Unset = UNSET
        if not isinstance(self.unexplained_move, Unset):
            unexplained_move = self.unexplained_move.to_dict()

        reference_family = self.reference_family

        timestamps: list[str] | Unset = UNSET
        if not isinstance(self.timestamps, Unset):
            timestamps = []
            for timestamps_item_data in self.timestamps:
                timestamps_item = timestamps_item_data.isoformat()
                timestamps.append(timestamps_item)

        min_severity: None | str | Unset
        if isinstance(self.min_severity, Unset):
            min_severity = UNSET
        elif (
            isinstance(self.min_severity, EventSelectorMinSeverityType1)
            or isinstance(self.min_severity, EventSelectorMinSeverityType2Type1)
            or isinstance(self.min_severity, EventSelectorMinSeverityType3Type1)
        ):
            min_severity = self.min_severity.value
        else:
            min_severity = self.min_severity

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if rule is not UNSET:
            field_dict["rule"] = rule
        if alert_rule_id is not UNSET:
            field_dict["alertRuleId"] = alert_rule_id
        if alert_rule_tag is not UNSET:
            field_dict["alertRuleTag"] = alert_rule_tag
        if intel_event_type is not UNSET:
            field_dict["intelEventType"] = intel_event_type
        if anomaly_rule_id is not UNSET:
            field_dict["anomalyRuleId"] = anomaly_rule_id
        if classification is not UNSET:
            field_dict["classification"] = classification
        if situation_activated is not UNSET:
            field_dict["situationActivated"] = situation_activated
        if change_point is not UNSET:
            field_dict["changePoint"] = change_point
        if unexplained_move is not UNSET:
            field_dict["unexplainedMove"] = unexplained_move
        if reference_family is not UNSET:
            field_dict["referenceFamily"] = reference_family
        if timestamps is not UNSET:
            field_dict["timestamps"] = timestamps
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if label is not UNSET:
            field_dict["label"] = label

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.event_selector_change_point import EventSelectorChangePoint
        from ..models.event_selector_rule import EventSelectorRule
        from ..models.event_selector_situation_activated import (
            EventSelectorSituationActivated,
        )
        from ..models.event_selector_unexplained_move import (
            EventSelectorUnexplainedMove,
        )

        d = dict(src_dict)
        _rule = d.pop("rule", UNSET)
        rule: EventSelectorRule | Unset
        if isinstance(_rule, Unset):
            rule = UNSET
        else:
            rule = EventSelectorRule.from_dict(_rule)

        alert_rule_id = d.pop("alertRuleId", UNSET)

        alert_rule_tag = d.pop("alertRuleTag", UNSET)

        intel_event_type = d.pop("intelEventType", UNSET)

        anomaly_rule_id = d.pop("anomalyRuleId", UNSET)

        classification = d.pop("classification", UNSET)

        _situation_activated = d.pop("situationActivated", UNSET)
        situation_activated: EventSelectorSituationActivated | Unset
        if isinstance(_situation_activated, Unset):
            situation_activated = UNSET
        else:
            situation_activated = EventSelectorSituationActivated.from_dict(
                _situation_activated
            )

        _change_point = d.pop("changePoint", UNSET)
        change_point: EventSelectorChangePoint | Unset
        if isinstance(_change_point, Unset):
            change_point = UNSET
        else:
            change_point = EventSelectorChangePoint.from_dict(_change_point)

        _unexplained_move = d.pop("unexplainedMove", UNSET)
        unexplained_move: EventSelectorUnexplainedMove | Unset
        if isinstance(_unexplained_move, Unset):
            unexplained_move = UNSET
        else:
            unexplained_move = EventSelectorUnexplainedMove.from_dict(_unexplained_move)

        reference_family = d.pop("referenceFamily", UNSET)

        _timestamps = d.pop("timestamps", UNSET)
        timestamps: list[datetime.datetime] | Unset = UNSET
        if _timestamps is not UNSET:
            timestamps = []
            for timestamps_item_data in _timestamps:
                timestamps_item = datetime.datetime.fromisoformat(timestamps_item_data)

                timestamps.append(timestamps_item)

        def _parse_min_severity(
            data: object,
        ) -> (
            EventSelectorMinSeverityType1
            | EventSelectorMinSeverityType2Type1
            | EventSelectorMinSeverityType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                min_severity_type_1 = EventSelectorMinSeverityType1(data)

                return min_severity_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                min_severity_type_2_type_1 = EventSelectorMinSeverityType2Type1(data)

                return min_severity_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                min_severity_type_3_type_1 = EventSelectorMinSeverityType3Type1(data)

                return min_severity_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                EventSelectorMinSeverityType1
                | EventSelectorMinSeverityType2Type1
                | EventSelectorMinSeverityType3Type1
                | None
                | Unset,
                data,
            )

        min_severity = _parse_min_severity(d.pop("minSeverity", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        event_selector = cls(
            rule=rule,
            alert_rule_id=alert_rule_id,
            alert_rule_tag=alert_rule_tag,
            intel_event_type=intel_event_type,
            anomaly_rule_id=anomaly_rule_id,
            classification=classification,
            situation_activated=situation_activated,
            change_point=change_point,
            unexplained_move=unexplained_move,
            reference_family=reference_family,
            timestamps=timestamps,
            min_severity=min_severity,
            region_codes=region_codes,
            label=label,
        )

        event_selector.additional_properties = d
        return event_selector

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
