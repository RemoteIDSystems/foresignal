from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialSettingsCuesXaiXSearch")


@_attrs_define
class SocialSettingsCuesXaiXSearch:
    """Optional xai-x-search backend

    Attributes:
        enabled (bool | Unset):  Default: False.
        max_results (int | Unset):  Default: 20.
    """

    enabled: bool | Unset = False
    max_results: int | Unset = 20
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        max_results = self.max_results

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if max_results is not UNSET:
            field_dict["maxResults"] = max_results

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        max_results = d.pop("maxResults", UNSET)

        social_settings_cues_xai_x_search = cls(
            enabled=enabled,
            max_results=max_results,
        )

        social_settings_cues_xai_x_search.additional_properties = d
        return social_settings_cues_xai_x_search

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
