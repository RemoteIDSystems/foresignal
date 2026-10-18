from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.ogc_tile_matrix_set_list_tile_matrix_sets_item import (
        OgcTileMatrixSetListTileMatrixSetsItem,
    )


T = TypeVar("T", bound="OgcTileMatrixSetList")


@_attrs_define
class OgcTileMatrixSetList:
    """Tile matrix sets offered

    Attributes:
        tile_matrix_sets (list[OgcTileMatrixSetListTileMatrixSetsItem]):
    """

    tile_matrix_sets: list[OgcTileMatrixSetListTileMatrixSetsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tile_matrix_sets = []
        for tile_matrix_sets_item_data in self.tile_matrix_sets:
            tile_matrix_sets_item = tile_matrix_sets_item_data.to_dict()
            tile_matrix_sets.append(tile_matrix_sets_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "tileMatrixSets": tile_matrix_sets,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_tile_matrix_set_list_tile_matrix_sets_item import (
            OgcTileMatrixSetListTileMatrixSetsItem,
        )

        d = dict(src_dict)
        tile_matrix_sets = []
        _tile_matrix_sets = d.pop("tileMatrixSets")
        for tile_matrix_sets_item_data in _tile_matrix_sets:
            tile_matrix_sets_item = OgcTileMatrixSetListTileMatrixSetsItem.from_dict(
                tile_matrix_sets_item_data
            )

            tile_matrix_sets.append(tile_matrix_sets_item)

        ogc_tile_matrix_set_list = cls(
            tile_matrix_sets=tile_matrix_sets,
        )

        ogc_tile_matrix_set_list.additional_properties = d
        return ogc_tile_matrix_set_list

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
