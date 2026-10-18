from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.taf_payload_forecast_item_clouds_item import (
        TafPayloadForecastItemCloudsItem,
    )
    from ..models.taf_payload_forecast_item_wind_type_0 import (
        TafPayloadForecastItemWindType0,
    )


T = TypeVar("T", bound="TafPayloadForecastItem")


@_attrs_define
class TafPayloadForecastItem:
    """Forecast group

    Attributes:
        change_indicator (None | str | Unset): FM, TEMPO, BECMG, PROB30/40
        from_ (datetime.datetime | None | Unset):
        to (datetime.datetime | None | Unset):
        wind (None | TafPayloadForecastItemWindType0 | Unset):
        visibility (None | str | Unset):
        wx (list[str] | Unset):
        clouds (list[TafPayloadForecastItemCloudsItem] | Unset):
    """

    change_indicator: None | str | Unset = UNSET
    from_: datetime.datetime | None | Unset = UNSET
    to: datetime.datetime | None | Unset = UNSET
    wind: None | TafPayloadForecastItemWindType0 | Unset = UNSET
    visibility: None | str | Unset = UNSET
    wx: list[str] | Unset = UNSET
    clouds: list[TafPayloadForecastItemCloudsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.taf_payload_forecast_item_wind_type_0 import (
            TafPayloadForecastItemWindType0,
        )

        change_indicator: None | str | Unset
        if isinstance(self.change_indicator, Unset):
            change_indicator = UNSET
        else:
            change_indicator = self.change_indicator

        from_: None | str | Unset
        if isinstance(self.from_, Unset):
            from_ = UNSET
        elif isinstance(self.from_, datetime.datetime):
            from_ = self.from_.isoformat()
        else:
            from_ = self.from_

        to: None | str | Unset
        if isinstance(self.to, Unset):
            to = UNSET
        elif isinstance(self.to, datetime.datetime):
            to = self.to.isoformat()
        else:
            to = self.to

        wind: dict[str, Any] | None | Unset
        if isinstance(self.wind, Unset):
            wind = UNSET
        elif isinstance(self.wind, TafPayloadForecastItemWindType0):
            wind = self.wind.to_dict()
        else:
            wind = self.wind

        visibility: None | str | Unset
        if isinstance(self.visibility, Unset):
            visibility = UNSET
        else:
            visibility = self.visibility

        wx: list[str] | Unset = UNSET
        if not isinstance(self.wx, Unset):
            wx = self.wx

        clouds: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.clouds, Unset):
            clouds = []
            for clouds_item_data in self.clouds:
                clouds_item = clouds_item_data.to_dict()
                clouds.append(clouds_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if change_indicator is not UNSET:
            field_dict["changeIndicator"] = change_indicator
        if from_ is not UNSET:
            field_dict["from"] = from_
        if to is not UNSET:
            field_dict["to"] = to
        if wind is not UNSET:
            field_dict["wind"] = wind
        if visibility is not UNSET:
            field_dict["visibility"] = visibility
        if wx is not UNSET:
            field_dict["wx"] = wx
        if clouds is not UNSET:
            field_dict["clouds"] = clouds

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.taf_payload_forecast_item_clouds_item import (
            TafPayloadForecastItemCloudsItem,
        )
        from ..models.taf_payload_forecast_item_wind_type_0 import (
            TafPayloadForecastItemWindType0,
        )

        d = dict(src_dict)

        def _parse_change_indicator(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        change_indicator = _parse_change_indicator(d.pop("changeIndicator", UNSET))

        def _parse_from_(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                from_type_0 = datetime.datetime.fromisoformat(data)

                return from_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        from_ = _parse_from_(d.pop("from", UNSET))

        def _parse_to(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                to_type_0 = datetime.datetime.fromisoformat(data)

                return to_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        to = _parse_to(d.pop("to", UNSET))

        def _parse_wind(data: object) -> None | TafPayloadForecastItemWindType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                wind_type_0 = TafPayloadForecastItemWindType0.from_dict(data)

                return wind_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | TafPayloadForecastItemWindType0 | Unset, data)

        wind = _parse_wind(d.pop("wind", UNSET))

        def _parse_visibility(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        visibility = _parse_visibility(d.pop("visibility", UNSET))

        wx = cast(list[str], d.pop("wx", UNSET))

        _clouds = d.pop("clouds", UNSET)
        clouds: list[TafPayloadForecastItemCloudsItem] | Unset = UNSET
        if _clouds is not UNSET:
            clouds = []
            for clouds_item_data in _clouds:
                clouds_item = TafPayloadForecastItemCloudsItem.from_dict(
                    clouds_item_data
                )

                clouds.append(clouds_item)

        taf_payload_forecast_item = cls(
            change_indicator=change_indicator,
            from_=from_,
            to=to,
            wind=wind,
            visibility=visibility,
            wx=wx,
            clouds=clouds,
        )

        taf_payload_forecast_item.additional_properties = d
        return taf_payload_forecast_item

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
