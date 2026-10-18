from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="OgcCoverageCollectionExtentSpatial")


@_attrs_define
class OgcCoverageCollectionExtentSpatial:
    """Bounds

    Attributes:
        bbox (list[list[float]] | Unset):
        crs (str | Unset):
    """

    bbox: list[list[float]] | Unset = UNSET
    crs: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        bbox: list[list[float]] | Unset = UNSET
        if not isinstance(self.bbox, Unset):
            bbox = []
            for bbox_item_data in self.bbox:
                bbox_item = bbox_item_data

                bbox.append(bbox_item)

        crs = self.crs

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if bbox is not UNSET:
            field_dict["bbox"] = bbox
        if crs is not UNSET:
            field_dict["crs"] = crs

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _bbox = d.pop("bbox", UNSET)
        bbox: list[list[float]] | Unset = UNSET
        if _bbox is not UNSET:
            bbox = []
            for bbox_item_data in _bbox:
                bbox_item = cast(list[float], bbox_item_data)

                bbox.append(bbox_item)

        crs = d.pop("crs", UNSET)

        ogc_coverage_collection_extent_spatial = cls(
            bbox=bbox,
            crs=crs,
        )

        ogc_coverage_collection_extent_spatial.additional_properties = d
        return ogc_coverage_collection_extent_spatial

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
