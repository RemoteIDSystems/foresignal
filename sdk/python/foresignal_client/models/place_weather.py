from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.place_weather_aurora_type_0 import PlaceWeatherAuroraType0
    from ..models.place_weather_cell import PlaceWeatherCell
    from ..models.place_weather_forecast import PlaceWeatherForecast
    from ..models.place_weather_hail import PlaceWeatherHail
    from ..models.place_weather_heat_type_0 import PlaceWeatherHeatType0
    from ..models.place_weather_lightning import PlaceWeatherLightning
    from ..models.place_weather_marine_type_0 import PlaceWeatherMarineType0
    from ..models.place_weather_precip import PlaceWeatherPrecip
    from ..models.place_weather_radar import PlaceWeatherRadar
    from ..models.place_weather_smoke import PlaceWeatherSmoke
    from ..models.place_weather_uv_type_0 import PlaceWeatherUvType0


T = TypeVar("T", bound="PlaceWeather")


@_attrs_define
class PlaceWeather:
    """Payload of `place-weather` records (wx-places, externalId `place:{id}`); metric keys are omitted before first data,
    never null

        Attributes:
            place (str): Place name
            radius_km (float):
            automated (bool):
            not_a_warning (bool):
            radar (PlaceWeatherRadar | Unset): MRMS reflectivity section
            hail (PlaceWeatherHail | Unset): MRMS MESH section
            precip (PlaceWeatherPrecip | Unset): Precipitation section
            lightning (PlaceWeatherLightning | Unset): GLM lightning section (GLM footprint ~8–10 km; all-clears advisory)
            cell (PlaceWeatherCell | Unset): Radar-cell ETA section
            smoke (PlaceWeatherSmoke | Unset): HRRR smoke section
            forecast (PlaceWeatherForecast | Unset): Forecast summaries
            marine (None | PlaceWeatherMarineType0 | Unset): 106: places tagged `coastal` only
            aurora (None | PlaceWeatherAuroraType0 | Unset): 106: only when |lat| ≥ 40°
            heat (None | PlaceWeatherHeatType0 | Unset): 106: WPC HeatRisk (experimental NWS product)
            uv (None | PlaceWeatherUvType0 | Unset): 106: EPA UV (needs place.attributes.zip)
    """

    place: str
    radius_km: float
    automated: bool
    not_a_warning: bool
    radar: PlaceWeatherRadar | Unset = UNSET
    hail: PlaceWeatherHail | Unset = UNSET
    precip: PlaceWeatherPrecip | Unset = UNSET
    lightning: PlaceWeatherLightning | Unset = UNSET
    cell: PlaceWeatherCell | Unset = UNSET
    smoke: PlaceWeatherSmoke | Unset = UNSET
    forecast: PlaceWeatherForecast | Unset = UNSET
    marine: None | PlaceWeatherMarineType0 | Unset = UNSET
    aurora: None | PlaceWeatherAuroraType0 | Unset = UNSET
    heat: None | PlaceWeatherHeatType0 | Unset = UNSET
    uv: None | PlaceWeatherUvType0 | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.place_weather_aurora_type_0 import PlaceWeatherAuroraType0
        from ..models.place_weather_heat_type_0 import PlaceWeatherHeatType0
        from ..models.place_weather_marine_type_0 import PlaceWeatherMarineType0
        from ..models.place_weather_uv_type_0 import PlaceWeatherUvType0

        place = self.place

        radius_km = self.radius_km

        automated = self.automated

        not_a_warning = self.not_a_warning

        radar: dict[str, Any] | Unset = UNSET
        if not isinstance(self.radar, Unset):
            radar = self.radar.to_dict()

        hail: dict[str, Any] | Unset = UNSET
        if not isinstance(self.hail, Unset):
            hail = self.hail.to_dict()

        precip: dict[str, Any] | Unset = UNSET
        if not isinstance(self.precip, Unset):
            precip = self.precip.to_dict()

        lightning: dict[str, Any] | Unset = UNSET
        if not isinstance(self.lightning, Unset):
            lightning = self.lightning.to_dict()

        cell: dict[str, Any] | Unset = UNSET
        if not isinstance(self.cell, Unset):
            cell = self.cell.to_dict()

        smoke: dict[str, Any] | Unset = UNSET
        if not isinstance(self.smoke, Unset):
            smoke = self.smoke.to_dict()

        forecast: dict[str, Any] | Unset = UNSET
        if not isinstance(self.forecast, Unset):
            forecast = self.forecast.to_dict()

        marine: dict[str, Any] | None | Unset
        if isinstance(self.marine, Unset):
            marine = UNSET
        elif isinstance(self.marine, PlaceWeatherMarineType0):
            marine = self.marine.to_dict()
        else:
            marine = self.marine

        aurora: dict[str, Any] | None | Unset
        if isinstance(self.aurora, Unset):
            aurora = UNSET
        elif isinstance(self.aurora, PlaceWeatherAuroraType0):
            aurora = self.aurora.to_dict()
        else:
            aurora = self.aurora

        heat: dict[str, Any] | None | Unset
        if isinstance(self.heat, Unset):
            heat = UNSET
        elif isinstance(self.heat, PlaceWeatherHeatType0):
            heat = self.heat.to_dict()
        else:
            heat = self.heat

        uv: dict[str, Any] | None | Unset
        if isinstance(self.uv, Unset):
            uv = UNSET
        elif isinstance(self.uv, PlaceWeatherUvType0):
            uv = self.uv.to_dict()
        else:
            uv = self.uv

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "place": place,
                "radiusKm": radius_km,
                "automated": automated,
                "notAWarning": not_a_warning,
            }
        )
        if radar is not UNSET:
            field_dict["radar"] = radar
        if hail is not UNSET:
            field_dict["hail"] = hail
        if precip is not UNSET:
            field_dict["precip"] = precip
        if lightning is not UNSET:
            field_dict["lightning"] = lightning
        if cell is not UNSET:
            field_dict["cell"] = cell
        if smoke is not UNSET:
            field_dict["smoke"] = smoke
        if forecast is not UNSET:
            field_dict["forecast"] = forecast
        if marine is not UNSET:
            field_dict["marine"] = marine
        if aurora is not UNSET:
            field_dict["aurora"] = aurora
        if heat is not UNSET:
            field_dict["heat"] = heat
        if uv is not UNSET:
            field_dict["uv"] = uv

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.place_weather_aurora_type_0 import PlaceWeatherAuroraType0
        from ..models.place_weather_cell import PlaceWeatherCell
        from ..models.place_weather_forecast import PlaceWeatherForecast
        from ..models.place_weather_hail import PlaceWeatherHail
        from ..models.place_weather_heat_type_0 import PlaceWeatherHeatType0
        from ..models.place_weather_lightning import PlaceWeatherLightning
        from ..models.place_weather_marine_type_0 import PlaceWeatherMarineType0
        from ..models.place_weather_precip import PlaceWeatherPrecip
        from ..models.place_weather_radar import PlaceWeatherRadar
        from ..models.place_weather_smoke import PlaceWeatherSmoke
        from ..models.place_weather_uv_type_0 import PlaceWeatherUvType0

        d = dict(src_dict)
        place = d.pop("place")

        radius_km = d.pop("radiusKm")

        automated = d.pop("automated")

        not_a_warning = d.pop("notAWarning")

        _radar = d.pop("radar", UNSET)
        radar: PlaceWeatherRadar | Unset
        if isinstance(_radar, Unset):
            radar = UNSET
        else:
            radar = PlaceWeatherRadar.from_dict(_radar)

        _hail = d.pop("hail", UNSET)
        hail: PlaceWeatherHail | Unset
        if isinstance(_hail, Unset):
            hail = UNSET
        else:
            hail = PlaceWeatherHail.from_dict(_hail)

        _precip = d.pop("precip", UNSET)
        precip: PlaceWeatherPrecip | Unset
        if isinstance(_precip, Unset):
            precip = UNSET
        else:
            precip = PlaceWeatherPrecip.from_dict(_precip)

        _lightning = d.pop("lightning", UNSET)
        lightning: PlaceWeatherLightning | Unset
        if isinstance(_lightning, Unset):
            lightning = UNSET
        else:
            lightning = PlaceWeatherLightning.from_dict(_lightning)

        _cell = d.pop("cell", UNSET)
        cell: PlaceWeatherCell | Unset
        if isinstance(_cell, Unset):
            cell = UNSET
        else:
            cell = PlaceWeatherCell.from_dict(_cell)

        _smoke = d.pop("smoke", UNSET)
        smoke: PlaceWeatherSmoke | Unset
        if isinstance(_smoke, Unset):
            smoke = UNSET
        else:
            smoke = PlaceWeatherSmoke.from_dict(_smoke)

        _forecast = d.pop("forecast", UNSET)
        forecast: PlaceWeatherForecast | Unset
        if isinstance(_forecast, Unset):
            forecast = UNSET
        else:
            forecast = PlaceWeatherForecast.from_dict(_forecast)

        def _parse_marine(data: object) -> None | PlaceWeatherMarineType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                marine_type_0 = PlaceWeatherMarineType0.from_dict(data)

                return marine_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PlaceWeatherMarineType0 | Unset, data)

        marine = _parse_marine(d.pop("marine", UNSET))

        def _parse_aurora(data: object) -> None | PlaceWeatherAuroraType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                aurora_type_0 = PlaceWeatherAuroraType0.from_dict(data)

                return aurora_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PlaceWeatherAuroraType0 | Unset, data)

        aurora = _parse_aurora(d.pop("aurora", UNSET))

        def _parse_heat(data: object) -> None | PlaceWeatherHeatType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                heat_type_0 = PlaceWeatherHeatType0.from_dict(data)

                return heat_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PlaceWeatherHeatType0 | Unset, data)

        heat = _parse_heat(d.pop("heat", UNSET))

        def _parse_uv(data: object) -> None | PlaceWeatherUvType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                uv_type_0 = PlaceWeatherUvType0.from_dict(data)

                return uv_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PlaceWeatherUvType0 | Unset, data)

        uv = _parse_uv(d.pop("uv", UNSET))

        place_weather = cls(
            place=place,
            radius_km=radius_km,
            automated=automated,
            not_a_warning=not_a_warning,
            radar=radar,
            hail=hail,
            precip=precip,
            lightning=lightning,
            cell=cell,
            smoke=smoke,
            forecast=forecast,
            marine=marine,
            aurora=aurora,
            heat=heat,
            uv=uv,
        )

        place_weather.additional_properties = d
        return place_weather

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
