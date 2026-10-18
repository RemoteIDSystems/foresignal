from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intelligence_event_context_weather_type_0 import (
        IntelligenceEventContextWeatherType0,
    )


T = TypeVar("T", bound="IntelligenceEventContext")


@_attrs_define
class IntelligenceEventContext:
    """Context added by annotators

    Attributes:
        weather (IntelligenceEventContextWeatherType0 | None | Unset): 105 weather annotators (outage-weather): NWS
            warnings, radar ≥ 50 dBZ, gusts ≥ 25 m/s near the event, as context members
    """

    weather: IntelligenceEventContextWeatherType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.intelligence_event_context_weather_type_0 import (
            IntelligenceEventContextWeatherType0,
        )

        weather: dict[str, Any] | None | Unset
        if isinstance(self.weather, Unset):
            weather = UNSET
        elif isinstance(self.weather, IntelligenceEventContextWeatherType0):
            weather = self.weather.to_dict()
        else:
            weather = self.weather

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if weather is not UNSET:
            field_dict["weather"] = weather

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intelligence_event_context_weather_type_0 import (
            IntelligenceEventContextWeatherType0,
        )

        d = dict(src_dict)

        def _parse_weather(
            data: object,
        ) -> IntelligenceEventContextWeatherType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                weather_type_0 = IntelligenceEventContextWeatherType0.from_dict(data)

                return weather_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IntelligenceEventContextWeatherType0 | None | Unset, data)

        weather = _parse_weather(d.pop("weather", UNSET))

        intelligence_event_context = cls(
            weather=weather,
        )

        intelligence_event_context.additional_properties = d
        return intelligence_event_context

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
