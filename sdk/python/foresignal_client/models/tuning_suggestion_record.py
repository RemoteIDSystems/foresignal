from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.tuning_suggestion_kind import TuningSuggestionKind
from ..models.tuning_suggestion_record_status import TuningSuggestionRecordStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.tuning_suggestion_expected_effect_type_0 import (
        TuningSuggestionExpectedEffectType0,
    )
    from ..models.tuning_suggestion_patch import TuningSuggestionPatch


T = TypeVar("T", bound="TuningSuggestionRecord")


@_attrs_define
class TuningSuggestionRecord:
    """A persisted tuning suggestion with lifecycle (extends 103's TuningSuggestion) (115)

    Attributes:
        id (str):
        kind (TuningSuggestionKind):
        rationale (str):
        patch (TuningSuggestionPatch): Partial TriggerRuleInput ready to apply as an ordinary rule update
        rule_id (str):
        rule_rev (str): The rule's definition hash when the suggestion was generated
        status (TuningSuggestionRecordStatus):
        created_at (datetime.datetime):
        expected_effect (None | TuningSuggestionExpectedEffectType0 | Unset): From a backtest of the patched variant
            when simulated
        caveats (list[Caveat] | Unset):
        decided_by (None | str | Unset):
        decided_at (datetime.datetime | None | Unset):
        decision_reason (None | str | Unset):
        explanation (None | str | Unset): Plain-language summary from the chat role when explain=true and the caller is
            authenticated (never edits the patch)
    """

    id: str
    kind: TuningSuggestionKind
    rationale: str
    patch: TuningSuggestionPatch
    rule_id: str
    rule_rev: str
    status: TuningSuggestionRecordStatus
    created_at: datetime.datetime
    expected_effect: None | TuningSuggestionExpectedEffectType0 | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    decided_by: None | str | Unset = UNSET
    decided_at: datetime.datetime | None | Unset = UNSET
    decision_reason: None | str | Unset = UNSET
    explanation: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.tuning_suggestion_expected_effect_type_0 import (
            TuningSuggestionExpectedEffectType0,
        )

        id = self.id

        kind = self.kind.value

        rationale = self.rationale

        patch = self.patch.to_dict()

        rule_id = self.rule_id

        rule_rev = self.rule_rev

        status = self.status.value

        created_at = self.created_at.isoformat()

        expected_effect: dict[str, Any] | None | Unset
        if isinstance(self.expected_effect, Unset):
            expected_effect = UNSET
        elif isinstance(self.expected_effect, TuningSuggestionExpectedEffectType0):
            expected_effect = self.expected_effect.to_dict()
        else:
            expected_effect = self.expected_effect

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        decided_by: None | str | Unset
        if isinstance(self.decided_by, Unset):
            decided_by = UNSET
        else:
            decided_by = self.decided_by

        decided_at: None | str | Unset
        if isinstance(self.decided_at, Unset):
            decided_at = UNSET
        elif isinstance(self.decided_at, datetime.datetime):
            decided_at = self.decided_at.isoformat()
        else:
            decided_at = self.decided_at

        decision_reason: None | str | Unset
        if isinstance(self.decision_reason, Unset):
            decision_reason = UNSET
        else:
            decision_reason = self.decision_reason

        explanation: None | str | Unset
        if isinstance(self.explanation, Unset):
            explanation = UNSET
        else:
            explanation = self.explanation

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "kind": kind,
                "rationale": rationale,
                "patch": patch,
                "ruleId": rule_id,
                "ruleRev": rule_rev,
                "status": status,
                "createdAt": created_at,
            }
        )
        if expected_effect is not UNSET:
            field_dict["expectedEffect"] = expected_effect
        if caveats is not UNSET:
            field_dict["caveats"] = caveats
        if decided_by is not UNSET:
            field_dict["decidedBy"] = decided_by
        if decided_at is not UNSET:
            field_dict["decidedAt"] = decided_at
        if decision_reason is not UNSET:
            field_dict["decisionReason"] = decision_reason
        if explanation is not UNSET:
            field_dict["explanation"] = explanation

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.tuning_suggestion_expected_effect_type_0 import (
            TuningSuggestionExpectedEffectType0,
        )
        from ..models.tuning_suggestion_patch import TuningSuggestionPatch

        d = dict(src_dict)
        id = d.pop("id")

        kind = TuningSuggestionKind(d.pop("kind"))

        rationale = d.pop("rationale")

        patch = TuningSuggestionPatch.from_dict(d.pop("patch"))

        rule_id = d.pop("ruleId")

        rule_rev = d.pop("ruleRev")

        status = TuningSuggestionRecordStatus(d.pop("status"))

        created_at = datetime.datetime.fromisoformat(d.pop("createdAt"))

        def _parse_expected_effect(
            data: object,
        ) -> None | TuningSuggestionExpectedEffectType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                expected_effect_type_0 = TuningSuggestionExpectedEffectType0.from_dict(
                    data
                )

                return expected_effect_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | TuningSuggestionExpectedEffectType0 | Unset, data)

        expected_effect = _parse_expected_effect(d.pop("expectedEffect", UNSET))

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        def _parse_decided_by(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        decided_by = _parse_decided_by(d.pop("decidedBy", UNSET))

        def _parse_decided_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                decided_at_type_0 = datetime.datetime.fromisoformat(data)

                return decided_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        decided_at = _parse_decided_at(d.pop("decidedAt", UNSET))

        def _parse_decision_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        decision_reason = _parse_decision_reason(d.pop("decisionReason", UNSET))

        def _parse_explanation(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        explanation = _parse_explanation(d.pop("explanation", UNSET))

        tuning_suggestion_record = cls(
            id=id,
            kind=kind,
            rationale=rationale,
            patch=patch,
            rule_id=rule_id,
            rule_rev=rule_rev,
            status=status,
            created_at=created_at,
            expected_effect=expected_effect,
            caveats=caveats,
            decided_by=decided_by,
            decided_at=decided_at,
            decision_reason=decision_reason,
            explanation=explanation,
        )

        tuning_suggestion_record.additional_properties = d
        return tuning_suggestion_record

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
