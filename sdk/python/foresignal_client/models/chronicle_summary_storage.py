from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ChronicleSummaryStorage")


@_attrs_define
class ChronicleSummaryStorage:
    """Storage use

    Attributes:
        bytes_used (int):
        size_cap_bytes (int):
        rows (int | Unset):
    """

    bytes_used: int
    size_cap_bytes: int
    rows: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        bytes_used = self.bytes_used

        size_cap_bytes = self.size_cap_bytes

        rows = self.rows

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "bytesUsed": bytes_used,
                "sizeCapBytes": size_cap_bytes,
            }
        )
        if rows is not UNSET:
            field_dict["rows"] = rows

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        bytes_used = d.pop("bytesUsed")

        size_cap_bytes = d.pop("sizeCapBytes")

        rows = d.pop("rows", UNSET)

        chronicle_summary_storage = cls(
            bytes_used=bytes_used,
            size_cap_bytes=size_cap_bytes,
            rows=rows,
        )

        chronicle_summary_storage.additional_properties = d
        return chronicle_summary_storage

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
