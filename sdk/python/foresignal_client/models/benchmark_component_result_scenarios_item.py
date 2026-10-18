from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.benchmark_component_result_scenarios_item_scenario import (
    BenchmarkComponentResultScenariosItemScenario,
)
from ..models.benchmark_component_result_scenarios_item_status import (
    BenchmarkComponentResultScenariosItemStatus,
)
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.benchmark_component_result_scenarios_item_metrics import (
        BenchmarkComponentResultScenariosItemMetrics,
    )
    from ..models.benchmark_component_result_scenarios_item_thresholds_item import (
        BenchmarkComponentResultScenariosItemThresholdsItem,
    )
    from ..models.benchmark_component_result_scenarios_item_truth import (
        BenchmarkComponentResultScenariosItemTruth,
    )


T = TypeVar("T", bound="BenchmarkComponentResultScenariosItem")


@_attrs_define
class BenchmarkComponentResultScenariosItem:
    """Per-scenario (or per-case) result

    Attributes:
        scenario (BenchmarkComponentResultScenariosItemScenario): Synthetic scenario (`bench-scenarios` 1.0.0) or
            `historical` for a corpus case
        status (BenchmarkComponentResultScenariosItemStatus): Result against the documented thresholds
        metrics (BenchmarkComponentResultScenariosItemMetrics): Named metrics: `fdr`, `falseAlarmRate`, `recall`,
            `precision`, `delayMedianHours`, `delayP90Hours`, `correctUnknownShare`, `duplicateInflation`,
            `linkingPrecision`, `linkingRecall`, `spuriousMerges`, `falseValidationRate`, `lagRecoveredShare`,
            `rateRecoveredShare`, `brier`, `logScore`, `reliabilityMaxDeviation`, `skill`, …
        case_id (None | str | Unset):
        seed (int | None | Unset):
        thresholds (list[BenchmarkComponentResultScenariosItemThresholdsItem] | Unset):
        truth (BenchmarkComponentResultScenariosItemTruth | Unset): Planted truth record (times, sizes, lags, groups) or
            case expectations
        notes (list[str] | Unset):
    """

    scenario: BenchmarkComponentResultScenariosItemScenario
    status: BenchmarkComponentResultScenariosItemStatus
    metrics: BenchmarkComponentResultScenariosItemMetrics
    case_id: None | str | Unset = UNSET
    seed: int | None | Unset = UNSET
    thresholds: list[BenchmarkComponentResultScenariosItemThresholdsItem] | Unset = (
        UNSET
    )
    truth: BenchmarkComponentResultScenariosItemTruth | Unset = UNSET
    notes: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        scenario = self.scenario.value

        status = self.status.value

        metrics = self.metrics.to_dict()

        case_id: None | str | Unset
        if isinstance(self.case_id, Unset):
            case_id = UNSET
        else:
            case_id = self.case_id

        seed: int | None | Unset
        if isinstance(self.seed, Unset):
            seed = UNSET
        else:
            seed = self.seed

        thresholds: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.thresholds, Unset):
            thresholds = []
            for thresholds_item_data in self.thresholds:
                thresholds_item = thresholds_item_data.to_dict()
                thresholds.append(thresholds_item)

        truth: dict[str, Any] | Unset = UNSET
        if not isinstance(self.truth, Unset):
            truth = self.truth.to_dict()

        notes: list[str] | Unset = UNSET
        if not isinstance(self.notes, Unset):
            notes = self.notes

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "scenario": scenario,
                "status": status,
                "metrics": metrics,
            }
        )
        if case_id is not UNSET:
            field_dict["caseId"] = case_id
        if seed is not UNSET:
            field_dict["seed"] = seed
        if thresholds is not UNSET:
            field_dict["thresholds"] = thresholds
        if truth is not UNSET:
            field_dict["truth"] = truth
        if notes is not UNSET:
            field_dict["notes"] = notes

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.benchmark_component_result_scenarios_item_metrics import (
            BenchmarkComponentResultScenariosItemMetrics,
        )
        from ..models.benchmark_component_result_scenarios_item_thresholds_item import (
            BenchmarkComponentResultScenariosItemThresholdsItem,
        )
        from ..models.benchmark_component_result_scenarios_item_truth import (
            BenchmarkComponentResultScenariosItemTruth,
        )

        d = dict(src_dict)
        scenario = BenchmarkComponentResultScenariosItemScenario(d.pop("scenario"))

        status = BenchmarkComponentResultScenariosItemStatus(d.pop("status"))

        metrics = BenchmarkComponentResultScenariosItemMetrics.from_dict(
            d.pop("metrics")
        )

        def _parse_case_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        case_id = _parse_case_id(d.pop("caseId", UNSET))

        def _parse_seed(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        seed = _parse_seed(d.pop("seed", UNSET))

        _thresholds = d.pop("thresholds", UNSET)
        thresholds: (
            list[BenchmarkComponentResultScenariosItemThresholdsItem] | Unset
        ) = UNSET
        if _thresholds is not UNSET:
            thresholds = []
            for thresholds_item_data in _thresholds:
                thresholds_item = (
                    BenchmarkComponentResultScenariosItemThresholdsItem.from_dict(
                        thresholds_item_data
                    )
                )

                thresholds.append(thresholds_item)

        _truth = d.pop("truth", UNSET)
        truth: BenchmarkComponentResultScenariosItemTruth | Unset
        if isinstance(_truth, Unset):
            truth = UNSET
        else:
            truth = BenchmarkComponentResultScenariosItemTruth.from_dict(_truth)

        notes = cast(list[str], d.pop("notes", UNSET))

        benchmark_component_result_scenarios_item = cls(
            scenario=scenario,
            status=status,
            metrics=metrics,
            case_id=case_id,
            seed=seed,
            thresholds=thresholds,
            truth=truth,
            notes=notes,
        )

        benchmark_component_result_scenarios_item.additional_properties = d
        return benchmark_component_result_scenarios_item

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
