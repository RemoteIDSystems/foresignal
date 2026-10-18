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
    from ..models.holding_pattern_payload_nearest_airport_type_0 import (
        HoldingPatternPayloadNearestAirportType0,
    )


T = TypeVar("T", bound="HoldingPatternPayload")


@_attrs_define
class HoldingPatternPayload:
    """Kind `holding-pattern` (`derived-holding`): sustained turning at steady altitude

    Attributes:
        automated (bool):
        not_a_warning (bool):
        detector (DerivedPayloadCommonDetector): Generator id and version
        status (DerivedPayloadCommonStatus):
        hex_ (str):
        turns_deg (float):
        confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
        evidence (list[DerivedEvidenceRef] | Unset):
        duration_min (float | Unset):
        altitude_ft (float | Unset):
        nearest_airport (HoldingPatternPayloadNearestAirportType0 | None | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    hex_: str
    turns_deg: float
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    duration_min: float | Unset = UNSET
    altitude_ft: float | Unset = UNSET
    nearest_airport: HoldingPatternPayloadNearestAirportType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.holding_pattern_payload_nearest_airport_type_0 import (
            HoldingPatternPayloadNearestAirportType0,
        )

        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        hex_ = self.hex_

        turns_deg = self.turns_deg

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        duration_min = self.duration_min

        altitude_ft = self.altitude_ft

        nearest_airport: dict[str, Any] | None | Unset
        if isinstance(self.nearest_airport, Unset):
            nearest_airport = UNSET
        elif isinstance(self.nearest_airport, HoldingPatternPayloadNearestAirportType0):
            nearest_airport = self.nearest_airport.to_dict()
        else:
            nearest_airport = self.nearest_airport

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "hex": hex_,
                "turnsDeg": turns_deg,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if duration_min is not UNSET:
            field_dict["durationMin"] = duration_min
        if altitude_ft is not UNSET:
            field_dict["altitudeFt"] = altitude_ft
        if nearest_airport is not UNSET:
            field_dict["nearestAirport"] = nearest_airport

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.derived_evidence_ref import DerivedEvidenceRef
        from ..models.derived_payload_common_detector import (
            DerivedPayloadCommonDetector,
        )
        from ..models.holding_pattern_payload_nearest_airport_type_0 import (
            HoldingPatternPayloadNearestAirportType0,
        )

        d = dict(src_dict)
        automated = d.pop("automated")

        not_a_warning = d.pop("notAWarning")

        detector = DerivedPayloadCommonDetector.from_dict(d.pop("detector"))

        status = DerivedPayloadCommonStatus(d.pop("status"))

        hex_ = d.pop("hex")

        turns_deg = d.pop("turnsDeg")

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        duration_min = d.pop("durationMin", UNSET)

        altitude_ft = d.pop("altitudeFt", UNSET)

        def _parse_nearest_airport(
            data: object,
        ) -> HoldingPatternPayloadNearestAirportType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                nearest_airport_type_0 = (
                    HoldingPatternPayloadNearestAirportType0.from_dict(data)
                )

                return nearest_airport_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HoldingPatternPayloadNearestAirportType0 | None | Unset, data)

        nearest_airport = _parse_nearest_airport(d.pop("nearestAirport", UNSET))

        holding_pattern_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            hex_=hex_,
            turns_deg=turns_deg,
            confidence=confidence,
            evidence=evidence,
            duration_min=duration_min,
            altitude_ft=altitude_ft,
            nearest_airport=nearest_airport,
        )

        holding_pattern_payload.additional_properties = d
        return holding_pattern_payload

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
