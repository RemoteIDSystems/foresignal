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


T = TypeVar("T", bound="AisGapPayload")


@_attrs_define
class AisGapPayload:
    """Kind `ais-gap` (`derived-ais-gap`): a moving vessel stopped reporting while ≥ 3 nearby vessels from the same source
    kept reporting

        Attributes:
            automated (bool):
            not_a_warning (bool):
            detector (DerivedPayloadCommonDetector): Generator id and version
            status (DerivedPayloadCommonStatus):
            mmsi (str):
            gap_start (datetime.datetime):
            open_ (bool):
            confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
            evidence (list[DerivedEvidenceRef] | Unset):
            entity_id (None | str | Unset):
            last_sog (float | Unset):
            last_cog (float | Unset):
            gap_end (datetime.datetime | None | Unset):
            gap_minutes (float | None | Unset):
            displacement_km (float | None | Unset):
            implied_kn (float | None | Unset):
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    mmsi: str
    gap_start: datetime.datetime
    open_: bool
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    entity_id: None | str | Unset = UNSET
    last_sog: float | Unset = UNSET
    last_cog: float | Unset = UNSET
    gap_end: datetime.datetime | None | Unset = UNSET
    gap_minutes: float | None | Unset = UNSET
    displacement_km: float | None | Unset = UNSET
    implied_kn: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        mmsi = self.mmsi

        gap_start = self.gap_start.isoformat()

        open_ = self.open_

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        entity_id: None | str | Unset
        if isinstance(self.entity_id, Unset):
            entity_id = UNSET
        else:
            entity_id = self.entity_id

        last_sog = self.last_sog

        last_cog = self.last_cog

        gap_end: None | str | Unset
        if isinstance(self.gap_end, Unset):
            gap_end = UNSET
        elif isinstance(self.gap_end, datetime.datetime):
            gap_end = self.gap_end.isoformat()
        else:
            gap_end = self.gap_end

        gap_minutes: float | None | Unset
        if isinstance(self.gap_minutes, Unset):
            gap_minutes = UNSET
        else:
            gap_minutes = self.gap_minutes

        displacement_km: float | None | Unset
        if isinstance(self.displacement_km, Unset):
            displacement_km = UNSET
        else:
            displacement_km = self.displacement_km

        implied_kn: float | None | Unset
        if isinstance(self.implied_kn, Unset):
            implied_kn = UNSET
        else:
            implied_kn = self.implied_kn

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "mmsi": mmsi,
                "gapStart": gap_start,
                "open": open_,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if entity_id is not UNSET:
            field_dict["entityId"] = entity_id
        if last_sog is not UNSET:
            field_dict["lastSog"] = last_sog
        if last_cog is not UNSET:
            field_dict["lastCog"] = last_cog
        if gap_end is not UNSET:
            field_dict["gapEnd"] = gap_end
        if gap_minutes is not UNSET:
            field_dict["gapMinutes"] = gap_minutes
        if displacement_km is not UNSET:
            field_dict["displacementKm"] = displacement_km
        if implied_kn is not UNSET:
            field_dict["impliedKn"] = implied_kn

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

        gap_start = datetime.datetime.fromisoformat(d.pop("gapStart"))

        open_ = d.pop("open")

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        def _parse_entity_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        entity_id = _parse_entity_id(d.pop("entityId", UNSET))

        last_sog = d.pop("lastSog", UNSET)

        last_cog = d.pop("lastCog", UNSET)

        def _parse_gap_end(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                gap_end_type_0 = datetime.datetime.fromisoformat(data)

                return gap_end_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        gap_end = _parse_gap_end(d.pop("gapEnd", UNSET))

        def _parse_gap_minutes(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        gap_minutes = _parse_gap_minutes(d.pop("gapMinutes", UNSET))

        def _parse_displacement_km(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        displacement_km = _parse_displacement_km(d.pop("displacementKm", UNSET))

        def _parse_implied_kn(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        implied_kn = _parse_implied_kn(d.pop("impliedKn", UNSET))

        ais_gap_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            mmsi=mmsi,
            gap_start=gap_start,
            open_=open_,
            confidence=confidence,
            evidence=evidence,
            entity_id=entity_id,
            last_sog=last_sog,
            last_cog=last_cog,
            gap_end=gap_end,
            gap_minutes=gap_minutes,
            displacement_km=displacement_km,
            implied_kn=implied_kn,
        )

        ais_gap_payload.additional_properties = d
        return ais_gap_payload

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
