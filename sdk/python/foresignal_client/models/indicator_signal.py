from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.indicator_signal_type import IndicatorSignalType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.indicator_signal_emerging_type_0 import IndicatorSignalEmergingType0
    from ..models.indicator_signal_intel_event_type_0 import (
        IndicatorSignalIntelEventType0,
    )
    from ..models.indicator_signal_question_type_0 import IndicatorSignalQuestionType0
    from ..models.indicator_signal_series_type_0 import IndicatorSignalSeriesType0
    from ..models.indicator_signal_situation_type_0 import IndicatorSignalSituationType0
    from ..models.trigger_rule_input import TriggerRuleInput


T = TypeVar("T", bound="IndicatorSignal")


@_attrs_define
class IndicatorSignal:
    """What the indicator watches; `type` selects the populated member

    Attributes:
        type_ (IndicatorSignalType):
        rule (None | TriggerRuleInput | Unset):
        precursor_id (None | str | Unset): 120 precursor id (subscribes to `precursor-active` findings)
        series (IndicatorSignalSeriesType0 | None | Unset):
        intel_event (IndicatorSignalIntelEventType0 | None | Unset):
        situation (IndicatorSignalSituationType0 | None | Unset):
        question (IndicatorSignalQuestionType0 | None | Unset):
        emerging (IndicatorSignalEmergingType0 | None | Unset):
        base_rate (float | None | Unset): Base rate of the signal when the hypotheses say nothing about it, used for
            information value (default 0.5)
    """

    type_: IndicatorSignalType
    rule: None | TriggerRuleInput | Unset = UNSET
    precursor_id: None | str | Unset = UNSET
    series: IndicatorSignalSeriesType0 | None | Unset = UNSET
    intel_event: IndicatorSignalIntelEventType0 | None | Unset = UNSET
    situation: IndicatorSignalSituationType0 | None | Unset = UNSET
    question: IndicatorSignalQuestionType0 | None | Unset = UNSET
    emerging: IndicatorSignalEmergingType0 | None | Unset = UNSET
    base_rate: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.indicator_signal_emerging_type_0 import (
            IndicatorSignalEmergingType0,
        )
        from ..models.indicator_signal_intel_event_type_0 import (
            IndicatorSignalIntelEventType0,
        )
        from ..models.indicator_signal_question_type_0 import (
            IndicatorSignalQuestionType0,
        )
        from ..models.indicator_signal_series_type_0 import IndicatorSignalSeriesType0
        from ..models.indicator_signal_situation_type_0 import (
            IndicatorSignalSituationType0,
        )
        from ..models.trigger_rule_input import TriggerRuleInput

        type_ = self.type_.value

        rule: dict[str, Any] | None | Unset
        if isinstance(self.rule, Unset):
            rule = UNSET
        elif isinstance(self.rule, TriggerRuleInput):
            rule = self.rule.to_dict()
        else:
            rule = self.rule

        precursor_id: None | str | Unset
        if isinstance(self.precursor_id, Unset):
            precursor_id = UNSET
        else:
            precursor_id = self.precursor_id

        series: dict[str, Any] | None | Unset
        if isinstance(self.series, Unset):
            series = UNSET
        elif isinstance(self.series, IndicatorSignalSeriesType0):
            series = self.series.to_dict()
        else:
            series = self.series

        intel_event: dict[str, Any] | None | Unset
        if isinstance(self.intel_event, Unset):
            intel_event = UNSET
        elif isinstance(self.intel_event, IndicatorSignalIntelEventType0):
            intel_event = self.intel_event.to_dict()
        else:
            intel_event = self.intel_event

        situation: dict[str, Any] | None | Unset
        if isinstance(self.situation, Unset):
            situation = UNSET
        elif isinstance(self.situation, IndicatorSignalSituationType0):
            situation = self.situation.to_dict()
        else:
            situation = self.situation

        question: dict[str, Any] | None | Unset
        if isinstance(self.question, Unset):
            question = UNSET
        elif isinstance(self.question, IndicatorSignalQuestionType0):
            question = self.question.to_dict()
        else:
            question = self.question

        emerging: dict[str, Any] | None | Unset
        if isinstance(self.emerging, Unset):
            emerging = UNSET
        elif isinstance(self.emerging, IndicatorSignalEmergingType0):
            emerging = self.emerging.to_dict()
        else:
            emerging = self.emerging

        base_rate: float | None | Unset
        if isinstance(self.base_rate, Unset):
            base_rate = UNSET
        else:
            base_rate = self.base_rate

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if rule is not UNSET:
            field_dict["rule"] = rule
        if precursor_id is not UNSET:
            field_dict["precursorId"] = precursor_id
        if series is not UNSET:
            field_dict["series"] = series
        if intel_event is not UNSET:
            field_dict["intelEvent"] = intel_event
        if situation is not UNSET:
            field_dict["situation"] = situation
        if question is not UNSET:
            field_dict["question"] = question
        if emerging is not UNSET:
            field_dict["emerging"] = emerging
        if base_rate is not UNSET:
            field_dict["baseRate"] = base_rate

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.indicator_signal_emerging_type_0 import (
            IndicatorSignalEmergingType0,
        )
        from ..models.indicator_signal_intel_event_type_0 import (
            IndicatorSignalIntelEventType0,
        )
        from ..models.indicator_signal_question_type_0 import (
            IndicatorSignalQuestionType0,
        )
        from ..models.indicator_signal_series_type_0 import IndicatorSignalSeriesType0
        from ..models.indicator_signal_situation_type_0 import (
            IndicatorSignalSituationType0,
        )
        from ..models.trigger_rule_input import TriggerRuleInput

        d = dict(src_dict)
        type_ = IndicatorSignalType(d.pop("type"))

        def _parse_rule(data: object) -> None | TriggerRuleInput | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                rule_type_0 = TriggerRuleInput.from_dict(data)

                return rule_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | TriggerRuleInput | Unset, data)

        rule = _parse_rule(d.pop("rule", UNSET))

        def _parse_precursor_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        precursor_id = _parse_precursor_id(d.pop("precursorId", UNSET))

        def _parse_series(data: object) -> IndicatorSignalSeriesType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                series_type_0 = IndicatorSignalSeriesType0.from_dict(data)

                return series_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndicatorSignalSeriesType0 | None | Unset, data)

        series = _parse_series(d.pop("series", UNSET))

        def _parse_intel_event(
            data: object,
        ) -> IndicatorSignalIntelEventType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                intel_event_type_0 = IndicatorSignalIntelEventType0.from_dict(data)

                return intel_event_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndicatorSignalIntelEventType0 | None | Unset, data)

        intel_event = _parse_intel_event(d.pop("intelEvent", UNSET))

        def _parse_situation(
            data: object,
        ) -> IndicatorSignalSituationType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                situation_type_0 = IndicatorSignalSituationType0.from_dict(data)

                return situation_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndicatorSignalSituationType0 | None | Unset, data)

        situation = _parse_situation(d.pop("situation", UNSET))

        def _parse_question(
            data: object,
        ) -> IndicatorSignalQuestionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                question_type_0 = IndicatorSignalQuestionType0.from_dict(data)

                return question_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndicatorSignalQuestionType0 | None | Unset, data)

        question = _parse_question(d.pop("question", UNSET))

        def _parse_emerging(
            data: object,
        ) -> IndicatorSignalEmergingType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                emerging_type_0 = IndicatorSignalEmergingType0.from_dict(data)

                return emerging_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(IndicatorSignalEmergingType0 | None | Unset, data)

        emerging = _parse_emerging(d.pop("emerging", UNSET))

        def _parse_base_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        base_rate = _parse_base_rate(d.pop("baseRate", UNSET))

        indicator_signal = cls(
            type_=type_,
            rule=rule,
            precursor_id=precursor_id,
            series=series,
            intel_event=intel_event,
            situation=situation,
            question=question,
            emerging=emerging,
            base_rate=base_rate,
        )

        indicator_signal.additional_properties = d
        return indicator_signal

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
