from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OgcTileSetTileMatrixSetLimitsItem")


@_attrs_define
class OgcTileSetTileMatrixSetLimitsItem:
    """Limits per matrix

    Attributes:
        tile_matrix (str | Unset):
        min_tile_row (int | Unset):
        max_tile_row (int | Unset):
        min_tile_col (int | Unset):
        max_tile_col (int | Unset):
    """

    tile_matrix: str | Unset = UNSET
    min_tile_row: int | Unset = UNSET
    max_tile_row: int | Unset = UNSET
    min_tile_col: int | Unset = UNSET
    max_tile_col: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        tile_matrix = self.tile_matrix

        min_tile_row = self.min_tile_row

        max_tile_row = self.max_tile_row

        min_tile_col = self.min_tile_col

        max_tile_col = self.max_tile_col

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if tile_matrix is not UNSET:
            field_dict["tileMatrix"] = tile_matrix
        if min_tile_row is not UNSET:
            field_dict["minTileRow"] = min_tile_row
        if max_tile_row is not UNSET:
            field_dict["maxTileRow"] = max_tile_row
        if min_tile_col is not UNSET:
            field_dict["minTileCol"] = min_tile_col
        if max_tile_col is not UNSET:
            field_dict["maxTileCol"] = max_tile_col

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        tile_matrix = d.pop("tileMatrix", UNSET)

        min_tile_row = d.pop("minTileRow", UNSET)

        max_tile_row = d.pop("maxTileRow", UNSET)

        min_tile_col = d.pop("minTileCol", UNSET)

        max_tile_col = d.pop("maxTileCol", UNSET)

        ogc_tile_set_tile_matrix_set_limits_item = cls(
            tile_matrix=tile_matrix,
            min_tile_row=min_tile_row,
            max_tile_row=max_tile_row,
            min_tile_col=min_tile_col,
            max_tile_col=max_tile_col,
        )

        ogc_tile_set_tile_matrix_set_limits_item.additional_properties = d
        return ogc_tile_set_tile_matrix_set_limits_item

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
