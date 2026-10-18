from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OsmAndLocationLocationBattery")


@_attrs_define
class OsmAndLocationLocationBattery:
    """Battery

    Attributes:
        level (float | Unset):
        is_charging (bool | Unset):
    """

    level: float | Unset = UNSET
    is_charging: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        level = self.level

        is_charging = self.is_charging

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if level is not UNSET:
            field_dict["level"] = level
        if is_charging is not UNSET:
            field_dict["is_charging"] = is_charging

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        level = d.pop("level", UNSET)

        is_charging = d.pop("is_charging", UNSET)

        osm_and_location_location_battery = cls(
            level=level,
            is_charging=is_charging,
        )

        osm_and_location_location_battery.additional_properties = d
        return osm_and_location_location_battery

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
