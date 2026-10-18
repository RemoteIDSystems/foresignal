from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.taf_payload_forecast_item import TafPayloadForecastItem


T = TypeVar("T", bound="TafPayload")


@_attrs_define
class TafPayload:
    """Kind `taf` (`awc-tafs`): terminal aerodrome forecast; observedAt = issue time; validFrom/validUntil explicit

    Attributes:
        station_id (str):  Example: KDFW.
        issue_time (datetime.datetime):
        valid_from (datetime.datetime):
        valid_to (datetime.datetime):
        raw_text (str):
        forecast (list[TafPayloadForecastItem] | Unset):
    """

    station_id: str
    issue_time: datetime.datetime
    valid_from: datetime.datetime
    valid_to: datetime.datetime
    raw_text: str
    forecast: list[TafPayloadForecastItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        station_id = self.station_id

        issue_time = self.issue_time.isoformat()

        valid_from = self.valid_from.isoformat()

        valid_to = self.valid_to.isoformat()

        raw_text = self.raw_text

        forecast: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.forecast, Unset):
            forecast = []
            for forecast_item_data in self.forecast:
                forecast_item = forecast_item_data.to_dict()
                forecast.append(forecast_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "stationId": station_id,
                "issueTime": issue_time,
                "validFrom": valid_from,
                "validTo": valid_to,
                "rawText": raw_text,
            }
        )
        if forecast is not UNSET:
            field_dict["forecast"] = forecast

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.taf_payload_forecast_item import TafPayloadForecastItem

        d = dict(src_dict)
        station_id = d.pop("stationId")

        issue_time = datetime.datetime.fromisoformat(d.pop("issueTime"))

        valid_from = datetime.datetime.fromisoformat(d.pop("validFrom"))

        valid_to = datetime.datetime.fromisoformat(d.pop("validTo"))

        raw_text = d.pop("rawText")

        _forecast = d.pop("forecast", UNSET)
        forecast: list[TafPayloadForecastItem] | Unset = UNSET
        if _forecast is not UNSET:
            forecast = []
            for forecast_item_data in _forecast:
                forecast_item = TafPayloadForecastItem.from_dict(forecast_item_data)

                forecast.append(forecast_item)

        taf_payload = cls(
            station_id=station_id,
            issue_time=issue_time,
            valid_from=valid_from,
            valid_to=valid_to,
            raw_text=raw_text,
            forecast=forecast,
        )

        taf_payload.additional_properties = d
        return taf_payload

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
