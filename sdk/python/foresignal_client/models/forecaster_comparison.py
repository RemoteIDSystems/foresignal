from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecaster_comparison_label import ForecasterComparisonLabel
from ..models.forecaster_comparison_metric import ForecasterComparisonMetric
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.stat_interval import StatInterval


T = TypeVar("T", bound="ForecasterComparison")


@_attrs_define
class ForecasterComparison:
    """Pairwise comparison on shared resolved questions with a paired bootstrap interval

    Attributes:
        a (str):
        b (str):
        n_shared (int):
        label (ForecasterComparisonLabel):
        metric (ForecasterComparisonMetric):
        scorer_version (str):
        computed_at (datetime.datetime):
        category (None | str | Unset):
        mean_score_a (float | None | Unset):
        mean_score_b (float | None | Unset):
        mean_difference (float | None | Unset): mean(A) − mean(B); negative favours A
        interval (None | StatInterval | Unset): Paired bootstrap 95% interval (1,000 resamples, deterministic seed)
    """

    a: str
    b: str
    n_shared: int
    label: ForecasterComparisonLabel
    metric: ForecasterComparisonMetric
    scorer_version: str
    computed_at: datetime.datetime
    category: None | str | Unset = UNSET
    mean_score_a: float | None | Unset = UNSET
    mean_score_b: float | None | Unset = UNSET
    mean_difference: float | None | Unset = UNSET
    interval: None | StatInterval | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.stat_interval import StatInterval

        a = self.a

        b = self.b

        n_shared = self.n_shared

        label = self.label.value

        metric = self.metric.value

        scorer_version = self.scorer_version

        computed_at = self.computed_at.isoformat()

        category: None | str | Unset
        if isinstance(self.category, Unset):
            category = UNSET
        else:
            category = self.category

        mean_score_a: float | None | Unset
        if isinstance(self.mean_score_a, Unset):
            mean_score_a = UNSET
        else:
            mean_score_a = self.mean_score_a

        mean_score_b: float | None | Unset
        if isinstance(self.mean_score_b, Unset):
            mean_score_b = UNSET
        else:
            mean_score_b = self.mean_score_b

        mean_difference: float | None | Unset
        if isinstance(self.mean_difference, Unset):
            mean_difference = UNSET
        else:
            mean_difference = self.mean_difference

        interval: dict[str, Any] | None | Unset
        if isinstance(self.interval, Unset):
            interval = UNSET
        elif isinstance(self.interval, StatInterval):
            interval = self.interval.to_dict()
        else:
            interval = self.interval

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "a": a,
                "b": b,
                "nShared": n_shared,
                "label": label,
                "metric": metric,
                "scorerVersion": scorer_version,
                "computedAt": computed_at,
            }
        )
        if category is not UNSET:
            field_dict["category"] = category
        if mean_score_a is not UNSET:
            field_dict["meanScoreA"] = mean_score_a
        if mean_score_b is not UNSET:
            field_dict["meanScoreB"] = mean_score_b
        if mean_difference is not UNSET:
            field_dict["meanDifference"] = mean_difference
        if interval is not UNSET:
            field_dict["interval"] = interval

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.stat_interval import StatInterval

        d = dict(src_dict)
        a = d.pop("a")

        b = d.pop("b")

        n_shared = d.pop("nShared")

        label = ForecasterComparisonLabel(d.pop("label"))

        metric = ForecasterComparisonMetric(d.pop("metric"))

        scorer_version = d.pop("scorerVersion")

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        def _parse_category(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        category = _parse_category(d.pop("category", UNSET))

        def _parse_mean_score_a(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean_score_a = _parse_mean_score_a(d.pop("meanScoreA", UNSET))

        def _parse_mean_score_b(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean_score_b = _parse_mean_score_b(d.pop("meanScoreB", UNSET))

        def _parse_mean_difference(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        mean_difference = _parse_mean_difference(d.pop("meanDifference", UNSET))

        def _parse_interval(data: object) -> None | StatInterval | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                interval_type_0 = StatInterval.from_dict(data)

                return interval_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | StatInterval | Unset, data)

        interval = _parse_interval(d.pop("interval", UNSET))

        forecaster_comparison = cls(
            a=a,
            b=b,
            n_shared=n_shared,
            label=label,
            metric=metric,
            scorer_version=scorer_version,
            computed_at=computed_at,
            category=category,
            mean_score_a=mean_score_a,
            mean_score_b=mean_score_b,
            mean_difference=mean_difference,
            interval=interval,
        )

        forecaster_comparison.additional_properties = d
        return forecaster_comparison

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
