from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..models.holdover_fire_risk_payload_precip_source import (
    HoldoverFireRiskPayloadPrecipSource,
)
from ..models.severity import Severity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="HoldoverFireRiskPayload")


@_attrs_define
class HoldoverFireRiskPayload:
    """Kind `holdover-fire-risk` (`derived-lightning-holdover`): GLM flashes over dry ground, watched 72 h for ignition

    Attributes:
        automated (bool):
        not_a_warning (bool):
        detector (DerivedPayloadCommonDetector): Generator id and version
        status (DerivedPayloadCommonStatus):
        cell (str):
        utc_day (datetime.date):
        flash_count (int):
        ignition_confirmed (bool):
        confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
        evidence (list[DerivedEvidenceRef] | Unset):
        qpe_72_h_mm (float | None | Unset):
        precip_source (HoldoverFireRiskPayloadPrecipSource | Unset):
        relative_humidity_pct (float | None | Unset):
        ignition_delay_h (float | None | Unset):
        watch_until (datetime.datetime | Unset):
        severity (Severity | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    cell: str
    utc_day: datetime.date
    flash_count: int
    ignition_confirmed: bool
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    qpe_72_h_mm: float | None | Unset = UNSET
    precip_source: HoldoverFireRiskPayloadPrecipSource | Unset = UNSET
    relative_humidity_pct: float | None | Unset = UNSET
    ignition_delay_h: float | None | Unset = UNSET
    watch_until: datetime.datetime | Unset = UNSET
    severity: Severity | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        cell = self.cell

        utc_day = self.utc_day.isoformat()

        flash_count = self.flash_count

        ignition_confirmed = self.ignition_confirmed

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        qpe_72_h_mm: float | None | Unset
        if isinstance(self.qpe_72_h_mm, Unset):
            qpe_72_h_mm = UNSET
        else:
            qpe_72_h_mm = self.qpe_72_h_mm

        precip_source: str | Unset = UNSET
        if not isinstance(self.precip_source, Unset):
            precip_source = self.precip_source.value

        relative_humidity_pct: float | None | Unset
        if isinstance(self.relative_humidity_pct, Unset):
            relative_humidity_pct = UNSET
        else:
            relative_humidity_pct = self.relative_humidity_pct

        ignition_delay_h: float | None | Unset
        if isinstance(self.ignition_delay_h, Unset):
            ignition_delay_h = UNSET
        else:
            ignition_delay_h = self.ignition_delay_h

        watch_until: str | Unset = UNSET
        if not isinstance(self.watch_until, Unset):
            watch_until = self.watch_until.isoformat()

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
                "cell": cell,
                "utcDay": utc_day,
                "flashCount": flash_count,
                "ignitionConfirmed": ignition_confirmed,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if qpe_72_h_mm is not UNSET:
            field_dict["qpe72hMm"] = qpe_72_h_mm
        if precip_source is not UNSET:
            field_dict["precipSource"] = precip_source
        if relative_humidity_pct is not UNSET:
            field_dict["relativeHumidityPct"] = relative_humidity_pct
        if ignition_delay_h is not UNSET:
            field_dict["ignitionDelayH"] = ignition_delay_h
        if watch_until is not UNSET:
            field_dict["watchUntil"] = watch_until
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

        cell = d.pop("cell")

        utc_day = datetime.date.fromisoformat(d.pop("utcDay"))

        flash_count = d.pop("flashCount")

        ignition_confirmed = d.pop("ignitionConfirmed")

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        def _parse_qpe_72_h_mm(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        qpe_72_h_mm = _parse_qpe_72_h_mm(d.pop("qpe72hMm", UNSET))

        _precip_source = d.pop("precipSource", UNSET)
        precip_source: HoldoverFireRiskPayloadPrecipSource | Unset
        if isinstance(_precip_source, Unset):
            precip_source = UNSET
        else:
            precip_source = HoldoverFireRiskPayloadPrecipSource(_precip_source)

        def _parse_relative_humidity_pct(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        relative_humidity_pct = _parse_relative_humidity_pct(
            d.pop("relativeHumidityPct", UNSET)
        )

        def _parse_ignition_delay_h(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        ignition_delay_h = _parse_ignition_delay_h(d.pop("ignitionDelayH", UNSET))

        _watch_until = d.pop("watchUntil", UNSET)
        watch_until: datetime.datetime | Unset
        if isinstance(_watch_until, Unset):
            watch_until = UNSET
        else:
            watch_until = datetime.datetime.fromisoformat(_watch_until)

        _severity = d.pop("severity", UNSET)
        severity: Severity | Unset
        if isinstance(_severity, Unset):
            severity = UNSET
        else:
            severity = Severity(_severity)

        holdover_fire_risk_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            cell=cell,
            utc_day=utc_day,
            flash_count=flash_count,
            ignition_confirmed=ignition_confirmed,
            confidence=confidence,
            evidence=evidence,
            qpe_72_h_mm=qpe_72_h_mm,
            precip_source=precip_source,
            relative_humidity_pct=relative_humidity_pct,
            ignition_delay_h=ignition_delay_h,
            watch_until=watch_until,
            severity=severity,
        )

        holdover_fire_risk_payload.additional_properties = d
        return holdover_fire_risk_payload

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
