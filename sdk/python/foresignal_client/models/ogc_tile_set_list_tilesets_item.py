from __future__ import annotations

from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="OgcTileSetListTilesetsItem")


@_attrs_define
class OgcTileSetListTilesetsItem:
    """Tileset summary

    Attributes:
        data_type (Literal['map']):
        links (list[OgcLink]):
        title (str | Unset):
        crs (str | Unset):
        tile_matrix_set_uri (str | Unset):
    """

    data_type: Literal["map"]
    links: list[OgcLink]
    title: str | Unset = UNSET
    crs: str | Unset = UNSET
    tile_matrix_set_uri: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        data_type = self.data_type

        links = []
        for links_item_data in self.links:
            links_item = links_item_data.to_dict()
            links.append(links_item)

        title = self.title

        crs = self.crs

        tile_matrix_set_uri = self.tile_matrix_set_uri

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "dataType": data_type,
                "links": links,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if crs is not UNSET:
            field_dict["crs"] = crs
        if tile_matrix_set_uri is not UNSET:
            field_dict["tileMatrixSetURI"] = tile_matrix_set_uri

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        data_type = cast(Literal["map"], d.pop("dataType"))
        if data_type != "map":
            raise ValueError(f"dataType must match const 'map', got '{data_type}'")

        links = []
        _links = d.pop("links")
        for links_item_data in _links:
            links_item = OgcLink.from_dict(links_item_data)

            links.append(links_item)

        title = d.pop("title", UNSET)

        crs = d.pop("crs", UNSET)

        tile_matrix_set_uri = d.pop("tileMatrixSetURI", UNSET)

        ogc_tile_set_list_tilesets_item = cls(
            data_type=data_type,
            links=links,
            title=title,
            crs=crs,
            tile_matrix_set_uri=tile_matrix_set_uri,
        )

        ogc_tile_set_list_tilesets_item.additional_properties = d
        return ogc_tile_set_list_tilesets_item

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
