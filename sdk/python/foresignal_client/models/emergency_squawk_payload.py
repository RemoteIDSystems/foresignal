from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..models.emergency_squawk_payload_meaning import EmergencySquawkPayloadMeaning
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="EmergencySquawkPayload")


@_attrs_define
class EmergencySquawkPayload:
    """Kind `emergency-squawk` (`derived-emergency-squawk`): 7500/7600/7700 or readsb emergency seen twice

    Attributes:
        automated (bool):
        not_a_warning (bool):
        detector (DerivedPayloadCommonDetector): Generator id and version
        status (DerivedPayloadCommonStatus):
        hex_ (str):
        meaning (EmergencySquawkPayloadMeaning):
        unverified (bool):
        confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
        evidence (list[DerivedEvidenceRef] | Unset):
        squawk (None | str | Unset):
        emergency (None | str | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    hex_: str
    meaning: EmergencySquawkPayloadMeaning
    unverified: bool
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    squawk: None | str | Unset = UNSET
    emergency: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        hex_ = self.hex_

        meaning = self.meaning.value

        unverified = self.unverified

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        squawk: None | str | Unset
        if isinstance(self.squawk, Unset):
            squawk = UNSET
        else:
            squawk = self.squawk

        emergency: None | str | Unset
        if isinstance(self.emergency, Unset):
            emergency = UNSET
        else:
            emergency = self.emergency

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "hex": hex_,
                "meaning": meaning,
                "unverified": unverified,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if squawk is not UNSET:
            field_dict["squawk"] = squawk
        if emergency is not UNSET:
            field_dict["emergency"] = emergency

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

        hex_ = d.pop("hex")

        meaning = EmergencySquawkPayloadMeaning(d.pop("meaning"))

        unverified = d.pop("unverified")

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        def _parse_squawk(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        squawk = _parse_squawk(d.pop("squawk", UNSET))

        def _parse_emergency(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        emergency = _parse_emergency(d.pop("emergency", UNSET))

        emergency_squawk_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            hex_=hex_,
            meaning=meaning,
            unverified=unverified,
            confidence=confidence,
            evidence=evidence,
            squawk=squawk,
            emergency=emergency,
        )

        emergency_squawk_payload.additional_properties = d
        return emergency_squawk_payload

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
