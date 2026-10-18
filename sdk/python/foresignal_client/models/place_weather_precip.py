from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherPrecip")


@_attrs_define
class PlaceWeatherPrecip:
    """Precipitation section

    Attributes:
        status (WeatherSectionStatus): Status of a derived weather section; anything but ok means unknown, never clear
        as_of (datetime.datetime | Unset): Time of the last good data; present only while status is not ok, fixed while
            not ok
        status_text (str | Unset): Explanation for non-ok statuses
        rate_mm_h (float | Unset): 1 mm/h steps
        qpe_1_h_mm_max (float | Unset): 053
        qpe_24_h_mm_max (float | Unset): 053
        ffg_ratio_1_h_pct_max (float | Unset): 053; ≥ 100 means rainfall exceeds flash-flood guidance
    """

    status: WeatherSectionStatus
    as_of: datetime.datetime | Unset = UNSET
    status_text: str | Unset = UNSET
    rate_mm_h: float | Unset = UNSET
    qpe_1_h_mm_max: float | Unset = UNSET
    qpe_24_h_mm_max: float | Unset = UNSET
    ffg_ratio_1_h_pct_max: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        status_text = self.status_text

        rate_mm_h = self.rate_mm_h

        qpe_1_h_mm_max = self.qpe_1_h_mm_max

        qpe_24_h_mm_max = self.qpe_24_h_mm_max

        ffg_ratio_1_h_pct_max = self.ffg_ratio_1_h_pct_max

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
        if rate_mm_h is not UNSET:
            field_dict["rateMmH"] = rate_mm_h
        if qpe_1_h_mm_max is not UNSET:
            field_dict["qpe1hMmMax"] = qpe_1_h_mm_max
        if qpe_24_h_mm_max is not UNSET:
            field_dict["qpe24hMmMax"] = qpe_24_h_mm_max
        if ffg_ratio_1_h_pct_max is not UNSET:
            field_dict["ffgRatio1hPctMax"] = ffg_ratio_1_h_pct_max

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

        rate_mm_h = d.pop("rateMmH", UNSET)

        qpe_1_h_mm_max = d.pop("qpe1hMmMax", UNSET)

        qpe_24_h_mm_max = d.pop("qpe24hMmMax", UNSET)

        ffg_ratio_1_h_pct_max = d.pop("ffgRatio1hPctMax", UNSET)

        place_weather_precip = cls(
            status=status,
            as_of=as_of,
            status_text=status_text,
            rate_mm_h=rate_mm_h,
            qpe_1_h_mm_max=qpe_1_h_mm_max,
            qpe_24_h_mm_max=qpe_24_h_mm_max,
            ffg_ratio_1_h_pct_max=ffg_ratio_1_h_pct_max,
        )

        place_weather_precip.additional_properties = d
        return place_weather_precip

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
