from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.record_mapping_spread_type_0_type import RecordMappingSpreadType0Type
from ..types import UNSET, Unset

T = TypeVar("T", bound="RecordMappingSpreadType0")


@_attrs_define
class RecordMappingSpreadType0:
    """Copies the scalar members of one object into top-level fields (ChirpStack `object`, TTS `decoded_payload`). Members
    in `drop`, members whose name is not a valid field name, and names a field already produced are skipped (059)

        Attributes:
            path (str): JSONPath of the object
            type_ (RecordMappingSpreadType0Type | Unset): Type every copied value must have; others are skipped Default:
                RecordMappingSpreadType0Type.NUMBER.
            max_ (int | Unset):  Default: 32.
    """

    path: str
    type_: RecordMappingSpreadType0Type | Unset = RecordMappingSpreadType0Type.NUMBER
    max_: int | Unset = 32
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        path = self.path

        type_: str | Unset = UNSET
        if not isinstance(self.type_, Unset):
            type_ = self.type_.value

        max_ = self.max_

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "path": path,
            }
        )
        if type_ is not UNSET:
            field_dict["type"] = type_
        if max_ is not UNSET:
            field_dict["max"] = max_

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        path = d.pop("path")

        _type_ = d.pop("type", UNSET)
        type_: RecordMappingSpreadType0Type | Unset
        if isinstance(_type_, Unset):
            type_ = UNSET
        else:
            type_ = RecordMappingSpreadType0Type(_type_)

        max_ = d.pop("max", UNSET)

        record_mapping_spread_type_0 = cls(
            path=path,
            type_=type_,
            max_=max_,
        )

        record_mapping_spread_type_0.additional_properties = d
        return record_mapping_spread_type_0

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
