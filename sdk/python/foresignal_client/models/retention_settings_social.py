from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.retention_settings_social_platforms import (
        RetentionSettingsSocialPlatforms,
    )
    from ..models.retention_settings_social_posts import RetentionSettingsSocialPosts


T = TypeVar("T", bound="RetentionSettingsSocial")


@_attrs_define
class RetentionSettingsSocial:
    """Analytic class `social` (135): `social-post` retention per platform (view over `social.postRetention`); narratives,
    claims, comparisons and volume series are kept as intel and series

        Attributes:
            posts (RetentionSettingsSocialPosts | Unset): Default post retention
            platforms (RetentionSettingsSocialPlatforms | Unset): Network → post retention Example: {'x': '30d', 'bluesky':
                '90d'}.
    """

    posts: RetentionSettingsSocialPosts | Unset = UNSET
    platforms: RetentionSettingsSocialPlatforms | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        posts: dict[str, Any] | Unset = UNSET
        if not isinstance(self.posts, Unset):
            posts = self.posts.to_dict()

        platforms: dict[str, Any] | Unset = UNSET
        if not isinstance(self.platforms, Unset):
            platforms = self.platforms.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if posts is not UNSET:
            field_dict["posts"] = posts
        if platforms is not UNSET:
            field_dict["platforms"] = platforms

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.retention_settings_social_platforms import (
            RetentionSettingsSocialPlatforms,
        )
        from ..models.retention_settings_social_posts import (
            RetentionSettingsSocialPosts,
        )

        d = dict(src_dict)
        _posts = d.pop("posts", UNSET)
        posts: RetentionSettingsSocialPosts | Unset
        if isinstance(_posts, Unset):
            posts = UNSET
        else:
            posts = RetentionSettingsSocialPosts.from_dict(_posts)

        _platforms = d.pop("platforms", UNSET)
        platforms: RetentionSettingsSocialPlatforms | Unset
        if isinstance(_platforms, Unset):
            platforms = UNSET
        else:
            platforms = RetentionSettingsSocialPlatforms.from_dict(_platforms)

        retention_settings_social = cls(
            posts=posts,
            platforms=platforms,
        )

        retention_settings_social.additional_properties = d
        return retention_settings_social

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
