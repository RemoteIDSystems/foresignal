from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.field_manifest_components_item_name import FieldManifestComponentsItemName

T = TypeVar("T", bound="FieldManifestComponentsItem")


@_attrs_define
class FieldManifestComponentsItem:
    """Planar component

    Attributes:
        name (FieldManifestComponentsItemName):
        offset (int): Byte offset in the .f32 data
    """

    name: FieldManifestComponentsItemName
    offset: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        name = self.name.value

        offset = self.offset

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "name": name,
                "offset": offset,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        name = FieldManifestComponentsItemName(d.pop("name"))

        offset = d.pop("offset")

        field_manifest_components_item = cls(
            name=name,
            offset=offset,
        )

        field_manifest_components_item.additional_properties = d
        return field_manifest_components_item

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
