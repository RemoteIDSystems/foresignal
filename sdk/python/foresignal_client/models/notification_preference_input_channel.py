from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.notification_preference_input_channel_type import (
    NotificationPreferenceInputChannelType,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.notification_preference_input_channel_config import (
        NotificationPreferenceInputChannelConfig,
    )
    from ..models.notification_preference_input_channel_secrets import (
        NotificationPreferenceInputChannelSecrets,
    )


T = TypeVar("T", bound="NotificationPreferenceInputChannel")


@_attrs_define
class NotificationPreferenceInputChannel:
    """Channel definition owned by the principal

    Attributes:
        type_ (NotificationPreferenceInputChannelType):
        config (NotificationPreferenceInputChannelConfig | Unset):
        secrets (NotificationPreferenceInputChannelSecrets | Unset): Write-only secret settings; omitted secrets keep
            their stored values
        language (None | str | Unset): BCP 47 (141)
    """

    type_: NotificationPreferenceInputChannelType
    config: NotificationPreferenceInputChannelConfig | Unset = UNSET
    secrets: NotificationPreferenceInputChannelSecrets | Unset = UNSET
    language: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        config: dict[str, Any] | Unset = UNSET
        if not isinstance(self.config, Unset):
            config = self.config.to_dict()

        secrets: dict[str, Any] | Unset = UNSET
        if not isinstance(self.secrets, Unset):
            secrets = self.secrets.to_dict()

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
        if config is not UNSET:
            field_dict["config"] = config
        if secrets is not UNSET:
            field_dict["secrets"] = secrets
        if language is not UNSET:
            field_dict["language"] = language

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.notification_preference_input_channel_config import (
            NotificationPreferenceInputChannelConfig,
        )
        from ..models.notification_preference_input_channel_secrets import (
            NotificationPreferenceInputChannelSecrets,
        )

        d = dict(src_dict)
        type_ = NotificationPreferenceInputChannelType(d.pop("type"))

        _config = d.pop("config", UNSET)
        config: NotificationPreferenceInputChannelConfig | Unset
        if isinstance(_config, Unset):
            config = UNSET
        else:
            config = NotificationPreferenceInputChannelConfig.from_dict(_config)

        _secrets = d.pop("secrets", UNSET)
        secrets: NotificationPreferenceInputChannelSecrets | Unset
        if isinstance(_secrets, Unset):
            secrets = UNSET
        else:
            secrets = NotificationPreferenceInputChannelSecrets.from_dict(_secrets)

        def _parse_language(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        language = _parse_language(d.pop("language", UNSET))

        notification_preference_input_channel = cls(
            type_=type_,
            config=config,
            secrets=secrets,
            language=language,
        )

        notification_preference_input_channel.additional_properties = d
        return notification_preference_input_channel

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
