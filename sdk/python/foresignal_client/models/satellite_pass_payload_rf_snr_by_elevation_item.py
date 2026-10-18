from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="SatellitePassPayloadRfSnrByElevationItem")


@_attrs_define
class SatellitePassPayloadRfSnrByElevationItem:
    """SNR sample

    Attributes:
        elevation_deg (float):
        snr_db (float):
    """

    elevation_deg: float
    snr_db: float
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        elevation_deg = self.elevation_deg

        snr_db = self.snr_db

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "elevationDeg": elevation_deg,
                "snrDb": snr_db,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        elevation_deg = d.pop("elevationDeg")

        snr_db = d.pop("snrDb")

        satellite_pass_payload_rf_snr_by_elevation_item = cls(
            elevation_deg=elevation_deg,
            snr_db=snr_db,
        )

        satellite_pass_payload_rf_snr_by_elevation_item.additional_properties = d
        return satellite_pass_payload_rf_snr_by_elevation_item

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
