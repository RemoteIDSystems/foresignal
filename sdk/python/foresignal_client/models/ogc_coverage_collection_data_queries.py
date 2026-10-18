from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ogc_coverage_collection_data_queries_area import (
        OgcCoverageCollectionDataQueriesArea,
    )
    from ..models.ogc_coverage_collection_data_queries_instances import (
        OgcCoverageCollectionDataQueriesInstances,
    )
    from ..models.ogc_coverage_collection_data_queries_position import (
        OgcCoverageCollectionDataQueriesPosition,
    )


T = TypeVar("T", bound="OgcCoverageCollectionDataQueries")


@_attrs_define
class OgcCoverageCollectionDataQueries:
    """EDR queries

    Attributes:
        position (OgcCoverageCollectionDataQueriesPosition | Unset): EDR position query
        area (OgcCoverageCollectionDataQueriesArea | Unset): EDR area query
        instances (OgcCoverageCollectionDataQueriesInstances | Unset): EDR instances query
    """

    position: OgcCoverageCollectionDataQueriesPosition | Unset = UNSET
    area: OgcCoverageCollectionDataQueriesArea | Unset = UNSET
    instances: OgcCoverageCollectionDataQueriesInstances | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        position: dict[str, Any] | Unset = UNSET
        if not isinstance(self.position, Unset):
            position = self.position.to_dict()

        area: dict[str, Any] | Unset = UNSET
        if not isinstance(self.area, Unset):
            area = self.area.to_dict()

        instances: dict[str, Any] | Unset = UNSET
        if not isinstance(self.instances, Unset):
            instances = self.instances.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if position is not UNSET:
            field_dict["position"] = position
        if area is not UNSET:
            field_dict["area"] = area
        if instances is not UNSET:
            field_dict["instances"] = instances

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ogc_coverage_collection_data_queries_area import (
            OgcCoverageCollectionDataQueriesArea,
        )
        from ..models.ogc_coverage_collection_data_queries_instances import (
            OgcCoverageCollectionDataQueriesInstances,
        )
        from ..models.ogc_coverage_collection_data_queries_position import (
            OgcCoverageCollectionDataQueriesPosition,
        )

        d = dict(src_dict)
        _position = d.pop("position", UNSET)
        position: OgcCoverageCollectionDataQueriesPosition | Unset
        if isinstance(_position, Unset):
            position = UNSET
        else:
            position = OgcCoverageCollectionDataQueriesPosition.from_dict(_position)

        _area = d.pop("area", UNSET)
        area: OgcCoverageCollectionDataQueriesArea | Unset
        if isinstance(_area, Unset):
            area = UNSET
        else:
            area = OgcCoverageCollectionDataQueriesArea.from_dict(_area)

        _instances = d.pop("instances", UNSET)
        instances: OgcCoverageCollectionDataQueriesInstances | Unset
        if isinstance(_instances, Unset):
            instances = UNSET
        else:
            instances = OgcCoverageCollectionDataQueriesInstances.from_dict(_instances)

        ogc_coverage_collection_data_queries = cls(
            position=position,
            area=area,
            instances=instances,
        )

        ogc_coverage_collection_data_queries.additional_properties = d
        return ogc_coverage_collection_data_queries

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
