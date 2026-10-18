from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="DarkVesselCandidatePayload")


@_attrs_define
class DarkVesselCandidatePayload:
    """Kind `dark-vessel-candidate` (`derived-dark-vessel-match`): AIS gap joined with an unmatched SAR detection. A
    candidate; SAR detections are not identifications.

        Attributes:
            automated (bool):
            not_a_warning (bool):
            detector (DerivedPayloadCommonDetector): Generator id and version
            status (DerivedPayloadCommonStatus):
            mmsi (str):
            gap_start (datetime.datetime):
            detection_id (str):
            match_score (float):
            confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
            evidence (list[DerivedEvidenceRef] | Unset):
            distance_km (float | Unset):
            time_offset_min (float | Unset):
            detection_length_m (float | None | Unset):
            label (Literal['candidate; SAR detections are not identifications'] | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    mmsi: str
    gap_start: datetime.datetime
    detection_id: str
    match_score: float
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    distance_km: float | Unset = UNSET
    time_offset_min: float | Unset = UNSET
    detection_length_m: float | None | Unset = UNSET
    label: Literal["candidate; SAR detections are not identifications"] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        mmsi = self.mmsi

        gap_start = self.gap_start.isoformat()

        detection_id = self.detection_id

        match_score = self.match_score

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        distance_km = self.distance_km

        time_offset_min = self.time_offset_min

        detection_length_m: float | None | Unset
        if isinstance(self.detection_length_m, Unset):
            detection_length_m = UNSET
        else:
            detection_length_m = self.detection_length_m

        label = self.label

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "mmsi": mmsi,
                "gapStart": gap_start,
                "detectionId": detection_id,
                "matchScore": match_score,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if time_offset_min is not UNSET:
            field_dict["timeOffsetMin"] = time_offset_min
        if detection_length_m is not UNSET:
            field_dict["detectionLengthM"] = detection_length_m
        if label is not UNSET:
            field_dict["label"] = label

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

        gap_start = datetime.datetime.fromisoformat(d.pop("gapStart"))

        detection_id = d.pop("detectionId")

        match_score = d.pop("matchScore")

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        distance_km = d.pop("distanceKm", UNSET)

        time_offset_min = d.pop("timeOffsetMin", UNSET)

        def _parse_detection_length_m(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        detection_length_m = _parse_detection_length_m(d.pop("detectionLengthM", UNSET))

        label = cast(
            Literal["candidate; SAR detections are not identifications"] | Unset,
            d.pop("label", UNSET),
        )
        if (
            label != "candidate; SAR detections are not identifications"
            and not isinstance(label, Unset)
        ):
            raise ValueError(
                f"label must match const 'candidate; SAR detections are not identifications', got '{label}'"
            )

        dark_vessel_candidate_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            mmsi=mmsi,
            gap_start=gap_start,
            detection_id=detection_id,
            match_score=match_score,
            confidence=confidence,
            evidence=evidence,
            distance_km=distance_km,
            time_offset_min=time_offset_min,
            detection_length_m=detection_length_m,
            label=label,
        )

        dark_vessel_candidate_payload.additional_properties = d
        return dark_vessel_candidate_payload

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
