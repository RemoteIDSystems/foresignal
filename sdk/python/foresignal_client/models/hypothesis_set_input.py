from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.hypothesis_set_input_cadence import HypothesisSetInputCadence
from ..models.hypothesis_set_input_kind import HypothesisSetInputKind
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis_input import HypothesisInput
    from ..models.hypothesis_set_input_ledger_question_type_0 import (
        HypothesisSetInputLedgerQuestionType0,
    )
    from ..models.indicator_input import IndicatorInput
    from ..models.key_assumption_input import KeyAssumptionInput


T = TypeVar("T", bound="HypothesisSetInput")


@_attrs_define
class HypothesisSetInput:
    """A hypothesis set to create, with hypotheses, indicators and assumptions

    Attributes:
        question (str):
        kind (HypothesisSetInputKind): `exclusive`: probabilities sum to 1 with exactly one `other` hypothesis
        cadence (HypothesisSetInputCadence):
        hypotheses (list[HypothesisInput]):
        description (None | str | Unset):
        horizon (datetime.date | None | Unset):
        situation_id (None | str | Unset):
        question_id (None | str | Unset):
        region_codes (list[str] | Unset): ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
        themes (list[str] | Unset):
        private (bool | Unset):  Default: False.
        indicators (list[IndicatorInput] | Unset):
        assumptions (list[KeyAssumptionInput] | Unset):
        ledger_question (HypothesisSetInputLedgerQuestionType0 | None | Unset):
        proposal_id (None | str | Unset):
    """

    question: str
    kind: HypothesisSetInputKind
    cadence: HypothesisSetInputCadence
    hypotheses: list[HypothesisInput]
    description: None | str | Unset = UNSET
    horizon: datetime.date | None | Unset = UNSET
    situation_id: None | str | Unset = UNSET
    question_id: None | str | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    themes: list[str] | Unset = UNSET
    private: bool | Unset = False
    indicators: list[IndicatorInput] | Unset = UNSET
    assumptions: list[KeyAssumptionInput] | Unset = UNSET
    ledger_question: HypothesisSetInputLedgerQuestionType0 | None | Unset = UNSET
    proposal_id: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.hypothesis_set_input_ledger_question_type_0 import (
            HypothesisSetInputLedgerQuestionType0,
        )

        question = self.question

        kind = self.kind.value

        cadence = self.cadence.value

        hypotheses = []
        for hypotheses_item_data in self.hypotheses:
            hypotheses_item = hypotheses_item_data.to_dict()
            hypotheses.append(hypotheses_item)

        description: None | str | Unset
        if isinstance(self.description, Unset):
            description = UNSET
        else:
            description = self.description

        horizon: None | str | Unset
        if isinstance(self.horizon, Unset):
            horizon = UNSET
        elif isinstance(self.horizon, datetime.date):
            horizon = self.horizon.isoformat()
        else:
            horizon = self.horizon

        situation_id: None | str | Unset
        if isinstance(self.situation_id, Unset):
            situation_id = UNSET
        else:
            situation_id = self.situation_id

        question_id: None | str | Unset
        if isinstance(self.question_id, Unset):
            question_id = UNSET
        else:
            question_id = self.question_id

        region_codes: list[str] | Unset = UNSET
        if not isinstance(self.region_codes, Unset):
            region_codes = self.region_codes

        themes: list[str] | Unset = UNSET
        if not isinstance(self.themes, Unset):
            themes = self.themes

        private = self.private

        indicators: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.indicators, Unset):
            indicators = []
            for indicators_item_data in self.indicators:
                indicators_item = indicators_item_data.to_dict()
                indicators.append(indicators_item)

        assumptions: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.assumptions, Unset):
            assumptions = []
            for assumptions_item_data in self.assumptions:
                assumptions_item = assumptions_item_data.to_dict()
                assumptions.append(assumptions_item)

        ledger_question: dict[str, Any] | None | Unset
        if isinstance(self.ledger_question, Unset):
            ledger_question = UNSET
        elif isinstance(self.ledger_question, HypothesisSetInputLedgerQuestionType0):
            ledger_question = self.ledger_question.to_dict()
        else:
            ledger_question = self.ledger_question

        proposal_id: None | str | Unset
        if isinstance(self.proposal_id, Unset):
            proposal_id = UNSET
        else:
            proposal_id = self.proposal_id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "question": question,
                "kind": kind,
                "cadence": cadence,
                "hypotheses": hypotheses,
            }
        )
        if description is not UNSET:
            field_dict["description"] = description
        if horizon is not UNSET:
            field_dict["horizon"] = horizon
        if situation_id is not UNSET:
            field_dict["situationId"] = situation_id
        if question_id is not UNSET:
            field_dict["questionId"] = question_id
        if region_codes is not UNSET:
            field_dict["regionCodes"] = region_codes
        if themes is not UNSET:
            field_dict["themes"] = themes
        if private is not UNSET:
            field_dict["private"] = private
        if indicators is not UNSET:
            field_dict["indicators"] = indicators
        if assumptions is not UNSET:
            field_dict["assumptions"] = assumptions
        if ledger_question is not UNSET:
            field_dict["ledgerQuestion"] = ledger_question
        if proposal_id is not UNSET:
            field_dict["proposalId"] = proposal_id

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis_input import HypothesisInput
        from ..models.hypothesis_set_input_ledger_question_type_0 import (
            HypothesisSetInputLedgerQuestionType0,
        )
        from ..models.indicator_input import IndicatorInput
        from ..models.key_assumption_input import KeyAssumptionInput

        d = dict(src_dict)
        question = d.pop("question")

        kind = HypothesisSetInputKind(d.pop("kind"))

        cadence = HypothesisSetInputCadence(d.pop("cadence"))

        hypotheses = []
        _hypotheses = d.pop("hypotheses")
        for hypotheses_item_data in _hypotheses:
            hypotheses_item = HypothesisInput.from_dict(hypotheses_item_data)

            hypotheses.append(hypotheses_item)

        def _parse_description(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        description = _parse_description(d.pop("description", UNSET))

        def _parse_horizon(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                horizon_type_0 = datetime.date.fromisoformat(data)

                return horizon_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        horizon = _parse_horizon(d.pop("horizon", UNSET))

        def _parse_situation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        situation_id = _parse_situation_id(d.pop("situationId", UNSET))

        def _parse_question_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        question_id = _parse_question_id(d.pop("questionId", UNSET))

        region_codes = cast(list[str], d.pop("regionCodes", UNSET))

        themes = cast(list[str], d.pop("themes", UNSET))

        private = d.pop("private", UNSET)

        _indicators = d.pop("indicators", UNSET)
        indicators: list[IndicatorInput] | Unset = UNSET
        if _indicators is not UNSET:
            indicators = []
            for indicators_item_data in _indicators:
                indicators_item = IndicatorInput.from_dict(indicators_item_data)

                indicators.append(indicators_item)

        _assumptions = d.pop("assumptions", UNSET)
        assumptions: list[KeyAssumptionInput] | Unset = UNSET
        if _assumptions is not UNSET:
            assumptions = []
            for assumptions_item_data in _assumptions:
                assumptions_item = KeyAssumptionInput.from_dict(assumptions_item_data)

                assumptions.append(assumptions_item)

        def _parse_ledger_question(
            data: object,
        ) -> HypothesisSetInputLedgerQuestionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                ledger_question_type_0 = (
                    HypothesisSetInputLedgerQuestionType0.from_dict(data)
                )

                return ledger_question_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(HypothesisSetInputLedgerQuestionType0 | None | Unset, data)

        ledger_question = _parse_ledger_question(d.pop("ledgerQuestion", UNSET))

        def _parse_proposal_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        proposal_id = _parse_proposal_id(d.pop("proposalId", UNSET))

        hypothesis_set_input = cls(
            question=question,
            kind=kind,
            cadence=cadence,
            hypotheses=hypotheses,
            description=description,
            horizon=horizon,
            situation_id=situation_id,
            question_id=question_id,
            region_codes=region_codes,
            themes=themes,
            private=private,
            indicators=indicators,
            assumptions=assumptions,
            ledger_question=ledger_question,
            proposal_id=proposal_id,
        )

        hypothesis_set_input.additional_properties = d
        return hypothesis_set_input

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
