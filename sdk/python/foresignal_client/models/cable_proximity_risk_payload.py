from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..models.severity import Severity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="CableProximityRiskPayload")


@_attrs_define
class CableProximityRiskPayload:
    """Kind `cable-proximity-risk` (`derived-cable-risk`): non-cable vessel anchored or slow near a submarine cable route

    Attributes:
        automated (bool):
        not_a_warning (bool):
        detector (DerivedPayloadCommonDetector): Generator id and version
        status (DerivedPayloadCommonStatus):
        mmsi (str):
        cable_id (str):
        started_at (datetime.datetime):
        confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
        evidence (list[DerivedEvidenceRef] | Unset):
        cable_name (None | str | Unset):
        duration_min (float | Unset):
        cables_within_2_km (int | Unset):
        possible_anchor_drag (bool | Unset):
        ais_gap_near_cable (bool | Unset):
        severity (Severity | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    mmsi: str
    cable_id: str
    started_at: datetime.datetime
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    cable_name: None | str | Unset = UNSET
    duration_min: float | Unset = UNSET
    cables_within_2_km: int | Unset = UNSET
    possible_anchor_drag: bool | Unset = UNSET
    ais_gap_near_cable: bool | Unset = UNSET
    severity: Severity | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        mmsi = self.mmsi

        cable_id = self.cable_id

        started_at = self.started_at.isoformat()

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        cable_name: None | str | Unset
        if isinstance(self.cable_name, Unset):
            cable_name = UNSET
        else:
            cable_name = self.cable_name

        duration_min = self.duration_min

        cables_within_2_km = self.cables_within_2_km

        possible_anchor_drag = self.possible_anchor_drag

        ais_gap_near_cable = self.ais_gap_near_cable

        severity: str | Unset = UNSET
        if not isinstance(self.severity, Unset):
            severity = self.severity.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "mmsi": mmsi,
                "cableId": cable_id,
                "startedAt": started_at,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if cable_name is not UNSET:
            field_dict["cableName"] = cable_name
        if duration_min is not UNSET:
            field_dict["durationMin"] = duration_min
        if cables_within_2_km is not UNSET:
            field_dict["cablesWithin2Km"] = cables_within_2_km
        if possible_anchor_drag is not UNSET:
            field_dict["possibleAnchorDrag"] = possible_anchor_drag
        if ais_gap_near_cable is not UNSET:
            field_dict["aisGapNearCable"] = ais_gap_near_cable
        if severity is not UNSET:
            field_dict["severity"] = severity

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

        cable_id = d.pop("cableId")

        started_at = datetime.datetime.fromisoformat(d.pop("startedAt"))

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        def _parse_cable_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        cable_name = _parse_cable_name(d.pop("cableName", UNSET))

        duration_min = d.pop("durationMin", UNSET)

        cables_within_2_km = d.pop("cablesWithin2Km", UNSET)

        possible_anchor_drag = d.pop("possibleAnchorDrag", UNSET)

        ais_gap_near_cable = d.pop("aisGapNearCable", UNSET)

        _severity = d.pop("severity", UNSET)
        severity: Severity | Unset
        if isinstance(_severity, Unset):
            severity = UNSET
        else:
            severity = Severity(_severity)

        cable_proximity_risk_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            mmsi=mmsi,
            cable_id=cable_id,
            started_at=started_at,
            confidence=confidence,
            evidence=evidence,
            cable_name=cable_name,
            duration_min=duration_min,
            cables_within_2_km=cables_within_2_km,
            possible_anchor_drag=possible_anchor_drag,
            ais_gap_near_cable=ais_gap_near_cable,
            severity=severity,
        )

        cable_proximity_risk_payload.additional_properties = d
        return cable_proximity_risk_payload

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
