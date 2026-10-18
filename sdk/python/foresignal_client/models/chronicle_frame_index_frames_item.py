from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_frame_index_frames_item_counts import (
        ChronicleFrameIndexFramesItemCounts,
    )


T = TypeVar("T", bound="ChronicleFrameIndexFramesItem")


@_attrs_define
class ChronicleFrameIndexFramesItem:
    """One playback step

    Attributes:
        t (datetime.datetime):
        counts (ChronicleFrameIndexFramesItemCounts): Layer id → captured versions in the step
        changed (list[str] | Unset): Changed entities (capped)
        changed_total (int | Unset):
    """

    t: datetime.datetime
    counts: ChronicleFrameIndexFramesItemCounts
    changed: list[str] | Unset = UNSET
    changed_total: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        t = self.t.isoformat()

        counts = self.counts.to_dict()

        changed: list[str] | Unset = UNSET
        if not isinstance(self.changed, Unset):
            changed = self.changed

        changed_total = self.changed_total

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "t": t,
                "counts": counts,
            }
        )
        if changed is not UNSET:
            field_dict["changed"] = changed
        if changed_total is not UNSET:
            field_dict["changedTotal"] = changed_total

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_frame_index_frames_item_counts import (
            ChronicleFrameIndexFramesItemCounts,
        )

        d = dict(src_dict)
        t = datetime.datetime.fromisoformat(d.pop("t"))

        counts = ChronicleFrameIndexFramesItemCounts.from_dict(d.pop("counts"))

        changed = cast(list[str], d.pop("changed", UNSET))

        changed_total = d.pop("changedTotal", UNSET)

        chronicle_frame_index_frames_item = cls(
            t=t,
            counts=counts,
            changed=changed,
            changed_total=changed_total,
        )

        chronicle_frame_index_frames_item.additional_properties = d
        return chronicle_frame_index_frames_item

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
