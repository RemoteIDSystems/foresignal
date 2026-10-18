from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.stats_grid_features_item_type import StatsGridFeaturesItemType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.stats_grid_features_item_properties import (
        StatsGridFeaturesItemProperties,
    )


T = TypeVar("T", bound="StatsGridFeaturesItem")


@_attrs_define
class StatsGridFeaturesItem:
    """One cell

    Attributes:
        type_ (StatsGridFeaturesItemType):
        geometry (Geometry): GeoJSON geometry (RFC 7946), WGS84
        properties (StatsGridFeaturesItemProperties): Cell properties
        id (str | Unset):
    """

    type_: StatsGridFeaturesItemType
    geometry: Geometry
    properties: StatsGridFeaturesItemProperties
    id: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        geometry = self.geometry.to_dict()

        properties = self.properties.to_dict()

        id = self.id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "geometry": geometry,
                "properties": properties,
            }
        )
        if id is not UNSET:
            field_dict["id"] = id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.stats_grid_features_item_properties import (
            StatsGridFeaturesItemProperties,
        )

        d = dict(src_dict)
        type_ = StatsGridFeaturesItemType(d.pop("type"))

        geometry = Geometry.from_dict(d.pop("geometry"))

        properties = StatsGridFeaturesItemProperties.from_dict(d.pop("properties"))

        id = d.pop("id", UNSET)

        stats_grid_features_item = cls(
            type_=type_,
            geometry=geometry,
            properties=properties,
            id=id,
        )

        stats_grid_features_item.additional_properties = d
        return stats_grid_features_item

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
