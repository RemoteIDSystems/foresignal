from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_series_provider import ForecastSeriesProvider
from ..models.weather_section_status import WeatherSectionStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_series_point import ForecastSeriesPoint


T = TypeVar("T", bound="ForecastSeries")


@_attrs_define
class ForecastSeries:
    """Cached forecast series for a place (GFS/HRRR/IFS 052/105; AIFS 106; NWS hourly 72 h, 053)

    Attributes:
        provider (ForecastSeriesProvider):
        updated_at (datetime.datetime):
        items (list[ForecastSeriesPoint]):
        run (None | str | Unset): Model run (gfs, hrrr)
        valid_from (datetime.datetime | Unset):
        status (WeatherSectionStatus | Unset): Status of a derived weather section; anything but ok means unknown, never
            clear
    """

    provider: ForecastSeriesProvider
    updated_at: datetime.datetime
    items: list[ForecastSeriesPoint]
    run: None | str | Unset = UNSET
    valid_from: datetime.datetime | Unset = UNSET
    status: WeatherSectionStatus | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        provider = self.provider.value

        updated_at = self.updated_at.isoformat()

        items = []
        for items_item_data in self.items:
            items_item = items_item_data.to_dict()
            items.append(items_item)

        run: None | str | Unset
        if isinstance(self.run, Unset):
            run = UNSET
        else:
            run = self.run

        valid_from: str | Unset = UNSET
        if not isinstance(self.valid_from, Unset):
            valid_from = self.valid_from.isoformat()

        status: str | Unset = UNSET
        if not isinstance(self.status, Unset):
            status = self.status.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "provider": provider,
                "updatedAt": updated_at,
                "items": items,
            }
        )
        if run is not UNSET:
            field_dict["run"] = run
        if valid_from is not UNSET:
            field_dict["validFrom"] = valid_from
        if status is not UNSET:
            field_dict["status"] = status

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_series_point import ForecastSeriesPoint

        d = dict(src_dict)
        provider = ForecastSeriesProvider(d.pop("provider"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

        items = []
        _items = d.pop("items")
        for items_item_data in _items:
            items_item = ForecastSeriesPoint.from_dict(items_item_data)

            items.append(items_item)

        def _parse_run(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run = _parse_run(d.pop("run", UNSET))

        _valid_from = d.pop("validFrom", UNSET)
        valid_from: datetime.datetime | Unset
        if isinstance(_valid_from, Unset):
            valid_from = UNSET
        else:
            valid_from = datetime.datetime.fromisoformat(_valid_from)

        _status = d.pop("status", UNSET)
        status: WeatherSectionStatus | Unset
        if isinstance(_status, Unset):
            status = UNSET
        else:
            status = WeatherSectionStatus(_status)

        forecast_series = cls(
            provider=provider,
            updated_at=updated_at,
            items=items,
            run=run,
            valid_from=valid_from,
            status=status,
        )

        forecast_series.additional_properties = d
        return forecast_series

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
