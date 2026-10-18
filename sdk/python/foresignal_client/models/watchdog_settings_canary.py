from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="WatchdogSettingsCanary")


@_attrs_define
class WatchdogSettingsCanary:
    """
    Attributes:
        enabled (bool | Unset): Submit a synthetic `system-canary` record and require its change within 30 s Default:
            False.
        interval (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Default: '1h'. Example: 1h.
    """

    enabled: bool | Unset = False
    interval: str | Unset = "1h"
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        interval = self.interval

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if interval is not UNSET:
            field_dict["interval"] = interval

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        interval = d.pop("interval", UNSET)

        watchdog_settings_canary = cls(
            enabled=enabled,
            interval=interval,
        )

        watchdog_settings_canary.additional_properties = d
        return watchdog_settings_canary

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
