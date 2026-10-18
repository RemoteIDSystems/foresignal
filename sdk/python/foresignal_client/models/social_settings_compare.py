from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_settings_compare_default_window import (
        SocialSettingsCompareDefaultWindow,
    )
    from ..models.social_settings_compare_family_windows import (
        SocialSettingsCompareFamilyWindows,
    )


T = TypeVar("T", bound="SocialSettingsCompare")


@_attrs_define
class SocialSettingsCompare:
    """Social–physical comparison

    Attributes:
        min_groups (int | Unset):  Default: 3.
        default_window (SocialSettingsCompareDefaultWindow | Unset): Window around the claim time
        family_windows (SocialSettingsCompareFamilyWindows | Unset): Signal family → window
    """

    min_groups: int | Unset = 3
    default_window: SocialSettingsCompareDefaultWindow | Unset = UNSET
    family_windows: SocialSettingsCompareFamilyWindows | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        min_groups = self.min_groups

        default_window: dict[str, Any] | Unset = UNSET
        if not isinstance(self.default_window, Unset):
            default_window = self.default_window.to_dict()

        family_windows: dict[str, Any] | Unset = UNSET
        if not isinstance(self.family_windows, Unset):
            family_windows = self.family_windows.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if min_groups is not UNSET:
            field_dict["minGroups"] = min_groups
        if default_window is not UNSET:
            field_dict["defaultWindow"] = default_window
        if family_windows is not UNSET:
            field_dict["familyWindows"] = family_windows

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_settings_compare_default_window import (
            SocialSettingsCompareDefaultWindow,
        )
        from ..models.social_settings_compare_family_windows import (
            SocialSettingsCompareFamilyWindows,
        )

        d = dict(src_dict)
        min_groups = d.pop("minGroups", UNSET)

        _default_window = d.pop("defaultWindow", UNSET)
        default_window: SocialSettingsCompareDefaultWindow | Unset
        if isinstance(_default_window, Unset):
            default_window = UNSET
        else:
            default_window = SocialSettingsCompareDefaultWindow.from_dict(
                _default_window
            )

        _family_windows = d.pop("familyWindows", UNSET)
        family_windows: SocialSettingsCompareFamilyWindows | Unset
        if isinstance(_family_windows, Unset):
            family_windows = UNSET
        else:
            family_windows = SocialSettingsCompareFamilyWindows.from_dict(
                _family_windows
            )

        social_settings_compare = cls(
            min_groups=min_groups,
            default_window=default_window,
            family_windows=family_windows,
        )

        social_settings_compare.additional_properties = d
        return social_settings_compare

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
