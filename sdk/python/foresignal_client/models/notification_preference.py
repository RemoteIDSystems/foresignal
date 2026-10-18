from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.notification_preference_event_types_item import (
    NotificationPreferenceEventTypesItem,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.notification_preference_channel import NotificationPreferenceChannel
    from ..models.quiet_hours import QuietHours


T = TypeVar("T", bound="NotificationPreference")


@_attrs_define
class NotificationPreference:
    """Where and when a principal receives personal notifications; delivered through 018's worker

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        event_types (list[NotificationPreferenceEventTypesItem]):
        channel (NotificationPreferenceChannel): The principal-owned 018 channel definition (own ntfy topic, email
            address, Matrix room, webhook…)
        enabled (bool):
        principal_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        quiet_hours (None | QuietHours | Unset):
        created_at (datetime.datetime | Unset):
        updated_at (datetime.datetime | Unset):
    """

    id: str
    event_types: list[NotificationPreferenceEventTypesItem]
    channel: NotificationPreferenceChannel
    enabled: bool
    principal_id: str | Unset = UNSET
    quiet_hours: None | QuietHours | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.quiet_hours import QuietHours

        id = self.id

        event_types = []
        for event_types_item_data in self.event_types:
            event_types_item = event_types_item_data.value
            event_types.append(event_types_item)

        channel = self.channel.to_dict()

        enabled = self.enabled

        principal_id = self.principal_id

        quiet_hours: dict[str, Any] | None | Unset
        if isinstance(self.quiet_hours, Unset):
            quiet_hours = UNSET
        elif isinstance(self.quiet_hours, QuietHours):
            quiet_hours = self.quiet_hours.to_dict()
        else:
            quiet_hours = self.quiet_hours

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "eventTypes": event_types,
                "channel": channel,
                "enabled": enabled,
            }
        )
        if principal_id is not UNSET:
            field_dict["principalId"] = principal_id
        if quiet_hours is not UNSET:
            field_dict["quietHours"] = quiet_hours
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.notification_preference_channel import (
            NotificationPreferenceChannel,
        )
        from ..models.quiet_hours import QuietHours

        d = dict(src_dict)
        id = d.pop("id")

        event_types = []
        _event_types = d.pop("eventTypes")
        for event_types_item_data in _event_types:
            event_types_item = NotificationPreferenceEventTypesItem(
                event_types_item_data
            )

            event_types.append(event_types_item)

        channel = NotificationPreferenceChannel.from_dict(d.pop("channel"))

        enabled = d.pop("enabled")

        principal_id = d.pop("principalId", UNSET)

        def _parse_quiet_hours(data: object) -> None | QuietHours | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                quiet_hours_type_0 = QuietHours.from_dict(data)

                return quiet_hours_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | QuietHours | Unset, data)

        quiet_hours = _parse_quiet_hours(d.pop("quietHours", UNSET))

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        notification_preference = cls(
            id=id,
            event_types=event_types,
            channel=channel,
            enabled=enabled,
            principal_id=principal_id,
            quiet_hours=quiet_hours,
            created_at=created_at,
            updated_at=updated_at,
        )

        notification_preference.additional_properties = d
        return notification_preference

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
