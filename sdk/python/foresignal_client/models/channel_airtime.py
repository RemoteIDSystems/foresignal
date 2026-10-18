from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="ChannelAirtime")


@_attrs_define
class ChannelAirtime:
    """LoRa airtime governor window of a mesh channel (084)

    Attributes:
        window_seconds (int):  Example: 3600.
        used_seconds (float):
        limit_seconds (float):
        duty_cycle_percent (float):
        queued (int):
        folded_last_hour (int):
        dropped_last_hour (int):
    """

    window_seconds: int
    used_seconds: float
    limit_seconds: float
    duty_cycle_percent: float
    queued: int
    folded_last_hour: int
    dropped_last_hour: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        window_seconds = self.window_seconds

        used_seconds = self.used_seconds

        limit_seconds = self.limit_seconds

        duty_cycle_percent = self.duty_cycle_percent

        queued = self.queued

        folded_last_hour = self.folded_last_hour

        dropped_last_hour = self.dropped_last_hour

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "windowSeconds": window_seconds,
                "usedSeconds": used_seconds,
                "limitSeconds": limit_seconds,
                "dutyCyclePercent": duty_cycle_percent,
                "queued": queued,
                "foldedLastHour": folded_last_hour,
                "droppedLastHour": dropped_last_hour,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        window_seconds = d.pop("windowSeconds")

        used_seconds = d.pop("usedSeconds")

        limit_seconds = d.pop("limitSeconds")

        duty_cycle_percent = d.pop("dutyCyclePercent")

        queued = d.pop("queued")

        folded_last_hour = d.pop("foldedLastHour")

        dropped_last_hour = d.pop("droppedLastHour")

        channel_airtime = cls(
            window_seconds=window_seconds,
            used_seconds=used_seconds,
            limit_seconds=limit_seconds,
            duty_cycle_percent=duty_cycle_percent,
            queued=queued,
            folded_last_hour=folded_last_hour,
            dropped_last_hour=dropped_last_hour,
        )

        channel_airtime.additional_properties = d
        return channel_airtime

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
