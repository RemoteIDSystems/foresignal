from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="StormTrackPayloadCell")


@_attrs_define
class StormTrackPayloadCell:
    """Current cell summary

    Attributes:
        max_dbz (float | Unset):
        area_km_2 (float | Unset):
        mesh_max_mm (float | None | Unset):
        flash_rate (float | None | Unset):
    """

    max_dbz: float | Unset = UNSET
    area_km_2: float | Unset = UNSET
    mesh_max_mm: float | None | Unset = UNSET
    flash_rate: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        max_dbz = self.max_dbz

        area_km_2 = self.area_km_2

        mesh_max_mm: float | None | Unset
        if isinstance(self.mesh_max_mm, Unset):
            mesh_max_mm = UNSET
        else:
            mesh_max_mm = self.mesh_max_mm

        flash_rate: float | None | Unset
        if isinstance(self.flash_rate, Unset):
            flash_rate = UNSET
        else:
            flash_rate = self.flash_rate

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if max_dbz is not UNSET:
            field_dict["maxDbz"] = max_dbz
        if area_km_2 is not UNSET:
            field_dict["areaKm2"] = area_km_2
        if mesh_max_mm is not UNSET:
            field_dict["meshMaxMm"] = mesh_max_mm
        if flash_rate is not UNSET:
            field_dict["flashRate"] = flash_rate

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        max_dbz = d.pop("maxDbz", UNSET)

        area_km_2 = d.pop("areaKm2", UNSET)

        def _parse_mesh_max_mm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mesh_max_mm = _parse_mesh_max_mm(d.pop("meshMaxMm", UNSET))

        def _parse_flash_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        flash_rate = _parse_flash_rate(d.pop("flashRate", UNSET))

        storm_track_payload_cell = cls(
            max_dbz=max_dbz,
            area_km_2=area_km_2,
            mesh_max_mm=mesh_max_mm,
            flash_rate=flash_rate,
        )

        storm_track_payload_cell.additional_properties = d
        return storm_track_payload_cell

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
