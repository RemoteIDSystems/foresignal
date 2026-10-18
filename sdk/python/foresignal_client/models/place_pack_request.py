from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="PlacePackRequest")


@_attrs_define
class PlacePackRequest:
    """Place offline pack: place-weather, forecast series, nearby alerts, legends, a loop GIF and one PMTiles per product
    (≤ 50 MB)

        Attributes:
            products (list[str] | Unset): Default (absent or empty): radar, IR and the place's forecast products. Products
                of sources the caller may not read are left out.
            frames (int | Unset):  Default: 12.
            radius_km (float | Unset):  Default: 150.0.
            max_zoom (int | Unset):  Default: 8.
    """

    products: list[str] | Unset = UNSET
    frames: int | Unset = 12
    radius_km: float | Unset = 150.0
    max_zoom: int | Unset = 8

    def to_dict(self) -> dict[str, Any]:
        products: list[str] | Unset = UNSET
        if not isinstance(self.products, Unset):
            products = self.products

        frames = self.frames

        radius_km = self.radius_km

        max_zoom = self.max_zoom

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if products is not UNSET:
            field_dict["products"] = products
        if frames is not UNSET:
            field_dict["frames"] = frames
        if radius_km is not UNSET:
            field_dict["radiusKm"] = radius_km
        if max_zoom is not UNSET:
            field_dict["maxZoom"] = max_zoom

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        products = cast(list[str], d.pop("products", UNSET))

        frames = d.pop("frames", UNSET)

        radius_km = d.pop("radiusKm", UNSET)

        max_zoom = d.pop("maxZoom", UNSET)

        place_pack_request = cls(
            products=products,
            frames=frames,
            radius_km=radius_km,
            max_zoom=max_zoom,
        )

        return place_pack_request
