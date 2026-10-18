from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chronicle_link_dimensions_item import ChronicleLinkDimensionsItem
from ..models.chronicle_link_status import ChronicleLinkStatus
from ..models.chronicle_link_strength import ChronicleLinkStrength
from ..models.chronicle_link_temporal import ChronicleLinkTemporal
from ..models.chronicle_link_time_overlap import ChronicleLinkTimeOverlap
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_link_scores import ChronicleLinkScores
    from ..models.object_envelope import ObjectEnvelope


T = TypeVar("T", bound="ChronicleLink")


@_attrs_define
class ChronicleLink:
    """A relationship between two chronicles at full disclosure (the caller can see both)

    Attributes:
        link_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        disclosure (Literal['full']):
        a (str): Local chronicle id
        b (str): Local chronicle id or `remote:{instanceId}/chronicle:{sketchId}`
        strength (ChronicleLinkStrength): From estimated Jaccard: low < 0.1 ≤ medium < 0.3 ≤ high
        dimensions (list[ChronicleLinkDimensionsItem]):
        temporal (ChronicleLinkTemporal):
        status (ChronicleLinkStatus):
        first_found (datetime.datetime):
        other (None | ObjectEnvelope | Unset):
        scores (ChronicleLinkScores | Unset): Per key type overlap
        time_overlap (ChronicleLinkTimeOverlap | Unset):
        shared_situations (list[str] | Unset):
        shared_canonical_events (list[str] | Unset): Computed exactly from both key sets (capped)
        shared_entities (list[str] | Unset):
        shared_key_count (int | Unset):
        last_updated (datetime.datetime | Unset):
    """

    link_id: str
    disclosure: Literal["full"]
    a: str
    b: str
    strength: ChronicleLinkStrength
    dimensions: list[ChronicleLinkDimensionsItem]
    temporal: ChronicleLinkTemporal
    status: ChronicleLinkStatus
    first_found: datetime.datetime
    other: None | ObjectEnvelope | Unset = UNSET
    scores: ChronicleLinkScores | Unset = UNSET
    time_overlap: ChronicleLinkTimeOverlap | Unset = UNSET
    shared_situations: list[str] | Unset = UNSET
    shared_canonical_events: list[str] | Unset = UNSET
    shared_entities: list[str] | Unset = UNSET
    shared_key_count: int | Unset = UNSET
    last_updated: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.object_envelope import ObjectEnvelope

        link_id = self.link_id

        disclosure = self.disclosure

        a = self.a

        b = self.b

        strength = self.strength.value

        dimensions = []
        for dimensions_item_data in self.dimensions:
            dimensions_item = dimensions_item_data.value
            dimensions.append(dimensions_item)

        temporal = self.temporal.value

        status = self.status.value

        first_found = self.first_found.isoformat()

        other: dict[str, Any] | None | Unset
        if isinstance(self.other, Unset):
            other = UNSET
        elif isinstance(self.other, ObjectEnvelope):
            other = self.other.to_dict()
        else:
            other = self.other

        scores: dict[str, Any] | Unset = UNSET
        if not isinstance(self.scores, Unset):
            scores = self.scores.to_dict()

        time_overlap: str | Unset = UNSET
        if not isinstance(self.time_overlap, Unset):
            time_overlap = self.time_overlap.value

        shared_situations: list[str] | Unset = UNSET
        if not isinstance(self.shared_situations, Unset):
            shared_situations = self.shared_situations

        shared_canonical_events: list[str] | Unset = UNSET
        if not isinstance(self.shared_canonical_events, Unset):
            shared_canonical_events = self.shared_canonical_events

        shared_entities: list[str] | Unset = UNSET
        if not isinstance(self.shared_entities, Unset):
            shared_entities = self.shared_entities

        shared_key_count = self.shared_key_count

        last_updated: str | Unset = UNSET
        if not isinstance(self.last_updated, Unset):
            last_updated = self.last_updated.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "linkId": link_id,
                "disclosure": disclosure,
                "a": a,
                "b": b,
                "strength": strength,
                "dimensions": dimensions,
                "temporal": temporal,
                "status": status,
                "firstFound": first_found,
            }
        )
        if other is not UNSET:
            field_dict["other"] = other
        if scores is not UNSET:
            field_dict["scores"] = scores
        if time_overlap is not UNSET:
            field_dict["timeOverlap"] = time_overlap
        if shared_situations is not UNSET:
            field_dict["sharedSituations"] = shared_situations
        if shared_canonical_events is not UNSET:
            field_dict["sharedCanonicalEvents"] = shared_canonical_events
        if shared_entities is not UNSET:
            field_dict["sharedEntities"] = shared_entities
        if shared_key_count is not UNSET:
            field_dict["sharedKeyCount"] = shared_key_count
        if last_updated is not UNSET:
            field_dict["lastUpdated"] = last_updated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_link_scores import ChronicleLinkScores
        from ..models.object_envelope import ObjectEnvelope

        d = dict(src_dict)
        link_id = d.pop("linkId")

        disclosure = cast(Literal["full"], d.pop("disclosure"))
        if disclosure != "full":
            raise ValueError(f"disclosure must match const 'full', got '{disclosure}'")

        a = d.pop("a")

        b = d.pop("b")

        strength = ChronicleLinkStrength(d.pop("strength"))

        dimensions = []
        _dimensions = d.pop("dimensions")
        for dimensions_item_data in _dimensions:
            dimensions_item = ChronicleLinkDimensionsItem(dimensions_item_data)

            dimensions.append(dimensions_item)

        temporal = ChronicleLinkTemporal(d.pop("temporal"))

        status = ChronicleLinkStatus(d.pop("status"))

        first_found = datetime.datetime.fromisoformat(d.pop("firstFound"))

        def _parse_other(data: object) -> None | ObjectEnvelope | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                other_type_0 = ObjectEnvelope.from_dict(data)

                return other_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | ObjectEnvelope | Unset, data)

        other = _parse_other(d.pop("other", UNSET))

        _scores = d.pop("scores", UNSET)
        scores: ChronicleLinkScores | Unset
        if isinstance(_scores, Unset):
            scores = UNSET
        else:
            scores = ChronicleLinkScores.from_dict(_scores)

        _time_overlap = d.pop("timeOverlap", UNSET)
        time_overlap: ChronicleLinkTimeOverlap | Unset
        if isinstance(_time_overlap, Unset):
            time_overlap = UNSET
        else:
            time_overlap = ChronicleLinkTimeOverlap(_time_overlap)

        shared_situations = cast(list[str], d.pop("sharedSituations", UNSET))

        shared_canonical_events = cast(list[str], d.pop("sharedCanonicalEvents", UNSET))

        shared_entities = cast(list[str], d.pop("sharedEntities", UNSET))

        shared_key_count = d.pop("sharedKeyCount", UNSET)

        _last_updated = d.pop("lastUpdated", UNSET)
        last_updated: datetime.datetime | Unset
        if isinstance(_last_updated, Unset):
            last_updated = UNSET
        else:
            last_updated = datetime.datetime.fromisoformat(_last_updated)

        chronicle_link = cls(
            link_id=link_id,
            disclosure=disclosure,
            a=a,
            b=b,
            strength=strength,
            dimensions=dimensions,
            temporal=temporal,
            status=status,
            first_found=first_found,
            other=other,
            scores=scores,
            time_overlap=time_overlap,
            shared_situations=shared_situations,
            shared_canonical_events=shared_canonical_events,
            shared_entities=shared_entities,
            shared_key_count=shared_key_count,
            last_updated=last_updated,
        )

        chronicle_link.additional_properties = d
        return chronicle_link

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
