from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.analytic_finding_inference_stats import AnalyticFindingInferenceStats
    from ..models.detector_ref import DetectorRef
    from ..models.finding_base_rate import FindingBaseRate


T = TypeVar("T", bound="AnalyticFindingInference")


@_attrs_define
class AnalyticFindingInference:
    """Inference by a named, versioned detector

    Attributes:
        detector (DetectorRef):
        stats (AnalyticFindingInferenceStats): Method numbers by type: change-point `{changeAt, levelBefore, levelAfter,
            effect, detectionDelay, bocpdPosterior}`; pre-event-signal `{car, z, placeboP, corradoP, window,
            linkedEventId}`; unexplained-move `{z, linkedEventId, leadDays}`; co-occurrence `{families, pValues, fisherP,
            adjustedP, expectedJointRate, lift}`; precursor-active `{precursorId, expectedFrom, expectedTo, probability}`
        base_rate (FindingBaseRate): How often a finding this strong happens by chance, and how that was measured
        explanation (str): Deterministic templated explanation
    """

    detector: DetectorRef
    stats: AnalyticFindingInferenceStats
    base_rate: FindingBaseRate
    explanation: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        detector = self.detector.to_dict()

        stats = self.stats.to_dict()

        base_rate = self.base_rate.to_dict()

        explanation = self.explanation

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "detector": detector,
                "stats": stats,
                "baseRate": base_rate,
                "explanation": explanation,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analytic_finding_inference_stats import (
            AnalyticFindingInferenceStats,
        )
        from ..models.detector_ref import DetectorRef
        from ..models.finding_base_rate import FindingBaseRate

        d = dict(src_dict)
        detector = DetectorRef.from_dict(d.pop("detector"))

        stats = AnalyticFindingInferenceStats.from_dict(d.pop("stats"))

        base_rate = FindingBaseRate.from_dict(d.pop("baseRate"))

        explanation = d.pop("explanation")

        analytic_finding_inference = cls(
            detector=detector,
            stats=stats,
            base_rate=base_rate,
            explanation=explanation,
        )

        analytic_finding_inference.additional_properties = d
        return analytic_finding_inference

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
