from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.push_provider_input_type import PushProviderInputType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.apns_provider_config import ApnsProviderConfig
    from ..models.fcm_provider_config import FcmProviderConfig
    from ..models.push_provider_input_secrets import PushProviderInputSecrets


T = TypeVar("T", bound="PushProviderInput")


@_attrs_define
class PushProviderInput:
    """Create or replace a native push provider (032); omitted `secrets` keep the stored ones

    Attributes:
        type_ (PushProviderInputType):
        app_ids (list[str]): Bundle ids / Android package names served by this key
        config (ApnsProviderConfig | FcmProviderConfig):
        secrets (PushProviderInputSecrets | Unset): Write-only secrets
        enabled (bool | Unset):  Default: True.
    """

    type_: PushProviderInputType
    app_ids: list[str]
    config: ApnsProviderConfig | FcmProviderConfig
    secrets: PushProviderInputSecrets | Unset = UNSET
    enabled: bool | Unset = True
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.apns_provider_config import ApnsProviderConfig

        type_ = self.type_.value

        app_ids = self.app_ids

        config: dict[str, Any]
        if isinstance(self.config, ApnsProviderConfig):
            config = self.config.to_dict()
        else:
            config = self.config.to_dict()

        secrets: dict[str, Any] | Unset = UNSET
        if not isinstance(self.secrets, Unset):
            secrets = self.secrets.to_dict()

        enabled = self.enabled

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "appIds": app_ids,
                "config": config,
            }
        )
        if secrets is not UNSET:
            field_dict["secrets"] = secrets
        if enabled is not UNSET:
            field_dict["enabled"] = enabled

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.apns_provider_config import ApnsProviderConfig
        from ..models.fcm_provider_config import FcmProviderConfig
        from ..models.push_provider_input_secrets import PushProviderInputSecrets

        d = dict(src_dict)
        type_ = PushProviderInputType(d.pop("type"))

        app_ids = cast(list[str], d.pop("appIds"))

        def _parse_config(data: object) -> ApnsProviderConfig | FcmProviderConfig:
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                config_type_0 = ApnsProviderConfig.from_dict(data)

                return config_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            if not isinstance(data, dict):
                raise TypeError()
            config_type_1 = FcmProviderConfig.from_dict(data)

            return config_type_1

        config = _parse_config(d.pop("config"))

        _secrets = d.pop("secrets", UNSET)
        secrets: PushProviderInputSecrets | Unset
        if isinstance(_secrets, Unset):
            secrets = UNSET
        else:
            secrets = PushProviderInputSecrets.from_dict(_secrets)

        enabled = d.pop("enabled", UNSET)

        push_provider_input = cls(
            type_=type_,
            app_ids=app_ids,
            config=config,
            secrets=secrets,
            enabled=enabled,
        )

        push_provider_input.additional_properties = d
        return push_provider_input

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
