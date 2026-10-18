from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.rule_backtest_result_anomaly_type_0_by_classification import (
        RuleBacktestResultAnomalyType0ByClassification,
    )


T = TypeVar("T", bound="RuleBacktestResultAnomalyType0")


@_attrs_define
class RuleBacktestResultAnomalyType0:
    """Anomaly rules

    Attributes:
        warmup_consumed (str | Unset):
        by_classification (RuleBacktestResultAnomalyType0ByClassification | Unset):
        coverage_drops (int | Unset):
    """

    warmup_consumed: str | Unset = UNSET
    by_classification: RuleBacktestResultAnomalyType0ByClassification | Unset = UNSET
    coverage_drops: int | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        warmup_consumed = self.warmup_consumed

        by_classification: dict[str, Any] | Unset = UNSET
        if not isinstance(self.by_classification, Unset):
            by_classification = self.by_classification.to_dict()

        coverage_drops = self.coverage_drops

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if warmup_consumed is not UNSET:
            field_dict["warmupConsumed"] = warmup_consumed
        if by_classification is not UNSET:
            field_dict["byClassification"] = by_classification
        if coverage_drops is not UNSET:
            field_dict["coverageDrops"] = coverage_drops

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.rule_backtest_result_anomaly_type_0_by_classification import (
            RuleBacktestResultAnomalyType0ByClassification,
        )

        d = dict(src_dict)
        warmup_consumed = d.pop("warmupConsumed", UNSET)

        _by_classification = d.pop("byClassification", UNSET)
        by_classification: RuleBacktestResultAnomalyType0ByClassification | Unset
        if isinstance(_by_classification, Unset):
            by_classification = UNSET
        else:
            by_classification = (
                RuleBacktestResultAnomalyType0ByClassification.from_dict(
                    _by_classification
                )
            )

        coverage_drops = d.pop("coverageDrops", UNSET)

        rule_backtest_result_anomaly_type_0 = cls(
            warmup_consumed=warmup_consumed,
            by_classification=by_classification,
            coverage_drops=coverage_drops,
        )

        rule_backtest_result_anomaly_type_0.additional_properties = d
        return rule_backtest_result_anomaly_type_0

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
