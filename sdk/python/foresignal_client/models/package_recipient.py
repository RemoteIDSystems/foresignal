from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="PackageRecipient")


@_attrs_define
class PackageRecipient:
    """A recipient instance and principal handles there

    Attributes:
        instance_id (str):
        principals (list[str]): Principal handles on that instance; `*` allowed only for GREEN/CLEAR
    """

    instance_id: str
    principals: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instance_id = self.instance_id

        principals = self.principals

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "instanceId": instance_id,
                "principals": principals,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        instance_id = d.pop("instanceId")

        principals = cast(list[str], d.pop("principals"))

        package_recipient = cls(
            instance_id=instance_id,
            principals=principals,
        )

        package_recipient.additional_properties = d
        return package_recipient

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
