from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OsmAndQuery")


@_attrs_define
class OsmAndQuery:
    """OsmAnd/Traccar query or form fields accepted by the `osmand` format (039)

    Attributes:
        id (str | Unset):
        deviceid (str | Unset):
        lat (float | Unset):
        lon (float | Unset):
        location (str | Unset): lat,lon
        timestamp (str | Unset): epoch s, epoch ms, ISO, or `yyyy-MM-dd HH:mm:ss` UTC
        speed (float | Unset): knots
        bearing (float | Unset):
        heading (float | Unset):
        altitude (float | Unset):
        accuracy (float | Unset):
        batt (float | Unset):
        charge (bool | Unset):
        valid (bool | Unset): false → rejected
    """

    id: str | Unset = UNSET
    deviceid: str | Unset = UNSET
    lat: float | Unset = UNSET
    lon: float | Unset = UNSET
    location: str | Unset = UNSET
    timestamp: str | Unset = UNSET
    speed: float | Unset = UNSET
    bearing: float | Unset = UNSET
    heading: float | Unset = UNSET
    altitude: float | Unset = UNSET
    accuracy: float | Unset = UNSET
    batt: float | Unset = UNSET
    charge: bool | Unset = UNSET
    valid: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        deviceid = self.deviceid

        lat = self.lat

        lon = self.lon

        location = self.location

        timestamp = self.timestamp

        speed = self.speed

        bearing = self.bearing

        heading = self.heading

        altitude = self.altitude

        accuracy = self.accuracy

        batt = self.batt

        charge = self.charge

        valid = self.valid

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if id is not UNSET:
            field_dict["id"] = id
        if deviceid is not UNSET:
            field_dict["deviceid"] = deviceid
        if lat is not UNSET:
            field_dict["lat"] = lat
        if lon is not UNSET:
            field_dict["lon"] = lon
        if location is not UNSET:
            field_dict["location"] = location
        if timestamp is not UNSET:
            field_dict["timestamp"] = timestamp
        if speed is not UNSET:
            field_dict["speed"] = speed
        if bearing is not UNSET:
            field_dict["bearing"] = bearing
        if heading is not UNSET:
            field_dict["heading"] = heading
        if altitude is not UNSET:
            field_dict["altitude"] = altitude
        if accuracy is not UNSET:
            field_dict["accuracy"] = accuracy
        if batt is not UNSET:
            field_dict["batt"] = batt
        if charge is not UNSET:
            field_dict["charge"] = charge
        if valid is not UNSET:
            field_dict["valid"] = valid

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id", UNSET)

        deviceid = d.pop("deviceid", UNSET)

        lat = d.pop("lat", UNSET)

        lon = d.pop("lon", UNSET)

        location = d.pop("location", UNSET)

        timestamp = d.pop("timestamp", UNSET)

        speed = d.pop("speed", UNSET)

        bearing = d.pop("bearing", UNSET)

        heading = d.pop("heading", UNSET)

        altitude = d.pop("altitude", UNSET)

        accuracy = d.pop("accuracy", UNSET)

        batt = d.pop("batt", UNSET)

        charge = d.pop("charge", UNSET)

        valid = d.pop("valid", UNSET)

        osm_and_query = cls(
            id=id,
            deviceid=deviceid,
            lat=lat,
            lon=lon,
            location=location,
            timestamp=timestamp,
            speed=speed,
            bearing=bearing,
            heading=heading,
            altitude=altitude,
            accuracy=accuracy,
            batt=batt,
            charge=charge,
            valid=valid,
        )

        osm_and_query.additional_properties = d
        return osm_and_query

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
