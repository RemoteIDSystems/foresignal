from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.derived_payload_common_status import DerivedPayloadCommonStatus
from ..models.jamming_cell_payload_grid import JammingCellPayloadGrid
from ..models.jamming_cell_payload_level import JammingCellPayloadLevel
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.derived_evidence_ref import DerivedEvidenceRef
    from ..models.derived_payload_common_detector import DerivedPayloadCommonDetector


T = TypeVar("T", bound="JammingCellPayload")


@_attrs_define
class JammingCellPayload:
    """Kind `jamming-cell` (`derived-gps-jamming`): share of ADS-B aircraft with degraded NACp/NIC in a cell over a 15-min
    window

        Attributes:
            automated (bool):
            not_a_warning (bool):
            detector (DerivedPayloadCommonDetector): Generator id and version
            status (DerivedPayloadCommonStatus):
            level (JammingCellPayloadLevel):
            cell (str):  Example: h3:842a107ffffffff.
            confidence (float | Unset): Banded to 0.1; a score, not a calibrated probability
            evidence (list[DerivedEvidenceRef] | Unset):
            grid (JammingCellPayloadGrid | Unset):
            aircraft (int | Unset): Distinct airborne ADS-B aircraft in the window
            bad_aircraft (int | Unset): Aircraft with NACp < 8 or NIC < 7
            ratio (float | Unset):
            window_minutes (int | Unset):  Default: 15.
    """

    automated: bool
    not_a_warning: bool
    detector: DerivedPayloadCommonDetector
    status: DerivedPayloadCommonStatus
    level: JammingCellPayloadLevel
    cell: str
    confidence: float | Unset = UNSET
    evidence: list[DerivedEvidenceRef] | Unset = UNSET
    grid: JammingCellPayloadGrid | Unset = UNSET
    aircraft: int | Unset = UNSET
    bad_aircraft: int | Unset = UNSET
    ratio: float | Unset = UNSET
    window_minutes: int | Unset = 15
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        automated = self.automated

        not_a_warning = self.not_a_warning

        detector = self.detector.to_dict()

        status = self.status.value

        level = self.level.value

        cell = self.cell

        confidence = self.confidence

        evidence: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.evidence, Unset):
            evidence = []
            for evidence_item_data in self.evidence:
                evidence_item = evidence_item_data.to_dict()
                evidence.append(evidence_item)

        grid: str | Unset = UNSET
        if not isinstance(self.grid, Unset):
            grid = self.grid.value

        aircraft = self.aircraft

        bad_aircraft = self.bad_aircraft

        ratio = self.ratio

        window_minutes = self.window_minutes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "automated": automated,
                "notAWarning": not_a_warning,
                "detector": detector,
                "status": status,
                "level": level,
                "cell": cell,
            }
        )
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if evidence is not UNSET:
            field_dict["evidence"] = evidence
        if grid is not UNSET:
            field_dict["grid"] = grid
        if aircraft is not UNSET:
            field_dict["aircraft"] = aircraft
        if bad_aircraft is not UNSET:
            field_dict["badAircraft"] = bad_aircraft
        if ratio is not UNSET:
            field_dict["ratio"] = ratio
        if window_minutes is not UNSET:
            field_dict["windowMinutes"] = window_minutes

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

        level = JammingCellPayloadLevel(d.pop("level"))

        cell = d.pop("cell")

        confidence = d.pop("confidence", UNSET)

        _evidence = d.pop("evidence", UNSET)
        evidence: list[DerivedEvidenceRef] | Unset = UNSET
        if _evidence is not UNSET:
            evidence = []
            for evidence_item_data in _evidence:
                evidence_item = DerivedEvidenceRef.from_dict(evidence_item_data)

                evidence.append(evidence_item)

        _grid = d.pop("grid", UNSET)
        grid: JammingCellPayloadGrid | Unset
        if isinstance(_grid, Unset):
            grid = UNSET
        else:
            grid = JammingCellPayloadGrid(_grid)

        aircraft = d.pop("aircraft", UNSET)

        bad_aircraft = d.pop("badAircraft", UNSET)

        ratio = d.pop("ratio", UNSET)

        window_minutes = d.pop("windowMinutes", UNSET)

        jamming_cell_payload = cls(
            automated=automated,
            not_a_warning=not_a_warning,
            detector=detector,
            status=status,
            level=level,
            cell=cell,
            confidence=confidence,
            evidence=evidence,
            grid=grid,
            aircraft=aircraft,
            bad_aircraft=bad_aircraft,
            ratio=ratio,
            window_minutes=window_minutes,
        )

        jamming_cell_payload.additional_properties = d
        return jamming_cell_payload

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
