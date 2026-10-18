from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ogc_coverage_collection_extent_spatial import (
        OgcCoverageCollectionExtentSpatial,
    )
    from ..models.ogc_coverage_collection_extent_temporal import (
        OgcCoverageCollectionExtentTemporal,
    )


T = TypeVar("T", bound="OgcCoverageCollectionExtent")


@_attrs_define
class OgcCoverageCollectionExtent:
    """Spatial and temporal extent

    Attributes:
        spatial (OgcCoverageCollectionExtentSpatial | Unset): Bounds
        temporal (OgcCoverageCollectionExtentTemporal | Unset): Frame times
    """

    spatial: OgcCoverageCollectionExtentSpatial | Unset = UNSET
    temporal: OgcCoverageCollectionExtentTemporal | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        spatial: dict[str, Any] | Unset = UNSET
        if not isinstance(self.spatial, Unset):
            spatial = self.spatial.to_dict()

        temporal: dict[str, Any] | Unset = UNSET
        if not isinstance(self.temporal, Unset):
            temporal = self.temporal.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if spatial is not UNSET:
            field_dict["spatial"] = spatial
        if temporal is not UNSET:
            field_dict["temporal"] = temporal

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_coverage_collection_extent_spatial import (
            OgcCoverageCollectionExtentSpatial,
        )
        from ..models.ogc_coverage_collection_extent_temporal import (
            OgcCoverageCollectionExtentTemporal,
        )

        d = dict(src_dict)
        _spatial = d.pop("spatial", UNSET)
        spatial: OgcCoverageCollectionExtentSpatial | Unset
        if isinstance(_spatial, Unset):
            spatial = UNSET
        else:
            spatial = OgcCoverageCollectionExtentSpatial.from_dict(_spatial)

        _temporal = d.pop("temporal", UNSET)
        temporal: OgcCoverageCollectionExtentTemporal | Unset
        if isinstance(_temporal, Unset):
            temporal = UNSET
        else:
            temporal = OgcCoverageCollectionExtentTemporal.from_dict(_temporal)

        ogc_coverage_collection_extent = cls(
            spatial=spatial,
            temporal=temporal,
        )

        ogc_coverage_collection_extent.additional_properties = d
        return ogc_coverage_collection_extent

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
