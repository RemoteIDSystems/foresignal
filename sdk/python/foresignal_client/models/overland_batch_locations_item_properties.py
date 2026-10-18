from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OverlandBatchLocationsItemProperties")


@_attrs_define
class OverlandBatchLocationsItemProperties:
    """Overland point properties

    Attributes:
        timestamp (datetime.datetime | Unset):
        horizontal_accuracy (float | Unset):
        altitude (float | Unset):
        speed (float | Unset): m/s
        course (float | Unset):
        motion (list[str] | Unset):
        battery_level (float | Unset):
        battery_state (str | Unset):
        wifi (str | Unset):
        device_id (str | Unset): → externalId (a device key forces its bound id)
    """

    timestamp: datetime.datetime | Unset = UNSET
    horizontal_accuracy: float | Unset = UNSET
    altitude: float | Unset = UNSET
    speed: float | Unset = UNSET
    course: float | Unset = UNSET
    motion: list[str] | Unset = UNSET
    battery_level: float | Unset = UNSET
    battery_state: str | Unset = UNSET
    wifi: str | Unset = UNSET
    device_id: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        timestamp: str | Unset = UNSET
        if not isinstance(self.timestamp, Unset):
            timestamp = self.timestamp.isoformat()

        horizontal_accuracy = self.horizontal_accuracy

        altitude = self.altitude

        speed = self.speed

        course = self.course

        motion: list[str] | Unset = UNSET
        if not isinstance(self.motion, Unset):
            motion = self.motion

        battery_level = self.battery_level

        battery_state = self.battery_state

        wifi = self.wifi

        device_id = self.device_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if timestamp is not UNSET:
            field_dict["timestamp"] = timestamp
        if horizontal_accuracy is not UNSET:
            field_dict["horizontal_accuracy"] = horizontal_accuracy
        if altitude is not UNSET:
            field_dict["altitude"] = altitude
        if speed is not UNSET:
            field_dict["speed"] = speed
        if course is not UNSET:
            field_dict["course"] = course
        if motion is not UNSET:
            field_dict["motion"] = motion
        if battery_level is not UNSET:
            field_dict["battery_level"] = battery_level
        if battery_state is not UNSET:
            field_dict["battery_state"] = battery_state
        if wifi is not UNSET:
            field_dict["wifi"] = wifi
        if device_id is not UNSET:
            field_dict["device_id"] = device_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _timestamp = d.pop("timestamp", UNSET)
        timestamp: datetime.datetime | Unset
        if isinstance(_timestamp, Unset):
            timestamp = UNSET
        else:
            timestamp = datetime.datetime.fromisoformat(_timestamp)

        horizontal_accuracy = d.pop("horizontal_accuracy", UNSET)

        altitude = d.pop("altitude", UNSET)

        speed = d.pop("speed", UNSET)

        course = d.pop("course", UNSET)

        motion = cast(list[str], d.pop("motion", UNSET))

        battery_level = d.pop("battery_level", UNSET)

        battery_state = d.pop("battery_state", UNSET)

        wifi = d.pop("wifi", UNSET)

        device_id = d.pop("device_id", UNSET)

        overland_batch_locations_item_properties = cls(
            timestamp=timestamp,
            horizontal_accuracy=horizontal_accuracy,
            altitude=altitude,
            speed=speed,
            course=course,
            motion=motion,
            battery_level=battery_level,
            battery_state=battery_state,
            wifi=wifi,
            device_id=device_id,
        )

        overland_batch_locations_item_properties.additional_properties = d
        return overland_batch_locations_item_properties

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
