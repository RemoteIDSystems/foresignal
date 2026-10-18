from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.tag_assign_result_skipped_item import TagAssignResultSkippedItem


T = TypeVar("T", bound="TagAssignResult")


@_attrs_define
class TagAssignResult:
    """Outcome of a bulk tag assignment or removal

    Attributes:
        tag_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        changed (int): Assignments created (assign) or removed (unassign)
        unchanged (int): Already assigned (assign) or not assigned (unassign)
        skipped (list[TagAssignResultSkippedItem]):
    """

    tag_id: str
    changed: int
    unchanged: int
    skipped: list[TagAssignResultSkippedItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tag_id = self.tag_id

        changed = self.changed

        unchanged = self.unchanged

        skipped = []
        for skipped_item_data in self.skipped:
            skipped_item = skipped_item_data.to_dict()
            skipped.append(skipped_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "tagId": tag_id,
                "changed": changed,
                "unchanged": unchanged,
                "skipped": skipped,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.tag_assign_result_skipped_item import TagAssignResultSkippedItem

        d = dict(src_dict)
        tag_id = d.pop("tagId")

        changed = d.pop("changed")

        unchanged = d.pop("unchanged")

        skipped = []
        _skipped = d.pop("skipped")
        for skipped_item_data in _skipped:
            skipped_item = TagAssignResultSkippedItem.from_dict(skipped_item_data)

            skipped.append(skipped_item)

        tag_assign_result = cls(
            tag_id=tag_id,
            changed=changed,
            unchanged=unchanged,
            skipped=skipped,
        )

        tag_assign_result.additional_properties = d
        return tag_assign_result

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
