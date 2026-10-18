from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="ChronicleArea")


@_attrs_define
class ChronicleArea:
    """A chronicle's area: geometry, places or region codes (all optional)

    Attributes:
        geometry (Geometry | None | Unset):
        place_ids (list[str] | Unset):
        region_codes (list[str] | Unset): 040 region codes (ISO 3166)
        buffer_km (float | None | Unset):
    """

    geometry: Geometry | None | Unset = UNSET
    place_ids: list[str] | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    buffer_km: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        place_ids: list[str] | Unset = UNSET
        if not isinstance(self.place_ids, Unset):
            place_ids = self.place_ids

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        buffer_km: float | None | Unset
        if isinstance(self.buffer_km, Unset):
            buffer_km = UNSET
        else:
            buffer_km = self.buffer_km

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if place_ids is not UNSET:
            field_dict["placeIds"] = place_ids
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if buffer_km is not UNSET:
            field_dict["bufferKm"] = buffer_km

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        place_ids = cast(list[str], d.pop("placeIds", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        def _parse_buffer_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        buffer_km = _parse_buffer_km(d.pop("bufferKm", UNSET))

        chronicle_area = cls(
            geometry=geometry,
            place_ids=place_ids,
            region_codes=region_codes,
            buffer_km=buffer_km,
        )

        chronicle_area.additional_properties = d
        return chronicle_area

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
