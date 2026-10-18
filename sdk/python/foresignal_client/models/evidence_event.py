from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.evidence_event_flags_added_item import EvidenceEventFlagsAddedItem
from ..models.evidence_event_quality_flags_item import EvidenceEventQualityFlagsItem
from ..models.evidence_event_status import EvidenceEventStatus
from ..types import UNSET, Unset

T = TypeVar("T", bound="EvidenceEvent")


@_attrs_define
class EvidenceEvent:
    """SSE payload on topic `evidence`: canonical.created, canonical.merged, canonical.flagged (summary only) (116)

    Attributes:
        canonical_event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        family (str):
        status (EvidenceEventStatus):
        event_time (datetime.datetime):
        observation_count (int):
        evidence_group_count (int):
        quality_flags (list[EvidenceEventQualityFlagsItem]):
        region_codes (list[str] | Unset):
        flags_added (list[EvidenceEventFlagsAddedItem] | Unset): canonical.flagged
        merged_into (None | str | Unset):
        merged_from (list[str] | Unset): canonical.merged
    """

    canonical_event_id: str
    ref: str
    family: str
    status: EvidenceEventStatus
    event_time: datetime.datetime
    observation_count: int
    evidence_group_count: int
    quality_flags: list[EvidenceEventQualityFlagsItem]
    region_codes: list[str] | Unset = UNSET
    flags_added: list[EvidenceEventFlagsAddedItem] | Unset = UNSET
    merged_into: None | str | Unset = UNSET
    merged_from: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        canonical_event_id = self.canonical_event_id

        ref = self.ref

        family = self.family

        status = self.status.value

        event_time = self.event_time.isoformat()

        observation_count = self.observation_count

        evidence_group_count = self.evidence_group_count

        quality_flags = []
        for quality_flags_item_data in self.quality_flags:
            quality_flags_item = quality_flags_item_data.value
            quality_flags.append(quality_flags_item)

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        flags_added: list[str] | Unset = UNSET
        if not isinstance(self.flags_added, Unset):
            flags_added = []
            for flags_added_item_data in self.flags_added:
                flags_added_item = flags_added_item_data.value
                flags_added.append(flags_added_item)

        merged_into: None | str | Unset
        if isinstance(self.merged_into, Unset):
            merged_into = UNSET
        else:
            merged_into = self.merged_into

        merged_from: list[str] | Unset = UNSET
        if not isinstance(self.merged_from, Unset):
            merged_from = self.merged_from

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "canonicalEventId": canonical_event_id,
                "ref": ref,
                "family": family,
                "status": status,
                "eventTime": event_time,
                "observationCount": observation_count,
                "evidenceGroupCount": evidence_group_count,
                "qualityFlags": quality_flags,
            }
        )
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if flags_added is not UNSET:
            field_dict["flagsAdded"] = flags_added
        if merged_into is not UNSET:
            field_dict["mergedInto"] = merged_into
        if merged_from is not UNSET:
            field_dict["mergedFrom"] = merged_from

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        canonical_event_id = d.pop("canonicalEventId")

        ref = d.pop("ref")

        family = d.pop("family")

        status = EvidenceEventStatus(d.pop("status"))

        event_time = datetime.datetime.fromisoformat(d.pop("eventTime"))

        observation_count = d.pop("observationCount")

        evidence_group_count = d.pop("evidenceGroupCount")

        quality_flags = []
        _quality_flags = d.pop("qualityFlags")
        for quality_flags_item_data in _quality_flags:
            quality_flags_item = EvidenceEventQualityFlagsItem(quality_flags_item_data)

            quality_flags.append(quality_flags_item)

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        _flags_added = d.pop("flagsAdded", UNSET)
        flags_added: list[EvidenceEventFlagsAddedItem] | Unset = UNSET
        if _flags_added is not UNSET:
            flags_added = []
            for flags_added_item_data in _flags_added:
                flags_added_item = EvidenceEventFlagsAddedItem(flags_added_item_data)

                flags_added.append(flags_added_item)

        def _parse_merged_into(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        merged_into = _parse_merged_into(d.pop("mergedInto", UNSET))

        merged_from = cast(list[str], d.pop("mergedFrom", UNSET))

        evidence_event = cls(
            canonical_event_id=canonical_event_id,
            ref=ref,
            family=family,
            status=status,
            event_time=event_time,
            observation_count=observation_count,
            evidence_group_count=evidence_group_count,
            quality_flags=quality_flags,
            region_codes=region_codes,
            flags_added=flags_added,
            merged_into=merged_into,
            merged_from=merged_from,
        )

        evidence_event.additional_properties = d
        return evidence_event

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
