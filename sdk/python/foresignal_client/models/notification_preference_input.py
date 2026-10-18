from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.notification_preference_input_event_types_item import (
    NotificationPreferenceInputEventTypesItem,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.notification_preference_input_channel import (
        NotificationPreferenceInputChannel,
    )
    from ..models.quiet_hours import QuietHours


T = TypeVar("T", bound="NotificationPreferenceInput")


@_attrs_define
class NotificationPreferenceInput:
    """One notification preference of the calling principal

    Attributes:
        event_types (list[NotificationPreferenceInputEventTypesItem]):
        channel (NotificationPreferenceInputChannel): Channel definition owned by the principal
        id (None | str | Unset):
        quiet_hours (None | QuietHours | Unset):
        enabled (bool | Unset):  Default: True.
    """

    event_types: list[NotificationPreferenceInputEventTypesItem]
    channel: NotificationPreferenceInputChannel
    id: None | str | Unset = UNSET
    quiet_hours: None | QuietHours | Unset = UNSET
    enabled: bool | Unset = True
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.quiet_hours import QuietHours

        event_types = []
        for event_types_item_data in self.event_types:
            event_types_item = event_types_item_data.value
            event_types.append(event_types_item)

        channel = self.channel.to_dict()

        id: None | str | Unset
        if isinstance(self.id, Unset):
            id = UNSET
        else:
            id = self.id

        quiet_hours: dict[str, Any] | None | Unset
        if isinstance(self.quiet_hours, Unset):
            quiet_hours = UNSET
        elif isinstance(self.quiet_hours, QuietHours):
            quiet_hours = self.quiet_hours.to_dict()
        else:
            quiet_hours = self.quiet_hours

        enabled = self.enabled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "eventTypes": event_types,
                "channel": channel,
            }
        )
        if id is not UNSET:
            field_dict["id"] = id
        if quiet_hours is not UNSET:
            field_dict["quietHours"] = quiet_hours
        if enabled is not UNSET:
            field_dict["enabled"] = enabled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.notification_preference_input_channel import (
            NotificationPreferenceInputChannel,
        )
        from ..models.quiet_hours import QuietHours

        d = dict(src_dict)
        event_types = []
        _event_types = d.pop("eventTypes")
        for event_types_item_data in _event_types:
            event_types_item = NotificationPreferenceInputEventTypesItem(
                event_types_item_data
            )

            event_types.append(event_types_item)

        channel = NotificationPreferenceInputChannel.from_dict(d.pop("channel"))

        def _parse_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        id = _parse_id(d.pop("id", UNSET))

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

        enabled = d.pop("enabled", UNSET)

        notification_preference_input = cls(
            event_types=event_types,
            channel=channel,
            id=id,
            quiet_hours=quiet_hours,
            enabled=enabled,
        )

        notification_preference_input.additional_properties = d
        return notification_preference_input

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
