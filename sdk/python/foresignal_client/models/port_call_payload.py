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


T = TypeVar("T", bound="PortCallPayload")


@_attrs_define
class PortCallPayload:
    """Kind `port-call` (`derived-port-calls`): arrival and departure at a World Port Index port

    Attributes:
        automated (bool):
        not_a_warning (bool):
        detector (DerivedPayloadCommonDetector): Generator id and version
        status (DerivedPayloadCommonStatus):
        mmsi (str):
        port_number (str):
        arrival (datetime.datetime):
        confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
        evidence (list[DerivedEvidenceRef] | Unset):
        port_name (None | str | Unset):
        departure (datetime.datetime | None | Unset):
        duration_h (float | None | Unset):
        departure_unknown (bool | Unset): True when the departure time could not be observed; never guessed
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    mmsi: str
    port_number: str
    arrival: datetime.datetime
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    port_name: None | str | Unset = UNSET
    departure: datetime.datetime | None | Unset = UNSET
    duration_h: float | None | Unset = UNSET
    departure_unknown: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        mmsi = self.mmsi

        port_number = self.port_number

        arrival = self.arrival.isoformat()

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        port_name: None | str | Unset
        if isinstance(self.port_name, Unset):
            port_name = UNSET
        else:
            port_name = self.port_name

        departure: None | str | Unset
        if isinstance(self.departure, Unset):
            departure = UNSET
        elif isinstance(self.departure, datetime.datetime):
            departure = self.departure.isoformat()
        else:
            departure = self.departure

        duration_h: float | None | Unset
        if isinstance(self.duration_h, Unset):
            duration_h = UNSET
        else:
            duration_h = self.duration_h

        departure_unknown = self.departure_unknown

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "mmsi": mmsi,
                "portNumber": port_number,
                "arrival": arrival,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if port_name is not UNSET:
            field_dict["portName"] = port_name
        if departure is not UNSET:
            field_dict["departure"] = departure
        if duration_h is not UNSET:
            field_dict["durationH"] = duration_h
        if departure_unknown is not UNSET:
            field_dict["departureUnknown"] = departure_unknown

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

        mmsi = d.pop("mmsi")

        port_number = d.pop("portNumber")

        arrival = datetime.datetime.fromisoformat(d.pop("arrival"))

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        def _parse_port_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        port_name = _parse_port_name(d.pop("portName", UNSET))

        def _parse_departure(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                departure_type_0 = datetime.datetime.fromisoformat(data)

                return departure_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        departure = _parse_departure(d.pop("departure", UNSET))

        def _parse_duration_h(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        duration_h = _parse_duration_h(d.pop("durationH", UNSET))

        departure_unknown = d.pop("departureUnknown", UNSET)

        port_call_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            mmsi=mmsi,
            port_number=port_number,
            arrival=arrival,
            confidence=confidence,
            evidence=evidence,
            port_name=port_name,
            departure=departure,
            duration_h=duration_h,
            departure_unknown=departure_unknown,
        )

        port_call_payload.additional_properties = d
        return port_call_payload

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
