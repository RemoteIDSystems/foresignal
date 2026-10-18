from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.weather_context_status import WeatherContextStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.weather_context_lightning import WeatherContextLightning
    from ..models.weather_context_precip import WeatherContextPrecip
    from ..models.weather_context_radar import WeatherContextRadar
    from ..models.weather_context_smoke import WeatherContextSmoke
    from ..models.weather_context_warnings import WeatherContextWarnings
    from ..models.weather_context_wind import WeatherContextWind


T = TypeVar("T", bound="WeatherContext")


@_attrs_define
class WeatherContext:
    """Deterministic weather facts around an alert geometry (056), computed from stored frames and record heads at the
    alert's observed_at within a 500 ms budget (a section that misses it is unavailable); non-ok sections carry
    statusText and no values and mean unknown, never clear

        Attributes:
            status (WeatherContextStatus): unavailable when the computation failed or timed out (sections omitted)
            at (datetime.datetime | Unset): Time computed for (the alert's observed_at)
            computed_at (datetime.datetime | Unset):
            radar (WeatherContextRadar | Unset): Radar section
            lightning (WeatherContextLightning | Unset): GOES GLM flashes within 20 km over the 15 minutes before the time;
                stale when no GLM file ended within 2 minutes of it. GLM's footprint over Texas is about 8–10 km and its
                detection efficiency is below 100%, so a zero count is advisory
            precip (WeatherContextPrecip | Unset): Precipitation section
            warnings (WeatherContextWarnings | Unset): Active NWS alerts intersecting the geometry
            wind (WeatherContextWind | Unset): 10-m wind of the newest complete HRRR run (else GFS) at the step nearest the
                time, with run, validTime and step; gustMs from the same model's gust product
            smoke (WeatherContextSmoke | Unset): HRRR near-surface smoke (µg/m³) at the step nearest the time
            summary (str | Unset): One deterministic sentence; non-ok sections are worded as unknown, never as absence
                Example: Radar max 52 dBZ within 10 km; cell moving ENE at 45 km/h; lightning data stale since 14:30 UTC.
                Automated — not an official warning..
    """

    status: WeatherContextStatus
    at: datetime.datetime | Unset = UNSET
    computed_at: datetime.datetime | Unset = UNSET
    radar: WeatherContextRadar | Unset = UNSET
    lightning: WeatherContextLightning | Unset = UNSET
    precip: WeatherContextPrecip | Unset = UNSET
    warnings: WeatherContextWarnings | Unset = UNSET
    wind: WeatherContextWind | Unset = UNSET
    smoke: WeatherContextSmoke | Unset = UNSET
    summary: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        status = self.status.value

        at: str | Unset = UNSET
        if not isinstance(self.at, Unset):
            at = self.at.isoformat()

        computed_at: str | Unset = UNSET
        if not isinstance(self.computed_at, Unset):
            computed_at = self.computed_at.isoformat()

        radar: dict[str, Any] | Unset = UNSET
        if not isinstance(self.radar, Unset):
            radar = self.radar.to_dict()

        lightning: dict[str, Any] | Unset = UNSET
        if not isinstance(self.lightning, Unset):
            lightning = self.lightning.to_dict()

        precip: dict[str, Any] | Unset = UNSET
        if not isinstance(self.precip, Unset):
            precip = self.precip.to_dict()

        warnings: dict[str, Any] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings.to_dict()

        wind: dict[str, Any] | Unset = UNSET
        if not isinstance(self.wind, Unset):
            wind = self.wind.to_dict()

        smoke: dict[str, Any] | Unset = UNSET
        if not isinstance(self.smoke, Unset):
            smoke = self.smoke.to_dict()

        summary = self.summary

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "status": status,
            }
        )
        if at is not UNSET:
            field_dict["at"] = at
        if computed_at is not UNSET:
            field_dict["computedAt"] = computed_at
        if radar is not UNSET:
            field_dict["radar"] = radar
        if lightning is not UNSET:
            field_dict["lightning"] = lightning
        if precip is not UNSET:
            field_dict["precip"] = precip
        if warnings is not UNSET:
            field_dict["warnings"] = warnings
        if wind is not UNSET:
            field_dict["wind"] = wind
        if smoke is not UNSET:
            field_dict["smoke"] = smoke
        if summary is not UNSET:
            field_dict["summary"] = summary

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.weather_context_lightning import WeatherContextLightning
        from ..models.weather_context_precip import WeatherContextPrecip
        from ..models.weather_context_radar import WeatherContextRadar
        from ..models.weather_context_smoke import WeatherContextSmoke
        from ..models.weather_context_warnings import WeatherContextWarnings
        from ..models.weather_context_wind import WeatherContextWind

        d = dict(src_dict)
        status = WeatherContextStatus(d.pop("status"))

        _at = d.pop("at", UNSET)
        at: datetime.datetime | Unset
        if isinstance(_at, Unset):
            at = UNSET
        else:
            at = datetime.datetime.fromisoformat(_at)

        _computed_at = d.pop("computedAt", UNSET)
        computed_at: datetime.datetime | Unset
        if isinstance(_computed_at, Unset):
            computed_at = UNSET
        else:
            computed_at = datetime.datetime.fromisoformat(_computed_at)

        _radar = d.pop("radar", UNSET)
        radar: WeatherContextRadar | Unset
        if isinstance(_radar, Unset):
            radar = UNSET
        else:
            radar = WeatherContextRadar.from_dict(_radar)

        _lightning = d.pop("lightning", UNSET)
        lightning: WeatherContextLightning | Unset
        if isinstance(_lightning, Unset):
            lightning = UNSET
        else:
            lightning = WeatherContextLightning.from_dict(_lightning)

        _precip = d.pop("precip", UNSET)
        precip: WeatherContextPrecip | Unset
        if isinstance(_precip, Unset):
            precip = UNSET
        else:
            precip = WeatherContextPrecip.from_dict(_precip)

        _warnings = d.pop("warnings", UNSET)
        warnings: WeatherContextWarnings | Unset
        if isinstance(_warnings, Unset):
            warnings = UNSET
        else:
            warnings = WeatherContextWarnings.from_dict(_warnings)

        _wind = d.pop("wind", UNSET)
        wind: WeatherContextWind | Unset
        if isinstance(_wind, Unset):
            wind = UNSET
        else:
            wind = WeatherContextWind.from_dict(_wind)

        _smoke = d.pop("smoke", UNSET)
        smoke: WeatherContextSmoke | Unset
        if isinstance(_smoke, Unset):
            smoke = UNSET
        else:
            smoke = WeatherContextSmoke.from_dict(_smoke)

        summary = d.pop("summary", UNSET)

        weather_context = cls(
            status=status,
            at=at,
            computed_at=computed_at,
            radar=radar,
            lightning=lightning,
            precip=precip,
            warnings=warnings,
            wind=wind,
            smoke=smoke,
            summary=summary,
        )

        weather_context.additional_properties = d
        return weather_context

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
