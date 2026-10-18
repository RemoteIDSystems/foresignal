from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.rule_backtest_result_rule_type import RuleBacktestResultRuleType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.rule_backtest_result_actual import RuleBacktestResultActual
    from ..models.rule_backtest_result_anomaly_type_0 import (
        RuleBacktestResultAnomalyType0,
    )
    from ..models.rule_backtest_result_overlap import RuleBacktestResultOverlap
    from ..models.rule_backtest_result_per_day_item import RuleBacktestResultPerDayItem
    from ..models.rule_backtest_result_samples_item import RuleBacktestResultSamplesItem
    from ..models.rule_backtest_result_top_entities_item import (
        RuleBacktestResultTopEntitiesItem,
    )
    from ..models.rule_backtest_result_variants_item import (
        RuleBacktestResultVariantsItem,
    )
    from ..models.rule_backtest_result_window_type_0 import (
        RuleBacktestResultWindowType0,
    )


T = TypeVar("T", bound="RuleBacktestResult")


@_attrs_define
class RuleBacktestResult:
    """What a rule would have fired over the range (record, windowed or anomaly rules), with variants side by side

    Attributes:
        rule_type (RuleBacktestResultRuleType):
        would_fire (int):
        per_day (list[RuleBacktestResultPerDayItem] | Unset):
        top_entities (list[RuleBacktestResultTopEntitiesItem] | Unset):
        samples (list[RuleBacktestResultSamplesItem] | Unset):
        actual (RuleBacktestResultActual | Unset): Alerts the rule really fired in range
        overlap (RuleBacktestResultOverlap | Unset): Overlap with what really happened
        window (None | RuleBacktestResultWindowType0 | Unset): Windowed rules
        anomaly (None | RuleBacktestResultAnomalyType0 | Unset): Anomaly rules
        variants (list[RuleBacktestResultVariantsItem] | Unset):
    """

    rule_type: RuleBacktestResultRuleType
    would_fire: int
    per_day: list[RuleBacktestResultPerDayItem] | Unset = UNSET
    top_entities: list[RuleBacktestResultTopEntitiesItem] | Unset = UNSET
    samples: list[RuleBacktestResultSamplesItem] | Unset = UNSET
    actual: RuleBacktestResultActual | Unset = UNSET
    overlap: RuleBacktestResultOverlap | Unset = UNSET
    window: None | RuleBacktestResultWindowType0 | Unset = UNSET
    anomaly: None | RuleBacktestResultAnomalyType0 | Unset = UNSET
    variants: list[RuleBacktestResultVariantsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.rule_backtest_result_anomaly_type_0 import (
            RuleBacktestResultAnomalyType0,
        )
        from ..models.rule_backtest_result_window_type_0 import (
            RuleBacktestResultWindowType0,
        )

        rule_type = self.rule_type.value

        would_fire = self.would_fire

        per_day: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.per_day, Unset):
            per_day = []
            for per_day_item_data in self.per_day:
                per_day_item = per_day_item_data.to_dict()
                per_day.append(per_day_item)

        top_entities: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.top_entities, Unset):
            top_entities = []
            for top_entities_item_data in self.top_entities:
                top_entities_item = top_entities_item_data.to_dict()
                top_entities.append(top_entities_item)

        samples: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.samples, Unset):
            samples = []
            for samples_item_data in self.samples:
                samples_item = samples_item_data.to_dict()
                samples.append(samples_item)

        actual: dict[str, Any] | Unset = UNSET
        if not isinstance(self.actual, Unset):
            actual = self.actual.to_dict()

        overlap: dict[str, Any] | Unset = UNSET
        if not isinstance(self.overlap, Unset):
            overlap = self.overlap.to_dict()

        window: dict[str, Any] | None | Unset
        if isinstance(self.window, Unset):
            window = UNSET
        elif isinstance(self.window, RuleBacktestResultWindowType0):
            window = self.window.to_dict()
        else:
            window = self.window

        anomaly: dict[str, Any] | None | Unset
        if isinstance(self.anomaly, Unset):
            anomaly = UNSET
        elif isinstance(self.anomaly, RuleBacktestResultAnomalyType0):
            anomaly = self.anomaly.to_dict()
        else:
            anomaly = self.anomaly

        variants: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.variants, Unset):
            variants = []
            for variants_item_data in self.variants:
                variants_item = variants_item_data.to_dict()
                variants.append(variants_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ruleType": rule_type,
                "wouldFire": would_fire,
            }
        )
        if per_day is not UNSET:
            field_dict["perDay"] = per_day
        if top_entities is not UNSET:
            field_dict["topEntities"] = top_entities
        if samples is not UNSET:
            field_dict["samples"] = samples
        if actual is not UNSET:
            field_dict["actual"] = actual
        if overlap is not UNSET:
            field_dict["overlap"] = overlap
        if window is not UNSET:
            field_dict["window"] = window
        if anomaly is not UNSET:
            field_dict["anomaly"] = anomaly
        if variants is not UNSET:
            field_dict["variants"] = variants

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.rule_backtest_result_actual import RuleBacktestResultActual
        from ..models.rule_backtest_result_anomaly_type_0 import (
            RuleBacktestResultAnomalyType0,
        )
        from ..models.rule_backtest_result_overlap import RuleBacktestResultOverlap
        from ..models.rule_backtest_result_per_day_item import (
            RuleBacktestResultPerDayItem,
        )
        from ..models.rule_backtest_result_samples_item import (
            RuleBacktestResultSamplesItem,
        )
        from ..models.rule_backtest_result_top_entities_item import (
            RuleBacktestResultTopEntitiesItem,
        )
        from ..models.rule_backtest_result_variants_item import (
            RuleBacktestResultVariantsItem,
        )
        from ..models.rule_backtest_result_window_type_0 import (
            RuleBacktestResultWindowType0,
        )

        d = dict(src_dict)
        rule_type = RuleBacktestResultRuleType(d.pop("ruleType"))

        would_fire = d.pop("wouldFire")

        _per_day = d.pop("perDay", UNSET)
        per_day: list[RuleBacktestResultPerDayItem] | Unset = UNSET
        if _per_day is not UNSET:
            per_day = []
            for per_day_item_data in _per_day:
                per_day_item = RuleBacktestResultPerDayItem.from_dict(per_day_item_data)

                per_day.append(per_day_item)

        _top_entities = d.pop("topEntities", UNSET)
        top_entities: list[RuleBacktestResultTopEntitiesItem] | Unset = UNSET
        if _top_entities is not UNSET:
            top_entities = []
            for top_entities_item_data in _top_entities:
                top_entities_item = RuleBacktestResultTopEntitiesItem.from_dict(
                    top_entities_item_data
                )

                top_entities.append(top_entities_item)

        _samples = d.pop("samples", UNSET)
        samples: list[RuleBacktestResultSamplesItem] | Unset = UNSET
        if _samples is not UNSET:
            samples = []
            for samples_item_data in _samples:
                samples_item = RuleBacktestResultSamplesItem.from_dict(
                    samples_item_data
                )

                samples.append(samples_item)

        _actual = d.pop("actual", UNSET)
        actual: RuleBacktestResultActual | Unset
        if isinstance(_actual, Unset):
            actual = UNSET
        else:
            actual = RuleBacktestResultActual.from_dict(_actual)

        _overlap = d.pop("overlap", UNSET)
        overlap: RuleBacktestResultOverlap | Unset
        if isinstance(_overlap, Unset):
            overlap = UNSET
        else:
            overlap = RuleBacktestResultOverlap.from_dict(_overlap)

        def _parse_window(data: object) -> None | RuleBacktestResultWindowType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                window_type_0 = RuleBacktestResultWindowType0.from_dict(data)

                return window_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RuleBacktestResultWindowType0 | Unset, data)

        window = _parse_window(d.pop("window", UNSET))

        def _parse_anomaly(
            data: object,
        ) -> None | RuleBacktestResultAnomalyType0 | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                anomaly_type_0 = RuleBacktestResultAnomalyType0.from_dict(data)

                return anomaly_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | RuleBacktestResultAnomalyType0 | Unset, data)

        anomaly = _parse_anomaly(d.pop("anomaly", UNSET))

        _variants = d.pop("variants", UNSET)
        variants: list[RuleBacktestResultVariantsItem] | Unset = UNSET
        if _variants is not UNSET:
            variants = []
            for variants_item_data in _variants:
                variants_item = RuleBacktestResultVariantsItem.from_dict(
                    variants_item_data
                )

                variants.append(variants_item)

        rule_backtest_result = cls(
            rule_type=rule_type,
            would_fire=would_fire,
            per_day=per_day,
            top_entities=top_entities,
            samples=samples,
            actual=actual,
            overlap=overlap,
            window=window,
            anomaly=anomaly,
            variants=variants,
        )

        rule_backtest_result.additional_properties = d
        return rule_backtest_result

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
