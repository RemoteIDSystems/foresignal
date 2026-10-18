from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="PolStay")


@_attrs_define
class PolStay:
    """A stay: consecutive fixes within stayRadiusM spanning at least minDwell (114)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        subject_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        arrive (datetime.datetime):
        depart (datetime.datetime):
        centroid (Geometry): GeoJSON geometry (RFC 7946), WGS84
        place_id (None | str | Unset):
        cluster_id (int | None | Unset): null = unclustered (noise)
        fixes (int | Unset):
    """

    id: str
    subject_id: str
    arrive: datetime.datetime
    depart: datetime.datetime
    centroid: Geometry
    place_id: None | str | Unset = UNSET
    cluster_id: int | None | Unset = UNSET
    fixes: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        subject_id = self.subject_id

        arrive = self.arrive.isoformat()

        depart = self.depart.isoformat()

        centroid = self.centroid.to_dict()

        place_id: None | str | Unset
        if isinstance(self.place_id, Unset):
            place_id = UNSET
        else:
            place_id = self.place_id

        cluster_id: int | None | Unset
        if isinstance(self.cluster_id, Unset):
            cluster_id = UNSET
        else:
            cluster_id = self.cluster_id

        fixes = self.fixes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "subjectId": subject_id,
                "arrive": arrive,
                "depart": depart,
                "centroid": centroid,
            }
        )
        if place_id is not UNSET:
            field_dict["placeId"] = place_id
        if cluster_id is not UNSET:
            field_dict["clusterId"] = cluster_id
        if fixes is not UNSET:
            field_dict["fixes"] = fixes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        id = d.pop("id")

        subject_id = d.pop("subjectId")

        arrive = datetime.datetime.fromisoformat(d.pop("arrive"))

        depart = datetime.datetime.fromisoformat(d.pop("depart"))

        centroid = Geometry.from_dict(d.pop("centroid"))

        def _parse_place_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        place_id = _parse_place_id(d.pop("placeId", UNSET))

        def _parse_cluster_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        cluster_id = _parse_cluster_id(d.pop("clusterId", UNSET))

        fixes = d.pop("fixes", UNSET)

        pol_stay = cls(
            id=id,
            subject_id=subject_id,
            arrive=arrive,
            depart=depart,
            centroid=centroid,
            place_id=place_id,
            cluster_id=cluster_id,
            fixes=fixes,
        )

        pol_stay.additional_properties = d
        return pol_stay

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
