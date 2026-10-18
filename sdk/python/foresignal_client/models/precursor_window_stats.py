from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_window_stats_verdict import PrecursorWindowStatsVerdict
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.precursor_window_stats_reverse_type_0 import (
        PrecursorWindowStatsReverseType0,
    )
    from ..models.stat_interval import StatInterval
    from ..models.time_range import TimeRange


T = TypeVar("T", bound="PrecursorWindowStats")


@_attrs_define
class PrecursorWindowStats:
    """Test statistics of a precursor on one window (discovery or validation)

    Attributes:
        window (TimeRange):
        n (int): Antecedent occurrences (events) or overlapping points (series)
        verdict (PrecursorWindowStatsVerdict):
        hits (int | None | Unset):
        hit_rate (float | None | Unset):
        hit_rate_interval (None | StatInterval | Unset):
        base_rate (float | None | Unset): Hit rate expected by chance (circular-shift baseline)
        lift (float | None | Unset):
        correlation (float | None | Unset): Series: cross-correlation at the best lag
        best_lag (None | str | Unset):
        p (float | None | Unset): Raw p-value
        adjusted_p (float | None | Unset): BH or BY adjusted p within the batch
        reverse (None | PrecursorWindowStatsReverseType0 | Unset):
        low_detectability_excluded (int | Unset): Antecedent occurrences excluded under `LOW_DETECTABILITY`
        coverage_gap_fraction (float | None | Unset): Share of the window touched by coverage gaps (events: involved
            sources; series: missing buckets)
        block_length (int | None | Unset): Series: surrogate block length in buckets
        reason (None | str | Unset): Why the window was insufficient
    """

    window: TimeRange
    n: int
    verdict: PrecursorWindowStatsVerdict
    hits: int | None | Unset = UNSET
    hit_rate: float | None | Unset = UNSET
    hit_rate_interval: None | StatInterval | Unset = UNSET
    base_rate: float | None | Unset = UNSET
    lift: float | None | Unset = UNSET
    correlation: float | None | Unset = UNSET
    best_lag: None | str | Unset = UNSET
    p: float | None | Unset = UNSET
    adjusted_p: float | None | Unset = UNSET
    reverse: None | PrecursorWindowStatsReverseType0 | Unset = UNSET
    low_detectability_excluded: int | Unset = UNSET
    coverage_gap_fraction: float | None | Unset = UNSET
    block_length: int | None | Unset = UNSET
    reason: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.precursor_window_stats_reverse_type_0 import (
            PrecursorWindowStatsReverseType0,
        )
        from ..models.stat_interval import StatInterval

        window = self.window.to_dict()

        n = self.n

        verdict = self.verdict.value

        hits: int | None | Unset
        if isinstance(self.hits, Unset):
            hits = UNSET
        else:
            hits = self.hits

        hit_rate: float | None | Unset
        if isinstance(self.hit_rate, Unset):
            hit_rate = UNSET
        else:
            hit_rate = self.hit_rate

        hit_rate_interval: dict[str, Any] | None | Unset
        if isinstance(self.hit_rate_interval, Unset):
            hit_rate_interval = UNSET
        elif isinstance(self.hit_rate_interval, StatInterval):
            hit_rate_interval = self.hit_rate_interval.to_dict()
        else:
            hit_rate_interval = self.hit_rate_interval

        base_rate: float | None | Unset
        if isinstance(self.base_rate, Unset):
            base_rate = UNSET
        else:
            base_rate = self.base_rate

        lift: float | None | Unset
        if isinstance(self.lift, Unset):
            lift = UNSET
        else:
            lift = self.lift

        correlation: float | None | Unset
        if isinstance(self.correlation, Unset):
            correlation = UNSET
        else:
            correlation = self.correlation

        best_lag: None | str | Unset
        if isinstance(self.best_lag, Unset):
            best_lag = UNSET
        else:
            best_lag = self.best_lag

        p: float | None | Unset
        if isinstance(self.p, Unset):
            p = UNSET
        else:
            p = self.p

        adjusted_p: float | None | Unset
        if isinstance(self.adjusted_p, Unset):
            adjusted_p = UNSET
        else:
            adjusted_p = self.adjusted_p

        reverse: dict[str, Any] | None | Unset
        if isinstance(self.reverse, Unset):
            reverse = UNSET
        elif isinstance(self.reverse, PrecursorWindowStatsReverseType0):
            reverse = self.reverse.to_dict()
        else:
            reverse = self.reverse

        low_detectability_excluded = self.low_detectability_excluded

        coverage_gap_fraction: float | None | Unset
        if isinstance(self.coverage_gap_fraction, Unset):
            coverage_gap_fraction = UNSET
        else:
            coverage_gap_fraction = self.coverage_gap_fraction

        block_length: int | None | Unset
        if isinstance(self.block_length, Unset):
            block_length = UNSET
        else:
            block_length = self.block_length

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "window": window,
                "n": n,
                "verdict": verdict,
            }
        )
        if hits is not UNSET:
            field_dict["hits"] = hits
        if hit_rate is not UNSET:
            field_dict["hitRate"] = hit_rate
        if hit_rate_interval is not UNSET:
            field_dict["hitRateInterval"] = hit_rate_interval
        if base_rate is not UNSET:
            field_dict["baseRate"] = base_rate
        if lift is not UNSET:
            field_dict["lift"] = lift
        if correlation is not UNSET:
            field_dict["correlation"] = correlation
        if best_lag is not UNSET:
            field_dict["bestLag"] = best_lag
        if p is not UNSET:
            field_dict["p"] = p
        if adjusted_p is not UNSET:
            field_dict["adjustedP"] = adjusted_p
        if reverse is not UNSET:
            field_dict["reverse"] = reverse
        if low_detectability_excluded is not UNSET:
            field_dict["lowDetectabilityExcluded"] = low_detectability_excluded
        if coverage_gap_fraction is not UNSET:
            field_dict["coverageGapFraction"] = coverage_gap_fraction
        if block_length is not UNSET:
            field_dict["blockLength"] = block_length
        if reason is not UNSET:
            field_dict["reason"] = reason

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.precursor_window_stats_reverse_type_0 import (
            PrecursorWindowStatsReverseType0,
        )
        from ..models.stat_interval import StatInterval
        from ..models.time_range import TimeRange

        d = dict(src_dict)
        window = TimeRange.from_dict(d.pop("window"))

        n = d.pop("n")

        verdict = PrecursorWindowStatsVerdict(d.pop("verdict"))

        def _parse_hits(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        hits = _parse_hits(d.pop("hits", UNSET))

        def _parse_hit_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        hit_rate = _parse_hit_rate(d.pop("hitRate", UNSET))

        def _parse_hit_rate_interval(data: object) -> None | StatInterval | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                hit_rate_interval_type_0 = StatInterval.from_dict(data)

                return hit_rate_interval_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | StatInterval | Unset, data)

        hit_rate_interval = _parse_hit_rate_interval(d.pop("hitRateInterval", UNSET))

        def _parse_base_rate(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        base_rate = _parse_base_rate(d.pop("baseRate", UNSET))

        def _parse_lift(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        lift = _parse_lift(d.pop("lift", UNSET))

        def _parse_correlation(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        correlation = _parse_correlation(d.pop("correlation", UNSET))

        def _parse_best_lag(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        best_lag = _parse_best_lag(d.pop("bestLag", UNSET))

        def _parse_p(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        p = _parse_p(d.pop("p", UNSET))

        def _parse_adjusted_p(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        adjusted_p = _parse_adjusted_p(d.pop("adjustedP", UNSET))

        def _parse_reverse(
            data: object,
        ) -> None | PrecursorWindowStatsReverseType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                reverse_type_0 = PrecursorWindowStatsReverseType0.from_dict(data)

                return reverse_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | PrecursorWindowStatsReverseType0 | Unset, data)

        reverse = _parse_reverse(d.pop("reverse", UNSET))

        low_detectability_excluded = d.pop("lowDetectabilityExcluded", UNSET)

        def _parse_coverage_gap_fraction(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        coverage_gap_fraction = _parse_coverage_gap_fraction(
            d.pop("coverageGapFraction", UNSET)
        )

        def _parse_block_length(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        block_length = _parse_block_length(d.pop("blockLength", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        precursor_window_stats = cls(
            window=window,
            n=n,
            verdict=verdict,
            hits=hits,
            hit_rate=hit_rate,
            hit_rate_interval=hit_rate_interval,
            base_rate=base_rate,
            lift=lift,
            correlation=correlation,
            best_lag=best_lag,
            p=p,
            adjusted_p=adjusted_p,
            reverse=reverse,
            low_detectability_excluded=low_detectability_excluded,
            coverage_gap_fraction=coverage_gap_fraction,
            block_length=block_length,
            reason=reason,
        )

        precursor_window_stats.additional_properties = d
        return precursor_window_stats

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
