from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.product_loop_crs import ProductLoopCrs
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.attribution import Attribution
    from ..models.product_loop_frames_item import ProductLoopFramesItem
    from ..models.product_loop_runs import ProductLoopRuns


T = TypeVar("T", bound="ProductLoop")


@_attrs_define
class ProductLoop:
    """Animation loop: one snapped bbox and a list of `/image` frame URLs (mobile and animation path)

    Attributes:
        crs (ProductLoopCrs):
        bounds (list[float]): Snapped bounds [w, s, e, n]
        coordinates (list[list[float]]): Corner coordinates [[tl], [tr], [br], [bl]] as [lon, lat] for image overlays
        width (int):
        height (int):
        frames (list[ProductLoopFramesItem]):
        server_time (datetime.datetime):
        products (list[str] | Unset):
        runs (ProductLoopRuns | Unset): Forecast run used per product (never mixed)
        legend_url (None | str | Unset):
        attribution (list[Attribution] | Unset):
    """

    crs: ProductLoopCrs
    bounds: list[float]
    coordinates: list[list[float]]
    width: int
    height: int
    frames: list[ProductLoopFramesItem]
    server_time: datetime.datetime
    products: list[str] | Unset = UNSET
    runs: ProductLoopRuns | Unset = UNSET
    legend_url: None | str | Unset = UNSET
    attribution: list[Attribution] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        crs = self.crs.value

        bounds = self.bounds

        coordinates = []
        for coordinates_item_data in self.coordinates:
            coordinates_item = coordinates_item_data

            coordinates.append(coordinates_item)

        width = self.width

        height = self.height

        frames = []
        for frames_item_data in self.frames:
            frames_item = frames_item_data.to_dict()
            frames.append(frames_item)

        server_time = self.server_time.isoformat()

        products: list[str] | Unset = UNSET
        if not isinstance(self.products, Unset):
            products = self.products

        runs: dict[str, Any] | Unset = UNSET
        if not isinstance(self.runs, Unset):
            runs = self.runs.to_dict()

        legend_url: None | str | Unset
        if isinstance(self.legend_url, Unset):
            legend_url = UNSET
        else:
            legend_url = self.legend_url

        attribution: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = []
            for attribution_item_data in self.attribution:
                attribution_item = attribution_item_data.to_dict()
                attribution.append(attribution_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "crs": crs,
                "bounds": bounds,
                "coordinates": coordinates,
                "width": width,
                "height": height,
                "frames": frames,
                "serverTime": server_time,
            }
        )
        if products is not UNSET:
            field_dict["products"] = products
        if runs is not UNSET:
            field_dict["runs"] = runs
        if legend_url is not UNSET:
            field_dict["legendUrl"] = legend_url
        if attribution is not UNSET:
            field_dict["attribution"] = attribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution
        from ..models.product_loop_frames_item import ProductLoopFramesItem
        from ..models.product_loop_runs import ProductLoopRuns

        d = dict(src_dict)
        crs = ProductLoopCrs(d.pop("crs"))

        bounds = cast(list[float], d.pop("bounds"))

        coordinates = []
        _coordinates = d.pop("coordinates")
        for coordinates_item_data in _coordinates:
            coordinates_item = cast(list[float], coordinates_item_data)

            coordinates.append(coordinates_item)

        width = d.pop("width")

        height = d.pop("height")

        frames = []
        _frames = d.pop("frames")
        for frames_item_data in _frames:
            frames_item = ProductLoopFramesItem.from_dict(frames_item_data)

            frames.append(frames_item)

        server_time = datetime.datetime.fromisoformat(d.pop("serverTime"))

        products = cast(list[str], d.pop("products", UNSET))

        _runs = d.pop("runs", UNSET)
        runs: ProductLoopRuns | Unset
        if isinstance(_runs, Unset):
            runs = UNSET
        else:
            runs = ProductLoopRuns.from_dict(_runs)

        def _parse_legend_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        legend_url = _parse_legend_url(d.pop("legendUrl", UNSET))

        _attribution = d.pop("attribution", UNSET)
        attribution: list[Attribution] | Unset = UNSET
        if _attribution is not UNSET:
            attribution = []
            for attribution_item_data in _attribution:
                attribution_item = Attribution.from_dict(attribution_item_data)

                attribution.append(attribution_item)

        product_loop = cls(
            crs=crs,
            bounds=bounds,
            coordinates=coordinates,
            width=width,
            height=height,
            frames=frames,
            server_time=server_time,
            products=products,
            runs=runs,
            legend_url=legend_url,
            attribution=attribution,
        )

        product_loop.additional_properties = d
        return product_loop

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
