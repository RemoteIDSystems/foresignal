from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialSettingsCompareFamilyWindowsAdditionalProperty")


@_attrs_define
class SocialSettingsCompareFamilyWindowsAdditionalProperty:
    """Family window override (`social-compare.json`)

    Attributes:
        before (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 30d.
        after (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 30d.
        tolerance_km (float | Unset):
        publication_lag (str | Unset): ISO-8601 duration (P7D, PT15M) or shorthand (7d, 15m, 12h) Example: 30d.
    """

    before: str | Unset = UNSET
    after: str | Unset = UNSET
    tolerance_km: float | Unset = UNSET
    publication_lag: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        before = self.before

        after = self.after

        tolerance_km = self.tolerance_km

        publication_lag = self.publication_lag

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if before is not UNSET:
            field_dict["before"] = before
        if after is not UNSET:
            field_dict["after"] = after
        if tolerance_km is not UNSET:
            field_dict["toleranceKm"] = tolerance_km
        if publication_lag is not UNSET:
            field_dict["publicationLag"] = publication_lag

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        before = d.pop("before", UNSET)

        after = d.pop("after", UNSET)

        tolerance_km = d.pop("toleranceKm", UNSET)

        publication_lag = d.pop("publicationLag", UNSET)

        social_settings_compare_family_windows_additional_property = cls(
            before=before,
            after=after,
            tolerance_km=tolerance_km,
            publication_lag=publication_lag,
        )

        social_settings_compare_family_windows_additional_property.additional_properties = d
        return social_settings_compare_family_windows_additional_property

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
