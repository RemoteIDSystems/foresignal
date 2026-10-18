from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.timeline_tick_kind import TimelineTickKind

if TYPE_CHECKING:
    from ..models.timeline_tick_frames_item import TimelineTickFramesItem


T = TypeVar("T", bound="TimelineTick")


@_attrs_define
class TimelineTick:
    """One tick of the cross-product timeline

    Attributes:
        time (datetime.datetime):
        kind (TimelineTickKind):
        frames (list[TimelineTickFramesItem]):
    """

    time: datetime.datetime
    kind: TimelineTickKind
    frames: list[TimelineTickFramesItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        time = self.time.isoformat()

        kind = self.kind.value

        frames = []
        for frames_item_data in self.frames:
            frames_item = frames_item_data.to_dict()
            frames.append(frames_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "time": time,
                "kind": kind,
                "frames": frames,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.timeline_tick_frames_item import TimelineTickFramesItem

        d = dict(src_dict)
        time = datetime.datetime.fromisoformat(d.pop("time"))

        kind = TimelineTickKind(d.pop("kind"))

        frames = []
        _frames = d.pop("frames")
        for frames_item_data in _frames:
            frames_item = TimelineTickFramesItem.from_dict(frames_item_data)

            frames.append(frames_item)

        timeline_tick = cls(
            time=time,
            kind=kind,
            frames=frames,
        )

        timeline_tick.additional_properties = d
        return timeline_tick

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
