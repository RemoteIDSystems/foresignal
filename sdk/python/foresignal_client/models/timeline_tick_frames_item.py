from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.timeline_tick_frames_item_availability import (
    TimelineTickFramesItemAvailability,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="TimelineTickFramesItem")


@_attrs_define
class TimelineTickFramesItem:
    """Frame chosen for a product at this tick

    Attributes:
        product_id (str):
        time (datetime.datetime): Valid time of the chosen frame (within maxGap of the tick)
        tiles (str): Expanded tile template
        availability (TimelineTickFramesItemAvailability): `unverified` for synthesized external ticks
        frame_key (None | str | Unset): Null for external entries
        run (None | str | Unset):
        step (int | None | Unset):
    """

    product_id: str
    time: datetime.datetime
    tiles: str
    availability: TimelineTickFramesItemAvailability
    frame_key: None | str | Unset = UNSET
    run: None | str | Unset = UNSET
    step: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        product_id = self.product_id

        time = self.time.isoformat()

        tiles = self.tiles

        availability = self.availability.value

        frame_key: None | str | Unset
        if isinstance(self.frame_key, Unset):
            frame_key = UNSET
        else:
            frame_key = self.frame_key

        run: None | str | Unset
        if isinstance(self.run, Unset):
            run = UNSET
        else:
            run = self.run

        step: int | None | Unset
        if isinstance(self.step, Unset):
            step = UNSET
        else:
            step = self.step

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "productId": product_id,
                "time": time,
                "tiles": tiles,
                "availability": availability,
            }
        )
        if frame_key is not UNSET:
            field_dict["frameKey"] = frame_key
        if run is not UNSET:
            field_dict["run"] = run
        if step is not UNSET:
            field_dict["step"] = step

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        product_id = d.pop("productId")

        time = datetime.datetime.fromisoformat(d.pop("time"))

        tiles = d.pop("tiles")

        availability = TimelineTickFramesItemAvailability(d.pop("availability"))

        def _parse_frame_key(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        frame_key = _parse_frame_key(d.pop("frameKey", UNSET))

        def _parse_run(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        run = _parse_run(d.pop("run", UNSET))

        def _parse_step(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        step = _parse_step(d.pop("step", UNSET))

        timeline_tick_frames_item = cls(
            product_id=product_id,
            time=time,
            tiles=tiles,
            availability=availability,
            frame_key=frame_key,
            run=run,
            step=step,
        )

        timeline_tick_frames_item.additional_properties = d
        return timeline_tick_frames_item

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
