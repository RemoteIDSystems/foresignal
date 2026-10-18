from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.capture_mode_mode import CaptureModeMode
from ..types import UNSET, Unset

T = TypeVar("T", bound="CaptureMode")


@_attrs_define
class CaptureMode:
    """How a chronicle layer captures matching data

    Attributes:
        mode (CaptureModeMode): `full` every matching version; `downsampled` one per entity per interval; `events-only`
            versions whose persist reason is FIRST, SIGNIFICANT, REAPPEAR, TOMBSTONE or TRIGGER_EDGE; `latest-per-interval`
            the latest per entity per interval; `rollup-only` counts per region and hour, no records; `frames` every Nth
            raster frame
        interval (str | Unset): For `downsampled` (default PT60S per entity) and `latest-per-interval` Example: PT60S.
        every_nth_frame (int | Unset): For `frames`
    """

    mode: CaptureModeMode
    interval: str | Unset = UNSET
    every_nth_frame: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mode = self.mode.value

        interval = self.interval

        every_nth_frame = self.every_nth_frame

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "mode": mode,
            }
        )
        if interval is not UNSET:
            field_dict["interval"] = interval
        if every_nth_frame is not UNSET:
            field_dict["everyNthFrame"] = every_nth_frame

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        mode = CaptureModeMode(d.pop("mode"))

        interval = d.pop("interval", UNSET)

        every_nth_frame = d.pop("everyNthFrame", UNSET)

        capture_mode = cls(
            mode=mode,
            interval=interval,
            every_nth_frame=every_nth_frame,
        )

        capture_mode.additional_properties = d
        return capture_mode

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
