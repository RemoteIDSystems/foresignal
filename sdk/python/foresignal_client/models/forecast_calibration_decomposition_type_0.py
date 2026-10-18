from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="ForecastCalibrationDecompositionType0")


@_attrs_define
class ForecastCalibrationDecompositionType0:
    """CORP decomposition

    Attributes:
        score (float):
        miscalibration (float):
        discrimination (float):
        uncertainty (float):
    """

    score: float
    miscalibration: float
    discrimination: float
    uncertainty: float
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        score = self.score

        miscalibration = self.miscalibration

        discrimination = self.discrimination

        uncertainty = self.uncertainty

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "score": score,
                "miscalibration": miscalibration,
                "discrimination": discrimination,
                "uncertainty": uncertainty,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        score = d.pop("score")

        miscalibration = d.pop("miscalibration")

        discrimination = d.pop("discrimination")

        uncertainty = d.pop("uncertainty")

        forecast_calibration_decomposition_type_0 = cls(
            score=score,
            miscalibration=miscalibration,
            discrimination=discrimination,
            uncertainty=uncertainty,
        )

        forecast_calibration_decomposition_type_0.additional_properties = d
        return forecast_calibration_decomposition_type_0

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
