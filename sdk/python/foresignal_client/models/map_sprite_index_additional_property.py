from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MapSpriteIndexAdditionalProperty")


@_attrs_define
class MapSpriteIndexAdditionalProperty:
    """
    Attributes:
        x (int):
        y (int):
        width (int):
        height (int):
        pixel_ratio (float):
        sdf (bool | Unset):
    """

    x: int
    y: int
    width: int
    height: int
    pixel_ratio: float
    sdf: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        x = self.x

        y = self.y

        width = self.width

        height = self.height

        pixel_ratio = self.pixel_ratio

        sdf = self.sdf

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "x": x,
                "y": y,
                "width": width,
                "height": height,
                "pixelRatio": pixel_ratio,
            }
        )
        if sdf is not UNSET:
            field_dict["sdf"] = sdf

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        x = d.pop("x")

        y = d.pop("y")

        width = d.pop("width")

        height = d.pop("height")

        pixel_ratio = d.pop("pixelRatio")

        sdf = d.pop("sdf", UNSET)

        map_sprite_index_additional_property = cls(
            x=x,
            y=y,
            width=width,
            height=height,
            pixel_ratio=pixel_ratio,
            sdf=sdf,
        )

        map_sprite_index_additional_property.additional_properties = d
        return map_sprite_index_additional_property

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
