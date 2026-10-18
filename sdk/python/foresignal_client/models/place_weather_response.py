from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_series import ForecastSeries
    from ..models.place_weather import PlaceWeather
    from ..models.record import Record


T = TypeVar("T", bound="PlaceWeatherResponse")


@_attrs_define
class PlaceWeatherResponse:
    """The place-weather record (head) plus cached forecast series for one place

    Attributes:
        place_id (str):
        forecast_series (list[ForecastSeries]):
        server_time (datetime.datetime):
        place (str | Unset):
        timezone (str | Unset):
        record (None | Record | Unset):
        weather (None | PlaceWeather | Unset):
    """

    place_id: str
    forecast_series: list[ForecastSeries]
    server_time: datetime.datetime
    place: str | Unset = UNSET
    timezone: str | Unset = UNSET
    record: None | Record | Unset = UNSET
    weather: None | PlaceWeather | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.place_weather import PlaceWeather
        from ..models.record import Record

        place_id = self.place_id

        forecast_series = []
        for forecast_series_item_data in self.forecast_series:
            forecast_series_item = forecast_series_item_data.to_dict()
            forecast_series.append(forecast_series_item)

        server_time = self.server_time.isoformat()

        place = self.place

        timezone = self.timezone

        record: dict[str, Any] | None | Unset
        if isinstance(self.record, Unset):
            record = UNSET
        elif isinstance(self.record, Record):
            record = self.record.to_dict()
        else:
            record = self.record

        weather: dict[str, Any] | None | Unset
        if isinstance(self.weather, Unset):
            weather = UNSET
        elif isinstance(self.weather, PlaceWeather):
            weather = self.weather.to_dict()
        else:
            weather = self.weather

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "placeId": place_id,
                "forecastSeries": forecast_series,
                "serverTime": server_time,
            }
        )
        if place is not UNSET:
            field_dict["place"] = place
        if timezone is not UNSET:
            field_dict["timezone"] = timezone
        if record is not UNSET:
            field_dict["record"] = record
        if weather is not UNSET:
            field_dict["weather"] = weather

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_series import ForecastSeries
        from ..models.place_weather import PlaceWeather
        from ..models.record import Record

        d = dict(src_dict)
        place_id = d.pop("placeId")

        forecast_series = []
        _forecast_series = d.pop("forecastSeries")
        for forecast_series_item_data in _forecast_series:
            forecast_series_item = ForecastSeries.from_dict(forecast_series_item_data)

            forecast_series.append(forecast_series_item)

        server_time = datetime.datetime.fromisoformat(d.pop("serverTime"))

        place = d.pop("place", UNSET)

        timezone = d.pop("timezone", UNSET)

        def _parse_record(data: object) -> None | Record | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                record_type_0 = Record.from_dict(data)

                return record_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Record | Unset, data)

        record = _parse_record(d.pop("record", UNSET))

        def _parse_weather(data: object) -> None | PlaceWeather | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                weather_type_0 = PlaceWeather.from_dict(data)

                return weather_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PlaceWeather | Unset, data)

        weather = _parse_weather(d.pop("weather", UNSET))

        place_weather_response = cls(
            place_id=place_id,
            forecast_series=forecast_series,
            server_time=server_time,
            place=place,
            timezone=timezone,
            record=record,
            weather=weather,
        )

        place_weather_response.additional_properties = d
        return place_weather_response

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
