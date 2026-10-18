from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.radar_cell_payload_motion import RadarCellPayloadMotion


T = TypeVar("T", bound="RadarCellPayload")


@_attrs_define
class RadarCellPayload:
    """Payload of `radar-cell` records (wx-cells, externalId `c-{ulid}`)

    Attributes:
        max_dbz (float):
        area_km_2 (float):
        mesh_max (float | None | Unset):
        glm_flashes_5_m (int | Unset):
        motion (RadarCellPayloadMotion | Unset): Cell motion
        automated (bool | Unset):
        not_a_warning (bool | Unset):
    """

    max_dbz: float
    area_km_2: float
    mesh_max: float | None | Unset = UNSET
    glm_flashes_5_m: int | Unset = UNSET
    motion: RadarCellPayloadMotion | Unset = UNSET
    automated: bool | Unset = UNSET
    not_a_warning: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        max_dbz = self.max_dbz

        area_km_2 = self.area_km_2

        mesh_max: float | None | Unset
        if isinstance(self.mesh_max, Unset):
            mesh_max = UNSET
        else:
            mesh_max = self.mesh_max

        glm_flashes_5_m = self.glm_flashes_5_m

        motion: dict[str, Any] | Unset = UNSET
        if not isinstance(self.motion, Unset):
            motion = self.motion.to_dict()

        automated = self.automated

        not_a_warning = self.not_a_warning

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "maxDbz": max_dbz,
                "areaKm2": area_km_2,
            }
        )
        if mesh_max is not UNSET:
            field_dict["meshMax"] = mesh_max
        if glm_flashes_5_m is not UNSET:
            field_dict["glmFlashes5m"] = glm_flashes_5_m
        if motion is not UNSET:
            field_dict["motion"] = motion
        if automated is not UNSET:
            field_dict["automated"] = automated
        if not_a_warning is not UNSET:
            field_dict["notAWarning"] = not_a_warning

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.radar_cell_payload_motion import RadarCellPayloadMotion

        d = dict(src_dict)
        max_dbz = d.pop("maxDbz")

        area_km_2 = d.pop("areaKm2")

        def _parse_mesh_max(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mesh_max = _parse_mesh_max(d.pop("meshMax", UNSET))

        glm_flashes_5_m = d.pop("glmFlashes5m", UNSET)

        _motion = d.pop("motion", UNSET)
        motion: RadarCellPayloadMotion | Unset
        if isinstance(_motion, Unset):
            motion = UNSET
        else:
            motion = RadarCellPayloadMotion.from_dict(_motion)

        automated = d.pop("automated", UNSET)

        not_a_warning = d.pop("notAWarning", UNSET)

        radar_cell_payload = cls(
            max_dbz=max_dbz,
            area_km_2=area_km_2,
            mesh_max=mesh_max,
            glm_flashes_5_m=glm_flashes_5_m,
            motion=motion,
            automated=automated,
            not_a_warning=not_a_warning,
        )

        radar_cell_payload.additional_properties = d
        return radar_cell_payload

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
