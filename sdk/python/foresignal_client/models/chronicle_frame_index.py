from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_frame_index_frames_item import ChronicleFrameIndexFramesItem


T = TypeVar("T", bound="ChronicleFrameIndex")


@_attrs_define
class ChronicleFrameIndex:
    """Frame index for chronicle playback: counts and changed entities per step

    Attributes:
        chronicle_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        from_ (datetime.datetime):
        to (datetime.datetime):
        step (str): ISO-8601 duration Example: PT60S.
        frames (list[ChronicleFrameIndexFramesItem]):
        layers (list[str] | Unset):
        truncated (bool | Unset):
    """

    chronicle_id: str
    from_: datetime.datetime
    to: datetime.datetime
    step: str
    frames: list[ChronicleFrameIndexFramesItem]
    layers: list[str] | Unset = UNSET
    truncated: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        chronicle_id = self.chronicle_id

        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        step = self.step

        frames = []
        for frames_item_data in self.frames:
            frames_item = frames_item_data.to_dict()
            frames.append(frames_item)

        layers: list[str] | Unset = UNSET
        if not isinstance(self.layers, Unset):
            layers = self.layers

        truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "chronicleId": chronicle_id,
                "from": from_,
                "to": to,
                "step": step,
                "frames": frames,
            }
        )
        if layers is not UNSET:
            field_dict["layers"] = layers
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_frame_index_frames_item import (
            ChronicleFrameIndexFramesItem,
        )

        d = dict(src_dict)
        chronicle_id = d.pop("chronicleId")

        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        step = d.pop("step")

        frames = []
        _frames = d.pop("frames")
        for frames_item_data in _frames:
            frames_item = ChronicleFrameIndexFramesItem.from_dict(frames_item_data)

            frames.append(frames_item)

        layers = cast(list[str], d.pop("layers", UNSET))

        truncated = d.pop("truncated", UNSET)

        chronicle_frame_index = cls(
            chronicle_id=chronicle_id,
            from_=from_,
            to=to,
            step=step,
            frames=frames,
            layers=layers,
            truncated=truncated,
        )

        chronicle_frame_index.additional_properties = d
        return chronicle_frame_index

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
