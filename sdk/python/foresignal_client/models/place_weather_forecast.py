from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.place_weather_forecast_gfs import PlaceWeatherForecastGfs
    from ..models.place_weather_forecast_nws import PlaceWeatherForecastNws


T = TypeVar("T", bound="PlaceWeatherForecast")


@_attrs_define
class PlaceWeatherForecast:
    """Forecast summaries

    Attributes:
        gfs (PlaceWeatherForecastGfs | Unset): GFS 24-h maxima
        nws (PlaceWeatherForecastNws | Unset): NWS gridpoint summary (053); stale when older than 6 h
    """

    gfs: PlaceWeatherForecastGfs | Unset = UNSET
    nws: PlaceWeatherForecastNws | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        gfs: dict[str, Any] | Unset = UNSET
        if not isinstance(self.gfs, Unset):
            gfs = self.gfs.to_dict()

        nws: dict[str, Any] | Unset = UNSET
        if not isinstance(self.nws, Unset):
            nws = self.nws.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if gfs is not UNSET:
            field_dict["gfs"] = gfs
        if nws is not UNSET:
            field_dict["nws"] = nws

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.place_weather_forecast_gfs import PlaceWeatherForecastGfs
        from ..models.place_weather_forecast_nws import PlaceWeatherForecastNws

        d = dict(src_dict)
        _gfs = d.pop("gfs", UNSET)
        gfs: PlaceWeatherForecastGfs | Unset
        if isinstance(_gfs, Unset):
            gfs = UNSET
        else:
            gfs = PlaceWeatherForecastGfs.from_dict(_gfs)

        _nws = d.pop("nws", UNSET)
        nws: PlaceWeatherForecastNws | Unset
        if isinstance(_nws, Unset):
            nws = UNSET
        else:
            nws = PlaceWeatherForecastNws.from_dict(_nws)

        place_weather_forecast = cls(
            gfs=gfs,
            nws=nws,
        )

        place_weather_forecast.additional_properties = d
        return place_weather_forecast

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
