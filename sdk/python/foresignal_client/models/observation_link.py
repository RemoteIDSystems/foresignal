from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.observation_link_link_reason import ObservationLinkLinkReason
    from ..models.originator import Originator


T = TypeVar("T", bound="ObservationLink")


@_attrs_define
class ObservationLink:
    """Link from an observation (record version) to a canonical event (116)

    Attributes:
        canonical_event_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        source_id (str):
        external_id (str):
        seq (int):
        originator_id (str):
        evidence_group_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        link_confidence (float):
        link_reason (ObservationLinkLinkReason): Why the observation was linked
        linked_at (datetime.datetime):
        record_ref (str | Unset): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        version (int | Unset):
        originator (None | Originator | Unset): Included on event detail
        upstream_event_id (None | str | Unset):
    """

    canonical_event_id: str
    source_id: str
    external_id: str
    seq: int
    originator_id: str
    evidence_group_id: str
    link_confidence: float
    link_reason: ObservationLinkLinkReason
    linked_at: datetime.datetime
    record_ref: str | Unset = UNSET
    version: int | Unset = UNSET
    originator: None | Originator | Unset = UNSET
    upstream_event_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.originator import Originator

        canonical_event_id = self.canonical_event_id

        source_id = self.source_id

        external_id = self.external_id

        seq = self.seq

        originator_id = self.originator_id

        evidence_group_id = self.evidence_group_id

        link_confidence = self.link_confidence

        link_reason = self.link_reason.to_dict()

        linked_at = self.linked_at.isoformat()

        record_ref = self.record_ref

        version = self.version

        originator: dict[str, Any] | None | Unset
        if isinstance(self.originator, Unset):
            originator = UNSET
        elif isinstance(self.originator, Originator):
            originator = self.originator.to_dict()
        else:
            originator = self.originator

        upstream_event_id: None | str | Unset
        if isinstance(self.upstream_event_id, Unset):
            upstream_event_id = UNSET
        else:
            upstream_event_id = self.upstream_event_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "canonicalEventId": canonical_event_id,
                "sourceId": source_id,
                "externalId": external_id,
                "seq": seq,
                "originatorId": originator_id,
                "evidenceGroupId": evidence_group_id,
                "linkConfidence": link_confidence,
                "linkReason": link_reason,
                "linkedAt": linked_at,
            }
        )
        if record_ref is not UNSET:
            field_dict["recordRef"] = record_ref
        if version is not UNSET:
            field_dict["version"] = version
        if originator is not UNSET:
            field_dict["originator"] = originator
        if upstream_event_id is not UNSET:
            field_dict["upstreamEventId"] = upstream_event_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.observation_link_link_reason import ObservationLinkLinkReason
        from ..models.originator import Originator

        d = dict(src_dict)
        canonical_event_id = d.pop("canonicalEventId")

        source_id = d.pop("sourceId")

        external_id = d.pop("externalId")

        seq = d.pop("seq")

        originator_id = d.pop("originatorId")

        evidence_group_id = d.pop("evidenceGroupId")

        link_confidence = d.pop("linkConfidence")

        link_reason = ObservationLinkLinkReason.from_dict(d.pop("linkReason"))

        linked_at = datetime.datetime.fromisoformat(d.pop("linkedAt"))

        record_ref = d.pop("recordRef", UNSET)

        version = d.pop("version", UNSET)

        def _parse_originator(data: object) -> None | Originator | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                originator_type_0 = Originator.from_dict(data)

                return originator_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Originator | Unset, data)

        originator = _parse_originator(d.pop("originator", UNSET))

        def _parse_upstream_event_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        upstream_event_id = _parse_upstream_event_id(d.pop("upstreamEventId", UNSET))

        observation_link = cls(
            canonical_event_id=canonical_event_id,
            source_id=source_id,
            external_id=external_id,
            seq=seq,
            originator_id=originator_id,
            evidence_group_id=evidence_group_id,
            link_confidence=link_confidence,
            link_reason=link_reason,
            linked_at=linked_at,
            record_ref=record_ref,
            version=version,
            originator=originator,
            upstream_event_id=upstream_event_id,
        )

        observation_link.additional_properties = d
        return observation_link

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
