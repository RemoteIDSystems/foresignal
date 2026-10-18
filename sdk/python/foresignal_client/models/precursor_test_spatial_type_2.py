from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.precursor_test_spatial_type_2_same_region import (
    PrecursorTestSpatialType2SameRegion,
)

T = TypeVar("T", bound="PrecursorTestSpatialType2")


@_attrs_define
class PrecursorTestSpatialType2:
    """Region rule

    Attributes:
        same_region (PrecursorTestSpatialType2SameRegion):
    """

    same_region: PrecursorTestSpatialType2SameRegion
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        same_region = self.same_region.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "sameRegion": same_region,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        same_region = PrecursorTestSpatialType2SameRegion(d.pop("sameRegion"))

        precursor_test_spatial_type_2 = cls(
            same_region=same_region,
        )

        precursor_test_spatial_type_2.additional_properties = d
        return precursor_test_spatial_type_2

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
