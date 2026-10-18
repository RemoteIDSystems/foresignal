from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="CustomSourceFormatsOverland")


@_attrs_define
class CustomSourceFormatsOverland:
    """Overland options (039)

    Attributes:
        keep_wifi (bool | Unset): Keep the Wi-Fi network name (`wifi`); dropped by default (039) Default: False.
        max_accuracy_meters (float | Unset): Fixes whose accuracy is worse than this many meters are rejected with
            reason LOW_ACCURACY (039) Default: 200.0.
    """

    keep_wifi: bool | Unset = False
    max_accuracy_meters: float | Unset = 200.0
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        keep_wifi = self.keep_wifi

        max_accuracy_meters = self.max_accuracy_meters

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if keep_wifi is not UNSET:
            field_dict["keepWifi"] = keep_wifi
        if max_accuracy_meters is not UNSET:
            field_dict["maxAccuracyMeters"] = max_accuracy_meters

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        keep_wifi = d.pop("keepWifi", UNSET)

        max_accuracy_meters = d.pop("maxAccuracyMeters", UNSET)

        custom_source_formats_overland = cls(
            keep_wifi=keep_wifi,
            max_accuracy_meters=max_accuracy_meters,
        )

        custom_source_formats_overland.additional_properties = d
        return custom_source_formats_overland

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
