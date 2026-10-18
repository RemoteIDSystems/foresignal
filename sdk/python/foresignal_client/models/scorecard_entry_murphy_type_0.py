from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ScorecardEntryMurphyType0")


@_attrs_define
class ScorecardEntryMurphyType0:
    """Murphy decomposition of the Brier score

    Attributes:
        reliability (float):
        resolution (float):
        uncertainty (float):
        within_bin_variance (float | Unset):
        within_bin_covariance (float | Unset):
    """

    reliability: float
    resolution: float
    uncertainty: float
    within_bin_variance: float | Unset = UNSET
    within_bin_covariance: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        reliability = self.reliability

        resolution = self.resolution

        uncertainty = self.uncertainty

        within_bin_variance = self.within_bin_variance

        within_bin_covariance = self.within_bin_covariance

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "reliability": reliability,
                "resolution": resolution,
                "uncertainty": uncertainty,
            }
        )
        if within_bin_variance is not UNSET:
            field_dict["withinBinVariance"] = within_bin_variance
        if within_bin_covariance is not UNSET:
            field_dict["withinBinCovariance"] = within_bin_covariance

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        reliability = d.pop("reliability")

        resolution = d.pop("resolution")

        uncertainty = d.pop("uncertainty")

        within_bin_variance = d.pop("withinBinVariance", UNSET)

        within_bin_covariance = d.pop("withinBinCovariance", UNSET)

        scorecard_entry_murphy_type_0 = cls(
            reliability=reliability,
            resolution=resolution,
            uncertainty=uncertainty,
            within_bin_variance=within_bin_variance,
            within_bin_covariance=within_bin_covariance,
        )

        scorecard_entry_murphy_type_0.additional_properties = d
        return scorecard_entry_murphy_type_0

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
