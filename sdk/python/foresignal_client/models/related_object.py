from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.related_object_direction import RelatedObjectDirection
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.object_envelope import ObjectEnvelope
    from ..models.relation import Relation


T = TypeVar("T", bound="RelatedObject")


@_attrs_define
class RelatedObject:
    """A neighbour with the relation that links it

    Attributes:
        relation (Relation): Time-aware, provenance-aware edge between two objects (Client-Contract §4)
        direction (RelatedObjectDirection):
        object_ (ObjectEnvelope): Uniform summary of any Foresignal object (docs/Client-Contract.md §3)
        distance_km (float | None | Unset):
    """

    relation: Relation
    direction: RelatedObjectDirection
    object_: ObjectEnvelope
    distance_km: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        relation = self.relation.to_dict()

        direction = self.direction.value

        object_ = self.object_.to_dict()

        distance_km: float | None | Unset
        if isinstance(self.distance_km, Unset):
            distance_km = UNSET
        else:
            distance_km = self.distance_km

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "relation": relation,
                "direction": direction,
                "object": object_,
            }
        )
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.object_envelope import ObjectEnvelope
        from ..models.relation import Relation

        d = dict(src_dict)
        relation = Relation.from_dict(d.pop("relation"))

        direction = RelatedObjectDirection(d.pop("direction"))

        object_ = ObjectEnvelope.from_dict(d.pop("object"))

        def _parse_distance_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        distance_km = _parse_distance_km(d.pop("distanceKm", UNSET))

        related_object = cls(
            relation=relation,
            direction=direction,
            object_=object_,
            distance_km=distance_km,
        )

        related_object.additional_properties = d
        return related_object

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
