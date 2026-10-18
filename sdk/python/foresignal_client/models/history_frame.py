from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.frame_item import FrameItem
    from ..models.history_frame_removals_item import HistoryFrameRemovalsItem


T = TypeVar("T", bound="HistoryFrame")


@_attrs_define
class HistoryFrame:
    """Changes in one frame interval (t_{i−1}, t_i] (037)

    Attributes:
        t (datetime.datetime):
        upserts (list[FrameItem]):
        removals (list[HistoryFrameRemovalsItem]):
    """

    t: datetime.datetime
    upserts: list[FrameItem]
    removals: list[HistoryFrameRemovalsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        t = self.t.isoformat()

        upserts = []
        for upserts_item_data in self.upserts:
            upserts_item = upserts_item_data.to_dict()
            upserts.append(upserts_item)

        removals = []
        for removals_item_data in self.removals:
            removals_item = removals_item_data.to_dict()
            removals.append(removals_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "t": t,
                "upserts": upserts,
                "removals": removals,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.frame_item import FrameItem
        from ..models.history_frame_removals_item import HistoryFrameRemovalsItem

        d = dict(src_dict)
        t = datetime.datetime.fromisoformat(d.pop("t"))

        upserts = []
        _upserts = d.pop("upserts")
        for upserts_item_data in _upserts:
            upserts_item = FrameItem.from_dict(upserts_item_data)

            upserts.append(upserts_item)

        removals = []
        _removals = d.pop("removals")
        for removals_item_data in _removals:
            removals_item = HistoryFrameRemovalsItem.from_dict(removals_item_data)

            removals.append(removals_item)

        history_frame = cls(
            t=t,
            upserts=upserts,
            removals=removals,
        )

        history_frame.additional_properties = d
        return history_frame

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
