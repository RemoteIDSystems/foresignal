from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ach_evidence import AchEvidence
    from ..models.ach_matrix_hypotheses_item import AchMatrixHypothesesItem
    from ..models.ach_matrix_scores import AchMatrixScores


T = TypeVar("T", bound="AchMatrix")


@_attrs_define
class AchMatrix:
    """Audit view of evidence × hypotheses with weighted inconsistency scores

    Attributes:
        set_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        computed_at (datetime.datetime):
        hypotheses (list[AchMatrixHypothesesItem]):
        rows (list[AchEvidence]):
        scores (AchMatrixScores): Inconsistency score per hypothesis id
        least_inconsistent_hypothesis_id (None | str | Unset):
        non_diagnostic_evidence_ids (list[str] | Unset):
        note (str | Unset):  Default: 'Audit view: probabilities come from the updater and are scored in the ledger'.
    """

    set_id: str
    computed_at: datetime.datetime
    hypotheses: list[AchMatrixHypothesesItem]
    rows: list[AchEvidence]
    scores: AchMatrixScores
    least_inconsistent_hypothesis_id: None | str | Unset = UNSET
    non_diagnostic_evidence_ids: list[str] | Unset = UNSET
    note: str | Unset = (
        "Audit view: probabilities come from the updater and are scored in the ledger"
    )
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        set_id = self.set_id

        computed_at = self.computed_at.isoformat()

        hypotheses = []
        for hypotheses_item_data in self.hypotheses:
            hypotheses_item = hypotheses_item_data.to_dict()
            hypotheses.append(hypotheses_item)

        rows = []
        for rows_item_data in self.rows:
            rows_item = rows_item_data.to_dict()
            rows.append(rows_item)

        scores = self.scores.to_dict()

        least_inconsistent_hypothesis_id: None | str | Unset
        if isinstance(self.least_inconsistent_hypothesis_id, Unset):
            least_inconsistent_hypothesis_id = UNSET
        else:
            least_inconsistent_hypothesis_id = self.least_inconsistent_hypothesis_id

        non_diagnostic_evidence_ids: list[str] | Unset = UNSET
        if not isinstance(self.non_diagnostic_evidence_ids, Unset):
            non_diagnostic_evidence_ids = self.non_diagnostic_evidence_ids

        note = self.note

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "setId": set_id,
                "computedAt": computed_at,
                "hypotheses": hypotheses,
                "rows": rows,
                "scores": scores,
            }
        )
        if least_inconsistent_hypothesis_id is not UNSET:
            field_dict["leastInconsistentHypothesisId"] = (
                least_inconsistent_hypothesis_id
            )
        if non_diagnostic_evidence_ids is not UNSET:
            field_dict["nonDiagnosticEvidenceIds"] = non_diagnostic_evidence_ids
        if note is not UNSET:
            field_dict["note"] = note

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ach_evidence import AchEvidence
        from ..models.ach_matrix_hypotheses_item import AchMatrixHypothesesItem
        from ..models.ach_matrix_scores import AchMatrixScores

        d = dict(src_dict)
        set_id = d.pop("setId")

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        hypotheses = []
        _hypotheses = d.pop("hypotheses")
        for hypotheses_item_data in _hypotheses:
            hypotheses_item = AchMatrixHypothesesItem.from_dict(hypotheses_item_data)

            hypotheses.append(hypotheses_item)

        rows = []
        _rows = d.pop("rows")
        for rows_item_data in _rows:
            rows_item = AchEvidence.from_dict(rows_item_data)

            rows.append(rows_item)

        scores = AchMatrixScores.from_dict(d.pop("scores"))

        def _parse_least_inconsistent_hypothesis_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        least_inconsistent_hypothesis_id = _parse_least_inconsistent_hypothesis_id(
            d.pop("leastInconsistentHypothesisId", UNSET)
        )

        non_diagnostic_evidence_ids = cast(
            list[str], d.pop("nonDiagnosticEvidenceIds", UNSET)
        )

        note = d.pop("note", UNSET)

        ach_matrix = cls(
            set_id=set_id,
            computed_at=computed_at,
            hypotheses=hypotheses,
            rows=rows,
            scores=scores,
            least_inconsistent_hypothesis_id=least_inconsistent_hypothesis_id,
            non_diagnostic_evidence_ids=non_diagnostic_evidence_ids,
            note=note,
        )

        ach_matrix.additional_properties = d
        return ach_matrix

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
