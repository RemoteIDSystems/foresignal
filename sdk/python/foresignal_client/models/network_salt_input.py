from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="NetworkSaltInput")


@_attrs_define
class NetworkSaltInput:
    """Install a per-network discovery salt agreed out of band (stored with secret material, never exported)

    Attributes:
        network_id (str):
        salt_ref (str): The shared salt value (or secret reference); never returned
    """

    network_id: str
    salt_ref: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        network_id = self.network_id

        salt_ref = self.salt_ref

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "networkId": network_id,
                "saltRef": salt_ref,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        network_id = d.pop("networkId")

        salt_ref = d.pop("saltRef")

        network_salt_input = cls(
            network_id=network_id,
            salt_ref=salt_ref,
        )

        network_salt_input.additional_properties = d
        return network_salt_input

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
