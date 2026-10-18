from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.native_push_payload_fcm_message_android_priority import (
    NativePushPayloadFcmMessageAndroidPriority,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="NativePushPayloadFcmMessageAndroid")


@_attrs_define
class NativePushPayloadFcmMessageAndroid:
    """Android options

    Attributes:
        priority (NativePushPayloadFcmMessageAndroidPriority | Unset):
        ttl (str | Unset):
        collapse_key (str | Unset):
    """

    priority: NativePushPayloadFcmMessageAndroidPriority | Unset = UNSET
    ttl: str | Unset = UNSET
    collapse_key: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        priority: str | Unset = UNSET
        if not isinstance(self.priority, Unset):
            priority = self.priority.value

        ttl = self.ttl

        collapse_key = self.collapse_key

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if priority is not UNSET:
            field_dict["priority"] = priority
        if ttl is not UNSET:
            field_dict["ttl"] = ttl
        if collapse_key is not UNSET:
            field_dict["collapse_key"] = collapse_key

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _priority = d.pop("priority", UNSET)
        priority: NativePushPayloadFcmMessageAndroidPriority | Unset
        if isinstance(_priority, Unset):
            priority = UNSET
        else:
            priority = NativePushPayloadFcmMessageAndroidPriority(_priority)

        ttl = d.pop("ttl", UNSET)

        collapse_key = d.pop("collapse_key", UNSET)

        native_push_payload_fcm_message_android = cls(
            priority=priority,
            ttl=ttl,
            collapse_key=collapse_key,
        )

        native_push_payload_fcm_message_android.additional_properties = d
        return native_push_payload_fcm_message_android

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
