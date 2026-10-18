from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="DeclaredObservabilityKindsAdditionalPropertyThresholdsItem")


@_attrs_define
class DeclaredObservabilityKindsAdditionalPropertyThresholdsItem:
    """Detection threshold, e.g. {field: mag, min: 2.5, area: US}

    Attributes:
        field (str):
        min_ (float):
        area (str | Unset):
    """

    field: str
    min_: float
    area: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        field = self.field

        min_ = self.min_

        area = self.area

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "field": field,
                "min": min_,
            }
        )
        if area is not UNSET:
            field_dict["area"] = area

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        field = d.pop("field")

        min_ = d.pop("min")

        area = d.pop("area", UNSET)

        declared_observability_kinds_additional_property_thresholds_item = cls(
            field=field,
            min_=min_,
            area=area,
        )

        declared_observability_kinds_additional_property_thresholds_item.additional_properties = d
        return declared_observability_kinds_additional_property_thresholds_item

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
