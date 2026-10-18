from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analyst_profile_scope_event_types_item import (
    AnalystProfileScopeEventTypesItem,
)
from ..models.severity import Severity
from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalystProfileScope")


@_attrs_define
class AnalystProfileScope:
    """Inputs the analyst looks at

    Attributes:
        sources (list[str] | Unset):
        kinds (list[str] | Unset):
        places (list[str] | Unset):
        bbox (list[float] | None | Unset):
        event_types (list[AnalystProfileScopeEventTypesItem] | Unset):
        rule_tags (list[str] | Unset):
        min_severity (Severity | Unset):
    """

    sources: list[str] | Unset = UNSET
    kinds: list[str] | Unset = UNSET
    places: list[str] | Unset = UNSET
    bbox: list[float] | None | Unset = UNSET
    event_types: list[AnalystProfileScopeEventTypesItem] | Unset = UNSET
    rule_tags: list[str] | Unset = UNSET
    min_severity: Severity | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        sources: list[str] | Unset = UNSET
        if not isinstance(self.sources, Unset):
            sources = self.sources

        kinds: list[str] | Unset = UNSET
        if not isinstance(self.kinds, Unset):
            kinds = self.kinds

        places: list[str] | Unset = UNSET
        if not isinstance(self.places, Unset):
            places = self.places

        bbox: list[float] | None | Unset
        if isinstance(self.bbox, Unset):
            bbox = UNSET
        elif isinstance(self.bbox, list):
            bbox = self.bbox

        else:
            bbox = self.bbox

        event_types: list[str] | Unset = UNSET
        if not isinstance(self.event_types, Unset):
            event_types = []
            for event_types_item_data in self.event_types:
                event_types_item = event_types_item_data.value
                event_types.append(event_types_item)

        rule_tags: list[str] | Unset = UNSET
        if not isinstance(self.rule_tags, Unset):
            rule_tags = self.rule_tags

        min_severity: str | Unset = UNSET
        if not isinstance(self.min_severity, Unset):
            min_severity = self.min_severity.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if sources is not UNSET:
            field_dict["sources"] = sources
        if kinds is not UNSET:
            field_dict["kinds"] = kinds
        if places is not UNSET:
            field_dict["places"] = places
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if event_types is not UNSET:
            field_dict["eventTypes"] = event_types
        if rule_tags is not UNSET:
            field_dict["ruleTags"] = rule_tags
        if min_severity is not UNSET:
            field_dict["minSeverity"] = min_severity

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        sources = cast(list[str], d.pop("sources", UNSET))

        kinds = cast(list[str], d.pop("kinds", UNSET))

        places = cast(list[str], d.pop("places", UNSET))

        def _parse_bbox(data: object) -> list[float] | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, list):
                    raise TypeError()
                bbox_type_0 = cast(list[float], data)

                return bbox_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(list[float] | None | Unset, data)

        bbox = _parse_bbox(d.pop("bbox", UNSET))

        _event_types = d.pop("eventTypes", UNSET)
        event_types: list[AnalystProfileScopeEventTypesItem] | Unset = UNSET
        if _event_types is not UNSET:
            event_types = []
            for event_types_item_data in _event_types:
                event_types_item = AnalystProfileScopeEventTypesItem(
                    event_types_item_data
                )

                event_types.append(event_types_item)

        rule_tags = cast(list[str], d.pop("ruleTags", UNSET))

        _min_severity = d.pop("minSeverity", UNSET)
        min_severity: Severity | Unset
        if isinstance(_min_severity, Unset):
            min_severity = UNSET
        else:
            min_severity = Severity(_min_severity)

        analyst_profile_scope = cls(
            sources=sources,
            kinds=kinds,
            places=places,
            bbox=bbox,
            event_types=event_types,
            rule_tags=rule_tags,
            min_severity=min_severity,
        )

        analyst_profile_scope.additional_properties = d
        return analyst_profile_scope

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
