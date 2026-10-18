from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="PlaceWeatherCell")


@_attrs_define
class PlaceWeatherCell:
    """Radar-cell ETA section

    Attributes:
        status (WeatherSectionStatus): Status of a derived weather section; anything but ok means unknown, never clear
        as_of (datetime.datetime | Unset): Time of the last good data; present only while status is not ok, fixed while
            not ok
        status_text (str | Unset): Explanation for non-ok statuses
        eta_50_min (int | Unset): Minutes until a ≥ 50 dBZ core enters the radius, 5-min steps (999 = none approaching)
        eta_cell_id (str | Unset):
        eta_speed_kmh (float | Unset):
        eta_bearing_deg (int | Unset):
    """

    status: WeatherSectionStatus
    as_of: datetime.datetime | Unset = UNSET
    status_text: str | Unset = UNSET
    eta_50_min: int | Unset = UNSET
    eta_cell_id: str | Unset = UNSET
    eta_speed_kmh: float | Unset = UNSET
    eta_bearing_deg: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        status_text = self.status_text

        eta_50_min = self.eta_50_min

        eta_cell_id = self.eta_cell_id

        eta_speed_kmh = self.eta_speed_kmh

        eta_bearing_deg = self.eta_bearing_deg

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
        if eta_50_min is not UNSET:
            field_dict["eta50Min"] = eta_50_min
        if eta_cell_id is not UNSET:
            field_dict["etaCellId"] = eta_cell_id
        if eta_speed_kmh is not UNSET:
            field_dict["etaSpeedKmh"] = eta_speed_kmh
        if eta_bearing_deg is not UNSET:
            field_dict["etaBearingDeg"] = eta_bearing_deg

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

        eta_50_min = d.pop("eta50Min", UNSET)

        eta_cell_id = d.pop("etaCellId", UNSET)

        eta_speed_kmh = d.pop("etaSpeedKmh", UNSET)

        eta_bearing_deg = d.pop("etaBearingDeg", UNSET)

        place_weather_cell = cls(
            status=status,
            as_of=as_of,
            status_text=status_text,
            eta_50_min=eta_50_min,
            eta_cell_id=eta_cell_id,
            eta_speed_kmh=eta_speed_kmh,
            eta_bearing_deg=eta_bearing_deg,
        )

        place_weather_cell.additional_properties = d
        return place_weather_cell

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
