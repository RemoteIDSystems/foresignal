from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="PolPlace")


@_attrs_define
class PolPlace:
    """A learned place (DBSCAN cluster of stays); outlives the TRACK window (114)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        subject_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        cluster_id (int):
        centroid (Geometry): GeoJSON geometry (RFC 7946), WGS84
        radius_m (float): p90 distance of member stays
        visits (int):
        total_dwell_minutes (float):
        hour_of_week (list[float]): Dwell minutes per hour of week (168 buckets, Monday 00:00 operator time first)
        first_seen (datetime.datetime):
        last_seen (datetime.datetime):
        label (None | str | Unset): Operator-set label, e.g. home
        private (bool | Unset):
    """

    id: str
    subject_id: str
    cluster_id: int
    centroid: Geometry
    radius_m: float
    visits: int
    total_dwell_minutes: float
    hour_of_week: list[float]
    first_seen: datetime.datetime
    last_seen: datetime.datetime
    label: None | str | Unset = UNSET
    private: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        subject_id = self.subject_id

        cluster_id = self.cluster_id

        centroid = self.centroid.to_dict()

        radius_m = self.radius_m

        visits = self.visits

        total_dwell_minutes = self.total_dwell_minutes

        hour_of_week = self.hour_of_week

        first_seen = self.first_seen.isoformat()

        last_seen = self.last_seen.isoformat()

        label: None | str | Unset
        if isinstance(self.label, Unset):
            label = UNSET
        else:
            label = self.label

        private = self.private

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "subjectId": subject_id,
                "clusterId": cluster_id,
                "centroid": centroid,
                "radiusM": radius_m,
                "visits": visits,
                "totalDwellMinutes": total_dwell_minutes,
                "hourOfWeek": hour_of_week,
                "firstSeen": first_seen,
                "lastSeen": last_seen,
            }
        )
        if label is not UNSET:
            field_dict["label"] = label
        if private is not UNSET:
            field_dict["private"] = private

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        id = d.pop("id")

        subject_id = d.pop("subjectId")

        cluster_id = d.pop("clusterId")

        centroid = Geometry.from_dict(d.pop("centroid"))

        radius_m = d.pop("radiusM")

        visits = d.pop("visits")

        total_dwell_minutes = d.pop("totalDwellMinutes")

        hour_of_week = cast(list[float], d.pop("hourOfWeek"))

        first_seen = datetime.datetime.fromisoformat(d.pop("firstSeen"))

        last_seen = datetime.datetime.fromisoformat(d.pop("lastSeen"))

        def _parse_label(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        label = _parse_label(d.pop("label", UNSET))

        private = d.pop("private", UNSET)

        pol_place = cls(
            id=id,
            subject_id=subject_id,
            cluster_id=cluster_id,
            centroid=centroid,
            radius_m=radius_m,
            visits=visits,
            total_dwell_minutes=total_dwell_minutes,
            hour_of_week=hour_of_week,
            first_seen=first_seen,
            last_seen=last_seen,
            label=label,
            private=private,
        )

        pol_place.additional_properties = d
        return pol_place

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
