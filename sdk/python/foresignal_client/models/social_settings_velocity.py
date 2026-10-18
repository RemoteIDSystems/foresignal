from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialSettingsVelocity")


@_attrs_define
class SocialSettingsVelocity:
    """`social-velocity` finding thresholds

    Attributes:
        min_ratio (float | Unset):  Default: 5.0.
        min_unique_authors (int | Unset):  Default: 20.
        dedup_hours (int | Unset):  Default: 6.
    """

    min_ratio: float | Unset = 5.0
    min_unique_authors: int | Unset = 20
    dedup_hours: int | Unset = 6
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        min_ratio = self.min_ratio

        min_unique_authors = self.min_unique_authors

        dedup_hours = self.dedup_hours

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if min_ratio is not UNSET:
            field_dict["minRatio"] = min_ratio
        if min_unique_authors is not UNSET:
            field_dict["minUniqueAuthors"] = min_unique_authors
        if dedup_hours is not UNSET:
            field_dict["dedupHours"] = dedup_hours

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        min_ratio = d.pop("minRatio", UNSET)

        min_unique_authors = d.pop("minUniqueAuthors", UNSET)

        dedup_hours = d.pop("dedupHours", UNSET)

        social_settings_velocity = cls(
            min_ratio=min_ratio,
            min_unique_authors=min_unique_authors,
            dedup_hours=dedup_hours,
        )

        social_settings_velocity.additional_properties = d
        return social_settings_velocity

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
