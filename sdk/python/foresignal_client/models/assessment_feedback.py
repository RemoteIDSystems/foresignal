from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.assessment_feedback_rating import AssessmentFeedbackRating
from ..types import UNSET, Unset

T = TypeVar("T", bound="AssessmentFeedback")


@_attrs_define
class AssessmentFeedback:
    """Recorded feedback on an assessment

    Attributes:
        id (str):
        assessment_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        rating (AssessmentFeedbackRating):
        by (str):
        at (datetime.datetime):
        comment (None | str | Unset):
    """

    id: str
    assessment_id: str
    rating: AssessmentFeedbackRating
    by: str
    at: datetime.datetime
    comment: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        assessment_id = self.assessment_id

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
                "assessmentId": assessment_id,
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

        assessment_id = d.pop("assessmentId")

        rating = AssessmentFeedbackRating(d.pop("rating"))

        by = d.pop("by")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        def _parse_comment(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        comment = _parse_comment(d.pop("comment", UNSET))

        assessment_feedback = cls(
            id=id,
            assessment_id=assessment_id,
            rating=rating,
            by=by,
            at=at,
            comment=comment,
        )

        assessment_feedback.additional_properties = d
        return assessment_feedback

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
