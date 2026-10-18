from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="NarrativeTimelineSeriesItemPointsItem")


@_attrs_define
class NarrativeTimelineSeriesItemPointsItem:
    """One interval

    Attributes:
        t (datetime.datetime):
        posts (int):
        unique_authors (int):
    """

    t: datetime.datetime
    posts: int
    unique_authors: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        t = self.t.isoformat()

        posts = self.posts

        unique_authors = self.unique_authors

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "t": t,
                "posts": posts,
                "uniqueAuthors": unique_authors,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        t = datetime.datetime.fromisoformat(d.pop("t"))

        posts = d.pop("posts")

        unique_authors = d.pop("uniqueAuthors")

        narrative_timeline_series_item_points_item = cls(
            t=t,
            posts=posts,
            unique_authors=unique_authors,
        )

        narrative_timeline_series_item_points_item.additional_properties = d
        return narrative_timeline_series_item_points_item

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
