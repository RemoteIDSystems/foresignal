from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="HazardForecastDetails")


@_attrs_define
class HazardForecastDetails:
    """Source-specific facts: NHC thresholdKt and horizonHours; SPC day and layer; WPC day and categoryName; a gauge's
    basis (forecast or persistence), categories, calibration {basis, minCategory, n, hits, p, lower, upper, minSamples}
    and reason when no probability is derived (below-category, uncalibrated, no-geometry); a storm cell's reason (no-
    position-error)

    """

    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        hazard_forecast_details = cls()

        hazard_forecast_details.additional_properties = d
        return hazard_forecast_details

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
