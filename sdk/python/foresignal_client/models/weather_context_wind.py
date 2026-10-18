from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="WeatherContextWind")


@_attrs_define
class WeatherContextWind:
    """10-m wind of the newest complete HRRR run (else GFS) at the step nearest the time, with run, validTime and step;
    gustMs from the same model's gust product

        Attributes:
            status (WeatherSectionStatus): Status of a derived weather section; anything but ok means unknown, never clear
            as_of (datetime.datetime | Unset): Time of the last good data; present only while status is not ok, fixed while
                not ok
            status_text (str | Unset): Explanation for non-ok statuses
            product_id (str | Unset):
            speed_ms (float | Unset):
            gust_ms (float | None | Unset):
            from_deg (int | Unset):
            run (str | Unset):
            valid_time (datetime.datetime | Unset):
            step (int | Unset):
    """

    status: WeatherSectionStatus
    as_of: datetime.datetime | Unset = UNSET
    status_text: str | Unset = UNSET
    product_id: str | Unset = UNSET
    speed_ms: float | Unset = UNSET
    gust_ms: float | None | Unset = UNSET
    from_deg: int | Unset = UNSET
    run: str | Unset = UNSET
    valid_time: datetime.datetime | Unset = UNSET
    step: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        as_of: str | Unset = UNSET
        if not isinstance(self.as_of, Unset):
            as_of = self.as_of.isoformat()

        status_text = self.status_text

        product_id = self.product_id

        speed_ms = self.speed_ms

        gust_ms: float | None | Unset
        if isinstance(self.gust_ms, Unset):
            gust_ms = UNSET
        else:
            gust_ms = self.gust_ms

        from_deg = self.from_deg

        run = self.run

        valid_time: str | Unset = UNSET
        if not isinstance(self.valid_time, Unset):
            valid_time = self.valid_time.isoformat()

        step = self.step

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
        if product_id is not UNSET:
            field_dict["productId"] = product_id
        if speed_ms is not UNSET:
            field_dict["speedMs"] = speed_ms
        if gust_ms is not UNSET:
            field_dict["gustMs"] = gust_ms
        if from_deg is not UNSET:
            field_dict["fromDeg"] = from_deg
        if run is not UNSET:
            field_dict["run"] = run
        if valid_time is not UNSET:
            field_dict["validTime"] = valid_time
        if step is not UNSET:
            field_dict["step"] = step

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

        product_id = d.pop("productId", UNSET)

        speed_ms = d.pop("speedMs", UNSET)

        def _parse_gust_ms(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        gust_ms = _parse_gust_ms(d.pop("gustMs", UNSET))

        from_deg = d.pop("fromDeg", UNSET)

        run = d.pop("run", UNSET)

        _valid_time = d.pop("validTime", UNSET)
        valid_time: datetime.datetime | Unset
        if isinstance(_valid_time, Unset):
            valid_time = UNSET
        else:
            valid_time = datetime.datetime.fromisoformat(_valid_time)

        step = d.pop("step", UNSET)

        weather_context_wind = cls(
            status=status,
            as_of=as_of,
            status_text=status_text,
            product_id=product_id,
            speed_ms=speed_ms,
            gust_ms=gust_ms,
            from_deg=from_deg,
            run=run,
            valid_time=valid_time,
            step=step,
        )

        weather_context_wind.additional_properties = d
        return weather_context_wind

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
