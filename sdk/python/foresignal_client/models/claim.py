from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.claim_extraction import ClaimExtraction
from ..models.claim_polarity import ClaimPolarity
from ..models.claim_source_reliability import ClaimSourceReliability
from ..models.claim_status import ClaimStatus
from ..models.tlp import Tlp
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.claim_event_time_type_0 import ClaimEventTimeType0
    from ..models.claim_place import ClaimPlace


T = TypeVar("T", bound="Claim")


@_attrs_define
class Claim:
    """A structured claim extracted from a narrative: what, where, when, who. A report, not a fact; usable as a graded 125
    evidence item (`source = social-claim`) (136)

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
                `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
                `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
                situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            type_ (str): Lexicon id (`intel/social/claim-lexicon.json`) Example: explosion.
            signal_family (str): 119 signal family the type maps to Example: conflict.
            polarity (ClaimPolarity):
            hedged (bool): Hedge markers such as "reportedly", "unconfirmed"
            support_count (int): Posts supporting the claim
            evidence_group_count (int):
            first_observed_at (datetime.datetime):
            extraction (ClaimExtraction):
            manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            confidence (float): Extraction confidence only
            status (ClaimStatus): Set by 137 comparisons
            source_reliability (ClaimSourceReliability): Admiralty: F by default, E/D from the account's or channel's 137
                track record, or an operator override
            information_credibility (int): From 137 status: 6 unassessed, 2 corroborated, 5 unobserved, 4 contradicted
            grade (str):  Example: F6.
            place (ClaimPlace | None | Unset):
            entity_refs (list[str] | Unset):
            event_time (ClaimEventTimeType0 | None | Unset):
            actor (None | str | Unset): Organisation name if stated
            comparison_id (None | str | Unset): 137 comparison
            tlp (Tlp | Unset): Traffic Light Protocol 2.0 label
            created_at (datetime.datetime | Unset):
    """

    id: str
    ref: str
    narrative_id: str
    type_: str
    signal_family: str
    polarity: ClaimPolarity
    hedged: bool
    support_count: int
    evidence_group_count: int
    first_observed_at: datetime.datetime
    extraction: ClaimExtraction
    manifest_id: str
    confidence: float
    status: ClaimStatus
    source_reliability: ClaimSourceReliability
    information_credibility: int
    grade: str
    place: ClaimPlace | None | Unset = UNSET
    entity_refs: list[str] | Unset = UNSET
    event_time: ClaimEventTimeType0 | None | Unset = UNSET
    actor: None | str | Unset = UNSET
    comparison_id: None | str | Unset = UNSET
    tlp: Tlp | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.claim_event_time_type_0 import ClaimEventTimeType0
        from ..models.claim_place import ClaimPlace

        id = self.id

        ref = self.ref

        narrative_id = self.narrative_id

        type_ = self.type_

        signal_family = self.signal_family

        polarity = self.polarity.value

        hedged = self.hedged

        support_count = self.support_count

        evidence_group_count = self.evidence_group_count

        first_observed_at = self.first_observed_at.isoformat()

        extraction = self.extraction.value

        manifest_id = self.manifest_id

        confidence = self.confidence

        status = self.status.value

        source_reliability = self.source_reliability.value

        information_credibility = self.information_credibility

        grade = self.grade

        place: dict[str, Any] | None | Unset
        if isinstance(self.place, Unset):
            place = UNSET
        elif isinstance(self.place, ClaimPlace):
            place = self.place.to_dict()
        else:
            place = self.place

        entity_refs: list[str] | Unset = UNSET
        if not isinstance(self.entity_refs, Unset):
            entity_refs = self.entity_refs

        event_time: dict[str, Any] | None | Unset
        if isinstance(self.event_time, Unset):
            event_time = UNSET
        elif isinstance(self.event_time, ClaimEventTimeType0):
            event_time = self.event_time.to_dict()
        else:
            event_time = self.event_time

        actor: None | str | Unset
        if isinstance(self.actor, Unset):
            actor = UNSET
        else:
            actor = self.actor

        comparison_id: None | str | Unset
        if isinstance(self.comparison_id, Unset):
            comparison_id = UNSET
        else:
            comparison_id = self.comparison_id

        tlp: str | Unset = UNSET
        if not isinstance(self.tlp, Unset):
            tlp = self.tlp.value

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ref": ref,
                "narrativeId": narrative_id,
                "type": type_,
                "signalFamily": signal_family,
                "polarity": polarity,
                "hedged": hedged,
                "supportCount": support_count,
                "evidenceGroupCount": evidence_group_count,
                "firstObservedAt": first_observed_at,
                "extraction": extraction,
                "manifestId": manifest_id,
                "confidence": confidence,
                "status": status,
                "sourceReliability": source_reliability,
                "informationCredibility": information_credibility,
                "grade": grade,
            }
        )
        if place is not UNSET:
            field_dict["place"] = place
        if entity_refs is not UNSET:
            field_dict["entityRefs"] = entity_refs
        if event_time is not UNSET:
            field_dict["eventTime"] = event_time
        if actor is not UNSET:
            field_dict["actor"] = actor
        if comparison_id is not UNSET:
            field_dict["comparisonId"] = comparison_id
        if tlp is not UNSET:
            field_dict["tlp"] = tlp
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.claim_event_time_type_0 import ClaimEventTimeType0
        from ..models.claim_place import ClaimPlace

        d = dict(src_dict)
        id = d.pop("id")

        ref = d.pop("ref")

        narrative_id = d.pop("narrativeId")

        type_ = d.pop("type")

        signal_family = d.pop("signalFamily")

        polarity = ClaimPolarity(d.pop("polarity"))

        hedged = d.pop("hedged")

        support_count = d.pop("supportCount")

        evidence_group_count = d.pop("evidenceGroupCount")

        first_observed_at = datetime.datetime.fromisoformat(d.pop("firstObservedAt"))

        extraction = ClaimExtraction(d.pop("extraction"))

        manifest_id = d.pop("manifestId")

        confidence = d.pop("confidence")

        status = ClaimStatus(d.pop("status"))

        source_reliability = ClaimSourceReliability(d.pop("sourceReliability"))

        information_credibility = d.pop("informationCredibility")

        grade = d.pop("grade")

        def _parse_place(data: object) -> ClaimPlace | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                place_type_0 = ClaimPlace.from_dict(data)

                return place_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ClaimPlace | None | Unset, data)

        place = _parse_place(d.pop("place", UNSET))

        entity_refs = cast(list[str], d.pop("entityRefs", UNSET))

        def _parse_event_time(data: object) -> ClaimEventTimeType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                event_time_type_0 = ClaimEventTimeType0.from_dict(data)

                return event_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ClaimEventTimeType0 | None | Unset, data)

        event_time = _parse_event_time(d.pop("eventTime", UNSET))

        def _parse_actor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        actor = _parse_actor(d.pop("actor", UNSET))

        def _parse_comparison_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        comparison_id = _parse_comparison_id(d.pop("comparisonId", UNSET))

        _tlp = d.pop("tlp", UNSET)
        tlp: Tlp | Unset
        if isinstance(_tlp, Unset):
            tlp = UNSET
        else:
            tlp = Tlp(_tlp)

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        claim = cls(
            id=id,
            ref=ref,
            narrative_id=narrative_id,
            type_=type_,
            signal_family=signal_family,
            polarity=polarity,
            hedged=hedged,
            support_count=support_count,
            evidence_group_count=evidence_group_count,
            first_observed_at=first_observed_at,
            extraction=extraction,
            manifest_id=manifest_id,
            confidence=confidence,
            status=status,
            source_reliability=source_reliability,
            information_credibility=information_credibility,
            grade=grade,
            place=place,
            entity_refs=entity_refs,
            event_time=event_time,
            actor=actor,
            comparison_id=comparison_id,
            tlp=tlp,
            created_at=created_at,
        )

        claim.additional_properties = d
        return claim

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
