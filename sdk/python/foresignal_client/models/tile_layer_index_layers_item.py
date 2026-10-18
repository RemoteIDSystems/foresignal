from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tile_layer_index_layers_item_type import TileLayerIndexLayersItemType
from ..types import UNSET, Unset

T = TypeVar("T", bound="TileLayerIndexLayersItem")


@_attrs_define
class TileLayerIndexLayersItem:
    """
    Attributes:
        id (str): Layer id (source id or kind)
        type_ (TileLayerIndexLayersItemType):
        name (str):
        tilejson (str): URL of the layer's TileJSON
        agg_below_zoom (int | None | Unset): Zoom below which `agg=auto` aggregates; null = never
    """

    id: str
    type_: TileLayerIndexLayersItemType
    name: str
    tilejson: str
    agg_below_zoom: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        type_ = self.type_.value

        name = self.name

        tilejson = self.tilejson

        agg_below_zoom: int | None | Unset
        if isinstance(self.agg_below_zoom, Unset):
            agg_below_zoom = UNSET
        else:
            agg_below_zoom = self.agg_below_zoom

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "name": name,
                "tilejson": tilejson,
            }
        )
        if agg_below_zoom is not UNSET:
            field_dict["aggBelowZoom"] = agg_below_zoom

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        type_ = TileLayerIndexLayersItemType(d.pop("type"))

        name = d.pop("name")

        tilejson = d.pop("tilejson")

        def _parse_agg_below_zoom(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        agg_below_zoom = _parse_agg_below_zoom(d.pop("aggBelowZoom", UNSET))

        tile_layer_index_layers_item = cls(
            id=id,
            type_=type_,
            name=name,
            tilejson=tilejson,
            agg_below_zoom=agg_below_zoom,
        )

        tile_layer_index_layers_item.additional_properties = d
        return tile_layer_index_layers_item

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
