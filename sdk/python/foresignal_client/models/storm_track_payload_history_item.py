from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="StormTrackPayloadHistoryItem")


@_attrs_define
class StormTrackPayloadHistoryItem:
    """Past position

    Attributes:
        time (datetime.datetime):
        lon (float):
        lat (float):
    """

    time: datetime.datetime
    lon: float
    lat: float
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        time = self.time.isoformat()

        lon = self.lon

        lat = self.lat

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "time": time,
                "lon": lon,
                "lat": lat,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        time = datetime.datetime.fromisoformat(d.pop("time"))

        lon = d.pop("lon")

        lat = d.pop("lat")

        storm_track_payload_history_item = cls(
            time=time,
            lon=lon,
            lat=lat,
        )

        storm_track_payload_history_item.additional_properties = d
        return storm_track_payload_history_item

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
