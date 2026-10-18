from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OgcTileMatrixSetTileMatricesItem")


@_attrs_define
class OgcTileMatrixSetTileMatricesItem:
    """One tile matrix

    Attributes:
        id (str):
        scale_denominator (float):
        point_of_origin (list[float]):
        tile_width (int):
        tile_height (int):
        matrix_width (int):
        matrix_height (int):
        cell_size (float | Unset):
        corner_of_origin (str | Unset):
    """

    id: str
    scale_denominator: float
    point_of_origin: list[float]
    tile_width: int
    tile_height: int
    matrix_width: int
    matrix_height: int
    cell_size: float | Unset = UNSET
    corner_of_origin: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        scale_denominator = self.scale_denominator

        point_of_origin = self.point_of_origin

        tile_width = self.tile_width

        tile_height = self.tile_height

        matrix_width = self.matrix_width

        matrix_height = self.matrix_height

        cell_size = self.cell_size

        corner_of_origin = self.corner_of_origin

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "scaleDenominator": scale_denominator,
                "pointOfOrigin": point_of_origin,
                "tileWidth": tile_width,
                "tileHeight": tile_height,
                "matrixWidth": matrix_width,
                "matrixHeight": matrix_height,
            }
        )
        if cell_size is not UNSET:
            field_dict["cellSize"] = cell_size
        if corner_of_origin is not UNSET:
            field_dict["cornerOfOrigin"] = corner_of_origin

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        scale_denominator = d.pop("scaleDenominator")

        point_of_origin = cast(list[float], d.pop("pointOfOrigin"))

        tile_width = d.pop("tileWidth")

        tile_height = d.pop("tileHeight")

        matrix_width = d.pop("matrixWidth")

        matrix_height = d.pop("matrixHeight")

        cell_size = d.pop("cellSize", UNSET)

        corner_of_origin = d.pop("cornerOfOrigin", UNSET)

        ogc_tile_matrix_set_tile_matrices_item = cls(
            id=id,
            scale_denominator=scale_denominator,
            point_of_origin=point_of_origin,
            tile_width=tile_width,
            tile_height=tile_height,
            matrix_width=matrix_width,
            matrix_height=matrix_height,
            cell_size=cell_size,
            corner_of_origin=corner_of_origin,
        )

        ogc_tile_matrix_set_tile_matrices_item.additional_properties = d
        return ogc_tile_matrix_set_tile_matrices_item

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
