from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..models.felt_report_estimate_payload_coefficient_set_type_1 import (
    FeltReportEstimatePayloadCoefficientSetType1,
)
from ..models.felt_report_estimate_payload_coefficient_set_type_2_type_1 import (
    FeltReportEstimatePayloadCoefficientSetType2Type1,
)
from ..models.felt_report_estimate_payload_coefficient_set_type_3_type_1 import (
    FeltReportEstimatePayloadCoefficientSetType3Type1,
)
from ..models.felt_report_estimate_payload_method import FeltReportEstimatePayloadMethod
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="FeltReportEstimatePayload")


@_attrs_define
class FeltReportEstimatePayload:
    """Kind `felt-report-estimate` (`derived-felt-locally`): estimated intensity at a place for an earthquake

    Attributes:
        automated (bool):
        not_a_warning (bool):
        detector (DerivedPayloadCommonDetector): Generator id and version
        status (DerivedPayloadCommonStatus):
        event_id (str):
        place_id (str):
        mmi (float):
        method (FeltReportEstimatePayloadMethod):
        confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
        evidence (list[DerivedEvidenceRef] | Unset):
        distance_km (float | Unset):
        felt_likely (bool | Unset):
        coefficient_set (FeltReportEstimatePayloadCoefficientSetType1 |
            FeltReportEstimatePayloadCoefficientSetType2Type1 | FeltReportEstimatePayloadCoefficientSetType3Type1 | None |
            Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    event_id: str
    place_id: str
    mmi: float
    method: FeltReportEstimatePayloadMethod
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    distance_km: float | Unset = UNSET
    felt_likely: bool | Unset = UNSET
    coefficient_set: (
        FeltReportEstimatePayloadCoefficientSetType1
        | FeltReportEstimatePayloadCoefficientSetType2Type1
        | FeltReportEstimatePayloadCoefficientSetType3Type1
        | None
        | Unset
    ) = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        event_id = self.event_id

        place_id = self.place_id

        mmi = self.mmi

        method = self.method.value

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        distance_km = self.distance_km

        felt_likely = self.felt_likely

        coefficient_set: None | str | Unset
        if isinstance(self.coefficient_set, Unset):
            coefficient_set = UNSET
        elif (
            isinstance(
                self.coefficient_set, FeltReportEstimatePayloadCoefficientSetType1
            )
            or isinstance(
                self.coefficient_set, FeltReportEstimatePayloadCoefficientSetType2Type1
            )
            or isinstance(
                self.coefficient_set, FeltReportEstimatePayloadCoefficientSetType3Type1
            )
        ):
            coefficient_set = self.coefficient_set.value
        else:
            coefficient_set = self.coefficient_set

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "eventId": event_id,
                "placeId": place_id,
                "mmi": mmi,
                "method": method,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if distance_km is not UNSET:
            field_dict["distanceKm"] = distance_km
        if felt_likely is not UNSET:
            field_dict["feltLikely"] = felt_likely
        if coefficient_set is not UNSET:
            field_dict["coefficientSet"] = coefficient_set

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

        event_id = d.pop("eventId")

        place_id = d.pop("placeId")

        mmi = d.pop("mmi")

        method = FeltReportEstimatePayloadMethod(d.pop("method"))

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        distance_km = d.pop("distanceKm", UNSET)

        felt_likely = d.pop("feltLikely", UNSET)

        def _parse_coefficient_set(
            data: object,
        ) -> (
            FeltReportEstimatePayloadCoefficientSetType1
            | FeltReportEstimatePayloadCoefficientSetType2Type1
            | FeltReportEstimatePayloadCoefficientSetType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                coefficient_set_type_1 = FeltReportEstimatePayloadCoefficientSetType1(
                    data
                )

                return coefficient_set_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                coefficient_set_type_2_type_1 = (
                    FeltReportEstimatePayloadCoefficientSetType2Type1(data)
                )

                return coefficient_set_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                coefficient_set_type_3_type_1 = (
                    FeltReportEstimatePayloadCoefficientSetType3Type1(data)
                )

                return coefficient_set_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                FeltReportEstimatePayloadCoefficientSetType1
                | FeltReportEstimatePayloadCoefficientSetType2Type1
                | FeltReportEstimatePayloadCoefficientSetType3Type1
                | None
                | Unset,
                data,
            )

        coefficient_set = _parse_coefficient_set(d.pop("coefficientSet", UNSET))

        felt_report_estimate_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            event_id=event_id,
            place_id=place_id,
            mmi=mmi,
            method=method,
            confidence=confidence,
            evidence=evidence,
            distance_km=distance_km,
            felt_likely=felt_likely,
            coefficient_set=coefficient_set,
        )

        felt_report_estimate_payload.additional_properties = d
        return felt_report_estimate_payload

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
