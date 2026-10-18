from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_comparison_found_by import SocialComparisonFoundBy
from ..models.social_comparison_status import SocialComparisonStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.claim_place import ClaimPlace
    from ..models.detector_ref import DetectorRef
    from ..models.social_comparison_contradiction_type_0 import (
        SocialComparisonContradictionType0,
    )
    from ..models.social_comparison_detectability import SocialComparisonDetectability
    from ..models.social_comparison_revision import SocialComparisonRevision
    from ..models.social_lead_times import SocialLeadTimes
    from ..models.social_physical_evidence import SocialPhysicalEvidence
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="SocialComparison")


@_attrs_define
class SocialComparison:
    """A claim tested against independent physical evidence in the same place and window, respecting detectability
    (`SocialPhysicalComparator` 1.0.0, 137)

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
                `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
                `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
                situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            claim_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            narrative_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            signal_family (str):
            place (ClaimPlace): Where a claim says something happened (reported, never observed)
            window (TimeRange):
            status (SocialComparisonStatus): `unobserved` only when `canSeeAbsence` is ok over the full window; otherwise
                `insufficient-coverage`; `pending` while the window (plus publication lag) is open
            physical_evidence (list[SocialPhysicalEvidence]):
            detectability (SocialComparisonDetectability): canSeeAbsence for the claim's family, place and window
            lead_times (SocialLeadTimes): First observations and signed lead of social first observation (positive = social
                first)
            found_by (SocialComparisonFoundBy): `cue` when the narrative was found by looking; lead-time statistics keep the
                two apart
            revision (int):
            comparator (DetectorRef):
            manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            evaluated_at (datetime.datetime):
            contradiction (None | SocialComparisonContradictionType0 | Unset):
            cue_id (None | str | Unset):
            question_id (None | str | Unset): 122 ledger question opened while pending
            final_at (datetime.datetime | None | Unset): Window end plus the family's publication lag; no revisions after
            revisions (list[SocialComparisonRevision] | Unset): Detail view
            caveats (list[Caveat] | Unset):
    """

    id: str
    ref: str
    claim_id: str
    narrative_id: str
    signal_family: str
    place: ClaimPlace
    window: TimeRange
    status: SocialComparisonStatus
    physical_evidence: list[SocialPhysicalEvidence]
    detectability: SocialComparisonDetectability
    lead_times: SocialLeadTimes
    found_by: SocialComparisonFoundBy
    revision: int
    comparator: DetectorRef
    manifest_id: str
    evaluated_at: datetime.datetime
    contradiction: None | SocialComparisonContradictionType0 | Unset = UNSET
    cue_id: None | str | Unset = UNSET
    question_id: None | str | Unset = UNSET
    final_at: datetime.datetime | None | Unset = UNSET
    revisions: list[SocialComparisonRevision] | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.social_comparison_contradiction_type_0 import (
            SocialComparisonContradictionType0,
        )

        id = self.id

        ref = self.ref

        claim_id = self.claim_id

        narrative_id = self.narrative_id

        signal_family = self.signal_family

        place = self.place.to_dict()

        window = self.window.to_dict()

        status = self.status.value

        physical_evidence = []
        for physical_evidence_item_data in self.physical_evidence:
            physical_evidence_item = physical_evidence_item_data.to_dict()
            physical_evidence.append(physical_evidence_item)

        detectability = self.detectability.to_dict()

        lead_times = self.lead_times.to_dict()

        found_by = self.found_by.value

        revision = self.revision

        comparator = self.comparator.to_dict()

        manifest_id = self.manifest_id

        evaluated_at = self.evaluated_at.isoformat()

        contradiction: dict[str, Any] | None | Unset
        if isinstance(self.contradiction, Unset):
            contradiction = UNSET
        elif isinstance(self.contradiction, SocialComparisonContradictionType0):
            contradiction = self.contradiction.to_dict()
        else:
            contradiction = self.contradiction

        cue_id: None | str | Unset
        if isinstance(self.cue_id, Unset):
            cue_id = UNSET
        else:
            cue_id = self.cue_id

        question_id: None | str | Unset
        if isinstance(self.question_id, Unset):
            question_id = UNSET
        else:
            question_id = self.question_id

        final_at: None | str | Unset
        if isinstance(self.final_at, Unset):
            final_at = UNSET
        elif isinstance(self.final_at, datetime.datetime):
            final_at = self.final_at.isoformat()
        else:
            final_at = self.final_at

        revisions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.revisions, Unset):
            revisions = []
            for revisions_item_data in self.revisions:
                revisions_item = revisions_item_data.to_dict()
                revisions.append(revisions_item)

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ref": ref,
                "claimId": claim_id,
                "narrativeId": narrative_id,
                "signalFamily": signal_family,
                "place": place,
                "window": window,
                "status": status,
                "physicalEvidence": physical_evidence,
                "detectability": detectability,
                "leadTimes": lead_times,
                "foundBy": found_by,
                "revision": revision,
                "comparator": comparator,
                "manifestId": manifest_id,
                "evaluatedAt": evaluated_at,
            }
        )
        if contradiction is not UNSET:
            field_dict["contradiction"] = contradiction
        if cue_id is not UNSET:
            field_dict["cueId"] = cue_id
        if question_id is not UNSET:
            field_dict["questionId"] = question_id
        if final_at is not UNSET:
            field_dict["finalAt"] = final_at
        if revisions is not UNSET:
            field_dict["revisions"] = revisions
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.claim_place import ClaimPlace
        from ..models.detector_ref import DetectorRef
        from ..models.social_comparison_contradiction_type_0 import (
            SocialComparisonContradictionType0,
        )
        from ..models.social_comparison_detectability import (
            SocialComparisonDetectability,
        )
        from ..models.social_comparison_revision import SocialComparisonRevision
        from ..models.social_lead_times import SocialLeadTimes
        from ..models.social_physical_evidence import SocialPhysicalEvidence
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        id = d.pop("id")

        ref = d.pop("ref")

        claim_id = d.pop("claimId")

        narrative_id = d.pop("narrativeId")

        signal_family = d.pop("signalFamily")

        place = ClaimPlace.from_dict(d.pop("place"))

        window = TimeRange.from_dict(d.pop("window"))

        status = SocialComparisonStatus(d.pop("status"))

        physical_evidence = []
        _physical_evidence = d.pop("physicalEvidence")
        for physical_evidence_item_data in _physical_evidence:
            physical_evidence_item = SocialPhysicalEvidence.from_dict(
                physical_evidence_item_data
            )

            physical_evidence.append(physical_evidence_item)

        detectability = SocialComparisonDetectability.from_dict(d.pop("detectability"))

        lead_times = SocialLeadTimes.from_dict(d.pop("leadTimes"))

        found_by = SocialComparisonFoundBy(d.pop("foundBy"))

        revision = d.pop("revision")

        comparator = DetectorRef.from_dict(d.pop("comparator"))

        manifest_id = d.pop("manifestId")

        evaluated_at = datetime.datetime.fromisoformat(d.pop("evaluatedAt"))

        def _parse_contradiction(
            data: object,
        ) -> None | SocialComparisonContradictionType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                contradiction_type_0 = SocialComparisonContradictionType0.from_dict(
                    data
                )

                return contradiction_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | SocialComparisonContradictionType0 | Unset, data)

        contradiction = _parse_contradiction(d.pop("contradiction", UNSET))

        def _parse_cue_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cue_id = _parse_cue_id(d.pop("cueId", UNSET))

        def _parse_question_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        question_id = _parse_question_id(d.pop("questionId", UNSET))

        def _parse_final_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                final_at_type_0 = datetime.datetime.fromisoformat(data)

                return final_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        final_at = _parse_final_at(d.pop("finalAt", UNSET))

        _revisions = d.pop("revisions", UNSET)
        revisions: list[SocialComparisonRevision] | Unset = UNSET
        if _revisions is not UNSET:
            revisions = []
            for revisions_item_data in _revisions:
                revisions_item = SocialComparisonRevision.from_dict(revisions_item_data)

                revisions.append(revisions_item)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        social_comparison = cls(
            id=id,
            ref=ref,
            claim_id=claim_id,
            narrative_id=narrative_id,
            signal_family=signal_family,
            place=place,
            window=window,
            status=status,
            physical_evidence=physical_evidence,
            detectability=detectability,
            lead_times=lead_times,
            found_by=found_by,
            revision=revision,
            comparator=comparator,
            manifest_id=manifest_id,
            evaluated_at=evaluated_at,
            contradiction=contradiction,
            cue_id=cue_id,
            question_id=question_id,
            final_at=final_at,
            revisions=revisions,
            caveats=caveats,
        )

        social_comparison.additional_properties = d
        return social_comparison

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
