from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intel_board_benchmarks_item_status import IntelBoardBenchmarksItemStatus

T = TypeVar("T", bound="IntelBoardBenchmarksItem")


@_attrs_define
class IntelBoardBenchmarksItem:
    """Latest benchmark status

    Attributes:
        component (str):
        status (IntelBoardBenchmarksItemStatus):
    """

    component: str
    status: IntelBoardBenchmarksItemStatus
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        component = self.component

        status = self.status.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "component": component,
                "status": status,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        component = d.pop("component")

        status = IntelBoardBenchmarksItemStatus(d.pop("status"))

        intel_board_benchmarks_item = cls(
            component=component,
            status=status,
        )

        intel_board_benchmarks_item.additional_properties = d
        return intel_board_benchmarks_item

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
