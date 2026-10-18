from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RetentionSettingsStats")


@_attrs_define
class RetentionSettingsStats:
    """Retention of 038 activity rollups and 119 region rollups

    Attributes:
        hourly (str | Unset): H3 hourly rollups (038) Default: '35d'. Example: 35d.
        daily (str | Unset): H3 daily rollups (038) Default: '1825d'. Example: 1825d.
        region_hourly (str | Unset): Region-hour rollups (resolution 2) Default: '1095d'. Example: 1095d.
        region_daily (str | Unset): Region-day rollups (resolution 3) Default: '3650d'. Example: 3650d.
    """

    hourly: str | Unset = "35d"
    daily: str | Unset = "1825d"
    region_hourly: str | Unset = "1095d"
    region_daily: str | Unset = "3650d"
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        hourly = self.hourly

        daily = self.daily

        region_hourly = self.region_hourly

        region_daily = self.region_daily

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if hourly is not UNSET:
            field_dict["hourly"] = hourly
        if daily is not UNSET:
            field_dict["daily"] = daily
        if region_hourly is not UNSET:
            field_dict["regionHourly"] = region_hourly
        if region_daily is not UNSET:
            field_dict["regionDaily"] = region_daily

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        hourly = d.pop("hourly", UNSET)

        daily = d.pop("daily", UNSET)

        region_hourly = d.pop("regionHourly", UNSET)

        region_daily = d.pop("regionDaily", UNSET)

        retention_settings_stats = cls(
            hourly=hourly,
            daily=daily,
            region_hourly=region_hourly,
            region_daily=region_daily,
        )

        retention_settings_stats.additional_properties = d
        return retention_settings_stats

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
