from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OsmAndLocationLocationCoords")


@_attrs_define
class OsmAndLocationLocationCoords:
    """Coordinates

    Attributes:
        latitude (float):
        longitude (float):
        speed (float | Unset): m/s
        heading (float | Unset):
        accuracy (float | Unset):
        altitude (float | Unset):
    """

    latitude: float
    longitude: float
    speed: float | Unset = UNSET
    heading: float | Unset = UNSET
    accuracy: float | Unset = UNSET
    altitude: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        latitude = self.latitude

        longitude = self.longitude

        speed = self.speed

        heading = self.heading

        accuracy = self.accuracy

        altitude = self.altitude

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "latitude": latitude,
                "longitude": longitude,
            }
        )
        if speed is not UNSET:
            field_dict["speed"] = speed
        if heading is not UNSET:
            field_dict["heading"] = heading
        if accuracy is not UNSET:
            field_dict["accuracy"] = accuracy
        if altitude is not UNSET:
            field_dict["altitude"] = altitude

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        latitude = d.pop("latitude")

        longitude = d.pop("longitude")

        speed = d.pop("speed", UNSET)

        heading = d.pop("heading", UNSET)

        accuracy = d.pop("accuracy", UNSET)

        altitude = d.pop("altitude", UNSET)

        osm_and_location_location_coords = cls(
            latitude=latitude,
            longitude=longitude,
            speed=speed,
            heading=heading,
            accuracy=accuracy,
            altitude=altitude,
        )

        osm_and_location_location_coords.additional_properties = d
        return osm_and_location_location_coords

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
