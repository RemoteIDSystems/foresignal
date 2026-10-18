from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.storm_track_payload_motion_method import StormTrackPayloadMotionMethod

T = TypeVar("T", bound="StormTrackPayloadMotion")


@_attrs_define
class StormTrackPayloadMotion:
    """Storm motion

    Attributes:
        speed_kmh (float):
        bearing_deg (float):
        method (StormTrackPayloadMotionMethod):
    """

    speed_kmh: float
    bearing_deg: float
    method: StormTrackPayloadMotionMethod
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        speed_kmh = self.speed_kmh

        bearing_deg = self.bearing_deg

        method = self.method.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "speedKmh": speed_kmh,
                "bearingDeg": bearing_deg,
                "method": method,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        speed_kmh = d.pop("speedKmh")

        bearing_deg = d.pop("bearingDeg")

        method = StormTrackPayloadMotionMethod(d.pop("method"))

        storm_track_payload_motion = cls(
            speed_kmh=speed_kmh,
            bearing_deg=bearing_deg,
            method=method,
        )

        storm_track_payload_motion.additional_properties = d
        return storm_track_payload_motion

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
