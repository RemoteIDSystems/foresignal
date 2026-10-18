from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_platform_id import SocialPlatformId
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.social_settings_cues_triggers import SocialSettingsCuesTriggers
    from ..models.social_settings_cues_xai_x_search import SocialSettingsCuesXaiXSearch


T = TypeVar("T", bound="SocialSettingsCues")


@_attrs_define
class SocialSettingsCues:
    """Automatic and manual cues

    Attributes:
        enabled (bool | Unset):  Default: True.
        triggers (SocialSettingsCuesTriggers | Unset): Physical triggers
        max_per_day (int | Unset):  Default: 50.
        max_cost_usd_per_cue (float | Unset):  Default: 1.0.
        platforms (list[SocialPlatformId] | Unset): Search-capable adapters cues may use; default all enabled
        xai_x_search (SocialSettingsCuesXaiXSearch | Unset): Optional xai-x-search backend
    """

    enabled: bool | Unset = True
    triggers: SocialSettingsCuesTriggers | Unset = UNSET
    max_per_day: int | Unset = 50
    max_cost_usd_per_cue: float | Unset = 1.0
    platforms: list[SocialPlatformId] | Unset = UNSET
    xai_x_search: SocialSettingsCuesXaiXSearch | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        triggers: dict[str, Any] | Unset = UNSET
        if not isinstance(self.triggers, Unset):
            triggers = self.triggers.to_dict()

        max_per_day = self.max_per_day

        max_cost_usd_per_cue = self.max_cost_usd_per_cue

        platforms: list[str] | Unset = UNSET
        if not isinstance(self.platforms, Unset):
            platforms = []
            for platforms_item_data in self.platforms:
                platforms_item = platforms_item_data.value
                platforms.append(platforms_item)

        xai_x_search: dict[str, Any] | Unset = UNSET
        if not isinstance(self.xai_x_search, Unset):
            xai_x_search = self.xai_x_search.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if triggers is not UNSET:
            field_dict["triggers"] = triggers
        if max_per_day is not UNSET:
            field_dict["maxPerDay"] = max_per_day
        if max_cost_usd_per_cue is not UNSET:
            field_dict["maxCostUsdPerCue"] = max_cost_usd_per_cue
        if platforms is not UNSET:
            field_dict["platforms"] = platforms
        if xai_x_search is not UNSET:
            field_dict["xaiXSearch"] = xai_x_search

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.social_settings_cues_triggers import SocialSettingsCuesTriggers
        from ..models.social_settings_cues_xai_x_search import (
            SocialSettingsCuesXaiXSearch,
        )

        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        _triggers = d.pop("triggers", UNSET)
        triggers: SocialSettingsCuesTriggers | Unset
        if isinstance(_triggers, Unset):
            triggers = UNSET
        else:
            triggers = SocialSettingsCuesTriggers.from_dict(_triggers)

        max_per_day = d.pop("maxPerDay", UNSET)

        max_cost_usd_per_cue = d.pop("maxCostUsdPerCue", UNSET)

        _platforms = d.pop("platforms", UNSET)
        platforms: list[SocialPlatformId] | Unset = UNSET
        if _platforms is not UNSET:
            platforms = []
            for platforms_item_data in _platforms:
                platforms_item = SocialPlatformId(platforms_item_data)

                platforms.append(platforms_item)

        _xai_x_search = d.pop("xaiXSearch", UNSET)
        xai_x_search: SocialSettingsCuesXaiXSearch | Unset
        if isinstance(_xai_x_search, Unset):
            xai_x_search = UNSET
        else:
            xai_x_search = SocialSettingsCuesXaiXSearch.from_dict(_xai_x_search)

        social_settings_cues = cls(
            enabled=enabled,
            triggers=triggers,
            max_per_day=max_per_day,
            max_cost_usd_per_cue=max_cost_usd_per_cue,
            platforms=platforms,
            xai_x_search=xai_x_search,
        )

        social_settings_cues.additional_properties = d
        return social_settings_cues

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
