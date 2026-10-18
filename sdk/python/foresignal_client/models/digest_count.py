from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.digest_count_by_reason import DigestCountByReason
    from ..models.digest_count_entities import DigestCountEntities
    from ..models.digest_count_previous_type_0 import DigestCountPreviousType0


T = TypeVar("T", bound="DigestCount")


@_attrs_define
class DigestCount:
    """Version counts by persist reason for one source and kind in a digest period (029). `routine` (SAMPLE + KEYFRAME +
    HEARTBEAT) is never counted as change.

        Attributes:
            source_id (str):
            kind (str):
            new (int): FIRST + REAPPEAR versions
            changed (int): SIGNIFICANT + CHANGE versions
            removed (int): TOMBSTONE versions
            edges (int | Unset): TRIGGER_EDGE versions
            routine (int | Unset): SAMPLE + KEYFRAME + HEARTBEAT versions
            entities (DigestCountEntities | Unset): Distinct entities per group
            by_reason (DigestCountByReason | Unset):
            previous (DigestCountPreviousType0 | None | Unset): Same counts for the previous equal-length period; null when
                below the retention floor
    """

    source_id: str
    kind: str
    new: int
    changed: int
    removed: int
    edges: int | Unset = UNSET
    routine: int | Unset = UNSET
    entities: DigestCountEntities | Unset = UNSET
    by_reason: DigestCountByReason | Unset = UNSET
    previous: DigestCountPreviousType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.digest_count_previous_type_0 import DigestCountPreviousType0

        source_id = self.source_id

        kind = self.kind

        new = self.new

        changed = self.changed

        removed = self.removed

        edges = self.edges

        routine = self.routine

        entities: dict[str, Any] | Unset = UNSET
        if not isinstance(self.entities, Unset):
            entities = self.entities.to_dict()

        by_reason: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_reason, Unset):
            by_reason = self.by_reason.to_dict()

        previous: dict[str, Any] | None | Unset
        if isinstance(self.previous, Unset):
            previous = UNSET
        elif isinstance(self.previous, DigestCountPreviousType0):
            previous = self.previous.to_dict()
        else:
            previous = self.previous

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sourceId": source_id,
                "kind": kind,
                "new": new,
                "changed": changed,
                "removed": removed,
            }
        )
        if edges is not UNSET:
            field_dict["edges"] = edges
        if routine is not UNSET:
            field_dict["routine"] = routine
        if entities is not UNSET:
            field_dict["entities"] = entities
        if by_reason is not UNSET:
            field_dict["byReason"] = by_reason
        if previous is not UNSET:
            field_dict["previous"] = previous

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.digest_count_by_reason import DigestCountByReason
        from ..models.digest_count_entities import DigestCountEntities
        from ..models.digest_count_previous_type_0 import DigestCountPreviousType0

        d = dict(src_dict)
        source_id = d.pop("sourceId")

        kind = d.pop("kind")

        new = d.pop("new")

        changed = d.pop("changed")

        removed = d.pop("removed")

        edges = d.pop("edges", UNSET)

        routine = d.pop("routine", UNSET)

        _entities = d.pop("entities", UNSET)
        entities: DigestCountEntities | Unset
        if isinstance(_entities, Unset):
            entities = UNSET
        else:
            entities = DigestCountEntities.from_dict(_entities)

        _by_reason = d.pop("byReason", UNSET)
        by_reason: DigestCountByReason | Unset
        if isinstance(_by_reason, Unset):
            by_reason = UNSET
        else:
            by_reason = DigestCountByReason.from_dict(_by_reason)

        def _parse_previous(data: object) -> DigestCountPreviousType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                previous_type_0 = DigestCountPreviousType0.from_dict(data)

                return previous_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(DigestCountPreviousType0 | None | Unset, data)

        previous = _parse_previous(d.pop("previous", UNSET))

        digest_count = cls(
            source_id=source_id,
            kind=kind,
            new=new,
            changed=changed,
            removed=removed,
            edges=edges,
            routine=routine,
            entities=entities,
            by_reason=by_reason,
            previous=previous,
        )

        digest_count.additional_properties = d
        return digest_count

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
