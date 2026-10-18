from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.severity import Severity
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.analog_outcomes_hypothesis_outcome_type_0 import (
        AnalogOutcomesHypothesisOutcomeType0,
    )


T = TypeVar("T", bound="AnalogOutcomes")


@_attrs_define
class AnalogOutcomes:
    """What happened next

    Attributes:
        duration_days (float | None | Unset):
        peak_severity (None | Severity | Unset):
        follow_on_families_7_d (list[str] | Unset):
        follow_on_families_30_d (list[str] | Unset):
        hypothesis_outcome (AnalogOutcomesHypothesisOutcomeType0 | None | Unset):
        target_outcome_occurred (bool | None | Unset): Whether the requested outcome occurred
    """

    duration_days: float | None | Unset = UNSET
    peak_severity: None | Severity | Unset = UNSET
    follow_on_families_7_d: list[str] | Unset = UNSET
    follow_on_families_30_d: list[str] | Unset = UNSET
    hypothesis_outcome: AnalogOutcomesHypothesisOutcomeType0 | None | Unset = UNSET
    target_outcome_occurred: bool | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.analog_outcomes_hypothesis_outcome_type_0 import (
            AnalogOutcomesHypothesisOutcomeType0,
        )

        duration_days: float | None | Unset
        if isinstance(self.duration_days, Unset):
            duration_days = UNSET
        else:
            duration_days = self.duration_days

        peak_severity: None | str | Unset
        if isinstance(self.peak_severity, Unset):
            peak_severity = UNSET
        elif isinstance(self.peak_severity, Severity):
            peak_severity = self.peak_severity.value
        else:
            peak_severity = self.peak_severity

        follow_on_families_7_d: list[str] | Unset = UNSET
        if not isinstance(self.follow_on_families_7_d, Unset):
            follow_on_families_7_d = self.follow_on_families_7_d

        follow_on_families_30_d: list[str] | Unset = UNSET
        if not isinstance(self.follow_on_families_30_d, Unset):
            follow_on_families_30_d = self.follow_on_families_30_d

        hypothesis_outcome: dict[str, Any] | None | Unset
        if isinstance(self.hypothesis_outcome, Unset):
            hypothesis_outcome = UNSET
        elif isinstance(self.hypothesis_outcome, AnalogOutcomesHypothesisOutcomeType0):
            hypothesis_outcome = self.hypothesis_outcome.to_dict()
        else:
            hypothesis_outcome = self.hypothesis_outcome

        target_outcome_occurred: bool | None | Unset
        if isinstance(self.target_outcome_occurred, Unset):
            target_outcome_occurred = UNSET
        else:
            target_outcome_occurred = self.target_outcome_occurred

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if duration_days is not UNSET:
            field_dict["durationDays"] = duration_days
        if peak_severity is not UNSET:
            field_dict["peakSeverity"] = peak_severity
        if follow_on_families_7_d is not UNSET:
            field_dict["followOnFamilies7d"] = follow_on_families_7_d
        if follow_on_families_30_d is not UNSET:
            field_dict["followOnFamilies30d"] = follow_on_families_30_d
        if hypothesis_outcome is not UNSET:
            field_dict["hypothesisOutcome"] = hypothesis_outcome
        if target_outcome_occurred is not UNSET:
            field_dict["targetOutcomeOccurred"] = target_outcome_occurred

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.analog_outcomes_hypothesis_outcome_type_0 import (
            AnalogOutcomesHypothesisOutcomeType0,
        )

        d = dict(src_dict)

        def _parse_duration_days(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        duration_days = _parse_duration_days(d.pop("durationDays", UNSET))

        def _parse_peak_severity(data: object) -> None | Severity | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                peak_severity_type_0 = Severity(data)

                return peak_severity_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Severity | Unset, data)

        peak_severity = _parse_peak_severity(d.pop("peakSeverity", UNSET))

        follow_on_families_7_d = cast(list[str], d.pop("followOnFamilies7d", UNSET))

        follow_on_families_30_d = cast(list[str], d.pop("followOnFamilies30d", UNSET))

        def _parse_hypothesis_outcome(
            data: object,
        ) -> AnalogOutcomesHypothesisOutcomeType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                hypothesis_outcome_type_0 = (
                    AnalogOutcomesHypothesisOutcomeType0.from_dict(data)
                )

                return hypothesis_outcome_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AnalogOutcomesHypothesisOutcomeType0 | None | Unset, data)

        hypothesis_outcome = _parse_hypothesis_outcome(
            d.pop("hypothesisOutcome", UNSET)
        )

        def _parse_target_outcome_occurred(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        target_outcome_occurred = _parse_target_outcome_occurred(
            d.pop("targetOutcomeOccurred", UNSET)
        )

        analog_outcomes = cls(
            duration_days=duration_days,
            peak_severity=peak_severity,
            follow_on_families_7_d=follow_on_families_7_d,
            follow_on_families_30_d=follow_on_families_30_d,
            hypothesis_outcome=hypothesis_outcome,
            target_outcome_occurred=target_outcome_occurred,
        )

        analog_outcomes.additional_properties = d
        return analog_outcomes

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
