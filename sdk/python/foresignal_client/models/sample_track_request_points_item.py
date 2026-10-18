from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="SampleTrackRequestPointsItem")


@_attrs_define
class SampleTrackRequestPointsItem:
    """Track point

    Attributes:
        lon (float):
        lat (float):
        time (datetime.datetime):
    """

    lon: float
    lat: float
    time: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        lon = self.lon

        lat = self.lat

        time = self.time.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "lon": lon,
                "lat": lat,
                "time": time,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        lon = d.pop("lon")

        lat = d.pop("lat")

        time = datetime.datetime.fromisoformat(d.pop("time"))

        sample_track_request_points_item = cls(
            lon=lon,
            lat=lat,
            time=time,
        )

        sample_track_request_points_item.additional_properties = d
        return sample_track_request_points_item

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
