from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tile_layer_index_tilejson import TileLayerIndexTilejson

if TYPE_CHECKING:
    from ..models.tile_layer_index_layers_item import TileLayerIndexLayersItem


T = TypeVar("T", bound="TileLayerIndex")


@_attrs_define
class TileLayerIndex:
    """Index of vector-tile layers (one per source id and per kind with a visible source)

    Attributes:
        tilejson (TileLayerIndexTilejson):
        layers (list[TileLayerIndexLayersItem]):
    """

    tilejson: TileLayerIndexTilejson
    layers: list[TileLayerIndexLayersItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tilejson = self.tilejson.value

        layers = []
        for layers_item_data in self.layers:
            layers_item = layers_item_data.to_dict()
            layers.append(layers_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "tilejson": tilejson,
                "layers": layers,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.tile_layer_index_layers_item import TileLayerIndexLayersItem

        d = dict(src_dict)
        tilejson = TileLayerIndexTilejson(d.pop("tilejson"))

        layers = []
        _layers = d.pop("layers")
        for layers_item_data in _layers:
            layers_item = TileLayerIndexLayersItem.from_dict(layers_item_data)

            layers.append(layers_item)

        tile_layer_index = cls(
            tilejson=tilejson,
            layers=layers,
        )

        tile_layer_index.additional_properties = d
        return tile_layer_index

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
