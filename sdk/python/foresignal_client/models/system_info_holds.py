from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SystemInfoHolds")


@_attrs_define
class SystemInfoHolds:
    """Holds

    Attributes:
        count (int | Unset):
        rows (int | Unset):
        bytes_ (int | Unset):
    """

    count: int | Unset = UNSET
    rows: int | Unset = UNSET
    bytes_: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        count = self.count

        rows = self.rows

        bytes_ = self.bytes_

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if count is not UNSET:
            field_dict["count"] = count
        if rows is not UNSET:
            field_dict["rows"] = rows
        if bytes_ is not UNSET:
            field_dict["bytes"] = bytes_

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        count = d.pop("count", UNSET)

        rows = d.pop("rows", UNSET)

        bytes_ = d.pop("bytes", UNSET)

        system_info_holds = cls(
            count=count,
            rows=rows,
            bytes_=bytes_,
        )

        system_info_holds.additional_properties = d
        return system_info_holds

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
