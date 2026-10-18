from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.caveat import Caveat
    from ..models.rule_quality_daily import RuleQualityDaily
    from ..models.rule_quality_feedback_mapping import RuleQualityFeedbackMapping
    from ..models.rule_quality_precision_ci_type_0 import RuleQualityPrecisionCiType0
    from ..models.rule_quality_severity_distribution import (
        RuleQualitySeverityDistribution,
    )
    from ..models.rule_quality_signal_weights import RuleQualitySignalWeights
    from ..models.rule_quality_signals import RuleQualitySignals


T = TypeVar("T", bound="RuleQuality")


@_attrs_define
class RuleQuality:
    """Alert-quality statistics for one rule from 017 feedback (useful → positive; noise, false_positive → negative; late →
    neutral, per the code mapping)

        Attributes:
            rule_id (UUID):
            since (datetime.datetime):
            alerts (int):
            labelled (int):
            positive (int):
            negative (int):
            neutral (int):
            labelled_share (float | Unset):
            precision (float | None | Unset): positive ÷ (positive + negative); null when labelled < 5
            precision_ci (None | RuleQualityPrecisionCiType0 | Unset):
            median_time_to_ack (None | str | Unset): ISO-8601 duration
            median_time_to_resolve (None | str | Unset): ISO-8601 duration
            flapping (int | Unset): enter → exit within 10 min
            duplicates (int | Unset): Same entity within 2 × cooldown
            severity_distribution (RuleQualitySeverityDistribution | Unset):
            feedback_mapping (RuleQualityFeedbackMapping | Unset):
            caveats (list[Caveat] | Unset):
            signals (RuleQualitySignals | Unset): Implicit signal counts (exercise alerts excluded)
            signal_score (float | None | Unset): Weighted implicit-signal score (ACK_FAST +0.5, SNOOZED −0.5, FLAP −0.5,
                UNOPENED −0.25), shown next to, never mixed into, feedback precision
            daily (list[RuleQualityDaily] | Unset): Daily trend rows since `since`
            signal_weights (RuleQualitySignalWeights | Unset): The weight table of signalScore (ACK_FAST 0.5, SNOOZED -0.5,
                FLAP -0.5, UNOPENED -0.25); signalScore = sum(weight x count) / alerts
    """

    rule_id: UUID
    since: datetime.datetime
    alerts: int
    labelled: int
    positive: int
    negative: int
    neutral: int
    labelled_share: float | Unset = UNSET
    precision: float | None | Unset = UNSET
    precision_ci: None | RuleQualityPrecisionCiType0 | Unset = UNSET
    median_time_to_ack: None | str | Unset = UNSET
    median_time_to_resolve: None | str | Unset = UNSET
    flapping: int | Unset = UNSET
    duplicates: int | Unset = UNSET
    severity_distribution: RuleQualitySeverityDistribution | Unset = UNSET
    feedback_mapping: RuleQualityFeedbackMapping | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    signals: RuleQualitySignals | Unset = UNSET
    signal_score: float | None | Unset = UNSET
    daily: list[RuleQualityDaily] | Unset = UNSET
    signal_weights: RuleQualitySignalWeights | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.rule_quality_precision_ci_type_0 import (
            RuleQualityPrecisionCiType0,
        )

        rule_id = str(self.rule_id)

        since = self.since.isoformat()

        alerts = self.alerts

        labelled = self.labelled

        positive = self.positive

        negative = self.negative

        neutral = self.neutral

        labelled_share = self.labelled_share

        precision: float | None | Unset
        if isinstance(self.precision, Unset):
            precision = UNSET
        else:
            precision = self.precision

        precision_ci: dict[str, Any] | None | Unset
        if isinstance(self.precision_ci, Unset):
            precision_ci = UNSET
        elif isinstance(self.precision_ci, RuleQualityPrecisionCiType0):
            precision_ci = self.precision_ci.to_dict()
        else:
            precision_ci = self.precision_ci

        median_time_to_ack: None | str | Unset
        if isinstance(self.median_time_to_ack, Unset):
            median_time_to_ack = UNSET
        else:
            median_time_to_ack = self.median_time_to_ack

        median_time_to_resolve: None | str | Unset
        if isinstance(self.median_time_to_resolve, Unset):
            median_time_to_resolve = UNSET
        else:
            median_time_to_resolve = self.median_time_to_resolve

        flapping = self.flapping

        duplicates = self.duplicates

        severity_distribution: dict[str, Any] | Unset = UNSET
        if not isinstance(self.severity_distribution, Unset):
            severity_distribution = self.severity_distribution.to_dict()

        feedback_mapping: dict[str, Any] | Unset = UNSET
        if not isinstance(self.feedback_mapping, Unset):
            feedback_mapping = self.feedback_mapping.to_dict()

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        signals: dict[str, Any] | Unset = UNSET
        if not isinstance(self.signals, Unset):
            signals = self.signals.to_dict()

        signal_score: float | None | Unset
        if isinstance(self.signal_score, Unset):
            signal_score = UNSET
        else:
            signal_score = self.signal_score

        daily: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.daily, Unset):
            daily = []
            for daily_item_data in self.daily:
                daily_item = daily_item_data.to_dict()
                daily.append(daily_item)

        signal_weights: dict[str, Any] | Unset = UNSET
        if not isinstance(self.signal_weights, Unset):
            signal_weights = self.signal_weights.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleId": rule_id,
                "since": since,
                "alerts": alerts,
                "labelled": labelled,
                "positive": positive,
                "negative": negative,
                "neutral": neutral,
            }
        )
        if labelled_share is not UNSET:
            field_dict["labelledShare"] = labelled_share
        if precision is not UNSET:
            field_dict["precision"] = precision
        if precision_ci is not UNSET:
            field_dict["precisionCi"] = precision_ci
        if median_time_to_ack is not UNSET:
            field_dict["medianTimeToAck"] = median_time_to_ack
        if median_time_to_resolve is not UNSET:
            field_dict["medianTimeToResolve"] = median_time_to_resolve
        if flapping is not UNSET:
            field_dict["flapping"] = flapping
        if duplicates is not UNSET:
            field_dict["duplicates"] = duplicates
        if severity_distribution is not UNSET:
            field_dict["severityDistribution"] = severity_distribution
        if feedback_mapping is not UNSET:
            field_dict["feedbackMapping"] = feedback_mapping
        if caveats is not UNSET:
            field_dict["caveats"] = caveats
        if signals is not UNSET:
            field_dict["signals"] = signals
        if signal_score is not UNSET:
            field_dict["signalScore"] = signal_score
        if daily is not UNSET:
            field_dict["daily"] = daily
        if signal_weights is not UNSET:
            field_dict["signalWeights"] = signal_weights

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.caveat import Caveat
        from ..models.rule_quality_daily import RuleQualityDaily
        from ..models.rule_quality_feedback_mapping import RuleQualityFeedbackMapping
        from ..models.rule_quality_precision_ci_type_0 import (
            RuleQualityPrecisionCiType0,
        )
        from ..models.rule_quality_severity_distribution import (
            RuleQualitySeverityDistribution,
        )
        from ..models.rule_quality_signal_weights import RuleQualitySignalWeights
        from ..models.rule_quality_signals import RuleQualitySignals

        d = dict(src_dict)
        rule_id = UUID(d.pop("ruleId"))

        since = datetime.datetime.fromisoformat(d.pop("since"))

        alerts = d.pop("alerts")

        labelled = d.pop("labelled")

        positive = d.pop("positive")

        negative = d.pop("negative")

        neutral = d.pop("neutral")

        labelled_share = d.pop("labelledShare", UNSET)

        def _parse_precision(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        precision = _parse_precision(d.pop("precision", UNSET))

        def _parse_precision_ci(
            data: object,
        ) -> None | RuleQualityPrecisionCiType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                precision_ci_type_0 = RuleQualityPrecisionCiType0.from_dict(data)

                return precision_ci_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RuleQualityPrecisionCiType0 | Unset, data)

        precision_ci = _parse_precision_ci(d.pop("precisionCi", UNSET))

        def _parse_median_time_to_ack(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        median_time_to_ack = _parse_median_time_to_ack(d.pop("medianTimeToAck", UNSET))

        def _parse_median_time_to_resolve(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        median_time_to_resolve = _parse_median_time_to_resolve(
            d.pop("medianTimeToResolve", UNSET)
        )

        flapping = d.pop("flapping", UNSET)

        duplicates = d.pop("duplicates", UNSET)

        _severity_distribution = d.pop("severityDistribution", UNSET)
        severity_distribution: RuleQualitySeverityDistribution | Unset
        if isinstance(_severity_distribution, Unset):
            severity_distribution = UNSET
        else:
            severity_distribution = RuleQualitySeverityDistribution.from_dict(
                _severity_distribution
            )

        _feedback_mapping = d.pop("feedbackMapping", UNSET)
        feedback_mapping: RuleQualityFeedbackMapping | Unset
        if isinstance(_feedback_mapping, Unset):
            feedback_mapping = UNSET
        else:
            feedback_mapping = RuleQualityFeedbackMapping.from_dict(_feedback_mapping)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        _signals = d.pop("signals", UNSET)
        signals: RuleQualitySignals | Unset
        if isinstance(_signals, Unset):
            signals = UNSET
        else:
            signals = RuleQualitySignals.from_dict(_signals)

        def _parse_signal_score(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        signal_score = _parse_signal_score(d.pop("signalScore", UNSET))

        _daily = d.pop("daily", UNSET)
        daily: list[RuleQualityDaily] | Unset = UNSET
        if _daily is not UNSET:
            daily = []
            for daily_item_data in _daily:
                daily_item = RuleQualityDaily.from_dict(daily_item_data)

                daily.append(daily_item)

        _signal_weights = d.pop("signalWeights", UNSET)
        signal_weights: RuleQualitySignalWeights | Unset
        if isinstance(_signal_weights, Unset):
            signal_weights = UNSET
        else:
            signal_weights = RuleQualitySignalWeights.from_dict(_signal_weights)

        rule_quality = cls(
            rule_id=rule_id,
            since=since,
            alerts=alerts,
            labelled=labelled,
            positive=positive,
            negative=negative,
            neutral=neutral,
            labelled_share=labelled_share,
            precision=precision,
            precision_ci=precision_ci,
            median_time_to_ack=median_time_to_ack,
            median_time_to_resolve=median_time_to_resolve,
            flapping=flapping,
            duplicates=duplicates,
            severity_distribution=severity_distribution,
            feedback_mapping=feedback_mapping,
            caveats=caveats,
            signals=signals,
            signal_score=signal_score,
            daily=daily,
            signal_weights=signal_weights,
        )

        rule_quality.additional_properties = d
        return rule_quality

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
