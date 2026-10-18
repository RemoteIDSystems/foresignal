from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.anomaly_observation_classification import AnomalyObservationClassification
from ..models.anomaly_observation_direction_type_1 import (
    AnomalyObservationDirectionType1,
)
from ..models.anomaly_observation_direction_type_2_type_1 import (
    AnomalyObservationDirectionType2Type1,
)
from ..models.anomaly_observation_direction_type_3_type_1 import (
    AnomalyObservationDirectionType3Type1,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.anomaly_observation_baseline import AnomalyObservationBaseline
    from ..models.anomaly_observation_evidence import AnomalyObservationEvidence
    from ..models.detector_ref import DetectorRef


T = TypeVar("T", bound="AnomalyObservation")


@_attrs_define
class AnomalyObservation:
    """One evaluated anomaly bucket. `pValue` assumes the stated count model; it is not the probability that something is
    happening.

        Attributes:
            id (int):
            rule_id (UUID):
            group_key (str):
            bucket_start (datetime.datetime):
            bucket_end (datetime.datetime):
            observed (int):
            expected (float): λ = slot mean × coverage ratio
            classification (AnomalyObservationClassification):
            baseline (AnomalyObservationBaseline): Baseline summary used
            detector (DetectorRef):
            watermark_seq (int):
            exposure (float | Unset): Coverage ratio applied to the expectation
            p_value (float | None | Unset):
            score (float | None | Unset): Anscombe-transformed z
            direction (AnomalyObservationDirectionType1 | AnomalyObservationDirectionType2Type1 |
                AnomalyObservationDirectionType3Type1 | None | Unset):
            low_confidence (bool | Unset):
            evidence (AnomalyObservationEvidence | Unset): Sample evidence refs and fetch_log ranges
            alert_id (int | None | Unset):
            manifest_id (None | str | Unset): The analysis manifest of this output (prompt 116); null for outputs written
                before it
    """

    id: int
    rule_id: UUID
    group_key: str
    bucket_start: datetime.datetime
    bucket_end: datetime.datetime
    observed: int
    expected: float
    classification: AnomalyObservationClassification
    baseline: AnomalyObservationBaseline
    detector: DetectorRef
    watermark_seq: int
    exposure: float | Unset = UNSET
    p_value: float | None | Unset = UNSET
    score: float | None | Unset = UNSET
    direction: (
        AnomalyObservationDirectionType1
        | AnomalyObservationDirectionType2Type1
        | AnomalyObservationDirectionType3Type1
        | None
        | Unset
    ) = UNSET
    low_confidence: bool | Unset = UNSET
    evidence: AnomalyObservationEvidence | Unset = UNSET
    alert_id: int | None | Unset = UNSET
    manifest_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        rule_id = str(self.rule_id)

        group_key = self.group_key

        bucket_start = self.bucket_start.isoformat()

        bucket_end = self.bucket_end.isoformat()

        observed = self.observed

        expected = self.expected

        classification = self.classification.value

        baseline = self.baseline.to_dict()

        detector = self.detector.to_dict()

        watermark_seq = self.watermark_seq

        exposure = self.exposure

        p_value: float | None | Unset
        if isinstance(self.p_value, Unset):
            p_value = UNSET
        else:
            p_value = self.p_value

        score: float | None | Unset
        if isinstance(self.score, Unset):
            score = UNSET
        else:
            score = self.score

        direction: None | str | Unset
        if isinstance(self.direction, Unset):
            direction = UNSET
        elif (
            isinstance(self.direction, AnomalyObservationDirectionType1)
            or isinstance(self.direction, AnomalyObservationDirectionType2Type1)
            or isinstance(self.direction, AnomalyObservationDirectionType3Type1)
        ):
            direction = self.direction.value
        else:
            direction = self.direction

        low_confidence = self.low_confidence

        evidence: dict[str, Any] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = self.evidence.to_dict()

        alert_id: int | None | Unset
        if isinstance(self.alert_id, Unset):
            alert_id = UNSET
        else:
            alert_id = self.alert_id

        manifest_id: None | str | Unset
        if isinstance(self.manifest_id, Unset):
            manifest_id = UNSET
        else:
            manifest_id = self.manifest_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "ruleId": rule_id,
                "groupKey": group_key,
                "bucketStart": bucket_start,
                "bucketEnd": bucket_end,
                "observed": observed,
                "expected": expected,
                "classification": classification,
                "baseline": baseline,
                "detector": detector,
                "watermarkSeq": watermark_seq,
            }
        )
        if exposure is not UNSET:
            field_dict["exposure"] = exposure
        if p_value is not UNSET:
            field_dict["pValue"] = p_value
        if score is not UNSET:
            field_dict["score"] = score
        if direction is not UNSET:
            field_dict["direction"] = direction
        if low_confidence is not UNSET:
            field_dict["lowConfidence"] = low_confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if alert_id is not UNSET:
            field_dict["alertId"] = alert_id
        if manifest_id is not UNSET:
            field_dict["manifestId"] = manifest_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.anomaly_observation_baseline import AnomalyObservationBaseline
        from ..models.anomaly_observation_evidence import AnomalyObservationEvidence
        from ..models.detector_ref import DetectorRef

        d = dict(src_dict)
        id = d.pop("id")

        rule_id = UUID(d.pop("ruleId"))

        group_key = d.pop("groupKey")

        bucket_start = datetime.datetime.fromisoformat(d.pop("bucketStart"))

        bucket_end = datetime.datetime.fromisoformat(d.pop("bucketEnd"))

        observed = d.pop("observed")

        expected = d.pop("expected")

        classification = AnomalyObservationClassification(d.pop("classification"))

        baseline = AnomalyObservationBaseline.from_dict(d.pop("baseline"))

        detector = DetectorRef.from_dict(d.pop("detector"))

        watermark_seq = d.pop("watermarkSeq")

        exposure = d.pop("exposure", UNSET)

        def _parse_p_value(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p_value = _parse_p_value(d.pop("pValue", UNSET))

        def _parse_score(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        score = _parse_score(d.pop("score", UNSET))

        def _parse_direction(
            data: object,
        ) -> (
            AnomalyObservationDirectionType1
            | AnomalyObservationDirectionType2Type1
            | AnomalyObservationDirectionType3Type1
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
                direction_type_1 = AnomalyObservationDirectionType1(data)

                return direction_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                direction_type_2_type_1 = AnomalyObservationDirectionType2Type1(data)

                return direction_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                direction_type_3_type_1 = AnomalyObservationDirectionType3Type1(data)

                return direction_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AnomalyObservationDirectionType1
                | AnomalyObservationDirectionType2Type1
                | AnomalyObservationDirectionType3Type1
                | None
                | Unset,
                data,
            )

        direction = _parse_direction(d.pop("direction", UNSET))

        low_confidence = d.pop("lowConfidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: AnomalyObservationEvidence | Unset
        if isinstance(_evidence, Unset):
            evidence = UNSET
        else:
            evidence = AnomalyObservationEvidence.from_dict(_evidence)

        def _parse_alert_id(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        alert_id = _parse_alert_id(d.pop("alertId", UNSET))

        def _parse_manifest_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        manifest_id = _parse_manifest_id(d.pop("manifestId", UNSET))

        anomaly_observation = cls(
            id=id,
            rule_id=rule_id,
            group_key=group_key,
            bucket_start=bucket_start,
            bucket_end=bucket_end,
            observed=observed,
            expected=expected,
            classification=classification,
            baseline=baseline,
            detector=detector,
            watermark_seq=watermark_seq,
            exposure=exposure,
            p_value=p_value,
            score=score,
            direction=direction,
            low_confidence=low_confidence,
            evidence=evidence,
            alert_id=alert_id,
            manifest_id=manifest_id,
        )

        anomaly_observation.additional_properties = d
        return anomaly_observation

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
