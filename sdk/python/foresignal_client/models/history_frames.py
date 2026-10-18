from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.frame_item import FrameItem
    from ..models.history_frame import HistoryFrame


T = TypeVar("T", bound="HistoryFrames")


@_attrs_define
class HistoryFrames:
    """Keyframe plus delta frames for animation on the transaction axis (037); applying frames 1..i to the keyframe equals
    asOf(t_i)

        Attributes:
            as_of (datetime.datetime):
            step (str):
            watermark_seq (int):
            keyframe (list[FrameItem]):
            frames (list[HistoryFrame]):
            horizon (datetime.datetime | None | Unset):
            approximate (bool | Unset):
    """

    as_of: datetime.datetime
    step: str
    watermark_seq: int
    keyframe: list[FrameItem]
    frames: list[HistoryFrame]
    horizon: datetime.datetime | None | Unset = UNSET
    approximate: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        as_of = self.as_of.isoformat()

        step = self.step

        watermark_seq = self.watermark_seq

        keyframe = []
        for keyframe_item_data in self.keyframe:
            keyframe_item = keyframe_item_data.to_dict()
            keyframe.append(keyframe_item)

        frames = []
        for frames_item_data in self.frames:
            frames_item = frames_item_data.to_dict()
            frames.append(frames_item)

        horizon: None | str | Unset
        if isinstance(self.horizon, Unset):
            horizon = UNSET
        elif isinstance(self.horizon, datetime.datetime):
            horizon = self.horizon.isoformat()
        else:
            horizon = self.horizon

        approximate = self.approximate

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "asOf": as_of,
                "step": step,
                "watermarkSeq": watermark_seq,
                "keyframe": keyframe,
                "frames": frames,
            }
        )
        if horizon is not UNSET:
            field_dict["horizon"] = horizon
        if approximate is not UNSET:
            field_dict["approximate"] = approximate

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.frame_item import FrameItem
        from ..models.history_frame import HistoryFrame

        d = dict(src_dict)
        as_of = datetime.datetime.fromisoformat(d.pop("asOf"))

        step = d.pop("step")

        watermark_seq = d.pop("watermarkSeq")

        keyframe = []
        _keyframe = d.pop("keyframe")
        for keyframe_item_data in _keyframe:
            keyframe_item = FrameItem.from_dict(keyframe_item_data)

            keyframe.append(keyframe_item)

        frames = []
        _frames = d.pop("frames")
        for frames_item_data in _frames:
            frames_item = HistoryFrame.from_dict(frames_item_data)

            frames.append(frames_item)

        def _parse_horizon(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                horizon_type_0 = datetime.datetime.fromisoformat(data)

                return horizon_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        horizon = _parse_horizon(d.pop("horizon", UNSET))

        approximate = d.pop("approximate", UNSET)

        history_frames = cls(
            as_of=as_of,
            step=step,
            watermark_seq=watermark_seq,
            keyframe=keyframe,
            frames=frames,
            horizon=horizon,
            approximate=approximate,
        )

        history_frames.additional_properties = d
        return history_frames

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
