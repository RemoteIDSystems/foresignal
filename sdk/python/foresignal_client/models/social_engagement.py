from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="SocialEngagement")


@_attrs_define
class SocialEngagement:
    """Engagement counts at capture; null where the platform does not supply them

    Attributes:
        replies (int | None | Unset):
        reposts (int | None | Unset):
        quotes (int | None | Unset):
        likes (int | None | Unset):
        views (int | None | Unset):
    """

    replies: int | None | Unset = UNSET
    reposts: int | None | Unset = UNSET
    quotes: int | None | Unset = UNSET
    likes: int | None | Unset = UNSET
    views: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        replies: int | None | Unset
        if isinstance(self.replies, Unset):
            replies = UNSET
        else:
            replies = self.replies

        reposts: int | None | Unset
        if isinstance(self.reposts, Unset):
            reposts = UNSET
        else:
            reposts = self.reposts

        quotes: int | None | Unset
        if isinstance(self.quotes, Unset):
            quotes = UNSET
        else:
            quotes = self.quotes

        likes: int | None | Unset
        if isinstance(self.likes, Unset):
            likes = UNSET
        else:
            likes = self.likes

        views: int | None | Unset
        if isinstance(self.views, Unset):
            views = UNSET
        else:
            views = self.views

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if replies is not UNSET:
            field_dict["replies"] = replies
        if reposts is not UNSET:
            field_dict["reposts"] = reposts
        if quotes is not UNSET:
            field_dict["quotes"] = quotes
        if likes is not UNSET:
            field_dict["likes"] = likes
        if views is not UNSET:
            field_dict["views"] = views

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)

        def _parse_replies(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        replies = _parse_replies(d.pop("replies", UNSET))

        def _parse_reposts(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        reposts = _parse_reposts(d.pop("reposts", UNSET))

        def _parse_quotes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        quotes = _parse_quotes(d.pop("quotes", UNSET))

        def _parse_likes(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        likes = _parse_likes(d.pop("likes", UNSET))

        def _parse_views(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        views = _parse_views(d.pop("views", UNSET))

        social_engagement = cls(
            replies=replies,
            reposts=reposts,
            quotes=quotes,
            likes=likes,
            views=views,
        )

        social_engagement.additional_properties = d
        return social_engagement

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
