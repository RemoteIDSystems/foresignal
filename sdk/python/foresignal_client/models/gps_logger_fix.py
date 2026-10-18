from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="GpsLoggerFix")


@_attrs_define
class GpsLoggerFix:
    """GPSLogger custom-URL fields accepted by the `gpslogger` format (039); names are case-insensitive. Template:
    `lat=%LAT&lon=%LON&time=%TIME&acc=%ACC&alt=%ALT&spd=%SPD&dir=%DIR&batt=%BATT&dev=%SER`

        Attributes:
            lat (float):
            lon (float):
            time (str | Unset):
            acc (float | Unset):
            alt (float | Unset):
            spd (float | Unset): m/s
            dir_ (float | Unset):
            batt (float | Unset):
            dev (str | Unset): → externalId
    """

    lat: float
    lon: float
    time: str | Unset = UNSET
    acc: float | Unset = UNSET
    alt: float | Unset = UNSET
    spd: float | Unset = UNSET
    dir_: float | Unset = UNSET
    batt: float | Unset = UNSET
    dev: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        lat = self.lat

        lon = self.lon

        time = self.time

        acc = self.acc

        alt = self.alt

        spd = self.spd

        dir_ = self.dir_

        batt = self.batt

        dev = self.dev

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "lat": lat,
                "lon": lon,
            }
        )
        if time is not UNSET:
            field_dict["time"] = time
        if acc is not UNSET:
            field_dict["acc"] = acc
        if alt is not UNSET:
            field_dict["alt"] = alt
        if spd is not UNSET:
            field_dict["spd"] = spd
        if dir_ is not UNSET:
            field_dict["dir"] = dir_
        if batt is not UNSET:
            field_dict["batt"] = batt
        if dev is not UNSET:
            field_dict["dev"] = dev

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        lat = d.pop("lat")

        lon = d.pop("lon")

        time = d.pop("time", UNSET)

        acc = d.pop("acc", UNSET)

        alt = d.pop("alt", UNSET)

        spd = d.pop("spd", UNSET)

        dir_ = d.pop("dir", UNSET)

        batt = d.pop("batt", UNSET)

        dev = d.pop("dev", UNSET)

        gps_logger_fix = cls(
            lat=lat,
            lon=lon,
            time=time,
            acc=acc,
            alt=alt,
            spd=spd,
            dir_=dir_,
            batt=batt,
            dev=dev,
        )

        gps_logger_fix.additional_properties = d
        return gps_logger_fix

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
