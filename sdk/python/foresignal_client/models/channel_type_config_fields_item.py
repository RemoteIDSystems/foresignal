from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.channel_type_config_fields_item_type import (
    ChannelTypeConfigFieldsItemType,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="ChannelTypeConfigFieldsItem")


@_attrs_define
class ChannelTypeConfigFieldsItem:
    """
    Attributes:
        name (str):
        type_ (ChannelTypeConfigFieldsItemType):
        required (bool):
        description (str | Unset):
        default (Any | Unset):
        allowed (list[str] | Unset):
    """

    name: str
    type_: ChannelTypeConfigFieldsItemType
    required: bool
    description: str | Unset = UNSET
    default: Any | Unset = UNSET
    allowed: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name

        type_ = self.type_.value

        required = self.required

        description = self.description

        default = self.default

        allowed: list[str] | Unset = UNSET
        if not isinstance(self.allowed, Unset):
            allowed = self.allowed

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "type": type_,
                "required": required,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if default is not UNSET:
            field_dict["default"] = default
        if allowed is not UNSET:
            field_dict["allowed"] = allowed

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = d.pop("name")

        type_ = ChannelTypeConfigFieldsItemType(d.pop("type"))

        required = d.pop("required")

        description = d.pop("description", UNSET)

        default = d.pop("default", UNSET)

        allowed = cast(list[str], d.pop("allowed", UNSET))

        channel_type_config_fields_item = cls(
            name=name,
            type_=type_,
            required=required,
            description=description,
            default=default,
            allowed=allowed,
        )

        channel_type_config_fields_item.additional_properties = d
        return channel_type_config_fields_item

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
