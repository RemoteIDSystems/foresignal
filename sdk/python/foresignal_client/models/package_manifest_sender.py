from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PackageManifestSender")


@_attrs_define
class PackageManifestSender:
    """Sending instance

    Attributes:
        instance_id (str):
        key_id (str): Kid in the sender's 109 JWKS
        instance_name (None | str | Unset):
    """

    instance_id: str
    key_id: str
    instance_name: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instance_id = self.instance_id

        key_id = self.key_id

        instance_name: None | str | Unset
        if isinstance(self.instance_name, Unset):
            instance_name = UNSET
        else:
            instance_name = self.instance_name

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "instanceId": instance_id,
                "keyId": key_id,
            }
        )
        if instance_name is not UNSET:
            field_dict["instanceName"] = instance_name

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        instance_id = d.pop("instanceId")

        key_id = d.pop("keyId")

        def _parse_instance_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        instance_name = _parse_instance_name(d.pop("instanceName", UNSET))

        package_manifest_sender = cls(
            instance_id=instance_id,
            key_id=key_id,
            instance_name=instance_name,
        )

        package_manifest_sender.additional_properties = d
        return package_manifest_sender

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
