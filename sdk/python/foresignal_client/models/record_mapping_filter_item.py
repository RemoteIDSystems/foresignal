from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.record_mapping_filter_item_op import RecordMappingFilterItemOp
from ..types import UNSET, Unset

T = TypeVar("T", bound="RecordMappingFilterItem")


@_attrs_define
class RecordMappingFilterItem:
    """
    Attributes:
        path (str):
        op (RecordMappingFilterItemOp):
        value (Any | Unset):
    """

    path: str
    op: RecordMappingFilterItemOp
    value: Any | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        path = self.path

        op = self.op.value

        value = self.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "path": path,
                "op": op,
            }
        )
        if value is not UNSET:
            field_dict["value"] = value

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        path = d.pop("path")

        op = RecordMappingFilterItemOp(d.pop("op"))

        value = d.pop("value", UNSET)

        record_mapping_filter_item = cls(
            path=path,
            op=op,
            value=value,
        )

        record_mapping_filter_item.additional_properties = d
        return record_mapping_filter_item

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
