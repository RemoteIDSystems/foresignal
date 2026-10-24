from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intelligence_event_inference_confidence_label import (
    IntelligenceEventInferenceConfidenceLabel,
)
from ..models.intelligence_event_inference_evidence_quality import (
    IntelligenceEventInferenceEvidenceQuality,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.geometry import Geometry


T = TypeVar("T", bound="IntelligenceEventInference")


@_attrs_define
class IntelligenceEventInference:
    """What the detector derived

    Attributes:
        confidence (float): The calibrated probability when `calibrated` is true, otherwise the raw EventScorer score
            (capped at 0.95)
        confidence_label (IntelligenceEventInferenceConfidenceLabel): low < 0.4 ≤ moderate < 0.7 ≤ high
        confidence_method (str):  Example: event-scorer-1.0.0.
        calibrated (bool): True when `confidence` is a calibrated probability backed by resolved outcomes (151); until a
            scope has enough resolved history the raw score stands and this is false
        evidence_quality (IntelligenceEventInferenceEvidenceQuality):
        start_time (datetime.datetime):
        explanation (str): Deterministic templated text citing members
        raw_confidence (float | None | Unset): The uncalibrated EventScorer score behind `confidence` (151); equal to
            `confidence` until calibrated
        end_time (datetime.datetime | None | Unset): null while open
        last_evidence_at (datetime.datetime | None | Unset):
        geometry (Geometry | None | Unset):
        centroid (Geometry | None | Unset):
        region_tags (list[str] | Unset):
    """

    confidence: float
    confidence_label: IntelligenceEventInferenceConfidenceLabel
    confidence_method: str
    calibrated: bool
    evidence_quality: IntelligenceEventInferenceEvidenceQuality
    start_time: datetime.datetime
    explanation: str
    raw_confidence: float | None | Unset = UNSET
    end_time: datetime.datetime | None | Unset = UNSET
    last_evidence_at: datetime.datetime | None | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    centroid: Geometry | None | Unset = UNSET
    region_tags: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.geometry import Geometry

        confidence = self.confidence

        confidence_label = self.confidence_label.value

        confidence_method = self.confidence_method

        calibrated = self.calibrated

        evidence_quality = self.evidence_quality.value

        start_time = self.start_time.isoformat()

        explanation = self.explanation

        raw_confidence: float | None | Unset
        if isinstance(self.raw_confidence, Unset):
            raw_confidence = UNSET
        else:
            raw_confidence = self.raw_confidence

        end_time: None | str | Unset
        if isinstance(self.end_time, Unset):
            end_time = UNSET
        elif isinstance(self.end_time, datetime.datetime):
            end_time = self.end_time.isoformat()
        else:
            end_time = self.end_time

        last_evidence_at: None | str | Unset
        if isinstance(self.last_evidence_at, Unset):
            last_evidence_at = UNSET
        elif isinstance(self.last_evidence_at, datetime.datetime):
            last_evidence_at = self.last_evidence_at.isoformat()
        else:
            last_evidence_at = self.last_evidence_at

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        centroid: dict[str, Any] | None | Unset
        if isinstance(self.centroid, Unset):
            centroid = UNSET
        elif isinstance(self.centroid, Geometry):
            centroid = self.centroid.to_dict()
        else:
            centroid = self.centroid

        region_tags: list[str] | Unset = UNSET
        if not isinstance(self.region_tags, Unset):
            region_tags = self.region_tags

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "confidence": confidence,
                "confidenceLabel": confidence_label,
                "confidenceMethod": confidence_method,
                "calibrated": calibrated,
                "evidenceQuality": evidence_quality,
                "startTime": start_time,
                "explanation": explanation,
            }
        )
        if raw_confidence is not UNSET:
            field_dict["rawConfidence"] = raw_confidence
        if end_time is not UNSET:
            field_dict["endTime"] = end_time
        if last_evidence_at is not UNSET:
            field_dict["lastEvidenceAt"] = last_evidence_at
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if centroid is not UNSET:
            field_dict["centroid"] = centroid
        if region_tags is not UNSET:
            field_dict["regionTags"] = region_tags

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.geometry import Geometry

        d = dict(src_dict)
        confidence = d.pop("confidence")

        confidence_label = IntelligenceEventInferenceConfidenceLabel(
            d.pop("confidenceLabel")
        )

        confidence_method = d.pop("confidenceMethod")

        calibrated = d.pop("calibrated")

        evidence_quality = IntelligenceEventInferenceEvidenceQuality(
            d.pop("evidenceQuality")
        )

        start_time = datetime.datetime.fromisoformat(d.pop("startTime"))

        explanation = d.pop("explanation")

        def _parse_raw_confidence(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        raw_confidence = _parse_raw_confidence(d.pop("rawConfidence", UNSET))

        def _parse_end_time(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                end_time_type_0 = datetime.datetime.fromisoformat(data)

                return end_time_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        end_time = _parse_end_time(d.pop("endTime", UNSET))

        def _parse_last_evidence_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_evidence_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_evidence_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_evidence_at = _parse_last_evidence_at(d.pop("lastEvidenceAt", UNSET))

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        def _parse_centroid(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                centroid_type_0 = Geometry.from_dict(data)

                return centroid_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        centroid = _parse_centroid(d.pop("centroid", UNSET))

        region_tags = cast(list[str], d.pop("regionTags", UNSET))

        intelligence_event_inference = cls(
            confidence=confidence,
            confidence_label=confidence_label,
            confidence_method=confidence_method,
            calibrated=calibrated,
            evidence_quality=evidence_quality,
            start_time=start_time,
            explanation=explanation,
            raw_confidence=raw_confidence,
            end_time=end_time,
            last_evidence_at=last_evidence_at,
            geometry=geometry,
            centroid=centroid,
            region_tags=region_tags,
        )

        intelligence_event_inference.additional_properties = d
        return intelligence_event_inference

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
