from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="CountResult")


@_attrs_define
class CountResult:
    """Count-only response for `count=true` (038)

    Attributes:
        count (int):
        exact (bool): False above 1,000,000
        watermark_seq (int):
    """

    count: int
    exact: bool
    watermark_seq: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        count = self.count

        exact = self.exact

        watermark_seq = self.watermark_seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "count": count,
                "exact": exact,
                "watermarkSeq": watermark_seq,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        count = d.pop("count")

        exact = d.pop("exact")

        watermark_seq = d.pop("watermarkSeq")

        count_result = cls(
            count=count,
            exact=exact,
            watermark_seq=watermark_seq,
        )

        count_result.additional_properties = d
        return count_result

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
