from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.attention_topic_social_channel_type import AttentionTopicSocialChannelType

T = TypeVar("T", bound="AttentionTopicSocialChannel")


@_attrs_define
class AttentionTopicSocialChannel:
    """A channel followed for a topic

    Attributes:
        platform (str): Social network: `bluesky`, `mastodon`, `reddit`, `youtube`, `x`, `threads`, `instagram`, or the
            platform an EDP provider (024, 138) declares Example: bluesky.
        type_ (AttentionTopicSocialChannelType):
        id (str): Subreddit name, channel username, instance host, hashtag or YouTube channel id
    """

    platform: str
    type_: AttentionTopicSocialChannelType
    id: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        platform = self.platform

        type_ = self.type_.value

        id = self.id

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "platform": platform,
                "type": type_,
                "id": id,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        platform = d.pop("platform")

        type_ = AttentionTopicSocialChannelType(d.pop("type"))

        id = d.pop("id")

        attention_topic_social_channel = cls(
            platform=platform,
            type_=type_,
            id=id,
        )

        attention_topic_social_channel.additional_properties = d
        return attention_topic_social_channel

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
