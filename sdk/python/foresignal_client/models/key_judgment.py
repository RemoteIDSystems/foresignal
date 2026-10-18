from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.confidence_level import ConfidenceLevel
from ..models.icd_term import IcdTerm
from ..models.key_judgment_text_origin import KeyJudgmentTextOrigin
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.key_judgment_change_type_0 import KeyJudgmentChangeType0


T = TypeVar("T", bound="KeyJudgment")


@_attrs_define
class KeyJudgment:
    """A typed, cited key judgment; terms are derived from the number

    Attributes:
        text (str):
        citations (list[str]):
        text_origin (KeyJudgmentTextOrigin):
        id (None | str | Unset):
        probability (float | None | Unset):
        estimative_term (IcdTerm | None | Unset):
        confidence (ConfidenceLevel | None | Unset):
        confidence_statement (None | str | Unset): Separate sentence; never in the same sentence as the likelihood term
        hypothesis_set_id (None | str | Unset):
        question_id (None | str | Unset):
        change (KeyJudgmentChangeType0 | None | Unset):
        downgraded (bool | Unset): Failed lint after one regeneration and was downgraded
        warnings (list[str] | Unset):
    """

    text: str
    citations: list[str]
    text_origin: KeyJudgmentTextOrigin
    id: None | str | Unset = UNSET
    probability: float | None | Unset = UNSET
    estimative_term: IcdTerm | None | Unset = UNSET
    confidence: ConfidenceLevel | None | Unset = UNSET
    confidence_statement: None | str | Unset = UNSET
    hypothesis_set_id: None | str | Unset = UNSET
    question_id: None | str | Unset = UNSET
    change: KeyJudgmentChangeType0 | None | Unset = UNSET
    downgraded: bool | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.key_judgment_change_type_0 import KeyJudgmentChangeType0

        text = self.text

        citations = self.citations

        text_origin = self.text_origin.value

        id: None | str | Unset
        if isinstance(self.id, Unset):
            id = UNSET
        else:
            id = self.id

        probability: float | None | Unset
        if isinstance(self.probability, Unset):
            probability = UNSET
        else:
            probability = self.probability

        estimative_term: None | str | Unset
        if isinstance(self.estimative_term, Unset):
            estimative_term = UNSET
        elif isinstance(self.estimative_term, IcdTerm):
            estimative_term = self.estimative_term.value
        else:
            estimative_term = self.estimative_term

        confidence: None | str | Unset
        if isinstance(self.confidence, Unset):
            confidence = UNSET
        elif isinstance(self.confidence, ConfidenceLevel):
            confidence = self.confidence.value
        else:
            confidence = self.confidence

        confidence_statement: None | str | Unset
        if isinstance(self.confidence_statement, Unset):
            confidence_statement = UNSET
        else:
            confidence_statement = self.confidence_statement

        hypothesis_set_id: None | str | Unset
        if isinstance(self.hypothesis_set_id, Unset):
            hypothesis_set_id = UNSET
        else:
            hypothesis_set_id = self.hypothesis_set_id

        question_id: None | str | Unset
        if isinstance(self.question_id, Unset):
            question_id = UNSET
        else:
            question_id = self.question_id

        change: dict[str, Any] | None | Unset
        if isinstance(self.change, Unset):
            change = UNSET
        elif isinstance(self.change, KeyJudgmentChangeType0):
            change = self.change.to_dict()
        else:
            change = self.change

        downgraded = self.downgraded

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "text": text,
                "citations": citations,
                "textOrigin": text_origin,
            }
        )
        if id is not UNSET:
            field_dict["id"] = id
        if probability is not UNSET:
            field_dict["probability"] = probability
        if estimative_term is not UNSET:
            field_dict["estimativeTerm"] = estimative_term
        if confidence is not UNSET:
            field_dict["confidence"] = confidence
        if confidence_statement is not UNSET:
            field_dict["confidenceStatement"] = confidence_statement
        if hypothesis_set_id is not UNSET:
            field_dict["hypothesisSetId"] = hypothesis_set_id
        if question_id is not UNSET:
            field_dict["questionId"] = question_id
        if change is not UNSET:
            field_dict["change"] = change
        if downgraded is not UNSET:
            field_dict["downgraded"] = downgraded
        if warnings is not UNSET:
            field_dict["warnings"] = warnings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.key_judgment_change_type_0 import KeyJudgmentChangeType0

        d = dict(src_dict)
        text = d.pop("text")

        citations = cast(list[str], d.pop("citations"))

        text_origin = KeyJudgmentTextOrigin(d.pop("textOrigin"))

        def _parse_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        id = _parse_id(d.pop("id", UNSET))

        def _parse_probability(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        probability = _parse_probability(d.pop("probability", UNSET))

        def _parse_estimative_term(data: object) -> IcdTerm | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                estimative_term_type_0 = IcdTerm(data)

                return estimative_term_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IcdTerm | None | Unset, data)

        estimative_term = _parse_estimative_term(d.pop("estimativeTerm", UNSET))

        def _parse_confidence(data: object) -> ConfidenceLevel | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                confidence_type_0 = ConfidenceLevel(data)

                return confidence_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ConfidenceLevel | None | Unset, data)

        confidence = _parse_confidence(d.pop("confidence", UNSET))

        def _parse_confidence_statement(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        confidence_statement = _parse_confidence_statement(
            d.pop("confidenceStatement", UNSET)
        )

        def _parse_hypothesis_set_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        hypothesis_set_id = _parse_hypothesis_set_id(d.pop("hypothesisSetId", UNSET))

        def _parse_question_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        question_id = _parse_question_id(d.pop("questionId", UNSET))

        def _parse_change(data: object) -> KeyJudgmentChangeType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                change_type_0 = KeyJudgmentChangeType0.from_dict(data)

                return change_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(KeyJudgmentChangeType0 | None | Unset, data)

        change = _parse_change(d.pop("change", UNSET))

        downgraded = d.pop("downgraded", UNSET)

        warnings = cast(list[str], d.pop("warnings", UNSET))

        key_judgment = cls(
            text=text,
            citations=citations,
            text_origin=text_origin,
            id=id,
            probability=probability,
            estimative_term=estimative_term,
            confidence=confidence,
            confidence_statement=confidence_statement,
            hypothesis_set_id=hypothesis_set_id,
            question_id=question_id,
            change=change,
            downgraded=downgraded,
            warnings=warnings,
        )

        key_judgment.additional_properties = d
        return key_judgment

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
