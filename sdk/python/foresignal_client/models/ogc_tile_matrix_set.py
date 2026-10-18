from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ogc_tile_matrix_set_id import OgcTileMatrixSetId
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ogc_tile_matrix_set_tile_matrices_item import (
        OgcTileMatrixSetTileMatricesItem,
    )


T = TypeVar("T", bound="OgcTileMatrixSet")


@_attrs_define
class OgcTileMatrixSet:
    """Tile matrix set definition (OGC 17-083r4 JSON encoding)

    Attributes:
        id (OgcTileMatrixSetId):
        crs (str):
        tile_matrices (list[OgcTileMatrixSetTileMatricesItem]):
        title (str | Unset):
        uri (str | Unset):
        ordered_axes (list[str] | Unset):
        well_known_scale_set (None | str | Unset):
    """

    id: OgcTileMatrixSetId
    crs: str
    tile_matrices: list[OgcTileMatrixSetTileMatricesItem]
    title: str | Unset = UNSET
    uri: str | Unset = UNSET
    ordered_axes: list[str] | Unset = UNSET
    well_known_scale_set: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id.value

        crs = self.crs

        tile_matrices = []
        for tile_matrices_item_data in self.tile_matrices:
            tile_matrices_item = tile_matrices_item_data.to_dict()
            tile_matrices.append(tile_matrices_item)

        title = self.title

        uri = self.uri

        ordered_axes: list[str] | Unset = UNSET
        if not isinstance(self.ordered_axes, Unset):
            ordered_axes = self.ordered_axes

        well_known_scale_set: None | str | Unset
        if isinstance(self.well_known_scale_set, Unset):
            well_known_scale_set = UNSET
        else:
            well_known_scale_set = self.well_known_scale_set

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "crs": crs,
                "tileMatrices": tile_matrices,
            }
        )
        if title is not UNSET:
            field_dict["title"] = title
        if uri is not UNSET:
            field_dict["uri"] = uri
        if ordered_axes is not UNSET:
            field_dict["orderedAxes"] = ordered_axes
        if well_known_scale_set is not UNSET:
            field_dict["wellKnownScaleSet"] = well_known_scale_set

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_tile_matrix_set_tile_matrices_item import (
            OgcTileMatrixSetTileMatricesItem,
        )

        d = dict(src_dict)
        id = OgcTileMatrixSetId(d.pop("id"))

        crs = d.pop("crs")

        tile_matrices = []
        _tile_matrices = d.pop("tileMatrices")
        for tile_matrices_item_data in _tile_matrices:
            tile_matrices_item = OgcTileMatrixSetTileMatricesItem.from_dict(
                tile_matrices_item_data
            )

            tile_matrices.append(tile_matrices_item)

        title = d.pop("title", UNSET)

        uri = d.pop("uri", UNSET)

        ordered_axes = cast(list[str], d.pop("orderedAxes", UNSET))

        def _parse_well_known_scale_set(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        well_known_scale_set = _parse_well_known_scale_set(
            d.pop("wellKnownScaleSet", UNSET)
        )

        ogc_tile_matrix_set = cls(
            id=id,
            crs=crs,
            tile_matrices=tile_matrices,
            title=title,
            uri=uri,
            ordered_axes=ordered_axes,
            well_known_scale_set=well_known_scale_set,
        )

        ogc_tile_matrix_set.additional_properties = d
        return ogc_tile_matrix_set

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
