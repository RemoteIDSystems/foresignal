from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.ogc_tile_set_list_tilesets_item import OgcTileSetListTilesetsItem


T = TypeVar("T", bound="OgcTileSetList")


@_attrs_define
class OgcTileSetList:
    """Tilesets of a collection

    Attributes:
        tilesets (list[OgcTileSetListTilesetsItem]):
    """

    tilesets: list[OgcTileSetListTilesetsItem]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tilesets = []
        for tilesets_item_data in self.tilesets:
            tilesets_item = tilesets_item_data.to_dict()
            tilesets.append(tilesets_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "tilesets": tilesets,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_tile_set_list_tilesets_item import OgcTileSetListTilesetsItem

        d = dict(src_dict)
        tilesets = []
        _tilesets = d.pop("tilesets")
        for tilesets_item_data in _tilesets:
            tilesets_item = OgcTileSetListTilesetsItem.from_dict(tilesets_item_data)

            tilesets.append(tilesets_item)

        ogc_tile_set_list = cls(
            tilesets=tilesets,
        )

        ogc_tile_set_list.additional_properties = d
        return ogc_tile_set_list

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
