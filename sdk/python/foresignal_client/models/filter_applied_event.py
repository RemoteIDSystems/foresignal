from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="FilterAppliedEvent")


@_attrs_define
class FilterAppliedEvent:
    """SSE `filter-applied` event: every later event uses the new filter (no server-side spatial replay)

    Attributes:
        at_seq (int): Last change seq delivered under the old filter
        filter_version (int):
    """

    at_seq: int
    filter_version: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        at_seq = self.at_seq

        filter_version = self.filter_version

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "atSeq": at_seq,
                "filterVersion": filter_version,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        at_seq = d.pop("atSeq")

        filter_version = d.pop("filterVersion")

        filter_applied_event = cls(
            at_seq=at_seq,
            filter_version=filter_version,
        )

        filter_applied_event.additional_properties = d
        return filter_applied_event

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
