from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherForecastGfs")


@_attrs_define
class PlaceWeatherForecastGfs:
    """GFS 24-h maxima

    Attributes:
        status (WeatherSectionStatus | Unset): Status of a derived weather section; anything but ok means unknown, never
            clear
        as_of (datetime.datetime | Unset): Time of the last good data; present only while status is not ok, fixed while
            not ok
        status_text (str | Unset): Explanation for non-ok statuses
        gust_max_24_h (float | Unset): m/s
        precip_24_h_mm (float | Unset):
    """

    status: WeatherSectionStatus | Unset = UNSET
    as_of: datetime.datetime | Unset = UNSET
    status_text: str | Unset = UNSET
    gust_max_24_h: float | Unset = UNSET
    precip_24_h_mm: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        status_text = self.status_text

        gust_max_24_h = self.gust_max_24_h

        precip_24_h_mm = self.precip_24_h_mm

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if status is not UNSET:
            field_dict["status"] = status
        if as_of is not UNSET:
            field_dict["asOf"] = as_of
        if status_text is not UNSET:
            field_dict["statusText"] = status_text
        if gust_max_24_h is not UNSET:
            field_dict["gustMax24h"] = gust_max_24_h
        if precip_24_h_mm is not UNSET:
            field_dict["precip24hMm"] = precip_24_h_mm

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _status = d.pop("status", UNSET)
        status: WeatherSectionStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = WeatherSectionStatus(_status)

        _as_of = d.pop("asOf", UNSET)
        as_of: datetime.datetime | Unset
        if isinstance(_as_of, Unset):
            as_of = UNSET
        else:
            as_of = datetime.datetime.fromisoformat(_as_of)

        status_text = d.pop("statusText", UNSET)

        gust_max_24_h = d.pop("gustMax24h", UNSET)

        precip_24_h_mm = d.pop("precip24hMm", UNSET)

        place_weather_forecast_gfs = cls(
            status=status,
            as_of=as_of,
            status_text=status_text,
            gust_max_24_h=gust_max_24_h,
            precip_24_h_mm=precip_24_h_mm,
        )

        place_weather_forecast_gfs.additional_properties = d
        return place_weather_forecast_gfs

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
