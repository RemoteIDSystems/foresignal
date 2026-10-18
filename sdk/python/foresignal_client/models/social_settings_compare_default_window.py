from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_settings_compare_default_window_after import (
        SocialSettingsCompareDefaultWindowAfter,
    )
    from ..models.social_settings_compare_default_window_before import (
        SocialSettingsCompareDefaultWindowBefore,
    )


T = TypeVar("T", bound="SocialSettingsCompareDefaultWindow")


@_attrs_define
class SocialSettingsCompareDefaultWindow:
    """Window around the claim time

    Attributes:
        before (SocialSettingsCompareDefaultWindowBefore | Unset):
        after (SocialSettingsCompareDefaultWindowAfter | Unset):
    """

    before: SocialSettingsCompareDefaultWindowBefore | Unset = UNSET
    after: SocialSettingsCompareDefaultWindowAfter | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        before: dict[str, Any] | Unset = UNSET
        if not isinstance(self.before, Unset):
            before = self.before.to_dict()

        after: dict[str, Any] | Unset = UNSET
        if not isinstance(self.after, Unset):
            after = self.after.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if before is not UNSET:
            field_dict["before"] = before
        if after is not UNSET:
            field_dict["after"] = after

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_settings_compare_default_window_after import (
            SocialSettingsCompareDefaultWindowAfter,
        )
        from ..models.social_settings_compare_default_window_before import (
            SocialSettingsCompareDefaultWindowBefore,
        )

        d = dict(src_dict)
        _before = d.pop("before", UNSET)
        before: SocialSettingsCompareDefaultWindowBefore | Unset
        if isinstance(_before, Unset):
            before = UNSET
        else:
            before = SocialSettingsCompareDefaultWindowBefore.from_dict(_before)

        _after = d.pop("after", UNSET)
        after: SocialSettingsCompareDefaultWindowAfter | Unset
        if isinstance(_after, Unset):
            after = UNSET
        else:
            after = SocialSettingsCompareDefaultWindowAfter.from_dict(_after)

        social_settings_compare_default_window = cls(
            before=before,
            after=after,
        )

        social_settings_compare_default_window.additional_properties = d
        return social_settings_compare_default_window

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
