from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="StatsResponseThresholdsItem")


@_attrs_define
class StatsResponseThresholdsItem:
    """Area above a threshold

    Attributes:
        threshold (float):
        area_km_2 (float):
    """

    threshold: float
    area_km_2: float
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        threshold = self.threshold

        area_km_2 = self.area_km_2

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "threshold": threshold,
                "areaKm2": area_km_2,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        threshold = d.pop("threshold")

        area_km_2 = d.pop("areaKm2")

        stats_response_thresholds_item = cls(
            threshold=threshold,
            area_km_2=area_km_2,
        )

        stats_response_thresholds_item.additional_properties = d
        return stats_response_thresholds_item

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
