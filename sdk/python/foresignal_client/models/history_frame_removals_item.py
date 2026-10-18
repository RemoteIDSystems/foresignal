from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.history_frame_removals_item_reason import HistoryFrameRemovalsItemReason

T = TypeVar("T", bound="HistoryFrameRemovalsItem")


@_attrs_define
class HistoryFrameRemovalsItem:
    """Entity leaving the frame state

    Attributes:
        id (str):
        reason (HistoryFrameRemovalsItemReason):
        left (bool): True when the entity left the bbox
    """

    id: str
    reason: HistoryFrameRemovalsItemReason
    left: bool
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        reason = self.reason.value

        left = self.left

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "reason": reason,
                "left": left,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        reason = HistoryFrameRemovalsItemReason(d.pop("reason"))

        left = d.pop("left")

        history_frame_removals_item = cls(
            id=id,
            reason=reason,
            left=left,
        )

        history_frame_removals_item.additional_properties = d
        return history_frame_removals_item

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
