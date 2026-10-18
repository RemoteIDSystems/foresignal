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
    from ..models.ogc_tile_set_tile_matrix_set_limits_item import (
        OgcTileSetTileMatrixSetLimitsItem,
    )


T = TypeVar("T", bound="OgcTileSet")


@_attrs_define
class OgcTileSet:
    """Tileset metadata (OGC API – Tiles 20-057) for one product and tile matrix set

    Attributes:
        data_type (Literal['map']):
        crs (str):
        links (list[OgcLink]):
        title (str | Unset):
        tile_matrix_set_uri (str | Unset):
        tile_matrix_set_limits (list[OgcTileSetTileMatrixSetLimitsItem] | Unset):
        attribution (None | str | Unset):
    """

    data_type: Literal["map"]
    crs: str
    links: list[OgcLink]
    title: str | Unset = UNSET
    tile_matrix_set_uri: str | Unset = UNSET
    tile_matrix_set_limits: list[OgcTileSetTileMatrixSetLimitsItem] | Unset = UNSET
    attribution: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        data_type = self.data_type

        crs = self.crs

        links = []
        for links_item_data in self.links:
            links_item = links_item_data.to_dict()
            links.append(links_item)

        title = self.title

        tile_matrix_set_uri = self.tile_matrix_set_uri

        tile_matrix_set_limits: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.tile_matrix_set_limits, Unset):
            tile_matrix_set_limits = []
            for tile_matrix_set_limits_item_data in self.tile_matrix_set_limits:
                tile_matrix_set_limits_item = tile_matrix_set_limits_item_data.to_dict()
                tile_matrix_set_limits.append(tile_matrix_set_limits_item)

        attribution: None | str | Unset
        if isinstance(self.attribution, Unset):
            attribution = UNSET
        else:
            attribution = self.attribution

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "dataType": data_type,
                "crs": crs,
                "links": links,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if tile_matrix_set_uri is not UNSET:
            field_dict["tileMatrixSetURI"] = tile_matrix_set_uri
        if tile_matrix_set_limits is not UNSET:
            field_dict["tileMatrixSetLimits"] = tile_matrix_set_limits
        if attribution is not UNSET:
            field_dict["attribution"] = attribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_link import OgcLink
        from ..models.ogc_tile_set_tile_matrix_set_limits_item import (
            OgcTileSetTileMatrixSetLimitsItem,
        )

        d = dict(src_dict)
        data_type = cast(Literal["map"], d.pop("dataType"))
        if data_type != "map":
            raise ValueError(f"dataType must match const 'map', got '{data_type}'")

        crs = d.pop("crs")

        links = []
        _links = d.pop("links")
        for links_item_data in _links:
            links_item = OgcLink.from_dict(links_item_data)

            links.append(links_item)

        title = d.pop("title", UNSET)

        tile_matrix_set_uri = d.pop("tileMatrixSetURI", UNSET)

        _tile_matrix_set_limits = d.pop("tileMatrixSetLimits", UNSET)
        tile_matrix_set_limits: list[OgcTileSetTileMatrixSetLimitsItem] | Unset = UNSET
        if _tile_matrix_set_limits is not UNSET:
            tile_matrix_set_limits = []
            for tile_matrix_set_limits_item_data in _tile_matrix_set_limits:
                tile_matrix_set_limits_item = (
                    OgcTileSetTileMatrixSetLimitsItem.from_dict(
                        tile_matrix_set_limits_item_data
                    )
                )

                tile_matrix_set_limits.append(tile_matrix_set_limits_item)

        def _parse_attribution(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        attribution = _parse_attribution(d.pop("attribution", UNSET))

        ogc_tile_set = cls(
            data_type=data_type,
            crs=crs,
            links=links,
            title=title,
            tile_matrix_set_uri=tile_matrix_set_uri,
            tile_matrix_set_limits=tile_matrix_set_limits,
            attribution=attribution,
        )

        ogc_tile_set.additional_properties = d
        return ogc_tile_set

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
