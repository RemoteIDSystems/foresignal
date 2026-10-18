from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ogc_feature_type import OgcFeatureType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.ogc_feature_properties import OgcFeatureProperties
    from ..models.ogc_link import OgcLink


T = TypeVar("T", bound="OgcFeature")


@_attrs_define
class OgcFeature:
    """OGC API feature: current id {sourceId}:{externalId}, history id {sourceId}:{externalId}:{version}; properties are
    the merged payload plus title, sourceId, observedAt, eventTime, version (112)

        Attributes:
            type_ (OgcFeatureType):
            id (str):
            geometry (Geometry | None):
            properties (OgcFeatureProperties):
            links (list[OgcLink] | Unset):
    """

    type_: OgcFeatureType
    id: str
    geometry: Geometry | None
    properties: OgcFeatureProperties
    links: list[OgcLink] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        type_ = self.type_.value

        id = self.id

        geometry: dict[str, Any] | None
        if isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        properties = self.properties.to_dict()

        links: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.links, Unset):
            links = []
            for links_item_data in self.links:
                links_item = links_item_data.to_dict()
                links.append(links_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "id": id,
                "geometry": geometry,
                "properties": properties,
            }
        )
        if links is not UNSET:
            field_dict["links"] = links

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.ogc_feature_properties import OgcFeatureProperties
        from ..models.ogc_link import OgcLink

        d = dict(src_dict)
        type_ = OgcFeatureType(d.pop("type"))

        id = d.pop("id")

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

        properties = OgcFeatureProperties.from_dict(d.pop("properties"))

        _links = d.pop("links", UNSET)
        links: list[OgcLink] | Unset = UNSET
        if _links is not UNSET:
            links = []
            for links_item_data in _links:
                links_item = OgcLink.from_dict(links_item_data)

                links.append(links_item)

        ogc_feature = cls(
            type_=type_,
            id=id,
            geometry=geometry,
            properties=properties,
            links=links,
        )

        ogc_feature.additional_properties = d
        return ogc_feature

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
