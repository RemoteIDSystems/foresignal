from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_filters_events_item import PushFiltersEventsItem
from ..models.push_filters_min_severity import PushFiltersMinSeverity
from ..types import UNSET, Unset

T = TypeVar("T", bound="PushFilters")


@_attrs_define
class PushFilters:
    """Which notifications a push target receives (031; shared by subscriptions and native devices)

    Attributes:
        events (list[PushFiltersEventsItem] | Unset):
        rule_ids (list[UUID] | None | Unset): null = all rules
        min_severity (PushFiltersMinSeverity | Unset):
        tags (list[str] | Unset): Any-of rule tags
    """

    events: list[PushFiltersEventsItem] | Unset = UNSET
    rule_ids: list[UUID] | None | Unset = UNSET
    min_severity: PushFiltersMinSeverity | Unset = UNSET
    tags: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        events: list[str] | Unset = UNSET
        if not isinstance(self.events, Unset):
            events = []
            for events_item_data in self.events:
                events_item = events_item_data.value
                events.append(events_item)

        rule_ids: list[str] | None | Unset
        if isinstance(self.rule_ids, Unset):
            rule_ids = UNSET
        elif isinstance(self.rule_ids, list):
            rule_ids = []
            for rule_ids_type_0_item_data in self.rule_ids:
                rule_ids_type_0_item = str(rule_ids_type_0_item_data)
                rule_ids.append(rule_ids_type_0_item)

        else:
            rule_ids = self.rule_ids

        min_severity: str | Unset = UNSET
        if not isinstance(self.min_severity, Unset):
            min_severity = self.min_severity.value

        tags: list[str] | Unset = UNSET
        if not isinstance(self.tags, Unset):
            tags = self.tags

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if events is not UNSET:
            field_dict["events"] = events
        if rule_ids is not UNSET:
            field_dict["ruleIds"] = rule_ids
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity
        if tags is not UNSET:
            field_dict["tags"] = tags

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _events = d.pop("events", UNSET)
        events: list[PushFiltersEventsItem] | Unset = UNSET
        if _events is not UNSET:
            events = []
            for events_item_data in _events:
                events_item = PushFiltersEventsItem(events_item_data)

                events.append(events_item)

        def _parse_rule_ids(data: object) -> list[UUID] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                rule_ids_type_0 = []
                _rule_ids_type_0 = data
                for rule_ids_type_0_item_data in _rule_ids_type_0:
                    rule_ids_type_0_item = UUID(rule_ids_type_0_item_data)

                    rule_ids_type_0.append(rule_ids_type_0_item)

                return rule_ids_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[UUID] | None | Unset, data)

        rule_ids = _parse_rule_ids(d.pop("ruleIds", UNSET))

        _min_severity = d.pop("minSeverity", UNSET)
        min_severity: PushFiltersMinSeverity | Unset
        if isinstance(_min_severity, Unset):
            min_severity = UNSET
        else:
            min_severity = PushFiltersMinSeverity(_min_severity)

        tags = cast(list[str], d.pop("tags", UNSET))

        push_filters = cls(
            events=events,
            rule_ids=rule_ids,
            min_severity=min_severity,
            tags=tags,
        )

        push_filters.additional_properties = d
        return push_filters

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
