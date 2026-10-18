from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.overland_batch_locations_item_type import OverlandBatchLocationsItemType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.overland_batch_locations_item_properties import (
        OverlandBatchLocationsItemProperties,
    )


T = TypeVar("T", bound="OverlandBatchLocationsItem")


@_attrs_define
class OverlandBatchLocationsItem:
    """One Overland point (GeoJSON Feature)

    Attributes:
        geometry (Geometry): GeoJSON geometry (RFC 7946), WGS84
        properties (OverlandBatchLocationsItemProperties): Overland point properties
        type_ (OverlandBatchLocationsItemType | Unset):
    """

    geometry: Geometry
    properties: OverlandBatchLocationsItemProperties
    type_: OverlandBatchLocationsItemType | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        geometry = self.geometry.to_dict()

        properties = self.properties.to_dict()

        type_: str | Unset = UNSET
        if not isinstance(self.type_, Unset):
            type_ = self.type_.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "geometry": geometry,
                "properties": properties,
            }
        )
        if type_ is not UNSET:
            field_dict["type"] = type_

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.overland_batch_locations_item_properties import (
            OverlandBatchLocationsItemProperties,
        )

        d = dict(src_dict)
        geometry = Geometry.from_dict(d.pop("geometry"))

        properties = OverlandBatchLocationsItemProperties.from_dict(d.pop("properties"))

        _type_ = d.pop("type", UNSET)
        type_: OverlandBatchLocationsItemType | Unset
        if isinstance(_type_, Unset):
            type_ = UNSET
        else:
            type_ = OverlandBatchLocationsItemType(_type_)

        overland_batch_locations_item = cls(
            geometry=geometry,
            properties=properties,
            type_=type_,
        )

        overland_batch_locations_item.additional_properties = d
        return overland_batch_locations_item

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
