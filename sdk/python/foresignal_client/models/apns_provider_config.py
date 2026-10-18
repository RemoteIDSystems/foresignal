from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.apns_provider_config_key_environment import (
    ApnsProviderConfigKeyEnvironment,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ApnsProviderConfig")


@_attrs_define
class ApnsProviderConfig:
    """APNs provider configuration (032)

    Attributes:
        team_id (str):
        key_id (str):
        key_environment (ApnsProviderConfigKeyEnvironment):
        time_sensitive (bool | Unset): Only when the app has the Time Sensitive capability Default: False.
    """

    team_id: str
    key_id: str
    key_environment: ApnsProviderConfigKeyEnvironment
    time_sensitive: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        team_id = self.team_id

        key_id = self.key_id

        key_environment = self.key_environment.value

        time_sensitive = self.time_sensitive

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "teamId": team_id,
                "keyId": key_id,
                "keyEnvironment": key_environment,
            }
        )
        if time_sensitive is not UNSET:
            field_dict["timeSensitive"] = time_sensitive

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        team_id = d.pop("teamId")

        key_id = d.pop("keyId")

        key_environment = ApnsProviderConfigKeyEnvironment(d.pop("keyEnvironment"))

        time_sensitive = d.pop("timeSensitive", UNSET)

        apns_provider_config = cls(
            team_id=team_id,
            key_id=key_id,
            key_environment=key_environment,
            time_sensitive=time_sensitive,
        )

        apns_provider_config.additional_properties = d
        return apns_provider_config

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
