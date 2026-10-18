from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.notification_preference_channel_type import (
    NotificationPreferenceChannelType,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.notification_preference_channel_config import (
        NotificationPreferenceChannelConfig,
    )


T = TypeVar("T", bound="NotificationPreferenceChannel")


@_attrs_define
class NotificationPreferenceChannel:
    """The principal-owned 018 channel definition (own ntfy topic, email address, Matrix room, webhook…)

    Attributes:
        type_ (NotificationPreferenceChannelType):
        channel_id (UUID | Unset): Id of the 018 channel row owned by the principal
        config (NotificationPreferenceChannelConfig | Unset): Non-secret settings per the channel type's configFields
        secret_fields (list[str] | Unset):
        language (None | str | Unset): BCP 47 language for translated notifications (141)
    """

    type_: NotificationPreferenceChannelType
    channel_id: UUID | Unset = UNSET
    config: NotificationPreferenceChannelConfig | Unset = UNSET
    secret_fields: list[str] | Unset = UNSET
    language: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        channel_id: str | Unset = UNSET
        if not isinstance(self.channel_id, Unset):
            channel_id = str(self.channel_id)

        config: dict[str, Any] | Unset = UNSET
        if not isinstance(self.config, Unset):
            config = self.config.to_dict()

        secret_fields: list[str] | Unset = UNSET
        if not isinstance(self.secret_fields, Unset):
            secret_fields = self.secret_fields

        language: None | str | Unset
        if isinstance(self.language, Unset):
            language = UNSET
        else:
            language = self.language

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if channel_id is not UNSET:
            field_dict["channelId"] = channel_id
        if config is not UNSET:
            field_dict["config"] = config
        if secret_fields is not UNSET:
            field_dict["secretFields"] = secret_fields
        if language is not UNSET:
            field_dict["language"] = language

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.notification_preference_channel_config import (
            NotificationPreferenceChannelConfig,
        )

        d = dict(src_dict)
        type_ = NotificationPreferenceChannelType(d.pop("type"))

        _channel_id = d.pop("channelId", UNSET)
        channel_id: UUID | Unset
        if isinstance(_channel_id, Unset):
            channel_id = UNSET
        else:
            channel_id = UUID(_channel_id)

        _config = d.pop("config", UNSET)
        config: NotificationPreferenceChannelConfig | Unset
        if isinstance(_config, Unset):
            config = UNSET
        else:
            config = NotificationPreferenceChannelConfig.from_dict(_config)

        secret_fields = cast(list[str], d.pop("secretFields", UNSET))

        def _parse_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        language = _parse_language(d.pop("language", UNSET))

        notification_preference_channel = cls(
            type_=type_,
            channel_id=channel_id,
            config=config,
            secret_fields=secret_fields,
            language=language,
        )

        notification_preference_channel.additional_properties = d
        return notification_preference_channel

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
