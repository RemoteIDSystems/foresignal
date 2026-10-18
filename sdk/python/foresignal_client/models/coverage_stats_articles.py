from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="CoverageStatsArticles")


@_attrs_define
class CoverageStatsArticles:
    """Counts by lean bucket

    Attributes:
        left (int):
        center (int):
        right (int):
        unrated (int):
    """

    left: int
    center: int
    right: int
    unrated: int
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

        coverage_stats_articles = cls(
            left=left,
            center=center,
            right=right,
            unrated=unrated,
        )

        coverage_stats_articles.additional_properties = d
        return coverage_stats_articles

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
