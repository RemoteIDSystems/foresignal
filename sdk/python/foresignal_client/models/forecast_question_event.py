from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.forecast_question_event_event import ForecastQuestionEventEvent

if TYPE_CHECKING:
    from ..models.forecast_question_summary import ForecastQuestionSummary


T = TypeVar("T", bound="ForecastQuestionEvent")


@_attrs_define
class ForecastQuestionEvent:
    """SSE payload on topic `forecasts` for question lifecycle changes

    Attributes:
        event (ForecastQuestionEventEvent):
        question (ForecastQuestionSummary): Compact forecast question for lists and SSE events
        at (datetime.datetime):
    """

    event: ForecastQuestionEventEvent
    question: ForecastQuestionSummary
    at: datetime.datetime
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        event = self.event.value

        question = self.question.to_dict()

        at = self.at.isoformat()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "event": event,
                "question": question,
                "at": at,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.forecast_question_summary import ForecastQuestionSummary

        d = dict(src_dict)
        event = ForecastQuestionEventEvent(d.pop("event"))

        question = ForecastQuestionSummary.from_dict(d.pop("question"))

        at = datetime.datetime.fromisoformat(d.pop("at"))

        forecast_question_event = cls(
            event=event,
            question=question,
            at=at,
        )

        forecast_question_event.additional_properties = d
        return forecast_question_event

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
