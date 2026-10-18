from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.annotation import Annotation


T = TypeVar("T", bound="AnnotationThread")


@_attrs_define
class AnnotationThread:
    """A comment thread: a root annotation with its replies (129 annotations extended by 130)

    Attributes:
        root (Annotation): A note or verdict on any object (labels use 130 tags)
        replies (list[Annotation]):
        reply_count (int):
        resolved (bool):
        last_activity_at (datetime.datetime):
    """

    root: Annotation
    replies: list[Annotation]
    reply_count: int
    resolved: bool
    last_activity_at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        root = self.root.to_dict()

        replies = []
        for replies_item_data in self.replies:
            replies_item = replies_item_data.to_dict()
            replies.append(replies_item)

        reply_count = self.reply_count

        resolved = self.resolved

        last_activity_at = self.last_activity_at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "root": root,
                "replies": replies,
                "replyCount": reply_count,
                "resolved": resolved,
                "lastActivityAt": last_activity_at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.annotation import Annotation

        d = dict(src_dict)
        root = Annotation.from_dict(d.pop("root"))

        replies = []
        _replies = d.pop("replies")
        for replies_item_data in _replies:
            replies_item = Annotation.from_dict(replies_item_data)

            replies.append(replies_item)

        reply_count = d.pop("replyCount")

        resolved = d.pop("resolved")

        last_activity_at = datetime.datetime.fromisoformat(d.pop("lastActivityAt"))

        annotation_thread = cls(
            root=root,
            replies=replies,
            reply_count=reply_count,
            resolved=resolved,
            last_activity_at=last_activity_at,
        )

        annotation_thread.additional_properties = d
        return annotation_thread

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
