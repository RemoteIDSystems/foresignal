from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.storm_swath_payload_status import StormSwathPayloadStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="StormSwathPayload")


@_attrs_define
class StormSwathPayload:
    """Kind `storm-swath` (externalId = track id): union of the cell polygons along a track

    Attributes:
        track_id (str):
        duration_min (float):
        mesh_max_mm (float | None | Unset):
        status (StormSwathPayloadStatus | Unset):
    """

    track_id: str
    duration_min: float
    mesh_max_mm: float | None | Unset = UNSET
    status: StormSwathPayloadStatus | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        track_id = self.track_id

        duration_min = self.duration_min

        mesh_max_mm: float | None | Unset
        if isinstance(self.mesh_max_mm, Unset):
            mesh_max_mm = UNSET
        else:
            mesh_max_mm = self.mesh_max_mm

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "trackId": track_id,
                "durationMin": duration_min,
            }
        )
        if mesh_max_mm is not UNSET:
            field_dict["meshMaxMm"] = mesh_max_mm
        if status is not UNSET:
            field_dict["status"] = status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        track_id = d.pop("trackId")

        duration_min = d.pop("durationMin")

        def _parse_mesh_max_mm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mesh_max_mm = _parse_mesh_max_mm(d.pop("meshMaxMm", UNSET))

        _status = d.pop("status", UNSET)
        status: StormSwathPayloadStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = StormSwathPayloadStatus(_status)

        storm_swath_payload = cls(
            track_id=track_id,
            duration_min=duration_min,
            mesh_max_mm=mesh_max_mm,
            status=status,
        )

        storm_swath_payload.additional_properties = d
        return storm_swath_payload

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
