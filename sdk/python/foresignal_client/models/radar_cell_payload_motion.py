from __future__ import annotations

from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RadarCellPayloadMotion")


@_attrs_define
class RadarCellPayloadMotion:
    """Cell motion

    Attributes:
        method (Literal['centroid-v1']):
        speed_kmh (float | Unset):
        bearing_deg (int | Unset):
    """

    method: Literal["centroid-v1"]
    speed_kmh: float | Unset = UNSET
    bearing_deg: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        method = self.method

        speed_kmh = self.speed_kmh

        bearing_deg = self.bearing_deg

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "method": method,
            }
        )
        if speed_kmh is not UNSET:
            field_dict["speedKmh"] = speed_kmh
        if bearing_deg is not UNSET:
            field_dict["bearingDeg"] = bearing_deg

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        method = cast(Literal["centroid-v1"], d.pop("method"))
        if method != "centroid-v1":
            raise ValueError(f"method must match const 'centroid-v1', got '{method}'")

        speed_kmh = d.pop("speedKmh", UNSET)

        bearing_deg = d.pop("bearingDeg", UNSET)

        radar_cell_payload_motion = cls(
            method=method,
            speed_kmh=speed_kmh,
            bearing_deg=bearing_deg,
        )

        radar_cell_payload_motion.additional_properties = d
        return radar_cell_payload_motion

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
