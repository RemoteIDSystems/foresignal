from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chokepoint_activity_payload_coverage_status import (
    ChokepointActivityPayloadCoverageStatus,
)
from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="ChokepointActivityPayload")


@_attrs_define
class ChokepointActivityPayload:
    """Kind `chokepoint-activity` (`derived-chokepoint`): hourly banded counts per chokepoint with coverage status

    Attributes:
        automated (bool):
        not_a_warning (bool):
        detector (DerivedPayloadCommonDetector): Generator id and version
        status (DerivedPayloadCommonStatus):
        chokepoint_id (str):
        hour_start (datetime.datetime):
        confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
        evidence (list[DerivedEvidenceRef] | Unset):
        name (str | Unset):
        transits (int | Unset):
        u_turns (int | Unset):
        stopped (int | Unset):
        coverage_status (ChokepointActivityPayloadCoverageStatus | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    chokepoint_id: str
    hour_start: datetime.datetime
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    name: str | Unset = UNSET
    transits: int | Unset = UNSET
    u_turns: int | Unset = UNSET
    stopped: int | Unset = UNSET
    coverage_status: ChokepointActivityPayloadCoverageStatus | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        chokepoint_id = self.chokepoint_id

        hour_start = self.hour_start.isoformat()

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        name = self.name

        transits = self.transits

        u_turns = self.u_turns

        stopped = self.stopped

        coverage_status: str | Unset = UNSET
        if not isinstance(self.coverage_status, Unset):
            coverage_status = self.coverage_status.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "chokepointId": chokepoint_id,
                "hourStart": hour_start,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if name is not UNSET:
            field_dict["name"] = name
        if transits is not UNSET:
            field_dict["transits"] = transits
        if u_turns is not UNSET:
            field_dict["uTurns"] = u_turns
        if stopped is not UNSET:
            field_dict["stopped"] = stopped
        if coverage_status is not UNSET:
            field_dict["coverageStatus"] = coverage_status

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

        chokepoint_id = d.pop("chokepointId")

        hour_start = datetime.datetime.fromisoformat(d.pop("hourStart"))

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        name = d.pop("name", UNSET)

        transits = d.pop("transits", UNSET)

        u_turns = d.pop("uTurns", UNSET)

        stopped = d.pop("stopped", UNSET)

        _coverage_status = d.pop("coverageStatus", UNSET)
        coverage_status: ChokepointActivityPayloadCoverageStatus | Unset
        if isinstance(_coverage_status, Unset):
            coverage_status = UNSET
        else:
            coverage_status = ChokepointActivityPayloadCoverageStatus(_coverage_status)

        chokepoint_activity_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            chokepoint_id=chokepoint_id,
            hour_start=hour_start,
            confidence=confidence,
            evidence=evidence,
            name=name,
            transits=transits,
            u_turns=u_turns,
            stopped=stopped,
            coverage_status=coverage_status,
        )

        chokepoint_activity_payload.additional_properties = d
        return chokepoint_activity_payload

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
