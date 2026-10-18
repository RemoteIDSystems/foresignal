from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ogc_tile_matrix_set_list_tile_matrix_sets_item_id import (
    OgcTileMatrixSetListTileMatrixSetsItemId,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="OgcTileMatrixSetListTileMatrixSetsItem")


@_attrs_define
class OgcTileMatrixSetListTileMatrixSetsItem:
    """Set reference

    Attributes:
        id (OgcTileMatrixSetListTileMatrixSetsItemId):
        links (list[OgcLink]):
        title (str | Unset):
        uri (str | Unset):
    """

    id: OgcTileMatrixSetListTileMatrixSetsItemId
    links: list[OgcLink]
    title: str | Unset = UNSET
    uri: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id.value

        links = []
        for links_item_data in self.links:
            links_item = links_item_data.to_dict()
            links.append(links_item)

        title = self.title

        uri = self.uri

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "links": links,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if uri is not UNSET:
            field_dict["uri"] = uri

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        id = OgcTileMatrixSetListTileMatrixSetsItemId(d.pop("id"))

        links = []
        _links = d.pop("links")
        for links_item_data in _links:
            links_item = OgcLink.from_dict(links_item_data)

            links.append(links_item)

        title = d.pop("title", UNSET)

        uri = d.pop("uri", UNSET)

        ogc_tile_matrix_set_list_tile_matrix_sets_item = cls(
            id=id,
            links=links,
            title=title,
            uri=uri,
        )

        ogc_tile_matrix_set_list_tile_matrix_sets_item.additional_properties = d
        return ogc_tile_matrix_set_list_tile_matrix_sets_item

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
