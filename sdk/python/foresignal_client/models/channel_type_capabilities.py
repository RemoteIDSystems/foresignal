from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ChannelTypeCapabilities")


@_attrs_define
class ChannelTypeCapabilities:
    """
    Attributes:
        edit_in_place (bool | Unset):
        attachments (bool | Unset):
        fan_out (bool | Unset):
    """

    edit_in_place: bool | Unset = UNSET
    attachments: bool | Unset = UNSET
    fan_out: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        edit_in_place = self.edit_in_place

        attachments = self.attachments

        fan_out = self.fan_out

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if edit_in_place is not UNSET:
            field_dict["editInPlace"] = edit_in_place
        if attachments is not UNSET:
            field_dict["attachments"] = attachments
        if fan_out is not UNSET:
            field_dict["fanOut"] = fan_out

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        edit_in_place = d.pop("editInPlace", UNSET)

        attachments = d.pop("attachments", UNSET)

        fan_out = d.pop("fanOut", UNSET)

        channel_type_capabilities = cls(
            edit_in_place=edit_in_place,
            attachments=attachments,
            fan_out=fan_out,
        )

        channel_type_capabilities.additional_properties = d
        return channel_type_capabilities

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
