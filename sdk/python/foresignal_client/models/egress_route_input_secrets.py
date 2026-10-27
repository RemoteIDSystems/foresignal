from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="EgressRouteInputSecrets")


@_attrs_define
class EgressRouteInputSecrets:
    """Write-only proxy/VPN credentials and config

    Attributes:
        username (str | Unset):
        password (str | Unset):
        wireguard_config (str | Unset): A WireGuard profile to import
    """

    username: str | Unset = UNSET
    password: str | Unset = UNSET
    wireguard_config: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        username = self.username

        password = self.password

        wireguard_config = self.wireguard_config

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if username is not UNSET:
            field_dict["username"] = username
        if password is not UNSET:
            field_dict["password"] = password
        if wireguard_config is not UNSET:
            field_dict["wireguardConfig"] = wireguard_config

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        username = d.pop("username", UNSET)

        password = d.pop("password", UNSET)

        wireguard_config = d.pop("wireguardConfig", UNSET)

        egress_route_input_secrets = cls(
            username=username,
            password=password,
            wireguard_config=wireguard_config,
        )

        egress_route_input_secrets.additional_properties = d
        return egress_route_input_secrets

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
