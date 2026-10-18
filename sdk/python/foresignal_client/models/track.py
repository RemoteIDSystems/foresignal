from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.track_type import TrackType

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.track_properties import TrackProperties


T = TypeVar("T", bound="Track")


@_attrs_define
class Track:
    """Simplified trail of one entity as a GeoJSON Feature with MultiLineString geometry (037)

    Attributes:
        type_ (TrackType):
        geometry (Geometry | None): MultiLineString; segments split at gaps, tombstones, versions outside the caller's
            restriction and the antimeridian; a lone point is repeated; null when the window holds no position
        properties (TrackProperties): Track properties
    """

    type_: TrackType
    geometry: Geometry | None
    properties: TrackProperties
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

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "geometry": geometry,
                "properties": properties,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.track_properties import TrackProperties

        d = dict(src_dict)
        type_ = TrackType(d.pop("type"))

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

        properties = TrackProperties.from_dict(d.pop("properties"))

        track = cls(
            type_=type_,
            geometry=geometry,
            properties=properties,
        )

        track.additional_properties = d
        return track

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
