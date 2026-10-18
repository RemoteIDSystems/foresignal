from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.benchmark_case_input_expectations_findings_item import (
        BenchmarkCaseInputExpectationsFindingsItem,
    )
    from ..models.benchmark_case_input_expectations_precursors_item import (
        BenchmarkCaseInputExpectationsPrecursorsItem,
    )
    from ..models.benchmark_case_input_expectations_questions_item import (
        BenchmarkCaseInputExpectationsQuestionsItem,
    )


T = TypeVar("T", bound="BenchmarkCaseInputExpectations")


@_attrs_define
class BenchmarkCaseInputExpectations:
    """Expected outcomes for a historical case

    Attributes:
        findings (list[BenchmarkCaseInputExpectationsFindingsItem] | Unset):
        precursors (list[BenchmarkCaseInputExpectationsPrecursorsItem] | Unset):
        questions (list[BenchmarkCaseInputExpectationsQuestionsItem] | Unset):
        notes (None | str | Unset):
    """

    findings: list[BenchmarkCaseInputExpectationsFindingsItem] | Unset = UNSET
    precursors: list[BenchmarkCaseInputExpectationsPrecursorsItem] | Unset = UNSET
    questions: list[BenchmarkCaseInputExpectationsQuestionsItem] | Unset = UNSET
    notes: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        findings: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.findings, Unset):
            findings = []
            for findings_item_data in self.findings:
                findings_item = findings_item_data.to_dict()
                findings.append(findings_item)

        precursors: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.precursors, Unset):
            precursors = []
            for precursors_item_data in self.precursors:
                precursors_item = precursors_item_data.to_dict()
                precursors.append(precursors_item)

        questions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.questions, Unset):
            questions = []
            for questions_item_data in self.questions:
                questions_item = questions_item_data.to_dict()
                questions.append(questions_item)

        notes: None | str | Unset
        if isinstance(self.notes, Unset):
            notes = UNSET
        else:
            notes = self.notes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if findings is not UNSET:
            field_dict["findings"] = findings
        if precursors is not UNSET:
            field_dict["precursors"] = precursors
        if questions is not UNSET:
            field_dict["questions"] = questions
        if notes is not UNSET:
            field_dict["notes"] = notes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.benchmark_case_input_expectations_findings_item import (
            BenchmarkCaseInputExpectationsFindingsItem,
        )
        from ..models.benchmark_case_input_expectations_precursors_item import (
            BenchmarkCaseInputExpectationsPrecursorsItem,
        )
        from ..models.benchmark_case_input_expectations_questions_item import (
            BenchmarkCaseInputExpectationsQuestionsItem,
        )

        d = dict(src_dict)
        _findings = d.pop("findings", UNSET)
        findings: list[BenchmarkCaseInputExpectationsFindingsItem] | Unset = UNSET
        if _findings is not UNSET:
            findings = []
            for findings_item_data in _findings:
                findings_item = BenchmarkCaseInputExpectationsFindingsItem.from_dict(
                    findings_item_data
                )

                findings.append(findings_item)

        _precursors = d.pop("precursors", UNSET)
        precursors: list[BenchmarkCaseInputExpectationsPrecursorsItem] | Unset = UNSET
        if _precursors is not UNSET:
            precursors = []
            for precursors_item_data in _precursors:
                precursors_item = (
                    BenchmarkCaseInputExpectationsPrecursorsItem.from_dict(
                        precursors_item_data
                    )
                )

                precursors.append(precursors_item)

        _questions = d.pop("questions", UNSET)
        questions: list[BenchmarkCaseInputExpectationsQuestionsItem] | Unset = UNSET
        if _questions is not UNSET:
            questions = []
            for questions_item_data in _questions:
                questions_item = BenchmarkCaseInputExpectationsQuestionsItem.from_dict(
                    questions_item_data
                )

                questions.append(questions_item)

        def _parse_notes(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        notes = _parse_notes(d.pop("notes", UNSET))

        benchmark_case_input_expectations = cls(
            findings=findings,
            precursors=precursors,
            questions=questions,
            notes=notes,
        )

        benchmark_case_input_expectations.additional_properties = d
        return benchmark_case_input_expectations

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
