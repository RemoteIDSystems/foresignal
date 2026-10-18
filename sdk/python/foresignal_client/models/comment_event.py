from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.comment_event_reason import CommentEventReason
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.principal_summary import PrincipalSummary


T = TypeVar("T", bound="CommentEvent")


@_attrs_define
class CommentEvent:
    """SSE `mention` and `comment.reply` on the `me` topic

    Attributes:
        reason (CommentEventReason):
        annotation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        target_ref (str): Canonical object reference `{type}:{id}` (docs/Client-Contract.md §2). Record refs are
            `record:{sourceId}/{externalId}[@{seq}]` with the external id percent-encoded. Remote refs (132) are
            `remote:{instanceId}/{type}:{id}`: read-only material received from another instance. Example:
            situation:01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        author (PrincipalSummary): Public view of a principal: handle, display name, kind and avatar only
        at (datetime.datetime):
        parent_id (None | str | Unset):
        excerpt (str | Unset):
    """

    reason: CommentEventReason
    annotation_id: str
    target_ref: str
    author: PrincipalSummary
    at: datetime.datetime
    parent_id: None | str | Unset = UNSET
    excerpt: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        reason = self.reason.value

        annotation_id = self.annotation_id

        target_ref = self.target_ref

        author = self.author.to_dict()

        at = self.at.isoformat()

        parent_id: None | str | Unset
        if isinstance(self.parent_id, Unset):
            parent_id = UNSET
        else:
            parent_id = self.parent_id

        excerpt = self.excerpt

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "reason": reason,
                "annotationId": annotation_id,
                "targetRef": target_ref,
                "author": author,
                "at": at,
            }
        )
        if parent_id is not UNSET:
            field_dict["parentId"] = parent_id
        if excerpt is not UNSET:
            field_dict["excerpt"] = excerpt

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.principal_summary import PrincipalSummary

        d = dict(src_dict)
        reason = CommentEventReason(d.pop("reason"))

        annotation_id = d.pop("annotationId")

        target_ref = d.pop("targetRef")

        author = PrincipalSummary.from_dict(d.pop("author"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_parent_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        parent_id = _parse_parent_id(d.pop("parentId", UNSET))

        excerpt = d.pop("excerpt", UNSET)

        comment_event = cls(
            reason=reason,
            annotation_id=annotation_id,
            target_ref=target_ref,
            author=author,
            at=at,
            parent_id=parent_id,
            excerpt=excerpt,
        )

        comment_event.additional_properties = d
        return comment_event

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
