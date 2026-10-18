from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.event_stream_alerts_filter_min_severity_type_1 import (
    EventStreamAlertsFilterMinSeverityType1,
)
from ..models.event_stream_alerts_filter_min_severity_type_2_type_1 import (
    EventStreamAlertsFilterMinSeverityType2Type1,
)
from ..models.event_stream_alerts_filter_min_severity_type_3_type_1 import (
    EventStreamAlertsFilterMinSeverityType3Type1,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="EventStreamAlertsFilter")


@_attrs_define
class EventStreamAlertsFilter:
    """Filter block for the `alerts` topic of /v1/events/stream

    Attributes:
        min_severity (EventStreamAlertsFilterMinSeverityType1 | EventStreamAlertsFilterMinSeverityType2Type1 |
            EventStreamAlertsFilterMinSeverityType3Type1 | None | Unset):
        rule_id (list[UUID] | Unset): Rule ids
        source (list[str] | Unset): Source ids
        kind (list[str] | Unset): Kinds
        include_suppressed (bool | Unset): Include alerts suppressed by a rule or alert snooze Default: False.
    """

    min_severity: (
        EventStreamAlertsFilterMinSeverityType1
        | EventStreamAlertsFilterMinSeverityType2Type1
        | EventStreamAlertsFilterMinSeverityType3Type1
        | None
        | Unset
    ) = UNSET
    rule_id: list[UUID] | Unset = UNSET
    source: list[str] | Unset = UNSET
    kind: list[str] | Unset = UNSET
    include_suppressed: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        min_severity: None | str | Unset
        if isinstance(self.min_severity, Unset):
            min_severity = UNSET
        elif (
            isinstance(self.min_severity, EventStreamAlertsFilterMinSeverityType1)
            or isinstance(
                self.min_severity, EventStreamAlertsFilterMinSeverityType2Type1
            )
            or isinstance(
                self.min_severity, EventStreamAlertsFilterMinSeverityType3Type1
            )
        ):
            min_severity = self.min_severity.value
        else:
            min_severity = self.min_severity

        rule_id: list[str] | Unset = UNSET
        if not isinstance(self.rule_id, Unset):
            rule_id = []
            for rule_id_item_data in self.rule_id:
                rule_id_item = str(rule_id_item_data)
                rule_id.append(rule_id_item)

        source: list[str] | Unset = UNSET
        if not isinstance(self.source, Unset):
            source = self.source

        kind: list[str] | Unset = UNSET
        if not isinstance(self.kind, Unset):
            kind = self.kind

        include_suppressed = self.include_suppressed

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity
        if rule_id is not UNSET:
            field_dict["ruleId"] = rule_id
        if source is not UNSET:
            field_dict["source"] = source
        if kind is not UNSET:
            field_dict["kind"] = kind
        if include_suppressed is not UNSET:
            field_dict["includeSuppressed"] = include_suppressed

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_min_severity(
            data: object,
        ) -> (
            EventStreamAlertsFilterMinSeverityType1
            | EventStreamAlertsFilterMinSeverityType2Type1
            | EventStreamAlertsFilterMinSeverityType3Type1
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
                min_severity_type_1 = EventStreamAlertsFilterMinSeverityType1(data)

                return min_severity_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                min_severity_type_2_type_1 = (
                    EventStreamAlertsFilterMinSeverityType2Type1(data)
                )

                return min_severity_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                min_severity_type_3_type_1 = (
                    EventStreamAlertsFilterMinSeverityType3Type1(data)
                )

                return min_severity_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                EventStreamAlertsFilterMinSeverityType1
                | EventStreamAlertsFilterMinSeverityType2Type1
                | EventStreamAlertsFilterMinSeverityType3Type1
                | None
                | Unset,
                data,
            )

        min_severity = _parse_min_severity(d.pop("minSeverity", UNSET))

        _rule_id = d.pop("ruleId", UNSET)
        rule_id: list[UUID] | Unset = UNSET
        if _rule_id is not UNSET:
            rule_id = []
            for rule_id_item_data in _rule_id:
                rule_id_item = UUID(rule_id_item_data)

                rule_id.append(rule_id_item)

        source = cast(list[str], d.pop("source", UNSET))

        kind = cast(list[str], d.pop("kind", UNSET))

        include_suppressed = d.pop("includeSuppressed", UNSET)

        event_stream_alerts_filter = cls(
            min_severity=min_severity,
            rule_id=rule_id,
            source=source,
            kind=kind,
            include_suppressed=include_suppressed,
        )

        event_stream_alerts_filter.additional_properties = d
        return event_stream_alerts_filter

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
