from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.corroboration_payload_kind_family import CorroborationPayloadKindFamily
from ..models.corroboration_payload_reliability import CorroborationPayloadReliability
from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="CorroborationPayload")


@_attrs_define
class CorroborationPayload:
    """Kind `corroboration` (`derived-corroboration`): cluster of event records across independent sources with Admiralty
    credibility

        Attributes:
            automated (bool):
            not_a_warning (bool):
            detector (DerivedPayloadCommonDetector): Generator id and version
            status (DerivedPayloadCommonStatus):
            kind_family (CorroborationPayloadKindFamily):
            members (list[DerivedEvidenceRef]):
            independent_sources (int):
            credibility (int): Admiralty information credibility; 6 means unknown, never false
            confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
            evidence (list[DerivedEvidenceRef] | Unset):
            reliability (CorroborationPayloadReliability | Unset): Source reliability from 109 grades; F means unknown
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    kind_family: CorroborationPayloadKindFamily
    members: list[DerivedEvidenceRef]
    independent_sources: int
    credibility: int
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    reliability: CorroborationPayloadReliability | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        kind_family = self.kind_family.value

        members = []
        for members_item_data in self.members:
            members_item = members_item_data.to_dict()
            members.append(members_item)

        independent_sources = self.independent_sources

        credibility = self.credibility

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        reliability: str | Unset = UNSET
        if not isinstance(self.reliability, Unset):
            reliability = self.reliability.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "kindFamily": kind_family,
                "members": members,
                "independentSources": independent_sources,
                "credibility": credibility,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if reliability is not UNSET:
            field_dict["reliability"] = reliability

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.derived_evidence_ref import DerivedEvidenceRef
        from ..models.derived_payload_common_detector import (
            DerivedPayloadCommonDetector,
        )

        d = dict(src_dict)
        automated = d.pop("automated")

        not_a_warning = d.pop("notAWarning")

        detector = DerivedPayloadCommonDetector.from_dict(d.pop("detector"))

        status = DerivedPayloadCommonStatus(d.pop("status"))

        kind_family = CorroborationPayloadKindFamily(d.pop("kindFamily"))

        members = []
        _members = d.pop("members")
        for members_item_data in _members:
            members_item = DerivedEvidenceRef.from_dict(members_item_data)

            members.append(members_item)

        independent_sources = d.pop("independentSources")

        credibility = d.pop("credibility")

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        _reliability = d.pop("reliability", UNSET)
        reliability: CorroborationPayloadReliability | Unset
        if isinstance(_reliability, Unset):
            reliability = UNSET
        else:
            reliability = CorroborationPayloadReliability(_reliability)

        corroboration_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            kind_family=kind_family,
            members=members,
            independent_sources=independent_sources,
            credibility=credibility,
            confidence=confidence,
            evidence=evidence,
            reliability=reliability,
        )

        corroboration_payload.additional_properties = d
        return corroboration_payload

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
