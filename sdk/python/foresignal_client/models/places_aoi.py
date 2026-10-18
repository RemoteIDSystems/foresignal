from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="PlacesAoi")


@_attrs_define
class PlacesAoi:
    """Union of place areas used by weather sources as their area of interest (036)

    Attributes:
        version (int):
        envelope (list[float]): [minLon, minLat, maxLon, maxLat] (WGS84)
        place_count (int):
        is_default (bool): True when there are no places and weather.defaultAoi is used
        geometry (Geometry | None | Unset): MultiPolygon: each place area buffered to at least max(radiusKm,
            weather.aoiBufferKm)
        buffer_km (float | Unset): weather.aoiBufferKm (default 150)
    """

    version: int
    envelope: list[float]
    place_count: int
    is_default: bool
    geometry: Geometry | None | Unset = UNSET
    buffer_km: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        version = self.version

        envelope = self.envelope

        place_count = self.place_count

        is_default = self.is_default

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        buffer_km = self.buffer_km

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "version": version,
                "envelope": envelope,
                "placeCount": place_count,
                "isDefault": is_default,
            }
        )
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if buffer_km is not UNSET:
            field_dict["bufferKm"] = buffer_km

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        version = d.pop("version")

        envelope = cast(list[float], d.pop("envelope"))

        place_count = d.pop("placeCount")

        is_default = d.pop("isDefault")

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

        buffer_km = d.pop("bufferKm", UNSET)

        places_aoi = cls(
            version=version,
            envelope=envelope,
            place_count=place_count,
            is_default=is_default,
            geometry=geometry,
            buffer_km=buffer_km,
        )

        places_aoi.additional_properties = d
        return places_aoi

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
