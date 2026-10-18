from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_calibration_label import ForecastCalibrationLabel
from ..models.forecast_calibration_method import ForecastCalibrationMethod
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.forecast_calibration_bins_item import ForecastCalibrationBinsItem
    from ..models.forecast_calibration_curve_item import ForecastCalibrationCurveItem
    from ..models.forecast_calibration_decomposition_type_0 import (
        ForecastCalibrationDecompositionType0,
    )


T = TypeVar("T", bound="ForecastCalibration")


@_attrs_define
class ForecastCalibration:
    """CORP reliability curve (isotonic regression by PAV) with decomposition; 10-bin table fallback

    Attributes:
        n (int):
        label (ForecastCalibrationLabel):
        method (ForecastCalibrationMethod): `corp`, or `bins` when n < 50
        curve (list[ForecastCalibrationCurveItem]):
        bins (list[ForecastCalibrationBinsItem]):
        scorer_version (str):
        computed_at (datetime.datetime):
        forecaster_id (None | str | Unset):
        category (None | str | Unset):
        decomposition (ForecastCalibrationDecompositionType0 | None | Unset):
        sharpness (float | None | Unset):
    """

    n: int
    label: ForecastCalibrationLabel
    method: ForecastCalibrationMethod
    curve: list[ForecastCalibrationCurveItem]
    bins: list[ForecastCalibrationBinsItem]
    scorer_version: str
    computed_at: datetime.datetime
    forecaster_id: None | str | Unset = UNSET
    category: None | str | Unset = UNSET
    decomposition: ForecastCalibrationDecompositionType0 | None | Unset = UNSET
    sharpness: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.forecast_calibration_decomposition_type_0 import (
            ForecastCalibrationDecompositionType0,
        )

        n = self.n

        label = self.label.value

        method = self.method.value

        curve = []
        for curve_item_data in self.curve:
            curve_item = curve_item_data.to_dict()
            curve.append(curve_item)

        bins = []
        for bins_item_data in self.bins:
            bins_item = bins_item_data.to_dict()
            bins.append(bins_item)

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

        decomposition: dict[str, Any] | None | Unset
        if isinstance(self.decomposition, Unset):
            decomposition = UNSET
        elif isinstance(self.decomposition, ForecastCalibrationDecompositionType0):
            decomposition = self.decomposition.to_dict()
        else:
            decomposition = self.decomposition

        sharpness: float | None | Unset
        if isinstance(self.sharpness, Unset):
            sharpness = UNSET
        else:
            sharpness = self.sharpness

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "n": n,
                "label": label,
                "method": method,
                "curve": curve,
                "bins": bins,
                "scorerVersion": scorer_version,
                "computedAt": computed_at,
            }
        )
        if forecaster_id is not UNSET:
            field_dict["forecasterId"] = forecaster_id
        if category is not UNSET:
            field_dict["category"] = category
        if decomposition is not UNSET:
            field_dict["decomposition"] = decomposition
        if sharpness is not UNSET:
            field_dict["sharpness"] = sharpness

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_calibration_bins_item import ForecastCalibrationBinsItem
        from ..models.forecast_calibration_curve_item import (
            ForecastCalibrationCurveItem,
        )
        from ..models.forecast_calibration_decomposition_type_0 import (
            ForecastCalibrationDecompositionType0,
        )

        d = dict(src_dict)
        n = d.pop("n")

        label = ForecastCalibrationLabel(d.pop("label"))

        method = ForecastCalibrationMethod(d.pop("method"))

        curve = []
        _curve = d.pop("curve")
        for curve_item_data in _curve:
            curve_item = ForecastCalibrationCurveItem.from_dict(curve_item_data)

            curve.append(curve_item)

        bins = []
        _bins = d.pop("bins")
        for bins_item_data in _bins:
            bins_item = ForecastCalibrationBinsItem.from_dict(bins_item_data)

            bins.append(bins_item)

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

        def _parse_decomposition(
            data: object,
        ) -> ForecastCalibrationDecompositionType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                decomposition_type_0 = ForecastCalibrationDecompositionType0.from_dict(
                    data
                )

                return decomposition_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ForecastCalibrationDecompositionType0 | None | Unset, data)

        decomposition = _parse_decomposition(d.pop("decomposition", UNSET))

        def _parse_sharpness(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        sharpness = _parse_sharpness(d.pop("sharpness", UNSET))

        forecast_calibration = cls(
            n=n,
            label=label,
            method=method,
            curve=curve,
            bins=bins,
            scorer_version=scorer_version,
            computed_at=computed_at,
            forecaster_id=forecaster_id,
            category=category,
            decomposition=decomposition,
            sharpness=sharpness,
        )

        forecast_calibration.additional_properties = d
        return forecast_calibration

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
