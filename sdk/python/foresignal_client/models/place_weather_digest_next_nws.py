from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherDigestNextNws")


@_attrs_define
class PlaceWeatherDigestNextNws:
    """NWS gridpoint summary

    Attributes:
        status (WeatherSectionStatus): Status of a derived weather section; anything but ok means unknown, never clear
        as_of (datetime.datetime | Unset): Time of the last good data; present only while status is not ok, fixed while
            not ok
        status_text (str | Unset): Explanation for non-ok statuses
        pop_max_24_h (int | Unset):
        qpf_24_h_mm (float | Unset):
        gust_max_ms_24_h (float | Unset):
        t_max_c24_h (float | Unset):
        t_min_c24_h (float | Unset):
        updated_at (datetime.datetime | Unset):
    """

    status: WeatherSectionStatus
    as_of: datetime.datetime | Unset = UNSET
    status_text: str | Unset = UNSET
    pop_max_24_h: int | Unset = UNSET
    qpf_24_h_mm: float | Unset = UNSET
    gust_max_ms_24_h: float | Unset = UNSET
    t_max_c24_h: float | Unset = UNSET
    t_min_c24_h: float | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        status_text = self.status_text

        pop_max_24_h = self.pop_max_24_h

        qpf_24_h_mm = self.qpf_24_h_mm

        gust_max_ms_24_h = self.gust_max_ms_24_h

        t_max_c24_h = self.t_max_c24_h

        t_min_c24_h = self.t_min_c24_h

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

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
        if pop_max_24_h is not UNSET:
            field_dict["popMax24h"] = pop_max_24_h
        if qpf_24_h_mm is not UNSET:
            field_dict["qpf24hMm"] = qpf_24_h_mm
        if gust_max_ms_24_h is not UNSET:
            field_dict["gustMaxMs24h"] = gust_max_ms_24_h
        if t_max_c24_h is not UNSET:
            field_dict["tMaxC24h"] = t_max_c24_h
        if t_min_c24_h is not UNSET:
            field_dict["tMinC24h"] = t_min_c24_h
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

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

        pop_max_24_h = d.pop("popMax24h", UNSET)

        qpf_24_h_mm = d.pop("qpf24hMm", UNSET)

        gust_max_ms_24_h = d.pop("gustMaxMs24h", UNSET)

        t_max_c24_h = d.pop("tMaxC24h", UNSET)

        t_min_c24_h = d.pop("tMinC24h", UNSET)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        place_weather_digest_next_nws = cls(
            status=status,
            as_of=as_of,
            status_text=status_text,
            pop_max_24_h=pop_max_24_h,
            qpf_24_h_mm=qpf_24_h_mm,
            gust_max_ms_24_h=gust_max_ms_24_h,
            t_max_c24_h=t_max_c24_h,
            t_min_c24_h=t_min_c24_h,
            updated_at=updated_at,
        )

        place_weather_digest_next_nws.additional_properties = d
        return place_weather_digest_next_nws

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
