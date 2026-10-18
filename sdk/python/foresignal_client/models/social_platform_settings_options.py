from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_platform_settings_options_fields_param import (
    SocialPlatformSettingsOptionsFieldsParam,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialPlatformSettingsOptions")


@_attrs_define
class SocialPlatformSettingsOptions:
    """Platform-specific options

    Attributes:
        instances (list[str] | Unset): Mastodon instance hosts
        replay (bool | Unset): Bluesky Jetstream Network Replay backfill (operator API token)
        standing_counts (bool | Unset): X hourly counts for standing topics
        full_archive (bool | Unset): X full-archive endpoints (upgraded access)
        comments (bool | Unset): YouTube comment threads
        max_results (int | Unset): X: max_results of a recent search (each post returned is billed) Default: 10.
        fields_param (SocialPlatformSettingsOptionsFieldsParam | Unset): X: the field parameter naming, post.fields (the
            live OpenAPI) or tweet.fields (the data dictionary); X's documents disagree (135) Default:
            SocialPlatformSettingsOptionsFieldsParam.POST.
    """

    instances: list[str] | Unset = UNSET
    replay: bool | Unset = UNSET
    standing_counts: bool | Unset = UNSET
    full_archive: bool | Unset = UNSET
    comments: bool | Unset = UNSET
    max_results: int | Unset = 10
    fields_param: SocialPlatformSettingsOptionsFieldsParam | Unset = (
        SocialPlatformSettingsOptionsFieldsParam.POST
    )
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instances: list[str] | Unset = UNSET
        if not isinstance(self.instances, Unset):
            instances = self.instances

        replay = self.replay

        standing_counts = self.standing_counts

        full_archive = self.full_archive

        comments = self.comments

        max_results = self.max_results

        fields_param: str | Unset = UNSET
        if not isinstance(self.fields_param, Unset):
            fields_param = self.fields_param.value

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if instances is not UNSET:
            field_dict["instances"] = instances
        if replay is not UNSET:
            field_dict["replay"] = replay
        if standing_counts is not UNSET:
            field_dict["standingCounts"] = standing_counts
        if full_archive is not UNSET:
            field_dict["fullArchive"] = full_archive
        if comments is not UNSET:
            field_dict["comments"] = comments
        if max_results is not UNSET:
            field_dict["maxResults"] = max_results
        if fields_param is not UNSET:
            field_dict["fieldsParam"] = fields_param

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        instances = cast(list[str], d.pop("instances", UNSET))

        replay = d.pop("replay", UNSET)

        standing_counts = d.pop("standingCounts", UNSET)

        full_archive = d.pop("fullArchive", UNSET)

        comments = d.pop("comments", UNSET)

        max_results = d.pop("maxResults", UNSET)

        _fields_param = d.pop("fieldsParam", UNSET)
        fields_param: SocialPlatformSettingsOptionsFieldsParam | Unset
        if isinstance(_fields_param, Unset):
            fields_param = UNSET
        else:
            fields_param = SocialPlatformSettingsOptionsFieldsParam(_fields_param)

        social_platform_settings_options = cls(
            instances=instances,
            replay=replay,
            standing_counts=standing_counts,
            full_archive=full_archive,
            comments=comments,
            max_results=max_results,
            fields_param=fields_param,
        )

        social_platform_settings_options.additional_properties = d
        return social_platform_settings_options

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
