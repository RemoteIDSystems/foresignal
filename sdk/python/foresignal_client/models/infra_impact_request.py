from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define

from ..models.infra_impact_request_min_label import InfraImpactRequestMinLabel
from ..models.infra_impact_request_types_item import InfraImpactRequestTypesItem
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry
    from ..models.infra_impact_request_record import InfraImpactRequestRecord


T = TypeVar("T", bound="InfraImpactRequest")


@_attrs_define
class InfraImpactRequest:
    """Affected-dependents query: exactly one of geometry, eventId or record

    Attributes:
        geometry (Geometry | Unset): GeoJSON geometry (RFC 7946), WGS84
        event_id (str | Unset): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        record (InfraImpactRequestRecord | Unset): A record whose geometry (or 040 region polygon when it is a centroid)
            seeds the query
        depth (int | Unset):  Default: 2.
        min_label (InfraImpactRequestMinLabel | Unset):  Default: InfraImpactRequestMinLabel.POSSIBLE.
        types (list[InfraImpactRequestTypesItem] | Unset):
    """

    geometry: Geometry | Unset = UNSET
    event_id: str | Unset = UNSET
    record: InfraImpactRequestRecord | Unset = UNSET
    depth: int | Unset = 2
    min_label: InfraImpactRequestMinLabel | Unset = InfraImpactRequestMinLabel.POSSIBLE
    types: list[InfraImpactRequestTypesItem] | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        geometry: dict[str, Any] | Unset = UNSET
        if not isinstance(self.geometry, Unset):
            geometry = self.geometry.to_dict()

        event_id = self.event_id

        record: dict[str, Any] | Unset = UNSET
        if not isinstance(self.record, Unset):
            record = self.record.to_dict()

        depth = self.depth

        min_label: str | Unset = UNSET
        if not isinstance(self.min_label, Unset):
            min_label = self.min_label.value

        types: list[str] | Unset = UNSET
        if not isinstance(self.types, Unset):
            types = []
            for types_item_data in self.types:
                types_item = types_item_data.value
                types.append(types_item)

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if event_id is not UNSET:
            field_dict["eventId"] = event_id
        if record is not UNSET:
            field_dict["record"] = record
        if depth is not UNSET:
            field_dict["depth"] = depth
        if min_label is not UNSET:
            field_dict["minLabel"] = min_label
        if types is not UNSET:
            field_dict["types"] = types

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry
        from ..models.infra_impact_request_record import InfraImpactRequestRecord

        d = dict(src_dict)
        _geometry = d.pop("geometry", UNSET)
        geometry: Geometry | Unset
        if isinstance(_geometry, Unset):
            geometry = UNSET
        else:
            geometry = Geometry.from_dict(_geometry)

        event_id = d.pop("eventId", UNSET)

        _record = d.pop("record", UNSET)
        record: InfraImpactRequestRecord | Unset
        if isinstance(_record, Unset):
            record = UNSET
        else:
            record = InfraImpactRequestRecord.from_dict(_record)

        depth = d.pop("depth", UNSET)

        _min_label = d.pop("minLabel", UNSET)
        min_label: InfraImpactRequestMinLabel | Unset
        if isinstance(_min_label, Unset):
            min_label = UNSET
        else:
            min_label = InfraImpactRequestMinLabel(_min_label)

        _types = d.pop("types", UNSET)
        types: list[InfraImpactRequestTypesItem] | Unset = UNSET
        if _types is not UNSET:
            types = []
            for types_item_data in _types:
                types_item = InfraImpactRequestTypesItem(types_item_data)

                types.append(types_item)

        infra_impact_request = cls(
            geometry=geometry,
            event_id=event_id,
            record=record,
            depth=depth,
            min_label=min_label,
            types=types,
        )

        return infra_impact_request
