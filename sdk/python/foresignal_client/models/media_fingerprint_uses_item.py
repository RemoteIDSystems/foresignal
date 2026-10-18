from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MediaFingerprintUsesItem")


@_attrs_define
class MediaFingerprintUsesItem:
    """One use (detail view; latest 100)

    Attributes:
        post_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        seen_at (datetime.datetime):
        platform (str | Unset): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`,
            or the platform an EDP provider (024, 138) declares Example: bluesky.
    """

    post_ref: str
    seen_at: datetime.datetime
    platform: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        post_ref = self.post_ref

        seen_at = self.seen_at.isoformat()

        platform = self.platform

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "postRef": post_ref,
                "seenAt": seen_at,
            }
        )
        if platform is not UNSET:
            field_dict["platform"] = platform

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        post_ref = d.pop("postRef")

        seen_at = datetime.datetime.fromisoformat(d.pop("seenAt"))

        platform = d.pop("platform", UNSET)

        media_fingerprint_uses_item = cls(
            post_ref=post_ref,
            seen_at=seen_at,
            platform=platform,
        )

        media_fingerprint_uses_item.additional_properties = d
        return media_fingerprint_uses_item

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
