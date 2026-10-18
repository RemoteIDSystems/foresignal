from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="WeatherContextRadarNearestCellType0Motion")


@_attrs_define
class WeatherContextRadarNearestCellType0Motion:
    """Cell motion

    Attributes:
        speed_kmh (float | Unset):
        bearing_deg (int | Unset):
        method (str | Unset):  Example: centroid-v1.
    """

    speed_kmh: float | Unset = UNSET
    bearing_deg: int | Unset = UNSET
    method: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        speed_kmh = self.speed_kmh

        bearing_deg = self.bearing_deg

        method = self.method

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if speed_kmh is not UNSET:
            field_dict["speedKmh"] = speed_kmh
        if bearing_deg is not UNSET:
            field_dict["bearingDeg"] = bearing_deg
        if method is not UNSET:
            field_dict["method"] = method

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        speed_kmh = d.pop("speedKmh", UNSET)

        bearing_deg = d.pop("bearingDeg", UNSET)

        method = d.pop("method", UNSET)

        weather_context_radar_nearest_cell_type_0_motion = cls(
            speed_kmh=speed_kmh,
            bearing_deg=bearing_deg,
            method=method,
        )

        weather_context_radar_nearest_cell_type_0_motion.additional_properties = d
        return weather_context_radar_nearest_cell_type_0_motion

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
