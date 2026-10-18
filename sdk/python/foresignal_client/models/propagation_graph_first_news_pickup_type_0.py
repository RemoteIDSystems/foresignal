from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PropagationGraphFirstNewsPickupType0")


@_attrs_define
class PropagationGraphFirstNewsPickupType0:
    """First 133 story linked by URL or text

    Attributes:
        story_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        observed_at (datetime.datetime):
        article_ref (None | str | Unset):
        after_first_post_seconds (float | Unset):
    """

    story_id: str
    observed_at: datetime.datetime
    article_ref: None | str | Unset = UNSET
    after_first_post_seconds: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        story_id = self.story_id

        observed_at = self.observed_at.isoformat()

        article_ref: None | str | Unset
        if isinstance(self.article_ref, Unset):
            article_ref = UNSET
        else:
            article_ref = self.article_ref

        after_first_post_seconds = self.after_first_post_seconds

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "storyId": story_id,
                "observedAt": observed_at,
            }
        )
        if article_ref is not UNSET:
            field_dict["articleRef"] = article_ref
        if after_first_post_seconds is not UNSET:
            field_dict["afterFirstPostSeconds"] = after_first_post_seconds

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        story_id = d.pop("storyId")

        observed_at = datetime.datetime.fromisoformat(d.pop("observedAt"))

        def _parse_article_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        article_ref = _parse_article_ref(d.pop("articleRef", UNSET))

        after_first_post_seconds = d.pop("afterFirstPostSeconds", UNSET)

        propagation_graph_first_news_pickup_type_0 = cls(
            story_id=story_id,
            observed_at=observed_at,
            article_ref=article_ref,
            after_first_post_seconds=after_first_post_seconds,
        )

        propagation_graph_first_news_pickup_type_0.additional_properties = d
        return propagation_graph_first_news_pickup_type_0

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
