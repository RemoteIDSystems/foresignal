from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RasterFramePagePrefetch")


@_attrs_define
class RasterFramePagePrefetch:
    """Prefetch hint for animation

    Attributes:
        recommended_frames (int):  Example: 12.
        max_zoom_for_animation (int | Unset):
    """

    recommended_frames: int
    max_zoom_for_animation: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        recommended_frames = self.recommended_frames

        max_zoom_for_animation = self.max_zoom_for_animation

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "recommendedFrames": recommended_frames,
            }
        )
        if max_zoom_for_animation is not UNSET:
            field_dict["maxZoomForAnimation"] = max_zoom_for_animation

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        recommended_frames = d.pop("recommendedFrames")

        max_zoom_for_animation = d.pop("maxZoomForAnimation", UNSET)

        raster_frame_page_prefetch = cls(
            recommended_frames=recommended_frames,
            max_zoom_for_animation=max_zoom_for_animation,
        )

        raster_frame_page_prefetch.additional_properties = d
        return raster_frame_page_prefetch

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
