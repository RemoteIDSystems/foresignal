from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ach_evidence_caveats_item import AchEvidenceCaveatsItem
from ..models.ach_evidence_credibility import AchEvidenceCredibility
from ..models.ach_evidence_relevance import AchEvidenceRelevance
from ..models.ach_evidence_source import AchEvidenceSource
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ach_evidence_ratings import AchEvidenceRatings
    from ..models.provenance_chain_entry import ProvenanceChainEntry


T = TypeVar("T", bound="AchEvidence")


@_attrs_define
class AchEvidence:
    """One ACH evidence row (one per 116 evidence group)

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        source (AchEvidenceSource):
        description (str):
        credibility (AchEvidenceCredibility):
        relevance (AchEvidenceRelevance):
        ratings (AchEvidenceRatings): Per hypothesis id
        added_by (str):
        added_at (datetime.datetime):
        ref (None | str | Unset): Object reference or id of the evidence
        evidence_group_id (None | str | Unset):
        weight (float | Unset): credibility × relevance (L 0.5, M 1, H 2)
        non_diagnostic (bool | Unset):
        removed_at (datetime.datetime | None | Unset):
        evidence_item_id (None | str | Unset): When `source=evidence-item`
        provenance_chain (list[ProvenanceChainEntry] | Unset): 134: chain used by CircularReportingDetector; circular
            support carries caveat CIRCULAR_REPORTING and counts as one evidence group
        refs (list[str] | Unset): Every reference attached to the row: the first and the re-reports of the same 116
            evidence group, which add no weight
        caveats (list[AchEvidenceCaveatsItem] | Unset): 134: CIRCULAR_REPORTING when the support's provenance chain
            returns to the consuming analyst, or when a further reference with the same origin attached to the row
    """

    id: str
    set_id: str
    source: AchEvidenceSource
    description: str
    credibility: AchEvidenceCredibility
    relevance: AchEvidenceRelevance
    ratings: AchEvidenceRatings
    added_by: str
    added_at: datetime.datetime
    ref: None | str | Unset = UNSET
    evidence_group_id: None | str | Unset = UNSET
    weight: float | Unset = UNSET
    non_diagnostic: bool | Unset = UNSET
    removed_at: datetime.datetime | None | Unset = UNSET
    evidence_item_id: None | str | Unset = UNSET
    provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
    refs: list[str] | Unset = UNSET
    caveats: list[AchEvidenceCaveatsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        set_id = self.set_id

        source = self.source.value

        description = self.description

        credibility = self.credibility.value

        relevance = self.relevance.value

        ratings = self.ratings.to_dict()

        added_by = self.added_by

        added_at = self.added_at.isoformat()

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        evidence_group_id: None | str | Unset
        if isinstance(self.evidence_group_id, Unset):
            evidence_group_id = UNSET
        else:
            evidence_group_id = self.evidence_group_id

        weight = self.weight

        non_diagnostic = self.non_diagnostic

        removed_at: None | str | Unset
        if isinstance(self.removed_at, Unset):
            removed_at = UNSET
        elif isinstance(self.removed_at, datetime.datetime):
            removed_at = self.removed_at.isoformat()
        else:
            removed_at = self.removed_at

        evidence_item_id: None | str | Unset
        if isinstance(self.evidence_item_id, Unset):
            evidence_item_id = UNSET
        else:
            evidence_item_id = self.evidence_item_id

        provenance_chain: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.provenance_chain, Unset):
            provenance_chain = []
            for provenance_chain_item_data in self.provenance_chain:
                provenance_chain_item = provenance_chain_item_data.to_dict()
                provenance_chain.append(provenance_chain_item)

        refs: list[str] | Unset = UNSET
        if not isinstance(self.refs, Unset):
            refs = self.refs

        caveats: list[str] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.value
                caveats.append(caveats_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "setId": set_id,
                "source": source,
                "description": description,
                "credibility": credibility,
                "relevance": relevance,
                "ratings": ratings,
                "addedBy": added_by,
                "addedAt": added_at,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if evidence_group_id is not UNSET:
            field_dict["evidenceGroupId"] = evidence_group_id
        if weight is not UNSET:
            field_dict["weight"] = weight
        if non_diagnostic is not UNSET:
            field_dict["nonDiagnostic"] = non_diagnostic
        if removed_at is not UNSET:
            field_dict["removedAt"] = removed_at
        if evidence_item_id is not UNSET:
            field_dict["evidenceItemId"] = evidence_item_id
        if provenance_chain is not UNSET:
            field_dict["provenanceChain"] = provenance_chain
        if refs is not UNSET:
            field_dict["refs"] = refs
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ach_evidence_ratings import AchEvidenceRatings
        from ..models.provenance_chain_entry import ProvenanceChainEntry

        d = dict(src_dict)
        id = d.pop("id")

        set_id = d.pop("setId")

        source = AchEvidenceSource(d.pop("source"))

        description = d.pop("description")

        credibility = AchEvidenceCredibility(d.pop("credibility"))

        relevance = AchEvidenceRelevance(d.pop("relevance"))

        ratings = AchEvidenceRatings.from_dict(d.pop("ratings"))

        added_by = d.pop("addedBy")

        added_at = datetime.datetime.fromisoformat(d.pop("addedAt"))

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        def _parse_evidence_group_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        evidence_group_id = _parse_evidence_group_id(d.pop("evidenceGroupId", UNSET))

        weight = d.pop("weight", UNSET)

        non_diagnostic = d.pop("nonDiagnostic", UNSET)

        def _parse_removed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                removed_at_type_0 = datetime.datetime.fromisoformat(data)

                return removed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        removed_at = _parse_removed_at(d.pop("removedAt", UNSET))

        def _parse_evidence_item_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        evidence_item_id = _parse_evidence_item_id(d.pop("evidenceItemId", UNSET))

        _provenance_chain = d.pop("provenanceChain", UNSET)
        provenance_chain: list[ProvenanceChainEntry] | Unset = UNSET
        if _provenance_chain is not UNSET:
            provenance_chain = []
            for provenance_chain_item_data in _provenance_chain:
                provenance_chain_item = ProvenanceChainEntry.from_dict(
                    provenance_chain_item_data
                )

                provenance_chain.append(provenance_chain_item)

        refs = cast(list[str], d.pop("refs", UNSET))

        _caveats = d.pop("caveats", UNSET)
        caveats: list[AchEvidenceCaveatsItem] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = AchEvidenceCaveatsItem(caveats_item_data)

                caveats.append(caveats_item)

        ach_evidence = cls(
            id=id,
            set_id=set_id,
            source=source,
            description=description,
            credibility=credibility,
            relevance=relevance,
            ratings=ratings,
            added_by=added_by,
            added_at=added_at,
            ref=ref,
            evidence_group_id=evidence_group_id,
            weight=weight,
            non_diagnostic=non_diagnostic,
            removed_at=removed_at,
            evidence_item_id=evidence_item_id,
            provenance_chain=provenance_chain,
            refs=refs,
            caveats=caveats,
        )

        ach_evidence.additional_properties = d
        return ach_evidence

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
