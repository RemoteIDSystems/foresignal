from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_social_volume_selector_scopes_item_type import (
    ChronicleSocialVolumeSelectorScopesItemType,
)

T = TypeVar("T", bound="ChronicleSocialVolumeSelectorScopesItem")


@_attrs_define
class ChronicleSocialVolumeSelectorScopesItem:
    """Volume scope

    Attributes:
        type_ (ChronicleSocialVolumeSelectorScopesItemType):
        id (str):
    """

    type_: ChronicleSocialVolumeSelectorScopesItemType
    id: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        id = self.id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "id": id,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = ChronicleSocialVolumeSelectorScopesItemType(d.pop("type"))

        id = d.pop("id")

        chronicle_social_volume_selector_scopes_item = cls(
            type_=type_,
            id=id,
        )

        chronicle_social_volume_selector_scopes_item.additional_properties = d
        return chronicle_social_volume_selector_scopes_item

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
