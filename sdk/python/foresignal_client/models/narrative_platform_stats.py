from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="NarrativePlatformStats")


@_attrs_define
class NarrativePlatformStats:
    """A narrative's presence on one platform; `earliestPostRef` is the earliest observed post, never the originator

    Attributes:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        posts (int):
        unique_authors (int):
        first_observed_at (datetime.datetime):
        earliest_post_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
    """

    platform: str
    posts: int
    unique_authors: int
    first_observed_at: datetime.datetime
    earliest_post_ref: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        platform = self.platform

        posts = self.posts

        unique_authors = self.unique_authors

        first_observed_at = self.first_observed_at.isoformat()

        earliest_post_ref = self.earliest_post_ref

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "platform": platform,
                "posts": posts,
                "uniqueAuthors": unique_authors,
                "firstObservedAt": first_observed_at,
                "earliestPostRef": earliest_post_ref,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        platform = d.pop("platform")

        posts = d.pop("posts")

        unique_authors = d.pop("uniqueAuthors")

        first_observed_at = datetime.datetime.fromisoformat(d.pop("firstObservedAt"))

        earliest_post_ref = d.pop("earliestPostRef")

        narrative_platform_stats = cls(
            platform=platform,
            posts=posts,
            unique_authors=unique_authors,
            first_observed_at=first_observed_at,
            earliest_post_ref=earliest_post_ref,
        )

        narrative_platform_stats.additional_properties = d
        return narrative_platform_stats

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
