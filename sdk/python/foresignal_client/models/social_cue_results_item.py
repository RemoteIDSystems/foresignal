from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.social_platform_id import SocialPlatformId
from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialCueResultsItem")


@_attrs_define
class SocialCueResultsItem:
    """Posts found on one platform

    Attributes:
        platform (SocialPlatformId): Social platform adapter (a 004 source with descriptor, budget, health and
            credentials). Only `bluesky-jetstream` is on by default (135)
        posts (int):
        new_posts (int | Unset):
        cost_usd (float | Unset):
        error (None | str | Unset):
    """

    platform: SocialPlatformId
    posts: int
    new_posts: int | Unset = UNSET
    cost_usd: float | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        platform = self.platform.value

        posts = self.posts

        new_posts = self.new_posts

        cost_usd = self.cost_usd

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "platform": platform,
                "posts": posts,
            }
        )
        if new_posts is not UNSET:
            field_dict["newPosts"] = new_posts
        if cost_usd is not UNSET:
            field_dict["costUsd"] = cost_usd
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        platform = SocialPlatformId(d.pop("platform"))

        posts = d.pop("posts")

        new_posts = d.pop("newPosts", UNSET)

        cost_usd = d.pop("costUsd", UNSET)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        social_cue_results_item = cls(
            platform=platform,
            posts=posts,
            new_posts=new_posts,
            cost_usd=cost_usd,
            error=error,
        )

        social_cue_results_item.additional_properties = d
        return social_cue_results_item

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
