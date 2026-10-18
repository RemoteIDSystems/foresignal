from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="KinematicState")


@_attrs_define
class KinematicState:
    """Kinematic state of a record from head ⋈ live (114)

    Attributes:
        source_id (str):
        external_id (str):
        position (Geometry): GeoJSON geometry (RFC 7946), WGS84
        observed_at (datetime.datetime):
        kind (str | Unset):
        speed_mps (float | None | Unset):
        course_deg (float | None | Unset):
        altitude_m (float | None | Unset):
        derived (bool | Unset): Speed and course derived from the last two persisted positions
        stale (bool | Unset): Older than 2 × the source TTL
    """

    source_id: str
    external_id: str
    position: Geometry
    observed_at: datetime.datetime
    kind: str | Unset = UNSET
    speed_mps: float | None | Unset = UNSET
    course_deg: float | None | Unset = UNSET
    altitude_m: float | None | Unset = UNSET
    derived: bool | Unset = UNSET
    stale: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        source_id = self.source_id

        external_id = self.external_id

        position = self.position.to_dict()

        observed_at = self.observed_at.isoformat()

        kind = self.kind

        speed_mps: float | None | Unset
        if isinstance(self.speed_mps, Unset):
            speed_mps = UNSET
        else:
            speed_mps = self.speed_mps

        course_deg: float | None | Unset
        if isinstance(self.course_deg, Unset):
            course_deg = UNSET
        else:
            course_deg = self.course_deg

        altitude_m: float | None | Unset
        if isinstance(self.altitude_m, Unset):
            altitude_m = UNSET
        else:
            altitude_m = self.altitude_m

        derived = self.derived

        stale = self.stale

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "externalId": external_id,
                "position": position,
                "observedAt": observed_at,
            }
        )
        if kind is not UNSET:
            field_dict["kind"] = kind
        if speed_mps is not UNSET:
            field_dict["speedMps"] = speed_mps
        if course_deg is not UNSET:
            field_dict["courseDeg"] = course_deg
        if altitude_m is not UNSET:
            field_dict["altitudeM"] = altitude_m
        if derived is not UNSET:
            field_dict["derived"] = derived
        if stale is not UNSET:
            field_dict["stale"] = stale

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        position = Geometry.from_dict(d.pop("position"))

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        kind = d.pop("kind", UNSET)

        def _parse_speed_mps(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        speed_mps = _parse_speed_mps(d.pop("speedMps", UNSET))

        def _parse_course_deg(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        course_deg = _parse_course_deg(d.pop("courseDeg", UNSET))

        def _parse_altitude_m(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        altitude_m = _parse_altitude_m(d.pop("altitudeM", UNSET))

        derived = d.pop("derived", UNSET)

        stale = d.pop("stale", UNSET)

        kinematic_state = cls(
            source_id=source_id,
            external_id=external_id,
            position=position,
            observed_at=observed_at,
            kind=kind,
            speed_mps=speed_mps,
            course_deg=course_deg,
            altitude_m=altitude_m,
            derived=derived,
            stale=stale,
        )

        kinematic_state.additional_properties = d
        return kinematic_state

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
