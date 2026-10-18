from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.confidence_level import ConfidenceLevel
from ..models.hypothesis_set_cadence import HypothesisSetCadence
from ..models.hypothesis_set_kind import HypothesisSetKind
from ..models.hypothesis_set_origin import HypothesisSetOrigin
from ..models.hypothesis_set_status import HypothesisSetStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.hypothesis import Hypothesis
    from ..models.indicator import Indicator
    from ..models.key_assumption import KeyAssumption
    from ..models.ledger_question_brief import LedgerQuestionBrief


T = TypeVar("T", bound="HypothesisSet")


@_attrs_define
class HypothesisSet:
    """A rolling hypothesis set: one key question and its competing answers

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        question (str):
        kind (HypothesisSetKind): `exclusive`: probabilities sum to 1 with exactly one `other` hypothesis
        cadence (HypothesisSetCadence):
        status (HypothesisSetStatus):
        origin (HypothesisSetOrigin):
        confidence (ConfidenceLevel): Analytic confidence in the evidence (separate from probability)
        private (bool):
        version (int): Optimistic lock for PATCH
        hypotheses (list[Hypothesis]):
        created_at (datetime.datetime):
        updated_at (datetime.datetime):
        description (None | str | Unset):
        horizon (datetime.date | None | Unset):
        situation_id (None | str | Unset):
        question_id (None | str | Unset):
        region_codes (list[str] | Unset): ISO 3166-1 alpha-2 country or ISO 3166-2 subdivision codes
        themes (list[str] | Unset):
        last_reviewed_at (datetime.datetime | None | Unset):
        next_review_at (datetime.datetime | None | Unset):
        last_revision_id (None | str | Unset):
        last_revision_at (datetime.datetime | None | Unset):
        indicators (list[Indicator] | Unset):
        assumptions (list[KeyAssumption] | Unset):
        linked_question (LedgerQuestionBrief | None | Unset):
        record_ref (None | str | Unset):
        review_flagged (bool | Unset): A broken linchpin assumption flagged the set for review; a review clears it
    """

    id: str
    question: str
    kind: HypothesisSetKind
    cadence: HypothesisSetCadence
    status: HypothesisSetStatus
    origin: HypothesisSetOrigin
    confidence: ConfidenceLevel
    private: bool
    version: int
    hypotheses: list[Hypothesis]
    created_at: datetime.datetime
    updated_at: datetime.datetime
    description: None | str | Unset = UNSET
    horizon: datetime.date | None | Unset = UNSET
    situation_id: None | str | Unset = UNSET
    question_id: None | str | Unset = UNSET
    region_codes: list[str] | Unset = UNSET
    themes: list[str] | Unset = UNSET
    last_reviewed_at: datetime.datetime | None | Unset = UNSET
    next_review_at: datetime.datetime | None | Unset = UNSET
    last_revision_id: None | str | Unset = UNSET
    last_revision_at: datetime.datetime | None | Unset = UNSET
    indicators: list[Indicator] | Unset = UNSET
    assumptions: list[KeyAssumption] | Unset = UNSET
    linked_question: LedgerQuestionBrief | None | Unset = UNSET
    record_ref: None | str | Unset = UNSET
    review_flagged: bool | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.ledger_question_brief import LedgerQuestionBrief

        id = self.id

        question = self.question

        kind = self.kind.value

        cadence = self.cadence.value

        status = self.status.value

        origin = self.origin.value

        confidence = self.confidence.value

        private = self.private

        version = self.version

        hypotheses = []
        for hypotheses_item_data in self.hypotheses:
            hypotheses_item = hypotheses_item_data.to_dict()
            hypotheses.append(hypotheses_item)

        created_at = self.created_at.isoformat()

        updated_at = self.updated_at.isoformat()

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

        last_reviewed_at: None | str | Unset
        if isinstance(self.last_reviewed_at, Unset):
            last_reviewed_at = UNSET
        elif isinstance(self.last_reviewed_at, datetime.datetime):
            last_reviewed_at = self.last_reviewed_at.isoformat()
        else:
            last_reviewed_at = self.last_reviewed_at

        next_review_at: None | str | Unset
        if isinstance(self.next_review_at, Unset):
            next_review_at = UNSET
        elif isinstance(self.next_review_at, datetime.datetime):
            next_review_at = self.next_review_at.isoformat()
        else:
            next_review_at = self.next_review_at

        last_revision_id: None | str | Unset
        if isinstance(self.last_revision_id, Unset):
            last_revision_id = UNSET
        else:
            last_revision_id = self.last_revision_id

        last_revision_at: None | str | Unset
        if isinstance(self.last_revision_at, Unset):
            last_revision_at = UNSET
        elif isinstance(self.last_revision_at, datetime.datetime):
            last_revision_at = self.last_revision_at.isoformat()
        else:
            last_revision_at = self.last_revision_at

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

        linked_question: dict[str, Any] | None | Unset
        if isinstance(self.linked_question, Unset):
            linked_question = UNSET
        elif isinstance(self.linked_question, LedgerQuestionBrief):
            linked_question = self.linked_question.to_dict()
        else:
            linked_question = self.linked_question

        record_ref: None | str | Unset
        if isinstance(self.record_ref, Unset):
            record_ref = UNSET
        else:
            record_ref = self.record_ref

        review_flagged = self.review_flagged

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "question": question,
                "kind": kind,
                "cadence": cadence,
                "status": status,
                "origin": origin,
                "confidence": confidence,
                "private": private,
                "version": version,
                "hypotheses": hypotheses,
                "createdAt": created_at,
                "updatedAt": updated_at,
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
        if last_reviewed_at is not UNSET:
            field_dict["lastReviewedAt"] = last_reviewed_at
        if next_review_at is not UNSET:
            field_dict["nextReviewAt"] = next_review_at
        if last_revision_id is not UNSET:
            field_dict["lastRevisionId"] = last_revision_id
        if last_revision_at is not UNSET:
            field_dict["lastRevisionAt"] = last_revision_at
        if indicators is not UNSET:
            field_dict["indicators"] = indicators
        if assumptions is not UNSET:
            field_dict["assumptions"] = assumptions
        if linked_question is not UNSET:
            field_dict["linkedQuestion"] = linked_question
        if record_ref is not UNSET:
            field_dict["recordRef"] = record_ref
        if review_flagged is not UNSET:
            field_dict["reviewFlagged"] = review_flagged

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.hypothesis import Hypothesis
        from ..models.indicator import Indicator
        from ..models.key_assumption import KeyAssumption
        from ..models.ledger_question_brief import LedgerQuestionBrief

        d = dict(src_dict)
        id = d.pop("id")

        question = d.pop("question")

        kind = HypothesisSetKind(d.pop("kind"))

        cadence = HypothesisSetCadence(d.pop("cadence"))

        status = HypothesisSetStatus(d.pop("status"))

        origin = HypothesisSetOrigin(d.pop("origin"))

        confidence = ConfidenceLevel(d.pop("confidence"))

        private = d.pop("private")

        version = d.pop("version")

        hypotheses = []
        _hypotheses = d.pop("hypotheses")
        for hypotheses_item_data in _hypotheses:
            hypotheses_item = Hypothesis.from_dict(hypotheses_item_data)

            hypotheses.append(hypotheses_item)

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        updated_at = datetime.datetime.fromisoformat(d.pop("updatedAt"))

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

        def _parse_last_reviewed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_reviewed_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_reviewed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_reviewed_at = _parse_last_reviewed_at(d.pop("lastReviewedAt", UNSET))

        def _parse_next_review_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                next_review_at_type_0 = datetime.datetime.fromisoformat(data)

                return next_review_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        next_review_at = _parse_next_review_at(d.pop("nextReviewAt", UNSET))

        def _parse_last_revision_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_revision_id = _parse_last_revision_id(d.pop("lastRevisionId", UNSET))

        def _parse_last_revision_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_revision_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_revision_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_revision_at = _parse_last_revision_at(d.pop("lastRevisionAt", UNSET))

        _indicators = d.pop("indicators", UNSET)
        indicators: list[Indicator] | Unset = UNSET
        if _indicators is not UNSET:
            indicators = []
            for indicators_item_data in _indicators:
                indicators_item = Indicator.from_dict(indicators_item_data)

                indicators.append(indicators_item)

        _assumptions = d.pop("assumptions", UNSET)
        assumptions: list[KeyAssumption] | Unset = UNSET
        if _assumptions is not UNSET:
            assumptions = []
            for assumptions_item_data in _assumptions:
                assumptions_item = KeyAssumption.from_dict(assumptions_item_data)

                assumptions.append(assumptions_item)

        def _parse_linked_question(data: object) -> LedgerQuestionBrief | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                linked_question_type_0 = LedgerQuestionBrief.from_dict(data)

                return linked_question_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(LedgerQuestionBrief | None | Unset, data)

        linked_question = _parse_linked_question(d.pop("linkedQuestion", UNSET))

        def _parse_record_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        record_ref = _parse_record_ref(d.pop("recordRef", UNSET))

        review_flagged = d.pop("reviewFlagged", UNSET)

        hypothesis_set = cls(
            id=id,
            question=question,
            kind=kind,
            cadence=cadence,
            status=status,
            origin=origin,
            confidence=confidence,
            private=private,
            version=version,
            hypotheses=hypotheses,
            created_at=created_at,
            updated_at=updated_at,
            description=description,
            horizon=horizon,
            situation_id=situation_id,
            question_id=question_id,
            region_codes=region_codes,
            themes=themes,
            last_reviewed_at=last_reviewed_at,
            next_review_at=next_review_at,
            last_revision_id=last_revision_id,
            last_revision_at=last_revision_at,
            indicators=indicators,
            assumptions=assumptions,
            linked_question=linked_question,
            record_ref=record_ref,
            review_flagged=review_flagged,
        )

        hypothesis_set.additional_properties = d
        return hypothesis_set

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
