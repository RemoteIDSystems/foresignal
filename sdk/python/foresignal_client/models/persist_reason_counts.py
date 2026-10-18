from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="PersistReasonCounts")


@_attrs_define
class PersistReasonCounts:
    """Persisted-version counts per persist reason; all nine reasons are always present (zeros included)

    Attributes:
        first (int):
        reappear (int):
        significant (int):
        tombstone (int):
        change (int):
        sample (int):
        keyframe (int):
        heartbeat (int):
        trigger_edge (int):
    """

    first: int
    reappear: int
    significant: int
    tombstone: int
    change: int
    sample: int
    keyframe: int
    heartbeat: int
    trigger_edge: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        first = self.first

        reappear = self.reappear

        significant = self.significant

        tombstone = self.tombstone

        change = self.change

        sample = self.sample

        keyframe = self.keyframe

        heartbeat = self.heartbeat

        trigger_edge = self.trigger_edge

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "FIRST": first,
                "REAPPEAR": reappear,
                "SIGNIFICANT": significant,
                "TOMBSTONE": tombstone,
                "CHANGE": change,
                "SAMPLE": sample,
                "KEYFRAME": keyframe,
                "HEARTBEAT": heartbeat,
                "TRIGGER_EDGE": trigger_edge,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        first = d.pop("FIRST")

        reappear = d.pop("REAPPEAR")

        significant = d.pop("SIGNIFICANT")

        tombstone = d.pop("TOMBSTONE")

        change = d.pop("CHANGE")

        sample = d.pop("SAMPLE")

        keyframe = d.pop("KEYFRAME")

        heartbeat = d.pop("HEARTBEAT")

        trigger_edge = d.pop("TRIGGER_EDGE")

        persist_reason_counts = cls(
            first=first,
            reappear=reappear,
            significant=significant,
            tombstone=tombstone,
            change=change,
            sample=sample,
            keyframe=keyframe,
            heartbeat=heartbeat,
            trigger_edge=trigger_edge,
        )

        persist_reason_counts.additional_properties = d
        return persist_reason_counts

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
