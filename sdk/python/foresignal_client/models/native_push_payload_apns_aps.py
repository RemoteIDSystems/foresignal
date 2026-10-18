from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.native_push_payload_apns_aps_interruption_level import (
    NativePushPayloadApnsApsInterruptionLevel,
)
from ..models.native_push_payload_apns_aps_mutable_content import (
    NativePushPayloadApnsApsMutableContent,
)
from ..models.native_push_payload_apns_aps_sound import NativePushPayloadApnsApsSound
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.native_push_payload_apns_aps_alert import (
        NativePushPayloadApnsApsAlert,
    )


T = TypeVar("T", bound="NativePushPayloadApnsAps")


@_attrs_define
class NativePushPayloadApnsAps:
    """APNs aps dictionary

    Attributes:
        alert (NativePushPayloadApnsApsAlert): Visible text by content level
        sound (NativePushPayloadApnsApsSound | Unset):
        mutable_content (NativePushPayloadApnsApsMutableContent | Unset):
        interruption_level (NativePushPayloadApnsApsInterruptionLevel | Unset):
        thread_id (str | Unset):
        category (str | Unset): FORESIGNAL_<EVENT> (032); FORESIGNAL_ALERT_ACTIONABLE for firing alerts with actions
            (033) Example: FORESIGNAL_ALERT.
        badge (int | Unset): Unread count (033)
    """

    alert: NativePushPayloadApnsApsAlert
    sound: NativePushPayloadApnsApsSound | Unset = UNSET
    mutable_content: NativePushPayloadApnsApsMutableContent | Unset = UNSET
    interruption_level: NativePushPayloadApnsApsInterruptionLevel | Unset = UNSET
    thread_id: str | Unset = UNSET
    category: str | Unset = UNSET
    badge: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        alert = self.alert.to_dict()

        sound: str | Unset = UNSET
        if not isinstance(self.sound, Unset):
            sound = self.sound.value

        mutable_content: int | Unset = UNSET
        if not isinstance(self.mutable_content, Unset):
            mutable_content = self.mutable_content.value

        interruption_level: str | Unset = UNSET
        if not isinstance(self.interruption_level, Unset):
            interruption_level = self.interruption_level.value

        thread_id = self.thread_id

        category = self.category

        badge = self.badge

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "alert": alert,
            }
        )
        if sound is not UNSET:
            field_dict["sound"] = sound
        if mutable_content is not UNSET:
            field_dict["mutable-content"] = mutable_content
        if interruption_level is not UNSET:
            field_dict["interruption-level"] = interruption_level
        if thread_id is not UNSET:
            field_dict["thread-id"] = thread_id
        if category is not UNSET:
            field_dict["category"] = category
        if badge is not UNSET:
            field_dict["badge"] = badge

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.native_push_payload_apns_aps_alert import (
            NativePushPayloadApnsApsAlert,
        )

        d = dict(src_dict)
        alert = NativePushPayloadApnsApsAlert.from_dict(d.pop("alert"))

        _sound = d.pop("sound", UNSET)
        sound: NativePushPayloadApnsApsSound | Unset
        if isinstance(_sound, Unset):
            sound = UNSET
        else:
            sound = NativePushPayloadApnsApsSound(_sound)

        _mutable_content = d.pop("mutable-content", UNSET)
        mutable_content: NativePushPayloadApnsApsMutableContent | Unset
        if isinstance(_mutable_content, Unset):
            mutable_content = UNSET
        else:
            mutable_content = NativePushPayloadApnsApsMutableContent(_mutable_content)

        _interruption_level = d.pop("interruption-level", UNSET)
        interruption_level: NativePushPayloadApnsApsInterruptionLevel | Unset
        if isinstance(_interruption_level, Unset):
            interruption_level = UNSET
        else:
            interruption_level = NativePushPayloadApnsApsInterruptionLevel(
                _interruption_level
            )

        thread_id = d.pop("thread-id", UNSET)

        category = d.pop("category", UNSET)

        badge = d.pop("badge", UNSET)

        native_push_payload_apns_aps = cls(
            alert=alert,
            sound=sound,
            mutable_content=mutable_content,
            interruption_level=interruption_level,
            thread_id=thread_id,
            category=category,
            badge=badge,
        )

        native_push_payload_apns_aps.additional_properties = d
        return native_push_payload_apns_aps

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
