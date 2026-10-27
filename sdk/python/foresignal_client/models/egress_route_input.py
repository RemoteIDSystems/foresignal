from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.egress_route_input_secrets import EgressRouteInputSecrets


T = TypeVar("T", bound="EgressRouteInput")


@_attrs_define
class EgressRouteInput:
    """Create or replace an egress route; credentials and WireGuard config are write-only and stored encrypted

    Attributes:
        name (str):
        type_ (str):
        enabled (bool | Unset):  Default: True.
        url (None | str | Unset):
        bound_interface (None | str | Unset):
        secrets (EgressRouteInputSecrets | Unset): Write-only proxy/VPN credentials and config
    """

    name: str
    type_: str
    enabled: bool | Unset = True
    url: None | str | Unset = UNSET
    bound_interface: None | str | Unset = UNSET
    secrets: EgressRouteInputSecrets | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        type_ = self.type_

        enabled = self.enabled

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        bound_interface: None | str | Unset
        if isinstance(self.bound_interface, Unset):
            bound_interface = UNSET
        else:
            bound_interface = self.bound_interface

        secrets: dict[str, Any] | Unset = UNSET
        if not isinstance(self.secrets, Unset):
            secrets = self.secrets.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "type": type_,
            }
        )
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if url is not UNSET:
            field_dict["url"] = url
        if bound_interface is not UNSET:
            field_dict["boundInterface"] = bound_interface
        if secrets is not UNSET:
            field_dict["secrets"] = secrets

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.egress_route_input_secrets import EgressRouteInputSecrets

        d = dict(src_dict)
        name = d.pop("name")

        type_ = d.pop("type")

        enabled = d.pop("enabled", UNSET)

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        def _parse_bound_interface(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        bound_interface = _parse_bound_interface(d.pop("boundInterface", UNSET))

        _secrets = d.pop("secrets", UNSET)
        secrets: EgressRouteInputSecrets | Unset
        if isinstance(_secrets, Unset):
            secrets = UNSET
        else:
            secrets = EgressRouteInputSecrets.from_dict(_secrets)

        egress_route_input = cls(
            name=name,
            type_=type_,
            enabled=enabled,
            url=url,
            bound_interface=bound_interface,
            secrets=secrets,
        )

        egress_route_input.additional_properties = d
        return egress_route_input

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
