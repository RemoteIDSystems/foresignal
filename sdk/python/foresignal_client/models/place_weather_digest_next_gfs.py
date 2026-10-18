from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherDigestNextGfs")


@_attrs_define
class PlaceWeatherDigestNextGfs:
    """GFS maxima

    Attributes:
        status (WeatherSectionStatus): Status of a derived weather section; anything but ok means unknown, never clear
        as_of (datetime.datetime | Unset): Time of the last good data; present only while status is not ok, fixed while
            not ok
        status_text (str | Unset): Explanation for non-ok statuses
        run (str | Unset):
        gust_max_ms (float | Unset):
        precip_mm (float | Unset):
        valid_time (datetime.datetime | Unset):
    """

    status: WeatherSectionStatus
    as_of: datetime.datetime | Unset = UNSET
    status_text: str | Unset = UNSET
    run: str | Unset = UNSET
    gust_max_ms: float | Unset = UNSET
    precip_mm: float | Unset = UNSET
    valid_time: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        status_text = self.status_text

        run = self.run

        gust_max_ms = self.gust_max_ms

        precip_mm = self.precip_mm

        valid_time: str | Unset = UNSET
        if not isinstance(self.valid_time, Unset):
            valid_time = self.valid_time.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if status_text is not UNSET:
            field_dict["statusText"] = status_text
        if run is not UNSET:
            field_dict["run"] = run
        if gust_max_ms is not UNSET:
            field_dict["gustMaxMs"] = gust_max_ms
        if precip_mm is not UNSET:
            field_dict["precipMm"] = precip_mm
        if valid_time is not UNSET:
            field_dict["validTime"] = valid_time

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        status = WeatherSectionStatus(d.pop("status"))

        _as_of = d.pop("asOf", UNSET)
        as_of: datetime.datetime | Unset
        if isinstance(_as_of, Unset):
            as_of = UNSET
        else:
            as_of = datetime.datetime.fromisoformat(_as_of)

        status_text = d.pop("statusText", UNSET)

        run = d.pop("run", UNSET)

        gust_max_ms = d.pop("gustMaxMs", UNSET)

        precip_mm = d.pop("precipMm", UNSET)

        _valid_time = d.pop("validTime", UNSET)
        valid_time: datetime.datetime | Unset
        if isinstance(_valid_time, Unset):
            valid_time = UNSET
        else:
            valid_time = datetime.datetime.fromisoformat(_valid_time)

        place_weather_digest_next_gfs = cls(
            status=status,
            as_of=as_of,
            status_text=status_text,
            run=run,
            gust_max_ms=gust_max_ms,
            precip_mm=precip_mm,
            valid_time=valid_time,
        )

        place_weather_digest_next_gfs.additional_properties = d
        return place_weather_digest_next_gfs

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
