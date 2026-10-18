from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.osm_and_location_location_battery import OsmAndLocationLocationBattery
    from ..models.osm_and_location_location_coords import OsmAndLocationLocationCoords


T = TypeVar("T", bound="OsmAndLocationLocation")


@_attrs_define
class OsmAndLocationLocation:
    """Fix

    Attributes:
        coords (OsmAndLocationLocationCoords): Coordinates
        timestamp (str | Unset): ISO-8601
        battery (OsmAndLocationLocationBattery | Unset): Battery
        is_moving (bool | Unset):
        event (str | Unset):
    """

    coords: OsmAndLocationLocationCoords
    timestamp: str | Unset = UNSET
    battery: OsmAndLocationLocationBattery | Unset = UNSET
    is_moving: bool | Unset = UNSET
    event: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        coords = self.coords.to_dict()

        timestamp = self.timestamp

        battery: dict[str, Any] | Unset = UNSET
        if not isinstance(self.battery, Unset):
            battery = self.battery.to_dict()

        is_moving = self.is_moving

        event = self.event

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "coords": coords,
            }
        )
        if timestamp is not UNSET:
            field_dict["timestamp"] = timestamp
        if battery is not UNSET:
            field_dict["battery"] = battery
        if is_moving is not UNSET:
            field_dict["is_moving"] = is_moving
        if event is not UNSET:
            field_dict["event"] = event

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.osm_and_location_location_battery import (
            OsmAndLocationLocationBattery,
        )
        from ..models.osm_and_location_location_coords import (
            OsmAndLocationLocationCoords,
        )

        d = dict(src_dict)
        coords = OsmAndLocationLocationCoords.from_dict(d.pop("coords"))

        timestamp = d.pop("timestamp", UNSET)

        _battery = d.pop("battery", UNSET)
        battery: OsmAndLocationLocationBattery | Unset
        if isinstance(_battery, Unset):
            battery = UNSET
        else:
            battery = OsmAndLocationLocationBattery.from_dict(_battery)

        is_moving = d.pop("is_moving", UNSET)

        event = d.pop("event", UNSET)

        osm_and_location_location = cls(
            coords=coords,
            timestamp=timestamp,
            battery=battery,
            is_moving=is_moving,
            event=event,
        )

        osm_and_location_location.additional_properties = d
        return osm_and_location_location

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
