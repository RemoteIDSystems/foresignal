from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.chokepoint_uturn_payload_manoeuvre import ChokepointUturnPayloadManoeuvre
from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="ChokepointUturnPayload")


@_attrs_define
class ChokepointUturnPayload:
    """Kind `chokepoint-uturn` (`derived-chokepoint`): course change, U-turn or sharp slowdown inside a chokepoint

    Attributes:
        automated (bool):
        not_a_warning (bool):
        detector (DerivedPayloadCommonDetector): Generator id and version
        status (DerivedPayloadCommonStatus):
        mmsi (str):
        chokepoint_id (str):
        at (datetime.datetime):
        manoeuvre (ChokepointUturnPayloadManoeuvre):
        confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
        evidence (list[DerivedEvidenceRef] | Unset):
        course_change_deg (float | None | Unset):
        speed_drop_pct (float | None | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    mmsi: str
    chokepoint_id: str
    at: datetime.datetime
    manoeuvre: ChokepointUturnPayloadManoeuvre
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    course_change_deg: float | None | Unset = UNSET
    speed_drop_pct: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        mmsi = self.mmsi

        chokepoint_id = self.chokepoint_id

        at = self.at.isoformat()

        manoeuvre = self.manoeuvre.value

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        course_change_deg: float | None | Unset
        if isinstance(self.course_change_deg, Unset):
            course_change_deg = UNSET
        else:
            course_change_deg = self.course_change_deg

        speed_drop_pct: float | None | Unset
        if isinstance(self.speed_drop_pct, Unset):
            speed_drop_pct = UNSET
        else:
            speed_drop_pct = self.speed_drop_pct

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "mmsi": mmsi,
                "chokepointId": chokepoint_id,
                "at": at,
                "manoeuvre": manoeuvre,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if course_change_deg is not UNSET:
            field_dict["courseChangeDeg"] = course_change_deg
        if speed_drop_pct is not UNSET:
            field_dict["speedDropPct"] = speed_drop_pct

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

        mmsi = d.pop("mmsi")

        chokepoint_id = d.pop("chokepointId")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        manoeuvre = ChokepointUturnPayloadManoeuvre(d.pop("manoeuvre"))

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        def _parse_course_change_deg(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        course_change_deg = _parse_course_change_deg(d.pop("courseChangeDeg", UNSET))

        def _parse_speed_drop_pct(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        speed_drop_pct = _parse_speed_drop_pct(d.pop("speedDropPct", UNSET))

        chokepoint_uturn_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            mmsi=mmsi,
            chokepoint_id=chokepoint_id,
            at=at,
            manoeuvre=manoeuvre,
            confidence=confidence,
            evidence=evidence,
            course_change_deg=course_change_deg,
            speed_drop_pct=speed_drop_pct,
        )

        chokepoint_uturn_payload.additional_properties = d
        return chokepoint_uturn_payload

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
