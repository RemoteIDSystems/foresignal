from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.cycle_packet_item import CyclePacketItem


T = TypeVar("T", bound="CyclePacketOutlooks")


@_attrs_define
class CyclePacketOutlooks:
    """Outlook and index changes

    Attributes:
        index_changes (list[CyclePacketItem] | Unset):
        windows_outside_p10p90 (list[CyclePacketItem] | Unset):
    """

    index_changes: list[CyclePacketItem] | Unset = UNSET
    windows_outside_p10p90: list[CyclePacketItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        index_changes: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.index_changes, Unset):
            index_changes = []
            for index_changes_item_data in self.index_changes:
                index_changes_item = index_changes_item_data.to_dict()
                index_changes.append(index_changes_item)

        windows_outside_p10p90: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.windows_outside_p10p90, Unset):
            windows_outside_p10p90 = []
            for windows_outside_p10p90_item_data in self.windows_outside_p10p90:
                windows_outside_p10p90_item = windows_outside_p10p90_item_data.to_dict()
                windows_outside_p10p90.append(windows_outside_p10p90_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if index_changes is not UNSET:
            field_dict["indexChanges"] = index_changes
        if windows_outside_p10p90 is not UNSET:
            field_dict["windowsOutsideP10P90"] = windows_outside_p10p90

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.cycle_packet_item import CyclePacketItem

        d = dict(src_dict)
        _index_changes = d.pop("indexChanges", UNSET)
        index_changes: list[CyclePacketItem] | Unset = UNSET
        if _index_changes is not UNSET:
            index_changes = []
            for index_changes_item_data in _index_changes:
                index_changes_item = CyclePacketItem.from_dict(index_changes_item_data)

                index_changes.append(index_changes_item)

        _windows_outside_p10p90 = d.pop("windowsOutsideP10P90", UNSET)
        windows_outside_p10p90: list[CyclePacketItem] | Unset = UNSET
        if _windows_outside_p10p90 is not UNSET:
            windows_outside_p10p90 = []
            for windows_outside_p10p90_item_data in _windows_outside_p10p90:
                windows_outside_p10p90_item = CyclePacketItem.from_dict(
                    windows_outside_p10p90_item_data
                )

                windows_outside_p10p90.append(windows_outside_p10p90_item)

        cycle_packet_outlooks = cls(
            index_changes=index_changes,
            windows_outside_p10p90=windows_outside_p10p90,
        )

        cycle_packet_outlooks.additional_properties = d
        return cycle_packet_outlooks

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
