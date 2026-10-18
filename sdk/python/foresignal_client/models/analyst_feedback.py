from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.analyst_feedback_rating import AnalystFeedbackRating
from ..models.analyst_feedback_target import AnalystFeedbackTarget
from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalystFeedback")


@_attrs_define
class AnalystFeedback:
    """Feedback on a note or proposal

    Attributes:
        id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        target (AnalystFeedbackTarget):
        target_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        rating (AnalystFeedbackRating):
        by (str):
        at (datetime.datetime):
        comment (None | str | Unset):
    """

    id: str
    target: AnalystFeedbackTarget
    target_id: str
    rating: AnalystFeedbackRating
    by: str
    at: datetime.datetime
    comment: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        target = self.target.value

        target_id = self.target_id

        rating = self.rating.value

        by = self.by

        at = self.at.isoformat()

        comment: None | str | Unset
        if isinstance(self.comment, Unset):
            comment = UNSET
        else:
            comment = self.comment

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "target": target,
                "targetId": target_id,
                "rating": rating,
                "by": by,
                "at": at,
            }
        )
        if comment is not UNSET:
            field_dict["comment"] = comment

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        target = AnalystFeedbackTarget(d.pop("target"))

        target_id = d.pop("targetId")

        rating = AnalystFeedbackRating(d.pop("rating"))

        by = d.pop("by")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_comment(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        comment = _parse_comment(d.pop("comment", UNSET))

        analyst_feedback = cls(
            id=id,
            target=target,
            target_id=target_id,
            rating=rating,
            by=by,
            at=at,
            comment=comment,
        )

        analyst_feedback.additional_properties = d
        return analyst_feedback

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
