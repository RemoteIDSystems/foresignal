from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="EvidenceGroup")


@_attrs_define
class EvidenceGroup:
    """Observations of one canonical event that trace to one originator; only groups count as independent (116)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        canonical_event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        originator_root (str):
        member_count (int):
        first_seq (int):
    """

    id: str
    canonical_event_id: str
    originator_root: str
    member_count: int
    first_seq: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        canonical_event_id = self.canonical_event_id

        originator_root = self.originator_root

        member_count = self.member_count

        first_seq = self.first_seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "canonicalEventId": canonical_event_id,
                "originatorRoot": originator_root,
                "memberCount": member_count,
                "firstSeq": first_seq,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        canonical_event_id = d.pop("canonicalEventId")

        originator_root = d.pop("originatorRoot")

        member_count = d.pop("memberCount")

        first_seq = d.pop("firstSeq")

        evidence_group = cls(
            id=id,
            canonical_event_id=canonical_event_id,
            originator_root=originator_root,
            member_count=member_count,
            first_seq=first_seq,
        )

        evidence_group.additional_properties = d
        return evidence_group

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
