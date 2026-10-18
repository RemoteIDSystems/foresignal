from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analytic_finding_finding_type import AnalyticFindingFindingType
from ..models.analytic_finding_status import AnalyticFindingStatus
from ..models.finding_severity import FindingSeverity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analytic_finding_dismissal_type_0 import AnalyticFindingDismissalType0
    from ..models.analytic_finding_evidence import AnalyticFindingEvidence
    from ..models.analytic_finding_inference import AnalyticFindingInference
    from ..models.caveat import Caveat
    from ..models.geometry import Geometry


T = TypeVar("T", bound="AnalyticFinding")


@_attrs_define
class AnalyticFinding:
    """A discovery output (`analytic_finding`): evidence, inference with base rate, and caveats; also a `foresignal-
    analytics` record

        Attributes:
            id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            finding_type (AnalyticFindingFindingType):
            title (str):
            severity (FindingSeverity):
            status (AnalyticFindingStatus):
            detected_at (datetime.datetime):
            region_codes (list[str]):
            private (bool):
            manifest_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
            evidence (AnalyticFindingEvidence): Evidence: the series and pinned members the finding rests on
            inference (AnalyticFindingInference): Inference by a named, versioned detector
            caveats (list[Caveat]): 103 codes plus `LAGGED_PUBLICATION`, `FEW_EVENTS`, `MULTIPLE_TESTS`,
                `LOW_REPORTING_DENSITY`, `DAILY_RESOLUTION_ONLY`, `LOW_DETECTABILITY`
            window_start (datetime.datetime | None | Unset):
            window_end (datetime.datetime | None | Unset):
            geometry (Geometry | None | Unset):
            situation_id (None | str | Unset):
            precursor_id (None | str | Unset):
            question_ids (list[str] | Unset):
            superseded_by (None | str | Unset):
            dismissal (AnalyticFindingDismissalType0 | None | Unset):
            as_of_seq (int | Unset): Seq the detector read up to
            created_at (datetime.datetime | Unset):
            updated_at (datetime.datetime | Unset):
    """

    id: str
    finding_type: AnalyticFindingFindingType
    title: str
    severity: FindingSeverity
    status: AnalyticFindingStatus
    detected_at: datetime.datetime
    region_codes: list[str]
    private: bool
    manifest_id: str
    evidence: AnalyticFindingEvidence
    inference: AnalyticFindingInference
    caveats: list[Caveat]
    window_start: datetime.datetime | None | Unset = UNSET
    window_end: datetime.datetime | None | Unset = UNSET
    geometry: Geometry | None | Unset = UNSET
    situation_id: None | str | Unset = UNSET
    precursor_id: None | str | Unset = UNSET
    question_ids: list[str] | Unset = UNSET
    superseded_by: None | str | Unset = UNSET
    dismissal: AnalyticFindingDismissalType0 | None | Unset = UNSET
    as_of_seq: int | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.analytic_finding_dismissal_type_0 import (
            AnalyticFindingDismissalType0,
        )
        from ..models.geometry import Geometry

        id = self.id

        finding_type = self.finding_type.value

        title = self.title

        severity = self.severity.value

        status = self.status.value

        detected_at = self.detected_at.isoformat()

        region_codes = self.region_codes

        private = self.private

        manifest_id = self.manifest_id

        evidence = self.evidence.to_dict()

        inference = self.inference.to_dict()

        caveats = []
        for caveats_item_data in self.caveats:
            caveats_item = caveats_item_data.to_dict()
            caveats.append(caveats_item)

        window_start: None | str | Unset
        if isinstance(self.window_start, Unset):
            window_start = UNSET
        elif isinstance(self.window_start, datetime.datetime):
            window_start = self.window_start.isoformat()
        else:
            window_start = self.window_start

        window_end: None | str | Unset
        if isinstance(self.window_end, Unset):
            window_end = UNSET
        elif isinstance(self.window_end, datetime.datetime):
            window_end = self.window_end.isoformat()
        else:
            window_end = self.window_end

        geometry: dict[str, Any] | None | Unset
        if isinstance(self.geometry, Unset):
            geometry = UNSET
        elif isinstance(self.geometry, Geometry):
            geometry = self.geometry.to_dict()
        else:
            geometry = self.geometry

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        precursor_id: None | str | Unset
        if isinstance(self.precursor_id, Unset):
            precursor_id = UNSET
        else:
            precursor_id = self.precursor_id

        question_ids: list[str] | Unset = UNSET
        if not isinstance(self.question_ids, Unset):
            question_ids = self.question_ids

        superseded_by: None | str | Unset
        if isinstance(self.superseded_by, Unset):
            superseded_by = UNSET
        else:
            superseded_by = self.superseded_by

        dismissal: dict[str, Any] | None | Unset
        if isinstance(self.dismissal, Unset):
            dismissal = UNSET
        elif isinstance(self.dismissal, AnalyticFindingDismissalType0):
            dismissal = self.dismissal.to_dict()
        else:
            dismissal = self.dismissal

        as_of_seq = self.as_of_seq

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "findingType": finding_type,
                "title": title,
                "severity": severity,
                "status": status,
                "detectedAt": detected_at,
                "regionCodes": region_codes,
                "private": private,
                "manifestId": manifest_id,
                "evidence": evidence,
                "inference": inference,
                "caveats": caveats,
            }
        )
        if window_start is not UNSET:
            field_dict["windowStart"] = window_start
        if window_end is not UNSET:
            field_dict["windowEnd"] = window_end
        if geometry is not UNSET:
            field_dict["geometry"] = geometry
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if precursor_id is not UNSET:
            field_dict["precursorId"] = precursor_id
        if question_ids is not UNSET:
            field_dict["questionIds"] = question_ids
        if superseded_by is not UNSET:
            field_dict["supersededBy"] = superseded_by
        if dismissal is not UNSET:
            field_dict["dismissal"] = dismissal
        if as_of_seq is not UNSET:
            field_dict["asOfSeq"] = as_of_seq
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analytic_finding_dismissal_type_0 import (
            AnalyticFindingDismissalType0,
        )
        from ..models.analytic_finding_evidence import AnalyticFindingEvidence
        from ..models.analytic_finding_inference import AnalyticFindingInference
        from ..models.caveat import Caveat
        from ..models.geometry import Geometry

        d = dict(src_dict)
        id = d.pop("id")

        finding_type = AnalyticFindingFindingType(d.pop("findingType"))

        title = d.pop("title")

        severity = FindingSeverity(d.pop("severity"))

        status = AnalyticFindingStatus(d.pop("status"))

        detected_at = datetime.datetime.fromisoformat(d.pop("detectedAt"))

        region_codes = cast(list[str], d.pop("regionCodes"))

        private = d.pop("private")

        manifest_id = d.pop("manifestId")

        evidence = AnalyticFindingEvidence.from_dict(d.pop("evidence"))

        inference = AnalyticFindingInference.from_dict(d.pop("inference"))

        caveats = []
        _caveats = d.pop("caveats")
        for caveats_item_data in _caveats:
            caveats_item = Caveat.from_dict(caveats_item_data)

            caveats.append(caveats_item)

        def _parse_window_start(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                window_start_type_0 = datetime.datetime.fromisoformat(data)

                return window_start_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        window_start = _parse_window_start(d.pop("windowStart", UNSET))

        def _parse_window_end(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                window_end_type_0 = datetime.datetime.fromisoformat(data)

                return window_end_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        window_end = _parse_window_end(d.pop("windowEnd", UNSET))

        def _parse_geometry(data: object) -> Geometry | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                geometry_type_0 = Geometry.from_dict(data)

                return geometry_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(Geometry | None | Unset, data)

        geometry = _parse_geometry(d.pop("geometry", UNSET))

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        def _parse_precursor_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        precursor_id = _parse_precursor_id(d.pop("precursorId", UNSET))

        question_ids = cast(list[str], d.pop("questionIds", UNSET))

        def _parse_superseded_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        superseded_by = _parse_superseded_by(d.pop("supersededBy", UNSET))

        def _parse_dismissal(
            data: object,
        ) -> AnalyticFindingDismissalType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                dismissal_type_0 = AnalyticFindingDismissalType0.from_dict(data)

                return dismissal_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AnalyticFindingDismissalType0 | None | Unset, data)

        dismissal = _parse_dismissal(d.pop("dismissal", UNSET))

        as_of_seq = d.pop("asOfSeq", UNSET)

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        analytic_finding = cls(
            id=id,
            finding_type=finding_type,
            title=title,
            severity=severity,
            status=status,
            detected_at=detected_at,
            region_codes=region_codes,
            private=private,
            manifest_id=manifest_id,
            evidence=evidence,
            inference=inference,
            caveats=caveats,
            window_start=window_start,
            window_end=window_end,
            geometry=geometry,
            situation_id=situation_id,
            precursor_id=precursor_id,
            question_ids=question_ids,
            superseded_by=superseded_by,
            dismissal=dismissal,
            as_of_seq=as_of_seq,
            created_at=created_at,
            updated_at=updated_at,
        )

        analytic_finding.additional_properties = d
        return analytic_finding

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
