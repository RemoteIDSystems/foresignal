from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_numeric_calibration_label import ForecastNumericCalibrationLabel
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_numeric_calibration_pit import ForecastNumericCalibrationPit
    from ..models.forecast_numeric_calibration_quantile_reliability_item import (
        ForecastNumericCalibrationQuantileReliabilityItem,
    )
    from ..models.forecast_numeric_calibration_summary import (
        ForecastNumericCalibrationSummary,
    )


T = TypeVar("T", bound="ForecastNumericCalibration")


@_attrs_define
class ForecastNumericCalibration:
    """Numeric calibration (152): the quantile-reliability diagram, PIT histogram and calibration-error summary over
    resolved numeric questions

        Attributes:
            type_ (Literal['numeric']):
            n (int): Resolved numeric questions with quantile coverage
            label (ForecastNumericCalibrationLabel):
            quantile_reliability (list[ForecastNumericCalibrationQuantileReliabilityItem]): Empirical coverage P(y ≤ q) of
                each stated quantile vs its nominal level
            pit (ForecastNumericCalibrationPit): PIT histogram over the required-quantile bins
            scorer_version (str):
            computed_at (datetime.datetime):
            forecaster_id (None | str | Unset):
            category (None | str | Unset):
            summary (ForecastNumericCalibrationSummary | Unset):
    """

    type_: Literal["numeric"]
    n: int
    label: ForecastNumericCalibrationLabel
    quantile_reliability: list[ForecastNumericCalibrationQuantileReliabilityItem]
    pit: ForecastNumericCalibrationPit
    scorer_version: str
    computed_at: datetime.datetime
    forecaster_id: None | str | Unset = UNSET
    category: None | str | Unset = UNSET
    summary: ForecastNumericCalibrationSummary | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_

        n = self.n

        label = self.label.value

        quantile_reliability = []
        for quantile_reliability_item_data in self.quantile_reliability:
            quantile_reliability_item = quantile_reliability_item_data.to_dict()
            quantile_reliability.append(quantile_reliability_item)

        pit = self.pit.to_dict()

        scorer_version = self.scorer_version

        computed_at = self.computed_at.isoformat()

        forecaster_id: None | str | Unset
        if isinstance(self.forecaster_id, Unset):
            forecaster_id = UNSET
        else:
            forecaster_id = self.forecaster_id

        category: None | str | Unset
        if isinstance(self.category, Unset):
            category = UNSET
        else:
            category = self.category

        summary: dict[str, Any] | Unset = UNSET
        if not isinstance(self.summary, Unset):
            summary = self.summary.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
                "n": n,
                "label": label,
                "quantileReliability": quantile_reliability,
                "pit": pit,
                "scorerVersion": scorer_version,
                "computedAt": computed_at,
            }
        )
        if forecaster_id is not UNSET:
            field_dict["forecasterId"] = forecaster_id
        if category is not UNSET:
            field_dict["category"] = category
        if summary is not UNSET:
            field_dict["summary"] = summary

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_numeric_calibration_pit import (
            ForecastNumericCalibrationPit,
        )
        from ..models.forecast_numeric_calibration_quantile_reliability_item import (
            ForecastNumericCalibrationQuantileReliabilityItem,
        )
        from ..models.forecast_numeric_calibration_summary import (
            ForecastNumericCalibrationSummary,
        )

        d = dict(src_dict)
        type_ = cast(Literal["numeric"], d.pop("type"))
        if type_ != "numeric":
            raise ValueError(f"type must match const 'numeric', got '{type_}'")

        n = d.pop("n")

        label = ForecastNumericCalibrationLabel(d.pop("label"))

        quantile_reliability = []
        _quantile_reliability = d.pop("quantileReliability")
        for quantile_reliability_item_data in _quantile_reliability:
            quantile_reliability_item = (
                ForecastNumericCalibrationQuantileReliabilityItem.from_dict(
                    quantile_reliability_item_data
                )
            )

            quantile_reliability.append(quantile_reliability_item)

        pit = ForecastNumericCalibrationPit.from_dict(d.pop("pit"))

        scorer_version = d.pop("scorerVersion")

        computed_at = datetime.datetime.fromisoformat(d.pop("computedAt"))

        def _parse_forecaster_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        forecaster_id = _parse_forecaster_id(d.pop("forecasterId", UNSET))

        def _parse_category(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        category = _parse_category(d.pop("category", UNSET))

        _summary = d.pop("summary", UNSET)
        summary: ForecastNumericCalibrationSummary | Unset
        if isinstance(_summary, Unset):
            summary = UNSET
        else:
            summary = ForecastNumericCalibrationSummary.from_dict(_summary)

        forecast_numeric_calibration = cls(
            type_=type_,
            n=n,
            label=label,
            quantile_reliability=quantile_reliability,
            pit=pit,
            scorer_version=scorer_version,
            computed_at=computed_at,
            forecaster_id=forecaster_id,
            category=category,
            summary=summary,
        )

        forecast_numeric_calibration.additional_properties = d
        return forecast_numeric_calibration

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
