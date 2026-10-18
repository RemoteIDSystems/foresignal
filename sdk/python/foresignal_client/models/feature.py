from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.feature_type import FeatureType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.feature_properties import FeatureProperties
    from ..models.geometry import Geometry


T = TypeVar("T", bound="Feature")


@_attrs_define
class Feature:
    """
    Attributes:
        type_ (FeatureType):
        geometry (Geometry | None):
        properties (FeatureProperties):
        id (str | Unset):
    """

    type_: FeatureType
    geometry: Geometry | None
    properties: FeatureProperties
    id: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        type_ = self.type_.value

        geometry: dict[str, Any] | None
        if isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

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
        from ..models.feature_properties import FeatureProperties
        from ..models.geometry import Geometry

        d = dict(src_dict)
        type_ = FeatureType(d.pop("type"))

        def _parse_geometry(data: object) -> Geometry | None:
            if data is None:
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None, data)

        geometry = _parse_geometry(d.pop("geometry"))

        properties = FeatureProperties.from_dict(d.pop("properties"))

        id = d.pop("id", UNSET)

        feature = cls(
            type_=type_,
            geometry=geometry,
            properties=properties,
            id=id,
        )

        feature.additional_properties = d
        return feature

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
