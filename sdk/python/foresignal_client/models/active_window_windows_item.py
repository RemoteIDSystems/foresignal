from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.active_window_windows_item_days_item import (
    ActiveWindowWindowsItemDaysItem,
)

T = TypeVar("T", bound="ActiveWindowWindowsItem")


@_attrs_define
class ActiveWindowWindowsItem:
    """
    Attributes:
        days (list[ActiveWindowWindowsItemDaysItem]):
        start (str):  Example: 22:00.
        end (str): end < start spans midnight Example: 07:00.
    """

    days: list[ActiveWindowWindowsItemDaysItem]
    start: str
    end: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        days = []
        for days_item_data in self.days:
            days_item = days_item_data.value
            days.append(days_item)

        start = self.start

        end = self.end

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "days": days,
                "start": start,
                "end": end,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        days = []
        _days = d.pop("days")
        for days_item_data in _days:
            days_item = ActiveWindowWindowsItemDaysItem(days_item_data)

            days.append(days_item)

        start = d.pop("start")

        end = d.pop("end")

        active_window_windows_item = cls(
            days=days,
            start=start,
            end=end,
        )

        active_window_windows_item.additional_properties = d
        return active_window_windows_item

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
