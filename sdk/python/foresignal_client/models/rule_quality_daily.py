from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.rule_quality_daily_feedback import RuleQualityDailyFeedback
    from ..models.rule_quality_daily_signals import RuleQualityDailySignals


T = TypeVar("T", bound="RuleQualityDaily")


@_attrs_define
class RuleQualityDaily:
    """Daily alert-quality trend row for a rule (retained 400 days) (115)

    Attributes:
        rule_id (str):
        day (datetime.date):
        fired (int):
        feedback (RuleQualityDailyFeedback | Unset): Counts per feedback value
        signals (RuleQualityDailySignals | Unset): Counts per implicit signal
        median_ack_seconds (float | None | Unset):
        flaps (int | Unset):
        suppressed_by_cooldown (int | Unset):
        suppressed_by_rate_cap (int | Unset):
    """

    rule_id: str
    day: datetime.date
    fired: int
    feedback: RuleQualityDailyFeedback | Unset = UNSET
    signals: RuleQualityDailySignals | Unset = UNSET
    median_ack_seconds: float | None | Unset = UNSET
    flaps: int | Unset = UNSET
    suppressed_by_cooldown: int | Unset = UNSET
    suppressed_by_rate_cap: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        rule_id = self.rule_id

        day = self.day.isoformat()

        fired = self.fired

        feedback: dict[str, Any] | Unset = UNSET
        if not isinstance(self.feedback, Unset):
            feedback = self.feedback.to_dict()

        signals: dict[str, Any] | Unset = UNSET
        if not isinstance(self.signals, Unset):
            signals = self.signals.to_dict()

        median_ack_seconds: float | None | Unset
        if isinstance(self.median_ack_seconds, Unset):
            median_ack_seconds = UNSET
        else:
            median_ack_seconds = self.median_ack_seconds

        flaps = self.flaps

        suppressed_by_cooldown = self.suppressed_by_cooldown

        suppressed_by_rate_cap = self.suppressed_by_rate_cap

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleId": rule_id,
                "day": day,
                "fired": fired,
            }
        )
        if feedback is not UNSET:
            field_dict["feedback"] = feedback
        if signals is not UNSET:
            field_dict["signals"] = signals
        if median_ack_seconds is not UNSET:
            field_dict["medianAckSeconds"] = median_ack_seconds
        if flaps is not UNSET:
            field_dict["flaps"] = flaps
        if suppressed_by_cooldown is not UNSET:
            field_dict["suppressedByCooldown"] = suppressed_by_cooldown
        if suppressed_by_rate_cap is not UNSET:
            field_dict["suppressedByRateCap"] = suppressed_by_rate_cap

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.rule_quality_daily_feedback import RuleQualityDailyFeedback
        from ..models.rule_quality_daily_signals import RuleQualityDailySignals

        d = dict(src_dict)
        rule_id = d.pop("ruleId")

        day = datetime.date.fromisoformat(d.pop("day"))

        fired = d.pop("fired")

        _feedback = d.pop("feedback", UNSET)
        feedback: RuleQualityDailyFeedback | Unset
        if isinstance(_feedback, Unset):
            feedback = UNSET
        else:
            feedback = RuleQualityDailyFeedback.from_dict(_feedback)

        _signals = d.pop("signals", UNSET)
        signals: RuleQualityDailySignals | Unset
        if isinstance(_signals, Unset):
            signals = UNSET
        else:
            signals = RuleQualityDailySignals.from_dict(_signals)

        def _parse_median_ack_seconds(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        median_ack_seconds = _parse_median_ack_seconds(d.pop("medianAckSeconds", UNSET))

        flaps = d.pop("flaps", UNSET)

        suppressed_by_cooldown = d.pop("suppressedByCooldown", UNSET)

        suppressed_by_rate_cap = d.pop("suppressedByRateCap", UNSET)

        rule_quality_daily = cls(
            rule_id=rule_id,
            day=day,
            fired=fired,
            feedback=feedback,
            signals=signals,
            median_ack_seconds=median_ack_seconds,
            flaps=flaps,
            suppressed_by_cooldown=suppressed_by_cooldown,
            suppressed_by_rate_cap=suppressed_by_rate_cap,
        )

        rule_quality_daily.additional_properties = d
        return rule_quality_daily

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
