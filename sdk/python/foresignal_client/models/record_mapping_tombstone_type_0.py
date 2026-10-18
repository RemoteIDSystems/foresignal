from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RecordMappingTombstoneType0")


@_attrs_define
class RecordMappingTombstoneType0:
    """A record whose value at `path` equals `equals` (or is in `in`) is a tombstone

    Attributes:
        path (str):
        equals (Any | Unset):
        in_ (list[Any] | Unset):
    """

    path: str
    equals: Any | Unset = UNSET
    in_: list[Any] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        path = self.path

        equals = self.equals

        in_: list[Any] | Unset = UNSET
        if not isinstance(self.in_, Unset):
            in_ = self.in_

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "path": path,
            }
        )
        if equals is not UNSET:
            field_dict["equals"] = equals
        if in_ is not UNSET:
            field_dict["in"] = in_

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        path = d.pop("path")

        equals = d.pop("equals", UNSET)

        in_ = cast(list[Any], d.pop("in", UNSET))

        record_mapping_tombstone_type_0 = cls(
            path=path,
            equals=equals,
            in_=in_,
        )

        record_mapping_tombstone_type_0.additional_properties = d
        return record_mapping_tombstone_type_0

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
