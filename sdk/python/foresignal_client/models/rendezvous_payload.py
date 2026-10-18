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


T = TypeVar("T", bound="RendezvousPayload")


@_attrs_define
class RendezvousPayload:
    """Kind `rendezvous` (`derived-rendezvous`): two slow vessels within 0.5 nm for ≥ 15 min away from ports

    Attributes:
        automated (bool):
        not_a_warning (bool):
        detector (DerivedPayloadCommonDetector): Generator id and version
        status (DerivedPayloadCommonStatus):
        vessels (list[str]): Sorted entity ids or MMSIs
        started_at (datetime.datetime):
        confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
        evidence (list[DerivedEvidenceRef] | Unset):
        distance_nm (float | Unset):
        duration_min (float | Unset):
        nearest_port_km (float | None | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    vessels: list[str]
    started_at: datetime.datetime
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    distance_nm: float | Unset = UNSET
    duration_min: float | Unset = UNSET
    nearest_port_km: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        vessels = self.vessels

        started_at = self.started_at.isoformat()

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        distance_nm = self.distance_nm

        duration_min = self.duration_min

        nearest_port_km: float | None | Unset
        if isinstance(self.nearest_port_km, Unset):
            nearest_port_km = UNSET
        else:
            nearest_port_km = self.nearest_port_km

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "vessels": vessels,
                "startedAt": started_at,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if distance_nm is not UNSET:
            field_dict["distanceNm"] = distance_nm
        if duration_min is not UNSET:
            field_dict["durationMin"] = duration_min
        if nearest_port_km is not UNSET:
            field_dict["nearestPortKm"] = nearest_port_km

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

        vessels = cast(list[str], d.pop("vessels"))

        started_at = datetime.datetime.fromisoformat(d.pop("startedAt"))

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        distance_nm = d.pop("distanceNm", UNSET)

        duration_min = d.pop("durationMin", UNSET)

        def _parse_nearest_port_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        nearest_port_km = _parse_nearest_port_km(d.pop("nearestPortKm", UNSET))

        rendezvous_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            vessels=vessels,
            started_at=started_at,
            confidence=confidence,
            evidence=evidence,
            distance_nm=distance_nm,
            duration_min=duration_min,
            nearest_port_km=nearest_port_km,
        )

        rendezvous_payload.additional_properties = d
        return rendezvous_payload

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
