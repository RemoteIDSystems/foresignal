from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ProductSpatial")


@_attrs_define
class ProductSpatial:
    """Spatial facts of a product for map clients

    Attributes:
        bounds (list[float]): [west, south, east, north] in WGS84 degrees
        min_zoom (int):
        max_zoom (int): Native max zoom (MRMS 8, GOES C13 7, GFS 4, HRRR 8); clients overzoom above it
        max_zoom_for_animation (int | Unset): Highest zoom warmed for animation (radar 7, IR 6)
        center (list[float] | Unset): [lon, lat, zoom]
    """

    bounds: list[float]
    min_zoom: int
    max_zoom: int
    max_zoom_for_animation: int | Unset = UNSET
    center: list[float] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        bounds = self.bounds

        min_zoom = self.min_zoom

        max_zoom = self.max_zoom

        max_zoom_for_animation = self.max_zoom_for_animation

        center: list[float] | Unset = UNSET
        if not isinstance(self.center, Unset):
            center = self.center

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "bounds": bounds,
                "minZoom": min_zoom,
                "maxZoom": max_zoom,
            }
        )
        if max_zoom_for_animation is not UNSET:
            field_dict["maxZoomForAnimation"] = max_zoom_for_animation
        if center is not UNSET:
            field_dict["center"] = center

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        bounds = cast(list[float], d.pop("bounds"))

        min_zoom = d.pop("minZoom")

        max_zoom = d.pop("maxZoom")

        max_zoom_for_animation = d.pop("maxZoomForAnimation", UNSET)

        center = cast(list[float], d.pop("center", UNSET))

        product_spatial = cls(
            bounds=bounds,
            min_zoom=min_zoom,
            max_zoom=max_zoom,
            max_zoom_for_animation=max_zoom_for_animation,
            center=center,
        )

        product_spatial.additional_properties = d
        return product_spatial

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
