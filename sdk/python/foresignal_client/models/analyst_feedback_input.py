from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

from ..models.analyst_feedback_input_rating import AnalystFeedbackInputRating
from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalystFeedbackInput")


@_attrs_define
class AnalystFeedbackInput:
    """Feedback body

    Attributes:
        rating (AnalystFeedbackInputRating):
        comment (str | Unset):
    """

    rating: AnalystFeedbackInputRating
    comment: str | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        rating = self.rating.value

        comment = self.comment

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "rating": rating,
            }
        )
        if comment is not UNSET:
            field_dict["comment"] = comment

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        rating = AnalystFeedbackInputRating(d.pop("rating"))

        comment = d.pop("comment", UNSET)

        analyst_feedback_input = cls(
            rating=rating,
            comment=comment,
        )

        return analyst_feedback_input
