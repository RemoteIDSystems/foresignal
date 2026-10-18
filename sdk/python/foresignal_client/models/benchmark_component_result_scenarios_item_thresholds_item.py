from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_component_result_scenarios_item_thresholds_item_comparator import (
    BenchmarkComponentResultScenariosItemThresholdsItemComparator,
)
from ..models.benchmark_component_result_scenarios_item_thresholds_item_status import (
    BenchmarkComponentResultScenariosItemThresholdsItemStatus,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="BenchmarkComponentResultScenariosItemThresholdsItem")


@_attrs_define
class BenchmarkComponentResultScenariosItemThresholdsItem:
    """One threshold check

    Attributes:
        metric (str):  Example: falseAlarmRate.
        comparator (BenchmarkComponentResultScenariosItemThresholdsItemComparator):
        threshold (float):
        status (BenchmarkComponentResultScenariosItemThresholdsItemStatus): Result against the documented thresholds
        observed (float | None | Unset):
        warn (float | None | Unset): The warn threshold; outside it the check fails (null: outside the pass threshold
            fails)
        scenario (str | Unset): The scenario (or `historical`) the check applies to
        note (str | Unset): What the threshold means
    """

    metric: str
    comparator: BenchmarkComponentResultScenariosItemThresholdsItemComparator
    threshold: float
    status: BenchmarkComponentResultScenariosItemThresholdsItemStatus
    observed: float | None | Unset = UNSET
    warn: float | None | Unset = UNSET
    scenario: str | Unset = UNSET
    note: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        metric = self.metric

        comparator = self.comparator.value

        threshold = self.threshold

        status = self.status.value

        observed: float | None | Unset
        if isinstance(self.observed, Unset):
            observed = UNSET
        else:
            observed = self.observed

        warn: float | None | Unset
        if isinstance(self.warn, Unset):
            warn = UNSET
        else:
            warn = self.warn

        scenario = self.scenario

        note = self.note

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "metric": metric,
                "comparator": comparator,
                "threshold": threshold,
                "status": status,
            }
        )
        if observed is not UNSET:
            field_dict["observed"] = observed
        if warn is not UNSET:
            field_dict["warn"] = warn
        if scenario is not UNSET:
            field_dict["scenario"] = scenario
        if note is not UNSET:
            field_dict["note"] = note

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        metric = d.pop("metric")

        comparator = BenchmarkComponentResultScenariosItemThresholdsItemComparator(
            d.pop("comparator")
        )

        threshold = d.pop("threshold")

        status = BenchmarkComponentResultScenariosItemThresholdsItemStatus(
            d.pop("status")
        )

        def _parse_observed(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        observed = _parse_observed(d.pop("observed", UNSET))

        def _parse_warn(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        warn = _parse_warn(d.pop("warn", UNSET))

        scenario = d.pop("scenario", UNSET)

        note = d.pop("note", UNSET)

        benchmark_component_result_scenarios_item_thresholds_item = cls(
            metric=metric,
            comparator=comparator,
            threshold=threshold,
            status=status,
            observed=observed,
            warn=warn,
            scenario=scenario,
            note=note,
        )

        benchmark_component_result_scenarios_item_thresholds_item.additional_properties = d
        return benchmark_component_result_scenarios_item_thresholds_item

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
