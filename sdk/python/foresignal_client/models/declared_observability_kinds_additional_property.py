from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.declared_observability_kinds_additional_property_area import (
        DeclaredObservabilityKindsAdditionalPropertyArea,
    )
    from ..models.declared_observability_kinds_additional_property_thresholds_item import (
        DeclaredObservabilityKindsAdditionalPropertyThresholdsItem,
    )


T = TypeVar("T", bound="DeclaredObservabilityKindsAdditionalProperty")


@_attrs_define
class DeclaredObservabilityKindsAdditionalProperty:
    """Declared footprint of one kind

    Attributes:
        area (DeclaredObservabilityKindsAdditionalPropertyArea): Where the source can observe
        thresholds (list[DeclaredObservabilityKindsAdditionalPropertyThresholdsItem] | Unset):
        typical_latency (None | str | Unset): Typical publication latency. ISO-8601 duration (P7D, PT15M) or shorthand
            (7d, 15m, 12h). Example: 15m.
        notes (None | str | Unset):
    """

    area: DeclaredObservabilityKindsAdditionalPropertyArea
    thresholds: (
        list[DeclaredObservabilityKindsAdditionalPropertyThresholdsItem] | Unset
    ) = UNSET
    typical_latency: None | str | Unset = UNSET
    notes: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        area = self.area.to_dict()

        thresholds: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.thresholds, Unset):
            thresholds = []
            for thresholds_item_data in self.thresholds:
                thresholds_item = thresholds_item_data.to_dict()
                thresholds.append(thresholds_item)

        typical_latency: None | str | Unset
        if isinstance(self.typical_latency, Unset):
            typical_latency = UNSET
        else:
            typical_latency = self.typical_latency

        notes: None | str | Unset
        if isinstance(self.notes, Unset):
            notes = UNSET
        else:
            notes = self.notes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "area": area,
            }
        )
        if thresholds is not UNSET:
            field_dict["thresholds"] = thresholds
        if typical_latency is not UNSET:
            field_dict["typicalLatency"] = typical_latency
        if notes is not UNSET:
            field_dict["notes"] = notes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.declared_observability_kinds_additional_property_area import (
            DeclaredObservabilityKindsAdditionalPropertyArea,
        )
        from ..models.declared_observability_kinds_additional_property_thresholds_item import (
            DeclaredObservabilityKindsAdditionalPropertyThresholdsItem,
        )

        d = dict(src_dict)
        area = DeclaredObservabilityKindsAdditionalPropertyArea.from_dict(d.pop("area"))

        _thresholds = d.pop("thresholds", UNSET)
        thresholds: (
            list[DeclaredObservabilityKindsAdditionalPropertyThresholdsItem] | Unset
        ) = UNSET
        if _thresholds is not UNSET:
            thresholds = []
            for thresholds_item_data in _thresholds:
                thresholds_item = DeclaredObservabilityKindsAdditionalPropertyThresholdsItem.from_dict(
                    thresholds_item_data
                )

                thresholds.append(thresholds_item)

        def _parse_typical_latency(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        typical_latency = _parse_typical_latency(d.pop("typicalLatency", UNSET))

        def _parse_notes(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        notes = _parse_notes(d.pop("notes", UNSET))

        declared_observability_kinds_additional_property = cls(
            area=area,
            thresholds=thresholds,
            typical_latency=typical_latency,
            notes=notes,
        )

        declared_observability_kinds_additional_property.additional_properties = d
        return declared_observability_kinds_additional_property

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
