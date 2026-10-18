from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoNotifications")


@_attrs_define
class SystemInfoNotifications:
    """
    Attributes:
        channels (int | Unset):
        pending_deliveries (int | Unset):
        failed_last_24_h (int | Unset):
    """

    channels: int | Unset = UNSET
    pending_deliveries: int | Unset = UNSET
    failed_last_24_h: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        channels = self.channels

        pending_deliveries = self.pending_deliveries

        failed_last_24_h = self.failed_last_24_h

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if channels is not UNSET:
            field_dict["channels"] = channels
        if pending_deliveries is not UNSET:
            field_dict["pendingDeliveries"] = pending_deliveries
        if failed_last_24_h is not UNSET:
            field_dict["failedLast24h"] = failed_last_24_h

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        channels = d.pop("channels", UNSET)

        pending_deliveries = d.pop("pendingDeliveries", UNSET)

        failed_last_24_h = d.pop("failedLast24h", UNSET)

        system_info_notifications = cls(
            channels=channels,
            pending_deliveries=pending_deliveries,
            failed_last_24_h=failed_last_24_h,
        )

        system_info_notifications.additional_properties = d
        return system_info_notifications

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
