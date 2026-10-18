from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="MilNavalCooccurrencePayload")


@_attrs_define
class MilNavalCooccurrencePayload:
    """Kind `mil-naval-cooccurrence` (`derived-mil-naval`): military aircraft and naval vessels in the same res-3 cell
    within 30 min

        Attributes:
            automated (bool):
            not_a_warning (bool):
            detector (DerivedPayloadCommonDetector): Generator id and version
            status (DerivedPayloadCommonStatus):
            cell (str):
            aircraft_count (int):
            vessel_count (int):
            confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
            evidence (list[DerivedEvidenceRef] | Unset):
            aircraft (list[str] | Unset):
            vessels (list[str] | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    cell: str
    aircraft_count: int
    vessel_count: int
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    aircraft: list[str] | Unset = UNSET
    vessels: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        cell = self.cell

        aircraft_count = self.aircraft_count

        vessel_count = self.vessel_count

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        aircraft: list[str] | Unset = UNSET
        if not isinstance(self.aircraft, Unset):
            aircraft = self.aircraft

        vessels: list[str] | Unset = UNSET
        if not isinstance(self.vessels, Unset):
            vessels = self.vessels

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "cell": cell,
                "aircraftCount": aircraft_count,
                "vesselCount": vessel_count,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if aircraft is not UNSET:
            field_dict["aircraft"] = aircraft
        if vessels is not UNSET:
            field_dict["vessels"] = vessels

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

        aircraft_count = d.pop("aircraftCount")

        vessel_count = d.pop("vesselCount")

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        aircraft = cast(list[str], d.pop("aircraft", UNSET))

        vessels = cast(list[str], d.pop("vessels", UNSET))

        mil_naval_cooccurrence_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            cell=cell,
            aircraft_count=aircraft_count,
            vessel_count=vessel_count,
            confidence=confidence,
            evidence=evidence,
            aircraft=aircraft,
            vessels=vessels,
        )

        mil_naval_cooccurrence_payload.additional_properties = d
        return mil_naval_cooccurrence_payload

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
