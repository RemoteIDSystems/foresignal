from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_component_result_status import BenchmarkComponentResultStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.benchmark_component_result_metrics import (
        BenchmarkComponentResultMetrics,
    )
    from ..models.benchmark_component_result_scenarios_item import (
        BenchmarkComponentResultScenariosItem,
    )
    from ..models.benchmark_component_result_thresholds_item import (
        BenchmarkComponentResultThresholdsItem,
    )
    from ..models.caveat import Caveat


T = TypeVar("T", bound="BenchmarkComponentResult")


@_attrs_define
class BenchmarkComponentResult:
    """Metrics, thresholds and status of one component in a benchmark run

    Attributes:
        component (str): Benchmarked component: `change-point`, `anomaly`, `event-study`, `co-occurrence`, `evidence`,
            `canonical-linking`, `precursor`, `situation-linker`, `forecaster:base-rate`, `forecaster:analog`,
            `forecaster:precursor` or `forecaster:ensemble` Example: change-point.
        component_version (str):  Example: cusum 1.0.0.
        status (BenchmarkComponentResultStatus): Result against the documented thresholds
        metrics (BenchmarkComponentResultMetrics): Named metrics: `fdr`, `falseAlarmRate`, `recall`, `precision`,
            `delayMedianHours`, `delayP90Hours`, `correctUnknownShare`, `duplicateInflation`, `linkingPrecision`,
            `linkingRecall`, `spuriousMerges`, `falseValidationRate`, `lagRecoveredShare`, `rateRecoveredShare`, `brier`,
            `logScore`, `reliabilityMaxDeviation`, `skill`, …
        thresholds (list[BenchmarkComponentResultThresholdsItem] | Unset):
        scenarios (list[BenchmarkComponentResultScenariosItem] | Unset):
        caveats (list[Caveat] | Unset):
    """

    component: str
    component_version: str
    status: BenchmarkComponentResultStatus
    metrics: BenchmarkComponentResultMetrics
    thresholds: list[BenchmarkComponentResultThresholdsItem] | Unset = UNSET
    scenarios: list[BenchmarkComponentResultScenariosItem] | Unset = UNSET
    caveats: list[Caveat] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        component = self.component

        component_version = self.component_version

        status = self.status.value

        metrics = self.metrics.to_dict()

        thresholds: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.thresholds, Unset):
            thresholds = []
            for thresholds_item_data in self.thresholds:
                thresholds_item = thresholds_item_data.to_dict()
                thresholds.append(thresholds_item)

        scenarios: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.scenarios, Unset):
            scenarios = []
            for scenarios_item_data in self.scenarios:
                scenarios_item = scenarios_item_data.to_dict()
                scenarios.append(scenarios_item)

        caveats: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.caveats, Unset):
            caveats = []
            for caveats_item_data in self.caveats:
                caveats_item = caveats_item_data.to_dict()
                caveats.append(caveats_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "component": component,
                "componentVersion": component_version,
                "status": status,
                "metrics": metrics,
            }
        )
        if thresholds is not UNSET:
            field_dict["thresholds"] = thresholds
        if scenarios is not UNSET:
            field_dict["scenarios"] = scenarios
        if caveats is not UNSET:
            field_dict["caveats"] = caveats

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.benchmark_component_result_metrics import (
            BenchmarkComponentResultMetrics,
        )
        from ..models.benchmark_component_result_scenarios_item import (
            BenchmarkComponentResultScenariosItem,
        )
        from ..models.benchmark_component_result_thresholds_item import (
            BenchmarkComponentResultThresholdsItem,
        )
        from ..models.caveat import Caveat

        d = dict(src_dict)
        component = d.pop("component")

        component_version = d.pop("componentVersion")

        status = BenchmarkComponentResultStatus(d.pop("status"))

        metrics = BenchmarkComponentResultMetrics.from_dict(d.pop("metrics"))

        _thresholds = d.pop("thresholds", UNSET)
        thresholds: list[BenchmarkComponentResultThresholdsItem] | Unset = UNSET
        if _thresholds is not UNSET:
            thresholds = []
            for thresholds_item_data in _thresholds:
                thresholds_item = BenchmarkComponentResultThresholdsItem.from_dict(
                    thresholds_item_data
                )

                thresholds.append(thresholds_item)

        _scenarios = d.pop("scenarios", UNSET)
        scenarios: list[BenchmarkComponentResultScenariosItem] | Unset = UNSET
        if _scenarios is not UNSET:
            scenarios = []
            for scenarios_item_data in _scenarios:
                scenarios_item = BenchmarkComponentResultScenariosItem.from_dict(
                    scenarios_item_data
                )

                scenarios.append(scenarios_item)

        _caveats = d.pop("caveats", UNSET)
        caveats: list[Caveat] | Unset = UNSET
        if _caveats is not UNSET:
            caveats = []
            for caveats_item_data in _caveats:
                caveats_item = Caveat.from_dict(caveats_item_data)

                caveats.append(caveats_item)

        benchmark_component_result = cls(
            component=component,
            component_version=component_version,
            status=status,
            metrics=metrics,
            thresholds=thresholds,
            scenarios=scenarios,
            caveats=caveats,
        )

        benchmark_component_result.additional_properties = d
        return benchmark_component_result

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
