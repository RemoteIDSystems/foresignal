from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="CoverageStatsCoverageBalance")


@_attrs_define
class CoverageStatsCoverageBalance:
    """Shares by lean bucket (0–1)

    Attributes:
        left (float):
        center (float):
        right (float):
        unrated (float):
    """

    left: float
    center: float
    right: float
    unrated: float
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        left = self.left

        center = self.center

        right = self.right

        unrated = self.unrated

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "left": left,
                "center": center,
                "right": right,
                "unrated": unrated,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        left = d.pop("left")

        center = d.pop("center")

        right = d.pop("right")

        unrated = d.pop("unrated")

        coverage_stats_coverage_balance = cls(
            left=left,
            center=center,
            right=right,
            unrated=unrated,
        )

        coverage_stats_coverage_balance.additional_properties = d
        return coverage_stats_coverage_balance

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
