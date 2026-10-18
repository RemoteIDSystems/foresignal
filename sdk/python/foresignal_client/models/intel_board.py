from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_board_band_changes_item import IntelBoardBandChangesItem
    from ..models.intel_board_benchmarks_item import IntelBoardBenchmarksItem
    from ..models.intel_board_calibration import IntelBoardCalibration
    from ..models.intel_board_forecasts_due_item import IntelBoardForecastsDueItem
    from ..models.intel_board_indicators_observed_item import (
        IntelBoardIndicatorsObservedItem,
    )
    from ..models.intel_board_latest_assessments_item import (
        IntelBoardLatestAssessmentsItem,
    )
    from ..models.intel_board_situations_item import IntelBoardSituationsItem


T = TypeVar("T", bound="IntelBoard")


@_attrs_define
class IntelBoard:
    """Small summary of the intelligence layer (≤ 8 KB)

    Attributes:
        generated_at (datetime.datetime):
        situations (list[IntelBoardSituationsItem]):
        band_changes (list[IntelBoardBandChangesItem]):
        indicators_observed (list[IntelBoardIndicatorsObservedItem]):
        forecasts_due (list[IntelBoardForecastsDueItem]):
        latest_assessments (list[IntelBoardLatestAssessmentsItem]):
        calibration (IntelBoardCalibration | Unset): Brier skill and calibration over the last 90 days, operator vs
            machine
        benchmarks (list[IntelBoardBenchmarksItem] | Unset):
        truncated (list[str] | Unset): Sections truncated to stay within 8 KB
    """

    generated_at: datetime.datetime
    situations: list[IntelBoardSituationsItem]
    band_changes: list[IntelBoardBandChangesItem]
    indicators_observed: list[IntelBoardIndicatorsObservedItem]
    forecasts_due: list[IntelBoardForecastsDueItem]
    latest_assessments: list[IntelBoardLatestAssessmentsItem]
    calibration: IntelBoardCalibration | Unset = UNSET
    benchmarks: list[IntelBoardBenchmarksItem] | Unset = UNSET
    truncated: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        generated_at = self.generated_at.isoformat()

        situations = []
        for situations_item_data in self.situations:
            situations_item = situations_item_data.to_dict()
            situations.append(situations_item)

        band_changes = []
        for band_changes_item_data in self.band_changes:
            band_changes_item = band_changes_item_data.to_dict()
            band_changes.append(band_changes_item)

        indicators_observed = []
        for indicators_observed_item_data in self.indicators_observed:
            indicators_observed_item = indicators_observed_item_data.to_dict()
            indicators_observed.append(indicators_observed_item)

        forecasts_due = []
        for forecasts_due_item_data in self.forecasts_due:
            forecasts_due_item = forecasts_due_item_data.to_dict()
            forecasts_due.append(forecasts_due_item)

        latest_assessments = []
        for latest_assessments_item_data in self.latest_assessments:
            latest_assessments_item = latest_assessments_item_data.to_dict()
            latest_assessments.append(latest_assessments_item)

        calibration: dict[str, Any] | Unset = UNSET
        if not isinstance(self.calibration, Unset):
            calibration = self.calibration.to_dict()

        benchmarks: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.benchmarks, Unset):
            benchmarks = []
            for benchmarks_item_data in self.benchmarks:
                benchmarks_item = benchmarks_item_data.to_dict()
                benchmarks.append(benchmarks_item)

        truncated: list[str] | Unset = UNSET
        if not isinstance(self.truncated, Unset):
            truncated = self.truncated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "generatedAt": generated_at,
                "situations": situations,
                "bandChanges": band_changes,
                "indicatorsObserved": indicators_observed,
                "forecastsDue": forecasts_due,
                "latestAssessments": latest_assessments,
            }
        )
        if calibration is not UNSET:
            field_dict["calibration"] = calibration
        if benchmarks is not UNSET:
            field_dict["benchmarks"] = benchmarks
        if truncated is not UNSET:
            field_dict["truncated"] = truncated

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_board_band_changes_item import IntelBoardBandChangesItem
        from ..models.intel_board_benchmarks_item import IntelBoardBenchmarksItem
        from ..models.intel_board_calibration import IntelBoardCalibration
        from ..models.intel_board_forecasts_due_item import IntelBoardForecastsDueItem
        from ..models.intel_board_indicators_observed_item import (
            IntelBoardIndicatorsObservedItem,
        )
        from ..models.intel_board_latest_assessments_item import (
            IntelBoardLatestAssessmentsItem,
        )
        from ..models.intel_board_situations_item import IntelBoardSituationsItem

        d = dict(src_dict)
        generated_at = datetime.datetime.fromisoformat(d.pop("generatedAt"))

        situations = []
        _situations = d.pop("situations")
        for situations_item_data in _situations:
            situations_item = IntelBoardSituationsItem.from_dict(situations_item_data)

            situations.append(situations_item)

        band_changes = []
        _band_changes = d.pop("bandChanges")
        for band_changes_item_data in _band_changes:
            band_changes_item = IntelBoardBandChangesItem.from_dict(
                band_changes_item_data
            )

            band_changes.append(band_changes_item)

        indicators_observed = []
        _indicators_observed = d.pop("indicatorsObserved")
        for indicators_observed_item_data in _indicators_observed:
            indicators_observed_item = IntelBoardIndicatorsObservedItem.from_dict(
                indicators_observed_item_data
            )

            indicators_observed.append(indicators_observed_item)

        forecasts_due = []
        _forecasts_due = d.pop("forecastsDue")
        for forecasts_due_item_data in _forecasts_due:
            forecasts_due_item = IntelBoardForecastsDueItem.from_dict(
                forecasts_due_item_data
            )

            forecasts_due.append(forecasts_due_item)

        latest_assessments = []
        _latest_assessments = d.pop("latestAssessments")
        for latest_assessments_item_data in _latest_assessments:
            latest_assessments_item = IntelBoardLatestAssessmentsItem.from_dict(
                latest_assessments_item_data
            )

            latest_assessments.append(latest_assessments_item)

        _calibration = d.pop("calibration", UNSET)
        calibration: IntelBoardCalibration | Unset
        if isinstance(_calibration, Unset):
            calibration = UNSET
        else:
            calibration = IntelBoardCalibration.from_dict(_calibration)

        _benchmarks = d.pop("benchmarks", UNSET)
        benchmarks: list[IntelBoardBenchmarksItem] | Unset = UNSET
        if _benchmarks is not UNSET:
            benchmarks = []
            for benchmarks_item_data in _benchmarks:
                benchmarks_item = IntelBoardBenchmarksItem.from_dict(
                    benchmarks_item_data
                )

                benchmarks.append(benchmarks_item)

        truncated = cast(list[str], d.pop("truncated", UNSET))

        intel_board = cls(
            generated_at=generated_at,
            situations=situations,
            band_changes=band_changes,
            indicators_observed=indicators_observed,
            forecasts_due=forecasts_due,
            latest_assessments=latest_assessments,
            calibration=calibration,
            benchmarks=benchmarks,
            truncated=truncated,
        )

        intel_board.additional_properties = d
        return intel_board

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
