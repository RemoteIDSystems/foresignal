from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.place_weather_digest_next import PlaceWeatherDigestNext
    from ..models.place_weather_digest_now import PlaceWeatherDigestNow
    from ..models.place_weather_digest_period import PlaceWeatherDigestPeriod


T = TypeVar("T", bound="PlaceWeatherDigest")


@_attrs_define
class PlaceWeatherDigest:
    """ "Weather at places" digest section for one place (056); stale sections are labeled, never omitted

    Attributes:
        place_id (str):
        place (str):
        timezone (str):
        now (PlaceWeatherDigestNow | Unset): Current conditions
        period (PlaceWeatherDigestPeriod | Unset): The briefing lookback
        next_ (PlaceWeatherDigestNext | Unset): Next 24–72 h
    """

    place_id: str
    place: str
    timezone: str
    now: PlaceWeatherDigestNow | Unset = UNSET
    period: PlaceWeatherDigestPeriod | Unset = UNSET
    next_: PlaceWeatherDigestNext | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        place_id = self.place_id

        place = self.place

        timezone = self.timezone

        now: dict[str, Any] | Unset = UNSET
        if not isinstance(self.now, Unset):
            now = self.now.to_dict()

        period: dict[str, Any] | Unset = UNSET
        if not isinstance(self.period, Unset):
            period = self.period.to_dict()

        next_: dict[str, Any] | Unset = UNSET
        if not isinstance(self.next_, Unset):
            next_ = self.next_.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "placeId": place_id,
                "place": place,
                "timezone": timezone,
            }
        )
        if now is not UNSET:
            field_dict["now"] = now
        if period is not UNSET:
            field_dict["period"] = period
        if next_ is not UNSET:
            field_dict["next"] = next_

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.place_weather_digest_next import PlaceWeatherDigestNext
        from ..models.place_weather_digest_now import PlaceWeatherDigestNow
        from ..models.place_weather_digest_period import PlaceWeatherDigestPeriod

        d = dict(src_dict)
        place_id = d.pop("placeId")

        place = d.pop("place")

        timezone = d.pop("timezone")

        _now = d.pop("now", UNSET)
        now: PlaceWeatherDigestNow | Unset
        if isinstance(_now, Unset):
            now = UNSET
        else:
            now = PlaceWeatherDigestNow.from_dict(_now)

        _period = d.pop("period", UNSET)
        period: PlaceWeatherDigestPeriod | Unset
        if isinstance(_period, Unset):
            period = UNSET
        else:
            period = PlaceWeatherDigestPeriod.from_dict(_period)

        _next_ = d.pop("next", UNSET)
        next_: PlaceWeatherDigestNext | Unset
        if isinstance(_next_, Unset):
            next_ = UNSET
        else:
            next_ = PlaceWeatherDigestNext.from_dict(_next_)

        place_weather_digest = cls(
            place_id=place_id,
            place=place,
            timezone=timezone,
            now=now,
            period=period,
            next_=next_,
        )

        place_weather_digest.additional_properties = d
        return place_weather_digest

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
