from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="WeatherContextPrecip")


@_attrs_define
class WeatherContextPrecip:
    """Precipitation section

    Attributes:
        status (WeatherSectionStatus): Status of a derived weather section; anything but ok means unknown, never clear
        as_of (datetime.datetime | Unset): Time of the last good data; present only while status is not ok, fixed while
            not ok
        status_text (str | Unset): Explanation for non-ok statuses
        rate_mm_h (float | Unset):
        qpe_1_h_mm (float | None | Unset):
    """

    status: WeatherSectionStatus
    as_of: datetime.datetime | Unset = UNSET
    status_text: str | Unset = UNSET
    rate_mm_h: float | Unset = UNSET
    qpe_1_h_mm: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        status_text = self.status_text

        rate_mm_h = self.rate_mm_h

        qpe_1_h_mm: float | None | Unset
        if isinstance(self.qpe_1_h_mm, Unset):
            qpe_1_h_mm = UNSET
        else:
            qpe_1_h_mm = self.qpe_1_h_mm

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
        if qpe_1_h_mm is not UNSET:
            field_dict["qpe1hMm"] = qpe_1_h_mm

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

        def _parse_qpe_1_h_mm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        qpe_1_h_mm = _parse_qpe_1_h_mm(d.pop("qpe1hMm", UNSET))

        weather_context_precip = cls(
            status=status,
            as_of=as_of,
            status_text=status_text,
            rate_mm_h=rate_mm_h,
            qpe_1_h_mm=qpe_1_h_mm,
        )

        weather_context_precip.additional_properties = d
        return weather_context_precip

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
