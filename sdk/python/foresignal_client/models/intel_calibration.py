from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.intel_calibration_scope import IntelCalibrationScope
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.intel_calibration_bins_item import IntelCalibrationBinsItem
    from ..models.intel_calibration_curve_item import IntelCalibrationCurveItem


T = TypeVar("T", bound="IntelCalibration")


@_attrs_define
class IntelCalibration:
    """The fitted calibration of one scope (151): the CORP recalibration curve, the 10-bin reliability table and the Brier
    score over resolved outcomes

        Attributes:
            scope (IntelCalibrationScope):
            calibrated (bool): True once at least `minSamples` outcomes have resolved; until then the raw score is reported
                unchanged
            sample_n (int): Resolved outcomes behind the fit
            min_samples (int): Outcomes needed before a scope is calibrated
            brier (float | None | Unset): Mean Brier score of the raw scores against the outcomes
            bins (list[IntelCalibrationBinsItem] | Unset): The 10-bin reliability table
            curve (list[IntelCalibrationCurveItem] | Unset): The CORP (PAV) recalibration curve, one point per distinct raw
                score
            fitted_at (datetime.datetime | None | Unset):
            updated_at (datetime.datetime | None | Unset):
    """

    scope: IntelCalibrationScope
    calibrated: bool
    sample_n: int
    min_samples: int
    brier: float | None | Unset = UNSET
    bins: list[IntelCalibrationBinsItem] | Unset = UNSET
    curve: list[IntelCalibrationCurveItem] | Unset = UNSET
    fitted_at: datetime.datetime | None | Unset = UNSET
    updated_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        scope = self.scope.value

        calibrated = self.calibrated

        sample_n = self.sample_n

        min_samples = self.min_samples

        brier: float | None | Unset
        if isinstance(self.brier, Unset):
            brier = UNSET
        else:
            brier = self.brier

        bins: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.bins, Unset):
            bins = []
            for bins_item_data in self.bins:
                bins_item = bins_item_data.to_dict()
                bins.append(bins_item)

        curve: list[dict[str, Any]] | Unset = UNSET
        if not isinstance(self.curve, Unset):
            curve = []
            for curve_item_data in self.curve:
                curve_item = curve_item_data.to_dict()
                curve.append(curve_item)

        fitted_at: None | str | Unset
        if isinstance(self.fitted_at, Unset):
            fitted_at = UNSET
        elif isinstance(self.fitted_at, datetime.datetime):
            fitted_at = self.fitted_at.isoformat()
        else:
            fitted_at = self.fitted_at

        updated_at: None | str | Unset
        if isinstance(self.updated_at, Unset):
            updated_at = UNSET
        elif isinstance(self.updated_at, datetime.datetime):
            updated_at = self.updated_at.isoformat()
        else:
            updated_at = self.updated_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "scope": scope,
                "calibrated": calibrated,
                "sampleN": sample_n,
                "minSamples": min_samples,
            }
        )
        if brier is not UNSET:
            field_dict["brier"] = brier
        if bins is not UNSET:
            field_dict["bins"] = bins
        if curve is not UNSET:
            field_dict["curve"] = curve
        if fitted_at is not UNSET:
            field_dict["fittedAt"] = fitted_at
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.intel_calibration_bins_item import IntelCalibrationBinsItem
        from ..models.intel_calibration_curve_item import IntelCalibrationCurveItem

        d = dict(src_dict)
        scope = IntelCalibrationScope(d.pop("scope"))

        calibrated = d.pop("calibrated")

        sample_n = d.pop("sampleN")

        min_samples = d.pop("minSamples")

        def _parse_brier(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        brier = _parse_brier(d.pop("brier", UNSET))

        _bins = d.pop("bins", UNSET)
        bins: list[IntelCalibrationBinsItem] | Unset = UNSET
        if _bins is not UNSET:
            bins = []
            for bins_item_data in _bins:
                bins_item = IntelCalibrationBinsItem.from_dict(bins_item_data)

                bins.append(bins_item)

        _curve = d.pop("curve", UNSET)
        curve: list[IntelCalibrationCurveItem] | Unset = UNSET
        if _curve is not UNSET:
            curve = []
            for curve_item_data in _curve:
                curve_item = IntelCalibrationCurveItem.from_dict(curve_item_data)

                curve.append(curve_item)

        def _parse_fitted_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                fitted_at_type_0 = datetime.datetime.fromisoformat(data)

                return fitted_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        fitted_at = _parse_fitted_at(d.pop("fittedAt", UNSET))

        def _parse_updated_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                updated_at_type_0 = datetime.datetime.fromisoformat(data)

                return updated_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        updated_at = _parse_updated_at(d.pop("updatedAt", UNSET))

        intel_calibration = cls(
            scope=scope,
            calibrated=calibrated,
            sample_n=sample_n,
            min_samples=min_samples,
            brier=brier,
            bins=bins,
            curve=curve,
            fitted_at=fitted_at,
            updated_at=updated_at,
        )

        intel_calibration.additional_properties = d
        return intel_calibration

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
