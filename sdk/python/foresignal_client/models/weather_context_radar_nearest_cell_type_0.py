from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.weather_context_radar_nearest_cell_type_0_motion import (
        WeatherContextRadarNearestCellType0Motion,
    )


T = TypeVar("T", bound="WeatherContextRadarNearestCellType0")


@_attrs_define
class WeatherContextRadarNearestCellType0:
    """Nearest radar cell

    Attributes:
        id (str): radar-cell externalId
        distance_km (float | Unset):
        motion (WeatherContextRadarNearestCellType0Motion | Unset): Cell motion
        mesh_max_mm (float | None | Unset):
    """

    id: str
    distance_km: float | Unset = UNSET
    motion: WeatherContextRadarNearestCellType0Motion | Unset = UNSET
    mesh_max_mm: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        distance_km = self.distance_km

        motion: dict[str, Any] | Unset = UNSET
        if not isinstance(self.motion, Unset):
            motion = self.motion.to_dict()

        mesh_max_mm: float | None | Unset
        if isinstance(self.mesh_max_mm, Unset):
            mesh_max_mm = UNSET
        else:
            mesh_max_mm = self.mesh_max_mm

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
            }
        )
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if motion is not UNSET:
            field_dict["motion"] = motion
        if mesh_max_mm is not UNSET:
            field_dict["meshMaxMm"] = mesh_max_mm

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.weather_context_radar_nearest_cell_type_0_motion import (
            WeatherContextRadarNearestCellType0Motion,
        )

        d = dict(src_dict)
        id = d.pop("id")

        distance_km = d.pop("distanceKm", UNSET)

        _motion = d.pop("motion", UNSET)
        motion: WeatherContextRadarNearestCellType0Motion | Unset
        if isinstance(_motion, Unset):
            motion = UNSET
        else:
            motion = WeatherContextRadarNearestCellType0Motion.from_dict(_motion)

        def _parse_mesh_max_mm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mesh_max_mm = _parse_mesh_max_mm(d.pop("meshMaxMm", UNSET))

        weather_context_radar_nearest_cell_type_0 = cls(
            id=id,
            distance_km=distance_km,
            motion=motion,
            mesh_max_mm=mesh_max_mm,
        )

        weather_context_radar_nearest_cell_type_0.additional_properties = d
        return weather_context_radar_nearest_cell_type_0

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
