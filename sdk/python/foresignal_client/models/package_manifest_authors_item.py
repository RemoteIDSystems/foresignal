from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.package_manifest_authors_item_role import PackageManifestAuthorsItemRole
from ..types import UNSET, Unset

T = TypeVar("T", bound="PackageManifestAuthorsItem")


@_attrs_define
class PackageManifestAuthorsItem:
    """Author or forwarder

    Attributes:
        handle (str):
        instance_id (str):
        role (PackageManifestAuthorsItemRole):
        display_name (str | Unset):
    """

    handle: str
    instance_id: str
    role: PackageManifestAuthorsItemRole
    display_name: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        handle = self.handle

        instance_id = self.instance_id

        role = self.role.value

        display_name = self.display_name

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "handle": handle,
                "instanceId": instance_id,
                "role": role,
            }
        )
        if display_name is not UNSET:
            field_dict["displayName"] = display_name

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        handle = d.pop("handle")

        instance_id = d.pop("instanceId")

        role = PackageManifestAuthorsItemRole(d.pop("role"))

        display_name = d.pop("displayName", UNSET)

        package_manifest_authors_item = cls(
            handle=handle,
            instance_id=instance_id,
            role=role,
            display_name=display_name,
        )

        package_manifest_authors_item.additional_properties = d
        return package_manifest_authors_item

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
