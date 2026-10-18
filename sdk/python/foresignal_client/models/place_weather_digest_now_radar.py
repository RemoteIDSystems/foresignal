from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherDigestNowRadar")


@_attrs_define
class PlaceWeatherDigestNowRadar:
    """Radar now

    Attributes:
        status (WeatherSectionStatus): Status of a derived weather section; anything but ok means unknown, never clear
        as_of (datetime.datetime | Unset): Time of the last good data; present only while status is not ok, fixed while
            not ok
        status_text (str | Unset): Explanation for non-ok statuses
        max_dbz_in_radius (float | Unset):
        nearest_50_km (float | Unset):
    """

    status: WeatherSectionStatus
    as_of: datetime.datetime | Unset = UNSET
    status_text: str | Unset = UNSET
    max_dbz_in_radius: float | Unset = UNSET
    nearest_50_km: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        status_text = self.status_text

        max_dbz_in_radius = self.max_dbz_in_radius

        nearest_50_km = self.nearest_50_km

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
        if max_dbz_in_radius is not UNSET:
            field_dict["maxDbzInRadius"] = max_dbz_in_radius
        if nearest_50_km is not UNSET:
            field_dict["nearest50Km"] = nearest_50_km

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

        max_dbz_in_radius = d.pop("maxDbzInRadius", UNSET)

        nearest_50_km = d.pop("nearest50Km", UNSET)

        place_weather_digest_now_radar = cls(
            status=status,
            as_of=as_of,
            status_text=status_text,
            max_dbz_in_radius=max_dbz_in_radius,
            nearest_50_km=nearest_50_km,
        )

        place_weather_digest_now_radar.additional_properties = d
        return place_weather_digest_now_radar

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
