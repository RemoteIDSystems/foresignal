from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.infra_node_facility_type import InfraNodeFacilityType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.infra_node_attributes import InfraNodeAttributes
    from ..models.infra_node_edges_type_0 import InfraNodeEdgesType0


T = TypeVar("T", bound="InfraNode")


@_attrs_define
class InfraNode:
    """A facility node: a view of a reference or OSM record, never edited by hand

    Attributes:
        id (int):
        source_id (str):
        external_id (str):
        facility_type (InfraNodeFacilityType):
        geometry (Geometry): GeoJSON geometry (RFC 7946), WGS84
        licence (str):
        name (None | str | Unset):
        point (Geometry | Unset): GeoJSON geometry (RFC 7946), WGS84
        attributes (InfraNodeAttributes | Unset): capacityMw, voltageKv, iata, beds, …
        region_tags (list[str] | Unset):
        built_at (datetime.datetime | Unset):
        edges (InfraNodeEdgesType0 | None | Unset): Present on getInfraNode
        attribution (list[str] | Unset): e.g. © OpenStreetMap contributors
    """

    id: int
    source_id: str
    external_id: str
    facility_type: InfraNodeFacilityType
    geometry: Geometry
    licence: str
    name: None | str | Unset = UNSET
    point: Geometry | Unset = UNSET
    attributes: InfraNodeAttributes | Unset = UNSET
    region_tags: list[str] | Unset = UNSET
    built_at: datetime.datetime | Unset = UNSET
    edges: InfraNodeEdgesType0 | None | Unset = UNSET
    attribution: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.infra_node_edges_type_0 import InfraNodeEdgesType0

        id = self.id

        source_id = self.source_id

        external_id = self.external_id

        facility_type = self.facility_type.value

        geometry = self.geometry.to_dict()

        licence = self.licence

        name: None | str | Unset
        if isinstance(self.name, Unset):
            name = UNSET
        else:
            name = self.name

        point: dict[str, Any] | Unset = UNSET
        if not isinstance(self.point, Unset):
            point = self.point.to_dict()

        attributes: dict[str, Any] | Unset = UNSET
        if not isinstance(self.attributes, Unset):
            attributes = self.attributes.to_dict()

        region_tags: list[str] | Unset = UNSET
        if not isinstance(self.region_tags, Unset):
            region_tags = self.region_tags

        built_at: str | Unset = UNSET
        if not isinstance(self.built_at, Unset):
            built_at = self.built_at.isoformat()

        edges: dict[str, Any] | None | Unset
        if isinstance(self.edges, Unset):
            edges = UNSET
        elif isinstance(self.edges, InfraNodeEdgesType0):
            edges = self.edges.to_dict()
        else:
            edges = self.edges

        attribution: list[str] | Unset = UNSET
        if not isinstance(self.attribution, Unset):
            attribution = self.attribution

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "sourceId": source_id,
                "externalId": external_id,
                "facilityType": facility_type,
                "geometry": geometry,
                "licence": licence,
            }
        )
        if name is not UNSET:
            field_dict["name"] = name
        if point is not UNSET:
            field_dict["point"] = point
        if attributes is not UNSET:
            field_dict["attributes"] = attributes
        if region_tags is not UNSET:
            field_dict["regionTags"] = region_tags
        if built_at is not UNSET:
            field_dict["builtAt"] = built_at
        if edges is not UNSET:
            field_dict["edges"] = edges
        if attribution is not UNSET:
            field_dict["attribution"] = attribution

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.infra_node_attributes import InfraNodeAttributes
        from ..models.infra_node_edges_type_0 import InfraNodeEdgesType0

        d = dict(src_dict)
        id = d.pop("id")

        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        facility_type = InfraNodeFacilityType(d.pop("facilityType"))

        geometry = Geometry.from_dict(d.pop("geometry"))

        licence = d.pop("licence")

        def _parse_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        name = _parse_name(d.pop("name", UNSET))

        _point = d.pop("point", UNSET)
        point: Geometry | Unset
        if isinstance(_point, Unset):
            point = UNSET
        else:
            point = Geometry.from_dict(_point)

        _attributes = d.pop("attributes", UNSET)
        attributes: InfraNodeAttributes | Unset
        if isinstance(_attributes, Unset):
            attributes = UNSET
        else:
            attributes = InfraNodeAttributes.from_dict(_attributes)

        region_tags = cast(list[str], d.pop("regionTags", UNSET))

        _built_at = d.pop("builtAt", UNSET)
        built_at: datetime.datetime | Unset
        if isinstance(_built_at, Unset):
            built_at = UNSET
        else:
            built_at = datetime.datetime.fromisoformat(_built_at)

        def _parse_edges(data: object) -> InfraNodeEdgesType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                edges_type_0 = InfraNodeEdgesType0.from_dict(data)

                return edges_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(InfraNodeEdgesType0 | None | Unset, data)

        edges = _parse_edges(d.pop("edges", UNSET))

        attribution = cast(list[str], d.pop("attribution", UNSET))

        infra_node = cls(
            id=id,
            source_id=source_id,
            external_id=external_id,
            facility_type=facility_type,
            geometry=geometry,
            licence=licence,
            name=name,
            point=point,
            attributes=attributes,
            region_tags=region_tags,
            built_at=built_at,
            edges=edges,
            attribution=attribution,
        )

        infra_node.additional_properties = d
        return infra_node

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
