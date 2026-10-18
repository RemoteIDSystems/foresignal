from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tag_assign_result_skipped_item_reason import (
    TagAssignResultSkippedItemReason,
)

T = TypeVar("T", bound="TagAssignResultSkippedItem")


@_attrs_define
class TagAssignResultSkippedItem:
    """A reference that was not processed

    Attributes:
        ref (str):
        reason (TagAssignResultSkippedItemReason):
    """

    ref: str
    reason: TagAssignResultSkippedItemReason
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        ref = self.ref

        reason = self.reason.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ref": ref,
                "reason": reason,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        ref = d.pop("ref")

        reason = TagAssignResultSkippedItemReason(d.pop("reason"))

        tag_assign_result_skipped_item = cls(
            ref=ref,
            reason=reason,
        )

        tag_assign_result_skipped_item.additional_properties = d
        return tag_assign_result_skipped_item

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
