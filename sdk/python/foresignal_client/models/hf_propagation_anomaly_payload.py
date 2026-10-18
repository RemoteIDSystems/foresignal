from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="HfPropagationAnomalyPayload")


@_attrs_define
class HfPropagationAnomalyPayload:
    """Kind `hf-propagation-anomaly` (`derived-hf-blackout`): FT8 spot or SNR drop on sunlit paths against a 14-day same-
    hour baseline

        Attributes:
            automated (bool):
            not_a_warning (bool):
            detector (DerivedPayloadCommonDetector): Generator id and version
            status (DerivedPayloadCommonStatus):
            band (str):  Example: 20m.
            path_class (str):  Example: EU-NA.
            utc_hour (datetime.datetime):
            confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
            evidence (list[DerivedEvidenceRef] | Unset):
            spot_count (int | Unset):
            baseline_spot_count (float | Unset):
            spot_drop_pct (float | Unset):
            median_snr_db (float | None | Unset):
            baseline_median_snr_db (float | None | Unset):
            snr_drop_db (float | None | Unset):
            sunlit (bool | Unset):
            flare_concurrent (None | str | Unset): Concurrent SWPC X-ray class when known Example: X1.2.
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    band: str
    path_class: str
    utc_hour: datetime.datetime
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    spot_count: int | Unset = UNSET
    baseline_spot_count: float | Unset = UNSET
    spot_drop_pct: float | Unset = UNSET
    median_snr_db: float | None | Unset = UNSET
    baseline_median_snr_db: float | None | Unset = UNSET
    snr_drop_db: float | None | Unset = UNSET
    sunlit: bool | Unset = UNSET
    flare_concurrent: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        band = self.band

        path_class = self.path_class

        utc_hour = self.utc_hour.isoformat()

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        spot_count = self.spot_count

        baseline_spot_count = self.baseline_spot_count

        spot_drop_pct = self.spot_drop_pct

        median_snr_db: float | None | Unset
        if isinstance(self.median_snr_db, Unset):
            median_snr_db = UNSET
        else:
            median_snr_db = self.median_snr_db

        baseline_median_snr_db: float | None | Unset
        if isinstance(self.baseline_median_snr_db, Unset):
            baseline_median_snr_db = UNSET
        else:
            baseline_median_snr_db = self.baseline_median_snr_db

        snr_drop_db: float | None | Unset
        if isinstance(self.snr_drop_db, Unset):
            snr_drop_db = UNSET
        else:
            snr_drop_db = self.snr_drop_db

        sunlit = self.sunlit

        flare_concurrent: None | str | Unset
        if isinstance(self.flare_concurrent, Unset):
            flare_concurrent = UNSET
        else:
            flare_concurrent = self.flare_concurrent

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "band": band,
                "pathClass": path_class,
                "utcHour": utc_hour,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if spot_count is not UNSET:
            field_dict["spotCount"] = spot_count
        if baseline_spot_count is not UNSET:
            field_dict["baselineSpotCount"] = baseline_spot_count
        if spot_drop_pct is not UNSET:
            field_dict["spotDropPct"] = spot_drop_pct
        if median_snr_db is not UNSET:
            field_dict["medianSnrDb"] = median_snr_db
        if baseline_median_snr_db is not UNSET:
            field_dict["baselineMedianSnrDb"] = baseline_median_snr_db
        if snr_drop_db is not UNSET:
            field_dict["snrDropDb"] = snr_drop_db
        if sunlit is not UNSET:
            field_dict["sunlit"] = sunlit
        if flare_concurrent is not UNSET:
            field_dict["flareConcurrent"] = flare_concurrent

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

        band = d.pop("band")

        path_class = d.pop("pathClass")

        utc_hour = datetime.datetime.fromisoformat(d.pop("utcHour"))

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        spot_count = d.pop("spotCount", UNSET)

        baseline_spot_count = d.pop("baselineSpotCount", UNSET)

        spot_drop_pct = d.pop("spotDropPct", UNSET)

        def _parse_median_snr_db(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        median_snr_db = _parse_median_snr_db(d.pop("medianSnrDb", UNSET))

        def _parse_baseline_median_snr_db(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        baseline_median_snr_db = _parse_baseline_median_snr_db(
            d.pop("baselineMedianSnrDb", UNSET)
        )

        def _parse_snr_drop_db(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        snr_drop_db = _parse_snr_drop_db(d.pop("snrDropDb", UNSET))

        sunlit = d.pop("sunlit", UNSET)

        def _parse_flare_concurrent(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        flare_concurrent = _parse_flare_concurrent(d.pop("flareConcurrent", UNSET))

        hf_propagation_anomaly_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            band=band,
            path_class=path_class,
            utc_hour=utc_hour,
            confidence=confidence,
            evidence=evidence,
            spot_count=spot_count,
            baseline_spot_count=baseline_spot_count,
            spot_drop_pct=spot_drop_pct,
            median_snr_db=median_snr_db,
            baseline_median_snr_db=baseline_median_snr_db,
            snr_drop_db=snr_drop_db,
            sunlit=sunlit,
            flare_concurrent=flare_concurrent,
        )

        hf_propagation_anomaly_payload.additional_properties = d
        return hf_propagation_anomaly_payload

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
