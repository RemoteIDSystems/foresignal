from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PrecursorLagDistributionType0")


@_attrs_define
class PrecursorLagDistributionType0:
    """Lag quartiles of hits

    Attributes:
        p25 (str | Unset): ISO-8601 duration Example: P3D.
        median (str | Unset): ISO-8601 duration Example: P3D.
        p75 (str | Unset): ISO-8601 duration Example: P3D.
    """

    p25: str | Unset = UNSET
    median: str | Unset = UNSET
    p75: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        p25 = self.p25

        median = self.median

        p75 = self.p75

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if p25 is not UNSET:
            field_dict["p25"] = p25
        if median is not UNSET:
            field_dict["median"] = median
        if p75 is not UNSET:
            field_dict["p75"] = p75

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        p25 = d.pop("p25", UNSET)

        median = d.pop("median", UNSET)

        p75 = d.pop("p75", UNSET)

        precursor_lag_distribution_type_0 = cls(
            p25=p25,
            median=median,
            p75=p75,
        )

        precursor_lag_distribution_type_0.additional_properties = d
        return precursor_lag_distribution_type_0

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
