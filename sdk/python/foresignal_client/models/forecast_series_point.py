from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ForecastSeriesPoint")


@_attrs_define
class ForecastSeriesPoint:
    """One step of a cached forecast series

    Attributes:
        time (datetime.datetime):
        temp_c (float | None | Unset):
        wind_speed_ms (float | None | Unset):
        wind_from_deg (int | None | Unset):
        gust_ms (float | None | Unset):
        pop_pct (int | None | Unset):
        precip_mm (float | None | Unset):
        sky_cover_pct (int | None | Unset):
        smoke_ug_m3 (float | None | Unset):
    """

    time: datetime.datetime
    temp_c: float | None | Unset = UNSET
    wind_speed_ms: float | None | Unset = UNSET
    wind_from_deg: int | None | Unset = UNSET
    gust_ms: float | None | Unset = UNSET
    pop_pct: int | None | Unset = UNSET
    precip_mm: float | None | Unset = UNSET
    sky_cover_pct: int | None | Unset = UNSET
    smoke_ug_m3: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        time = self.time.isoformat()

        temp_c: float | None | Unset
        if isinstance(self.temp_c, Unset):
            temp_c = UNSET
        else:
            temp_c = self.temp_c

        wind_speed_ms: float | None | Unset
        if isinstance(self.wind_speed_ms, Unset):
            wind_speed_ms = UNSET
        else:
            wind_speed_ms = self.wind_speed_ms

        wind_from_deg: int | None | Unset
        if isinstance(self.wind_from_deg, Unset):
            wind_from_deg = UNSET
        else:
            wind_from_deg = self.wind_from_deg

        gust_ms: float | None | Unset
        if isinstance(self.gust_ms, Unset):
            gust_ms = UNSET
        else:
            gust_ms = self.gust_ms

        pop_pct: int | None | Unset
        if isinstance(self.pop_pct, Unset):
            pop_pct = UNSET
        else:
            pop_pct = self.pop_pct

        precip_mm: float | None | Unset
        if isinstance(self.precip_mm, Unset):
            precip_mm = UNSET
        else:
            precip_mm = self.precip_mm

        sky_cover_pct: int | None | Unset
        if isinstance(self.sky_cover_pct, Unset):
            sky_cover_pct = UNSET
        else:
            sky_cover_pct = self.sky_cover_pct

        smoke_ug_m3: float | None | Unset
        if isinstance(self.smoke_ug_m3, Unset):
            smoke_ug_m3 = UNSET
        else:
            smoke_ug_m3 = self.smoke_ug_m3

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "time": time,
            }
        )
        if temp_c is not UNSET:
            field_dict["tempC"] = temp_c
        if wind_speed_ms is not UNSET:
            field_dict["windSpeedMs"] = wind_speed_ms
        if wind_from_deg is not UNSET:
            field_dict["windFromDeg"] = wind_from_deg
        if gust_ms is not UNSET:
            field_dict["gustMs"] = gust_ms
        if pop_pct is not UNSET:
            field_dict["popPct"] = pop_pct
        if precip_mm is not UNSET:
            field_dict["precipMm"] = precip_mm
        if sky_cover_pct is not UNSET:
            field_dict["skyCoverPct"] = sky_cover_pct
        if smoke_ug_m3 is not UNSET:
            field_dict["smokeUgM3"] = smoke_ug_m3

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        time = datetime.datetime.fromisoformat(d.pop("time"))

        def _parse_temp_c(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        temp_c = _parse_temp_c(d.pop("tempC", UNSET))

        def _parse_wind_speed_ms(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        wind_speed_ms = _parse_wind_speed_ms(d.pop("windSpeedMs", UNSET))

        def _parse_wind_from_deg(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        wind_from_deg = _parse_wind_from_deg(d.pop("windFromDeg", UNSET))

        def _parse_gust_ms(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        gust_ms = _parse_gust_ms(d.pop("gustMs", UNSET))

        def _parse_pop_pct(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        pop_pct = _parse_pop_pct(d.pop("popPct", UNSET))

        def _parse_precip_mm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        precip_mm = _parse_precip_mm(d.pop("precipMm", UNSET))

        def _parse_sky_cover_pct(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        sky_cover_pct = _parse_sky_cover_pct(d.pop("skyCoverPct", UNSET))

        def _parse_smoke_ug_m3(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        smoke_ug_m3 = _parse_smoke_ug_m3(d.pop("smokeUgM3", UNSET))

        forecast_series_point = cls(
            time=time,
            temp_c=temp_c,
            wind_speed_ms=wind_speed_ms,
            wind_from_deg=wind_from_deg,
            gust_ms=gust_ms,
            pop_pct=pop_pct,
            precip_mm=precip_mm,
            sky_cover_pct=sky_cover_pct,
            smoke_ug_m3=smoke_ug_m3,
        )

        forecast_series_point.additional_properties = d
        return forecast_series_point

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
