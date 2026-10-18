from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.uv_forecast_payload_hours_item import UvForecastPayloadHoursItem


T = TypeVar("T", bound="UvForecastPayload")


@_attrs_define
class UvForecastPayload:
    """Kind `uv-forecast` (`epa-uv`, externalId `place:{id}`): hourly UV index for a place's ZIP

    Attributes:
        zip_ (str):
        hours (list[UvForecastPayloadHoursItem]):
        max_uv (float | None | Unset):
        max_at (datetime.datetime | None | Unset):
    """

    zip_: str
    hours: list[UvForecastPayloadHoursItem]
    max_uv: float | None | Unset = UNSET
    max_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        zip_ = self.zip_

        hours = []
        for hours_item_data in self.hours:
            hours_item = hours_item_data.to_dict()
            hours.append(hours_item)

        max_uv: float | None | Unset
        if isinstance(self.max_uv, Unset):
            max_uv = UNSET
        else:
            max_uv = self.max_uv

        max_at: None | str | Unset
        if isinstance(self.max_at, Unset):
            max_at = UNSET
        elif isinstance(self.max_at, datetime.datetime):
            max_at = self.max_at.isoformat()
        else:
            max_at = self.max_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "zip": zip_,
                "hours": hours,
            }
        )
        if max_uv is not UNSET:
            field_dict["maxUv"] = max_uv
        if max_at is not UNSET:
            field_dict["maxAt"] = max_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.uv_forecast_payload_hours_item import UvForecastPayloadHoursItem

        d = dict(src_dict)
        zip_ = d.pop("zip")

        hours = []
        _hours = d.pop("hours")
        for hours_item_data in _hours:
            hours_item = UvForecastPayloadHoursItem.from_dict(hours_item_data)

            hours.append(hours_item)

        def _parse_max_uv(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        max_uv = _parse_max_uv(d.pop("maxUv", UNSET))

        def _parse_max_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                max_at_type_0 = datetime.datetime.fromisoformat(data)

                return max_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        max_at = _parse_max_at(d.pop("maxAt", UNSET))

        uv_forecast_payload = cls(
            zip_=zip_,
            hours=hours,
            max_uv=max_uv,
            max_at=max_at,
        )

        uv_forecast_payload.additional_properties = d
        return uv_forecast_payload

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
