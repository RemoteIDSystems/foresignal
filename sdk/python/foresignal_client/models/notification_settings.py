from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.notification_settings_pause_bypass_severity_type_1 import (
    NotificationSettingsPauseBypassSeverityType1,
)
from ..models.notification_settings_pause_bypass_severity_type_2_type_1 import (
    NotificationSettingsPauseBypassSeverityType2Type1,
)
from ..models.notification_settings_pause_bypass_severity_type_3_type_1 import (
    NotificationSettingsPauseBypassSeverityType3Type1,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.notification_settings_chatops import NotificationSettingsChatops


T = TypeVar("T", bound="NotificationSettings")


@_attrs_define
class NotificationSettings:
    """Global notification pause (`app_setting['notifications']`)

    Attributes:
        paused_until (datetime.datetime | None | Unset): Pause all deliveries until this time; paused deliveries are
            released as digests
        pause_bypass_severity (None | NotificationSettingsPauseBypassSeverityType1 |
            NotificationSettingsPauseBypassSeverityType2Type1 | NotificationSettingsPauseBypassSeverityType3Type1 | Unset):
            Default: NotificationSettingsPauseBypassSeverityType1.CRITICAL.
        chatops (NotificationSettingsChatops | Unset): Interactive chat commands (061)
        image_links (bool | Unset): 108: add feed-key image links to map attachments for channels that cannot take
            attachments; requires an https publicBaseUrl (400 IMAGE_LINKS_REQUIRE_HTTPS) Default: False.
        default_language (str | Unset): 141 (`notify.defaultLanguage`): BCP 47 language notifications are written in; a
            channel whose `language` differs gets the title and body translated Default: 'en'.
    """

    paused_until: datetime.datetime | None | Unset = UNSET
    pause_bypass_severity: (
        None
        | NotificationSettingsPauseBypassSeverityType1
        | NotificationSettingsPauseBypassSeverityType2Type1
        | NotificationSettingsPauseBypassSeverityType3Type1
        | Unset
    ) = NotificationSettingsPauseBypassSeverityType1.CRITICAL
    chatops: NotificationSettingsChatops | Unset = UNSET
    image_links: bool | Unset = False
    default_language: str | Unset = "en"
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        paused_until: None | str | Unset
        if isinstance(self.paused_until, Unset):
            paused_until = UNSET
        elif isinstance(self.paused_until, datetime.datetime):
            paused_until = self.paused_until.isoformat()
        else:
            paused_until = self.paused_until

        pause_bypass_severity: None | str | Unset
        if isinstance(self.pause_bypass_severity, Unset):
            pause_bypass_severity = UNSET
        elif (
            isinstance(
                self.pause_bypass_severity, NotificationSettingsPauseBypassSeverityType1
            )
            or isinstance(
                self.pause_bypass_severity,
                NotificationSettingsPauseBypassSeverityType2Type1,
            )
            or isinstance(
                self.pause_bypass_severity,
                NotificationSettingsPauseBypassSeverityType3Type1,
            )
        ):
            pause_bypass_severity = self.pause_bypass_severity.value
        else:
            pause_bypass_severity = self.pause_bypass_severity

        chatops: dict[str, Any] | Unset = UNSET
        if not isinstance(self.chatops, Unset):
            chatops = self.chatops.to_dict()

        image_links = self.image_links

        default_language = self.default_language

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if paused_until is not UNSET:
            field_dict["pausedUntil"] = paused_until
        if pause_bypass_severity is not UNSET:
            field_dict["pauseBypassSeverity"] = pause_bypass_severity
        if chatops is not UNSET:
            field_dict["chatops"] = chatops
        if image_links is not UNSET:
            field_dict["imageLinks"] = image_links
        if default_language is not UNSET:
            field_dict["defaultLanguage"] = default_language

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.notification_settings_chatops import NotificationSettingsChatops

        d = dict(src_dict)

        def _parse_paused_until(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                paused_until_type_0 = datetime.datetime.fromisoformat(data)

                return paused_until_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        paused_until = _parse_paused_until(d.pop("pausedUntil", UNSET))

        def _parse_pause_bypass_severity(
            data: object,
        ) -> (
            None
            | NotificationSettingsPauseBypassSeverityType1
            | NotificationSettingsPauseBypassSeverityType2Type1
            | NotificationSettingsPauseBypassSeverityType3Type1
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                pause_bypass_severity_type_1 = (
                    NotificationSettingsPauseBypassSeverityType1(data)
                )

                return pause_bypass_severity_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                pause_bypass_severity_type_2_type_1 = (
                    NotificationSettingsPauseBypassSeverityType2Type1(data)
                )

                return pause_bypass_severity_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                pause_bypass_severity_type_3_type_1 = (
                    NotificationSettingsPauseBypassSeverityType3Type1(data)
                )

                return pause_bypass_severity_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                None
                | NotificationSettingsPauseBypassSeverityType1
                | NotificationSettingsPauseBypassSeverityType2Type1
                | NotificationSettingsPauseBypassSeverityType3Type1
                | Unset,
                data,
            )

        pause_bypass_severity = _parse_pause_bypass_severity(
            d.pop("pauseBypassSeverity", UNSET)
        )

        _chatops = d.pop("chatops", UNSET)
        chatops: NotificationSettingsChatops | Unset
        if isinstance(_chatops, Unset):
            chatops = UNSET
        else:
            chatops = NotificationSettingsChatops.from_dict(_chatops)

        image_links = d.pop("imageLinks", UNSET)

        default_language = d.pop("defaultLanguage", UNSET)

        notification_settings = cls(
            paused_until=paused_until,
            pause_bypass_severity=pause_bypass_severity,
            chatops=chatops,
            image_links=image_links,
            default_language=default_language,
        )

        notification_settings.additional_properties = d
        return notification_settings

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
